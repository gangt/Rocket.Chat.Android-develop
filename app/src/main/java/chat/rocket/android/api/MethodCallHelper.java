package chat.rocket.android.api;

import android.content.Context;
import android.os.Build;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.helper.CheckSum;
import chat.rocket.android.helper.JsonHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.ChatConnectivityManager;
import chat.rocket.android.video.model.VideoSendMsgModel;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.core.PublicSettingsConstants;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmAuth;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import chat.rocket.persistence.realm.models.ddp.RealmOrgCompany;
import chat.rocket.persistence.realm.models.ddp.RealmPermission;
import chat.rocket.persistence.realm.models.ddp.RealmPublicSetting;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import chat.rocket.persistence.realm.models.ddp.RealmRoomRole;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlight;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlightRoom;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlightUser;
import chat.rocket.persistence.realm.models.ddp.RealmSubscription;
import chat.rocket.persistence.realm.models.ddp.RealmUserEntity;
import chat.rocket.persistence.realm.models.internal.MethodCall;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import chat.rocket.persistence.realm.repositories.RealmLabels;
import hugo.weaving.DebugLog;
import io.realm.RealmQuery;
import okhttp3.HttpUrl;

/**
 * Utility class for creating/handling MethodCall or RPC.
 * <p>
 * TODO: separate method into several manager classes (SubscriptionManager, MessageManager, ...).
 */
public class MethodCallHelper {

    private static final String TAG = "MethodCallHelper";
    protected static final long TIMEOUT_MS = 20000;
    protected static final Continuation<String, Task<JSONObject>> CONVERT_TO_JSON_OBJECT =
            task -> Task.forResult(new JSONObject(task.getResult()));
    protected static final Continuation<String, Task<JSONArray>> CONVERT_TO_JSON_ARRAY =
            task -> Task.forResult(new JSONArray(task.getResult()));
    protected final Context context;
    protected final RealmHelper realmHelper;

    /**
     * initialize with Context and hostname.
     */
    public MethodCallHelper(Context context, String hostname) {
        this.context = context.getApplicationContext();
        this.realmHelper = RealmStore.getOrCreate(hostname);
    }

    /**
     * initialize with RealmHelper and DDPClient.
     */
    public MethodCallHelper(RealmHelper realmHelper) {
        this.context = null;
        this.realmHelper = realmHelper;
    }

    public MethodCallHelper(Context context, RealmHelper realmHelper) {
        this.context = context.getApplicationContext();
        this.realmHelper = realmHelper;
    }

    @DebugLog
    private Task<String> executeMethodCall(String methodName, String param, long timeout) {
        if (DDPClient.get() != null) {
            return DDPClient.get().rpc(UUID.randomUUID().toString(), methodName, param, timeout)
                    .onSuccessTask(task -> Task.forResult(task.getResult().result))
                    .continueWithTask(task_ -> {
                        if (task_.isFaulted()) {
                            return Task.forError(task_.getError());
                        }
                        return Task.forResult(task_.getResult());
                    });
        } else {
            return MethodCall.execute(realmHelper, methodName, param, timeout)
                    .onSuccessTask(task -> {
                        ChatConnectivityManager.getInstance(context.getApplicationContext())
                                .keepAliveServer();
                        return task;
                    });
        }
    }

    private Task<String> injectErrorHandler(Task<String> task) {
        return task.continueWithTask(_task -> {
            if (_task.isFaulted()) {
                Exception exception = _task.getError();
                if (exception instanceof MethodCall.Error || exception instanceof DDPClientCallback.RPC.Error) {
                    String errMessageJson;
                    if (exception instanceof DDPClientCallback.RPC.Error) {
                        errMessageJson = ((DDPClientCallback.RPC.Error) exception).error.toString();
                    } else {
                        errMessageJson = exception.getMessage();
                    }
                    if (TextUtils.isEmpty(errMessageJson)) {
                        return Task.forError(exception);
                    }
                    String errType = new JSONObject(errMessageJson).optString("error");
                    String errMessage = new JSONObject(errMessageJson).getString("message");

                    if (TwoStepAuthException.TYPE.equals(errType)) {
                        return Task.forError(new TwoStepAuthException(errMessage));
                    }
                    return Task.forError(new Exception(errMessage));
                } else if (exception instanceof DDPClientCallback.RPC.Timeout) {
                    return Task.forError(new MethodCall.Timeout());
                } else if (exception instanceof DDPClientCallback.Closed) {
                    return Task.forError(new Exception(exception.getMessage()));
                } else {
                    return Task.forError(exception);
                }
            } else {
                return _task;
            }
        });
    }

    protected final Task<String> call(String methodName, long timeout) {
        return injectErrorHandler(executeMethodCall(methodName, null, timeout));
    }

    protected final Task<String> call(String methodName, long timeout, ParamBuilder paramBuilder) {
        try {
            final JSONArray params = paramBuilder.buildParam();
            return injectErrorHandler(executeMethodCall(methodName,
                    params != null ? params.toString() : null, timeout));
        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    /**
     * Register RealmUser.
     */
    public Task<String> registerUser(final String name, final String email,
                                     final String password, final String confirmPassword) {
        return call("registerUser", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
                .put("name", name)
                .put("email", email)
                .put("pass", password)
                .put("confirm-pass", confirmPassword))); // nothing to do.
    }

    /**
     * set current user's name.
     */
    public Task<String> setUsername(final String username) {
        return call("setUsername", TIMEOUT_MS, () -> new JSONArray().put(username));
    }

    public Task<Void> joinDefaultChannels() {
        return call("joinDefaultChannels", TIMEOUT_MS)
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<Void> joinRoom(String roomId) {
        return call("joinRoom", TIMEOUT_MS, () -> new JSONArray().put(roomId))
                .onSuccessTask(task -> Task.forResult(null));
    }

    /**
     * Login with username/email and password.
     */
    public Task<Void> loginWithEmail(final String usernameOrEmail, final String password) {
        return call("login", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
                param.put("user", new JSONObject().put("email", usernameOrEmail));
            } else {
                param.put("user", new JSONObject().put("username", usernameOrEmail));
            }
            param.put("password", new JSONObject()
                    .put("digest", CheckSum.sha256(password))
                    .put("algorithm", "sha-256"));
            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(new Continuation<JSONObject, Task<String>>() {
                    @Override
                    public Task<String> then(Task<JSONObject> task) throws Exception {
                        JSONObject jsonObject = task.getResult();
                        return Task.forResult(jsonObject.getString("token"));
                    }
                })
                .onSuccessTask(this::saveToken);
    }

    public Task<Void> loginWithLdap(final String username, final String password) {
        return call("login", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("ldap", true);
            param.put("username", username);
            param.put("ldapPass", password);
            param.put("ldapOptions", new JSONObject());

            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken);
    }

    /**
     * 威宁通登录
     *
     * @param activity
     * @return
     */
    public Task<Void> loginUser(Context activity, String sessionId) {
        RCLog.d("loginUser sessionId="+sessionId);
        return call("signupLogin", TIMEOUT_MS, () -> new JSONArray()
                .put(new JSONObject()
                        .put("sessionId", sessionId)))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(new Continuation<JSONObject, Task<String>>() {
                    @Override
                    public Task<String> then(Task<JSONObject> task) throws Exception {
                        JSONObject jsonObject = task.getResult();
                        RCLog.d("loginUser result: " + jsonObject.toString());
                        realmHelper.executeTransaction(realm -> {
                            realm.delete(RealmAuth.class);
                            realm.createOrUpdateObjectFromJson(RealmAuth.class, RealmAuth.customizeJson(task.getResult()));
                            return null;
                        });
                        RCLog.d("Main----保存sessionId");
                        return Task.forResult(jsonObject.getString("token"));
                    }
                }).onSuccessTask(this::saveToken);
    }


    private Task<Void> saveToken(Task<String> task) {
//        RocketChatCache.INSTANCE.setSessionToken(task.getResult());
        return realmHelper.executeTransaction(realm ->
                realm.createOrUpdateObjectFromJson(RealmSession.class, new JSONObject()
                        .put("sessionId", RealmSession.DEFAULT_ID)
                        .put("token", task.getResult())
                        .put("tokenVerified", true)
                        .put("error", JSONObject.NULL)
                ));
    }

    /**
     * Login with OAuth.
     */
    public Task<Void> loginWithOAuth(final String credentialToken,
                                     final String credentialSecret) {
        return call("login", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
                .put("oauth", new JSONObject()
                        .put("credentialToken", credentialToken)
                        .put("credentialSecret", credentialSecret))
        )).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken);
    }

    /**
     * Login with token.
     */
    public Task<Void> loginWithToken(final String token) {
        RCLog.d("loginWithToken");
        return call("login", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
                .put("resume", token)
        )).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken)
                .continueWithTask(task -> {
                    if (task.isFaulted()) {
                        RealmSession.logError(realmHelper, task.getError());
                    }
                    return task;
                });
    }


    public Task<Void> twoStepCodeLogin(final String usernameOrEmail, final String password,
                                       final String twoStepCode) {
        return call("login", TIMEOUT_MS, () -> {
            JSONObject loginParam = new JSONObject();
            if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
                loginParam.put("user", new JSONObject().put("email", usernameOrEmail));
            } else {
                loginParam.put("user", new JSONObject().put("username", usernameOrEmail));
            }
            loginParam.put("password", new JSONObject()
                    .put("digest", CheckSum.sha256(password))
                    .put("algorithm", "sha-256"));

            JSONObject twoStepParam = new JSONObject();
            twoStepParam.put("login", loginParam);
            twoStepParam.put("code", twoStepCode);

            JSONObject param = new JSONObject();
            param.put("totp", twoStepParam);

            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
                .onSuccessTask(this::saveToken);
    }

    /**
     * Logout.
     */
    public Task<Void> logout() {
        return call("logout", TIMEOUT_MS).onSuccessTask(task -> {
            if (task.isFaulted()) {
                return Task.forError(task.getError());
            }
            return null;
        });
    }

    /**
     * request "room/get"
     */

    public Task<Void> getRooms() {
        return call("rooms/get", TIMEOUT_MS).onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
//                    RCLog.d("rooms/get->>"+ task.getResult());
                    JSONArray result = task.getResult();
                    for (int i = 0; i < result.length(); i++) {
                        if (TextUtils.isEmpty(result.get(i).toString())||result.get(i).toString().equals("{}")) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                result.remove(i);
                            }else {
                                JsonHelper.getInstance().remove(result,i);
                            }
                        }else {
                            RealmRoom.customizeJson(result.getJSONObject(i));
                        }
                    }
                    return realmHelper.executeTransaction(realm -> {
                        if (result.length() == 0) {
                            return task;
                        }
                        realm.delete(RealmRoom.class);
                        realm.createOrUpdateAllFromJson(RealmRoom.class, result);
                        return task;
                    });
                }).continueWithTask(task -> {
                    if (task.isFaulted()) {
//                        RCLog.e("rooms/get error="+task.getError());
                    }
                    return null;
                });
    }

    /**
     * request "subscriptions/get".
     */
    public Task<Void> getRoomSubscriptions() {
        return call("subscriptions/get", TIMEOUT_MS).onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray result = task.getResult();
                    try {
                        for (int i = 0; i < result.length(); i++) {
                            RealmSubscription.customizeJson(result.getJSONObject(i));
                        }

                        return realmHelper.executeTransaction(realm -> {
                            if (result.length() == 0) {
                                return null;
                            }
                            realm.delete(RealmSubscription.class);
//                            RCLog.d("->>>>>>>>>"+result);
                            realm.createOrUpdateAllFromJson(
                                    RealmSubscription.class, result);

                            JSONObject openedRooms = RocketChatCache.INSTANCE.getOpenedRooms();

                            RealmQuery<RealmSubscription> query = realm.where(RealmSubscription.class);
                            Iterator<String> keys = openedRooms.keys();
                            while (keys.hasNext()) {
                                String rid = keys.next();
                                RealmSubscription realmSubscription = query.equalTo(RealmSubscription.ID, rid).findFirst();
                                if (realmSubscription == null) {
                                    RocketChatCache.INSTANCE.removeOpenedRoom(rid);
                                } else {
                                    loadMissedMessages(rid, realmSubscription.getLs())
                                            .continueWithTask(task1 -> {
                                                if (task1.isFaulted()) {
                                                    Exception error = task1.getError();
                                                    RCLog.e(error);
                                                }
                                                return null;
                                            });
                                }
                            }
                            return null;
                        });
                    } catch (JSONException exception) {
                        return Task.forError(exception);
                    }
                });
    }

    public Task<JSONArray> loadMissedMessages(final String roomId, final long timestamp) {
        return call("loadMissedMessages", TIMEOUT_MS, () -> new JSONArray()
                .put(roomId)
                .put(timestamp > 0 ? new JSONObject().put("$date", timestamp) : JSONObject.NULL)
        ).onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    JSONArray result = task.getResult();
                    for (int i = 0; i < result.length(); i++) {
                        RealmMessage.customizeJson(result.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
                        if (timestamp == 0) {
                            realm.where(RealmMessage.class)
                                    .equalTo("rid", roomId)
                                    .equalTo("syncstate", SyncState.SYNCED)
                                    .findAll().deleteAllFromRealm();
                        }
                        if (result.length() > 0) {
                            realm.createOrUpdateAllFromJson(RealmMessage.class, result);
                        }
                        return null;
                    }).onSuccessTask(_task -> Task.forResult(result));
                });
    }

    /**
     * Load messages for room.
     */
    public Task<JSONArray> loadHistory(final String roomId, final long timestamp,
                                       final int count, final long lastSeen) {
        return call("loadHistory", TIMEOUT_MS, () -> new JSONArray()
                .put(roomId)
                .put(timestamp > 0 ? new JSONObject().put("$date", timestamp) : JSONObject.NULL)
                .put(count)
                .put(lastSeen > 0 ? new JSONObject().put("$date", lastSeen) : JSONObject.NULL)
        ).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> {
                    JSONObject result = task.getResult();
                    final JSONArray messages = result.getJSONArray("messages");
                    for (int i = 0; i < messages.length(); i++) {
                        RealmMessage.customizeJson(messages.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
//                        if (timestamp == 0) {
//                            realm.where(RealmMessage.class)
//                                    .equalTo("rid", roomId)
//                                    .equalTo("syncstate", SyncState.SYNCED)
//                                    .findAll().deleteAllFromRealm();
//                        }
                        if (messages.length() > 0) {
                            realm.createOrUpdateAllFromJson(RealmMessage.class, messages);
                        }
                        return null;
                    }).onSuccessTask(_task -> Task.forResult(messages));
                });
    }

    /**
     * update user's status.
     */
    public Task<Void> setUserStatus(final String status) {
        return call("UserPresence:setDefaultStatus", TIMEOUT_MS, () -> new JSONArray().put(status))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<Void> setUserPresence(final String status) {
        return call("UserPresence:" + status, TIMEOUT_MS)
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<JSONObject> getUsersOfRoom(final String roomId, final boolean showAll) {
        return call("getUsersOfRoom", TIMEOUT_MS, () -> new JSONArray().put(roomId).put(showAll))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT);
    }

    public Task<Void> createChannel(final String name, final boolean readOnly) {
        return call("createChannel", TIMEOUT_MS, () -> new JSONArray()
                .put(name)
                .put(new JSONArray())
                .put(readOnly))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<Void> createPrivateGroup(final String name, final boolean readOnly) {
        return call("createPrivateGroup", TIMEOUT_MS, () -> new JSONArray()
                .put(name)
                .put(new JSONArray())
                .put(readOnly))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<String> createDirectMessage(final String username) {
        return call("createDirectMessage", TIMEOUT_MS, () -> new JSONArray().put(username))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> Task.forResult(task.getResult().getString("rid")));
    }

    /**
     * send message.
     */
    public Task<String> sendMessage(String messageId, String roomId, String msg, long editedAt) {
        try {
            JSONObject messageJson = new JSONObject()
                    .put("_id", messageId)
                    .put("rid", roomId)
                    .put("msg", msg);


            if (editedAt == 0) {
                return sendMessage(messageJson);
            } else {
                return updateMessage(messageJson);
            }
        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    public Task<Void> deleteMessage(String messageID) {
        try {
            JSONObject messageJson = new JSONObject()
                    .put("_id", messageID);

            return deleteMessage(messageJson);
        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }

    /**
     * Send message object.
     */
    private Task<String> sendMessage(final JSONObject messageJson) {
        return call("sendMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(task.getResult()));
//                .continueWithTask(new LogIfError());
    }

    private Task<String> updateMessage(final JSONObject messageJson) {
        return call("updateMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(task.getResult()));
    }

    private Task<Void> deleteMessage(final JSONObject messageJson) {
        return call("deleteMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
                .onSuccessTask(task -> Task.forResult(null));
    }

    /**
     * mark all messages are read in the room.
     */
    public Task<Void> readMessages(final String roomId) {
        return call("readMessages", TIMEOUT_MS, () -> new JSONArray().put(roomId))
                .onSuccessTask(task -> Task.forResult(null));
    }

    public Task<Void> getPublicSettings(String currentHostname) {
        return call("public-settings/get", TIMEOUT_MS)
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray settings = task.getResult();
                    String siteUrl = null;
                    String siteName = null;
                    for (int i = 0; i < settings.length(); i++) {
                        JSONObject jsonObject = settings.getJSONObject(i);
                        RealmPublicSetting.customizeJson(jsonObject);
                        if (isPublicSetting(jsonObject, PublicSettingsConstants.General.SITE_URL)) {
                            siteUrl = jsonObject.getString(RealmPublicSetting.VALUE);
                        } else if (isPublicSetting(jsonObject, PublicSettingsConstants.General.SITE_NAME)) {
                            siteName = jsonObject.getString(RealmPublicSetting.VALUE);
                        }
                    }

                    if (siteName != null && siteUrl != null) {
                        HttpUrl httpSiteUrl = HttpUrl.parse(siteUrl);
                        if (httpSiteUrl != null) {
                            String host = httpSiteUrl.host();
                            RocketChatCache.INSTANCE.addSiteUrl(host, currentHostname);
                            RocketChatCache.INSTANCE.addSiteName(currentHostname, siteName);
                        }
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(RealmPublicSetting.class);
                        realm.createOrUpdateAllFromJson(RealmPublicSetting.class, settings);
                        return null;
                    });
                });
    }

    private boolean isPublicSetting(JSONObject jsonObject, String id) {
        return jsonObject.optString(RealmPublicSetting.ID).equalsIgnoreCase(id);
    }

    public Task<Void> getPermissions() {
        return call("permissions/get", TIMEOUT_MS)
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray permissions = task.getResult();
                    for (int i = 0; i < permissions.length(); i++) {
                        RealmPermission.customizeJson(permissions.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(RealmPermission.class);
                        realm.createOrUpdateAllFromJson(RealmPermission.class, permissions);
                        return null;
                    });
                });
    }

    public Task<Void> getRoomRoles(final String roomId) {
        return call("getRoomRoles", TIMEOUT_MS, () -> new JSONArray().put(roomId))
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray roomRoles = task.getResult();
                    for (int i = 0; i < roomRoles.length(); i++) {
                        RealmRoomRole.customizeJson(roomRoles.getJSONObject(i));
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(RealmRoomRole.class);
                        realm.createOrUpdateAllFromJson(RealmRoomRole.class, roomRoles);
                        return null;
                    });
                });
    }

    /**
     * 删除点对点
     *
     * @param userId
     * @param rid
     * @return
     */
    public Task<Void> hideRoomForMobile(String userId, String rid) {
        return call("hideRoomForMobile", TIMEOUT_MS
                , () -> new JSONArray()
                        .put(new JSONObject()
                                .put("userId", userId)
                                .put("rid", rid)))
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    task.getResult();
                    return null;
                });
    }

    /**
     * 创建点对点
     *
     * @param userName
     * @return
     */
    public Task<Void> createDirectMessageForMobile(String userName) {
        return call("createDirectMessageForMobile", TIMEOUT_MS
                , () -> new JSONArray()
                        .put(new JSONObject().put("username", userName)))
                .onSuccessTask(task -> {
                  task.getResult();
                    return task;
                })
                .continueWithTask(task -> {
                    return null;
                });
    }

    /**
     * 获取体系数据
     *
     * @return
     */
    public Task<String> labelsForMobile() {
        return call("labelsForMobile", TIMEOUT_MS)
                .onSuccessTask(CONVERT_TO_JSON_ARRAY)
                .onSuccessTask(task -> {
                    final JSONArray result = task.getResult();
                    for (int i = 0; i < result.length(); i++) {
                        RealmLabels.customizeJson(result.getJSONObject(i));
                    }
                    realmHelper.executeTransaction(realm -> {
                        realm.delete(RealmLabels.class);
                        realm.createOrUpdateAllFromJson(RealmLabels.class, result);
                        return null;
                    });
                    return null;
                });
    }

    /**
     * 订阅数据
     *
     * @param userId
     * @return
     */
    public Task<Void> subscriptionForMobile(String userId) {
        return call("subscriptionForMobile", TIMEOUT_MS, () ->
                new JSONArray().put(new JSONObject().put("userId", userId)))
                .onSuccessTask(task -> {
                    task.getResult();
                    return null;
                }).continueWithTask(task -> {
                    if (task.isFaulted()) {
                        task.getError();
                    }
                    return null;
                });
    }

    /**
     * 获取频道数据
     *
     * @param userId
     * @return
     */
    public Task<Void> roomsForMobile(String userId) {
        return call("roomsForMobile", TIMEOUT_MS, () ->
                new JSONArray().put(new JSONObject().put("userId", userId)))
                .onSuccessTask(task -> {
                    task.getResult();
                    return null;
                }).continueWithTask(task -> {
                    if (task.isFaulted()) {
                        task.getError();
                    }
                    return null;
                });
    }

    /**
     * 获取频道类型
     *
     * @return
     */
    public Task<Void> roomsTypesForMobile() {
        return call("roomsTypesForMobile", TIMEOUT_MS)
                .onSuccessTask(task -> {
                    task.getResult();
                    return null;
                }).continueWithTask(task -> {
                    if (task.isFaulted()) {
                        task.getError();
                    }
                    return null;
                });
    }

    /**
     * 用户和房间搜索
     *
     * @param term
     * @param userId
     * @return
     */
    public Task<Void> searchSpotlight(String term, String userId) {
        return call("sideNavSearch", TIMEOUT_MS, () ->
                new JSONArray()
                        .put(new JSONObject().put("userId", userId)
                                .put("text", term)))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> {
                    JSONArray users = task.getResult().getJSONArray("users");
                   return realmHelper.executeTransaction(realm -> {
                       realm.delete(RealmSpotlightUser.class);
                       realm.createOrUpdateAllFromJson(RealmSpotlightUser.class,users);
;                       return null;
                   });
                });
    }

    public Task<Void> searchSpotlightUsers(String term) {
        return searchSpotlight(RealmSpotlightUser.class, "users", term);
    }

    public Task<Void> searchSpotlightRooms(String term) {
        return searchSpotlight(RealmSpotlightRoom.class, "rooms", term);
    }

    public Task<Void> searchSpotlight(String term) {
        return call("spotlight", TIMEOUT_MS, () ->
                new JSONArray()
                        .put(term)
                        .put(JSONObject.NULL)
                        .put(new JSONObject().put("rooms", true).put("users", true))
        ).onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> {
                    String jsonString = null;
                    final JSONObject result = task.getResult();

                    JSONArray roomJsonArray = (JSONArray) result.get("rooms");
                    int roomTotal = roomJsonArray.length();
                    if (roomTotal > 0) {
                        for (int i = 0; i < roomTotal; ++i) {
                            RealmSpotlight.Companion.customizeRoomJSONObject(roomJsonArray.getJSONObject(i));
                        }
                        jsonString = roomJsonArray.toString();
                    }

                    JSONArray userJsonArray = (JSONArray) result.get("users");
                    int usersTotal = userJsonArray.length();
                    if (usersTotal > 0) {
                        for (int i = 0; i < usersTotal; ++i) {
                            RealmSpotlight.Companion.customizeUserJSONObject(userJsonArray.getJSONObject(i));
                        }

                        if (jsonString == null) {
                            jsonString = userJsonArray.toString();
                        } else {
                            jsonString = jsonString.replace("]", "") + "," + userJsonArray.toString().replace("[", "");
                        }
                    }

                    if (jsonString != null) {
                        String jsonStringResults = jsonString;
                        realmHelper.executeTransaction(realm -> {
                            realm.delete(RealmSpotlight.class);
                            realm.createOrUpdateAllFromJson(RealmSpotlight.class, jsonStringResults);
                            return null;
                        });
                    }
                    return null;
                });
    }

    private Task<Void> searchSpotlight(Class clazz, String key, String term) {
        return call("spotlight", TIMEOUT_MS, () -> new JSONArray()
                .put(term)
                .put(JSONObject.NULL)
                .put(new JSONObject().put(key, true)))
                .onSuccessTask(CONVERT_TO_JSON_OBJECT)
                .onSuccessTask(task -> {
                    final JSONObject result = task.getResult();
                    if (!result.has(key)) {
                        return null;
                    }

                    Object items = result.get(key);
                    if (!(items instanceof JSONArray)) {
                        return null;
                    }

                    return realmHelper.executeTransaction(realm -> {
                        realm.delete(clazz);
                        realm.createOrUpdateAllFromJson(clazz, (JSONArray) items);
                        return null;
                    });
                });
    }

    /**
     * userId	String	用户id		是
     * name	String	频道名称		是
     * members	Array	频道成员		是
     * channel	String	父级频道	此参数是频道数据里包含@userId+时间戳的字段	否
     * label	Object	企业体系	{“ id”:”xxxxx”,”name”:”xxxxx”}	否
     * readOnly	Bool	只读		否
     *
     * @return
     */
    public Task<String> createWorkingGroupForMobile(String userId, String name, JSONArray list,
                                                    String channel, String tixiClass, boolean readOnly) {
        return call("createWorkingGroupForMobile", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();

            param.put("userId", userId);
            param.put("name", name);
            param.put("members", list);
//            param.put("channel", channel);
            if (android.text.TextUtils.isEmpty(tixiClass)) {
//                param.put("label", new JSONObject());
            } else {
                param.put("label", new JSONObject(tixiClass));
            }
            param.put("readonly", readOnly);

            return new JSONArray().put(param);
        });
    }

    /**
     * 创建会议巢频道
     * userId	String	用户id		是
     * name	String	频道名称		是
     * members	Array	频道成员		是
     * subMeetingType	String	父级频道	text | audio | vide	是
     * encrypt	Boolean	加密		否
     * readOnly	Boolean	只读		否
     * startDate	Date	开始时间		是
     * endDate	Date	结束时间		是
     * meetingSubject	String	会议主题		否
     *
     * @return
     */
    public Task<String> createMeetingGroupForMobile(String userId, String name, JSONArray str,JSONArray zhuchirenJsonArray,
        Date startDate, Date endDate, String subMeetingType, boolean encrypt, boolean qiandao, String meetingSubject) {
        return call("createMeetingGroupForMobile", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();

            JSONObject objStartDate = new JSONObject().put("$date", startDate);
            JSONObject objEndDate = new JSONObject().put("$date", endDate);
            param.put("userId", userId);
            param.put("name", name);
            param.put("host", zhuchirenJsonArray.toString().equals("[]")?"":zhuchirenJsonArray);
            param.put("members", str);
            param.put("subMeetingType", subMeetingType);
            param.put("startDate", objStartDate);
            param.put("endDate", objEndDate);
            param.put("encrypt", encrypt);
            param.put("readonly", false);
            param.put("attendance", qiandao);
            param.put("meetingSubject", meetingSubject);

            return new JSONArray().put(param);
        });
    }

    /**
     * 创建组织巢频道
     *
     * @param userId
     * @param name
     * @param list
     * @param channel
     * @param tixiClass
     * @param readOnly
     * @return
     */
    public Task<String> createPrivateGroupForMobile(String userId, String name, JSONArray list,
                                                    String channel, String tixiClass, boolean readOnly) {
        return call("createPrivateGroupForMobile", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();

            param.put("userId", userId);
            param.put("name", name);
            param.put("members", list);
//            param.put("channel", channel);
            if (android.text.TextUtils.isEmpty(tixiClass)) {
//                param.put("label", new JSONObject());
            } else {
                param.put("label", new JSONObject(tixiClass));
            }
            param.put("readonly", readOnly);

            return new JSONArray().put(param);
        });
    }

    public Task<String> userForCreateRoom(String type, String companyId, String[] filterUsernames) {
        return call("userForCreateRoom", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            if(!TextUtils.isEmpty(type)) {
                param.put("type", type);
            }
            if(!TextUtils.isEmpty(companyId)) {
                param.put("companyId", companyId);
            }

            if(filterUsernames != null) {
                JSONArray array = new JSONArray();
                for (int i = 0; i < filterUsernames.length; i++) {
                    array.put(filterUsernames[i]);
                }
                param.put("filterUsernames", array);
            }

            return new JSONArray().put(param);
        });
    }

    public Task<Void> userForCreateRoomToRealm(String type, String companyId) {
        return call("userForCreateRoom", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("type", type);
            param.put("companyId", companyId);

            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_ARRAY).onSuccessTask((Task<JSONArray> task) -> {
            JSONArray result = task.getResult();
            if(result == null) {
                return null;
            }
            /*您可以直接将 JSON 对象添加到 Realm 中，这些 JSON 对象可以是一个 String、一个 JSONObject 或者是一个
            InputStream。Realm 会忽略 JSON 中存在但未定义在 Realm 模型类里的字段。单独对象可以通过
            Realm.createObjectFromJson() 添加。对象列表可以通过 Realm.createAllFromJson() 添加。*/
            for (int i = 0; i < result.length(); i++) {
                RealmUserEntity.customizeJson(result.getJSONObject(i), type);
            }
//            List<RealmUserEntity> realmUserEntityAll = userEntityRepository.getRealmUserEntityAll(type, companyId);
            realmHelper.executeTransaction(realm -> {
//                if(realmUserEntityAll != null && realmUserEntityAll.size() > 0){
//                    realm.delete(RealmUserEntity.class);
//                }
                realm.createOrUpdateAllFromJson(RealmUserEntity.class, result);
                return null;
            }).continueWithTask(task1 -> {
                if (task1.isFaulted() || task1.getError() != null) {
                    RCLog.e("userForCreateRoomToRealm: result=" + task1.getError());
                }
                return null;
            });
            return null;
        });
    }


    public Task<JSONArray> userForCreateRoomShowDeptToRealm(String type, String name ,String companyId,boolean showDeptRole) {
        return call("userForCreateRoom", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("type", type);
            param.put("companyId", companyId);
            param.put("name", name);
            param.put("showDeptRole", showDeptRole);

            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_ARRAY);
    }

    /**
     * rid	String	频道id		是
     userId	String	锁定，解锁用户id		是
     block	Boolean	锁定或解锁		是
     * @return
     */
    public Task<String> blockUserForMobile(String rid, String userId, boolean block) {
        return call("blockUserForMobile", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("userId", userId);
            param.put("block", block);

            return new JSONArray().put(param);
        });
    }

    /**设置，解除群主 websocket method
     * rid	String	频道id
     userId	String	用户id
     set	Boolean	是否设置
     */
    public Task<String> setOwner(String rid, String userId, boolean set) {
        return call("setOwner", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("userId", userId);
            param.put("set", set);

            return new JSONArray().put(param);
        });
    }

    /**
     * 设置，解除管理员
     * rid	String	频道id
     userId	String	用户id
     set	Boolean	是否设置
     */
    public Task<String> setModerator(String rid, String userId, boolean set) {
        return call("setModerator", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("userId", userId);
            param.put("set", set);

            return new JSONArray().put(param);
        });
    }

    /**
     * 禁言、允许发言
     * rid	String	频道id
     userId	String	锁定，解锁用户id
     username	String	用户名
     mute	Boolean	是否禁言
     */
    public Task<String> muteUser(String rid, String userId, String username, boolean mute) {
        return call("muteUser", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("userId", userId);
            param.put("username", username);
            param.put("mute", mute);

            return new JSONArray().put(param);
        });
    }

    /**
     * 接口名称：	modifyRoomInfo
     接口说明：	修改频道信息
     输入内容：
     字段名	字段类型	字段名称
     rid	String	频道id
     name	String	频道名
     topic	String	频道主题
     description	String	频道描述
     */
    public Task<String> modifyRoomInfo(String rid, String name) {
        return call("modifyRoomInfo", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("name", name);
//            param.put("description", description);

            return new JSONArray().put(param);
        });
    }

    public Task<String> modifyRoomInfo2(String rid, String topic) {
        return call("modifyRoomInfo", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("topic", topic);
//            param.put("description", description);

            return new JSONArray().put(param);
        });
    }

    /**
     * rid	String	频道id
     username	String	用户名
     * @param rid
     * @param username
     * @return
     */
    public Task<String> removeUser(String rid, String username) {
        return call("removeUser", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("username", username);

            return new JSONArray().put(param);
        });
    }

    public Task<String> addMembers(String rid, JSONArray username) {
        return call("addMembers", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("usernames", username);

            return new JSONArray().put(param);
        });
    }

    /**
     * 签到
    rid	String	频道id
    username	String	用户名
    date	Dated	签到日期
     */
    public void attendanceFromMobile(String rid, String username, Button btn_qiandao) {
        call("attendanceFromMobile", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            JSONObject objStartDate = new JSONObject().put("$date", new Date());

            param.put("rid", rid);
            param.put("username", username);
            param.put("date", objStartDate);

            return new JSONArray().put(param);
        }).onSuccessTask(task -> {
            return task;
        }).continueWithTask(task -> {
            if (task.isFaulted() || task.getError() != null) {
                RCLog.e( "attendanceFromMobile: result=" + task.getError());
                ToastUtils.showToast(task.getError()+"");
                return null;
            }else{
                btn_qiandao.setVisibility(View.GONE);
                ToastUtils.showToast("签到成功！");
            }
            return task;
        }, Task.UI_THREAD_EXECUTOR);
    }

    /**
     * 暂停/重启会议
     * */
    public  Task<String>  pauseOrRestartMeetting(String rid,String userId,boolean pauseOrRestart){
        return call("pauseOrRestartMeeting", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("userId", userId);
            param.put("pauseOrRestart", pauseOrRestart);
            return new JSONArray().put(param);
        });
    }

    /**
     * 获取组织架构（websocket method）
     * type	String	频道类型
       sessionId	String	会话Id
     */
    public  Task<Void>  organizationForMobile(String type,String sessionId){
        return call("organizationForMobile", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("type", type);
            param.put("sessionId", sessionId);
            return new JSONArray().put(param);
        }).onSuccessTask(CONVERT_TO_JSON_OBJECT).onSuccessTask((Task<JSONObject> task) -> {
            JSONObject result = task.getResult();
            if(result.get("res") == null || !(result.get("res") instanceof JSONArray)) {
                RCLog.e("result="+result);
                return null;
            }
            JSONArray jsonArray = (JSONArray) result.get("res");

            realmHelper.executeTransaction(realm -> {
                realm.delete(RealmOrgCompany.class);
                realm.createOrUpdateAllFromJson(RealmOrgCompany.class, jsonArray);
                return null;
            }).continueWithTask(task1 -> {
                if (task1.isFaulted() || task1.getError() != null) {
                    RCLog.e( "organizationForMobile: result=" + task1.getError());
                }
                return null;
            });
            return null;
        });

    }

    public Task<String>  organizationUserForMobile(String type,String sessionId, String orgId){
        return call("organizationUserForMobile", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("type", type);
            param.put("sessionId", sessionId);
            param.put("orgId", orgId);
            return new JSONArray().put(param);
        });
    }

    /**
     * 结束会议
     * */
    public  Task<String>  forceEndMeeting(String rid,String userId){
        return call("forceEndMeeting", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("userId", userId);
            return new JSONArray().put(param);
        });
    }


    /**
     * 设置会议主持人
     * */
    public  Task<String>  setHost(String rid,String username){
        return call("setHost", TIMEOUT_MS, () -> {
            JSONObject param = new JSONObject();
            param.put("rid", rid);
            param.put("username", username);
            return new JSONArray().put(param);
        });
    }

    /**
     *消息免打扰
     * @param rid
     * @param userId
     * @param mnd
     * @return
     */
    public Task<String> roomMND(String rid,String userId,boolean mnd){
        return call("roomMND",TIMEOUT_MS,()->{
            JSONObject param=new JSONObject();
            param.put("rid",rid);
            param.put("userId",userId);
            param.put("mnd",mnd);
            return new JSONArray().put(param);
        });
    }

    /**
     * 群频道禁言
     * @param rid
     * @param readonly
     * @return
     */
    public Task<String> setReadonly(String rid,boolean readonly){
        return call("setReadonly",TIMEOUT_MS,()->{
            JSONObject param=new JSONObject();
            param.put("rid",rid);
            param.put("readonly",readonly);
            return new JSONArray().put(param);
        });
    }

    /**
     * 删除或者退出
     * @param rid
     * @param username
     * @param type
     * @return
     */
    public Task<String> deleteOrLeaveRoom(String rid,String username,int type){
        return call("deleteOrLeaveRoom",TIMEOUT_MS,()->{
            JSONObject param=new JSONObject();
            param.put("rid",rid);
            param.put("username",username);
            param.put("type",type);
            return new JSONArray().put(param);
        });
    }

    /**
     *  setUserStatusForMobile(params)
     userId, status,boolean
     */
    public Task<String> setUserStatusForMobile(String userId,String status,boolean isPush){
        return call("setUserStatusForMobile",TIMEOUT_MS,()->{
            JSONObject param=new JSONObject();
            param.put("userId",userId);
            param.put("status",status);
            param.put("boolean",isPush);// TODO 该接口需要调试
            return new JSONArray().put(param);
        });
    }

    /**
     * {
     "_id" : "4503a6584f9d4224993fcd17d4fdfd0a",
     "token" : {
     "gcm" : "a1d189278a194ae89b02cd6fc35a5bc6"
     },
     "appName" : "weiningPgy",
     "userId" : "rdWH6n9vrwNkLFJGD",
     }
     */
    public Task<String> setUserPushTokenForMobile(String userId,String id,String gcmToken){
        return call("setUserPushTokenForMobile",TIMEOUT_MS,()->{
            JSONObject param=new JSONObject();
            param.put("uuid",id);
            param.put("userId",userId);
            param.put("appName","weiningPgy");
            JSONObject objStartDate = new JSONObject().put("gcm", gcmToken);
            param.put("token", objStartDate);
            return new JSONArray().put(param);
        });
    }
    /**
     */
    public Task<String> getFileClassify(String flag,String fileUrl,String fileName,String fileId,String userAccount){
        return call("fileClassify",TIMEOUT_MS,()->{
            JSONObject param=new JSONObject();
            param.put("flag",flag);
            param.put("fileUrl",fileUrl);
            param.put("fileName",fileName);
            param.put("fileId",fileId);//
            param.put("userAccount",userAccount);
            return new JSONArray().put(param);
        });
    }

    /**
     * userAccount	String	会话id		是
     name	String	文件名		否	搜索时传
     pageSize	Int	当前页数		是	默认传20条
     pageIndex	Int	页码		是	第几页
     */
    public Task<String> getFileClassifyList(String pageIndex,String pageSize,String name,String userAccount){
        return call("classifyFileList",TIMEOUT_MS,()->{
            JSONObject param=new JSONObject();
            param.put("pageIndex",pageIndex);
            param.put("pageSize",pageSize);
            param.put("name",name);//
            param.put("userAccount",userAccount);
            return new JSONArray().put(param);
        });
    }

    protected interface ParamBuilder {
        JSONArray buildParam() throws JSONException;
    }


    public Task<Void> sendVideo(String url,
                                VideoSendMsgModel.MediaBean mediaBean,
                                String roomId,
                                VideoSendMsgModel.CalluserBean calluserBean,
                                String from, String warnmsg, String calltype, int av_type
    ) {
        return call("stream-notify-user", TIMEOUT_MS, () ->
                new JSONArray()
                        .put(url)
                        .put("false")
                        .put("call")
                        .put(new JSONObject()
                                .put("media",new JSONObject().put("video", mediaBean.isVideo()).put("audio", mediaBean.isAudio()))
                                .put("room", roomId)
                                .put("calluser",new JSONObject().put("username", calluserBean.getUsername()) .put("_id", calluserBean.get_id()))
                                .put("from", from)
                                .put("warnmsg", warnmsg)
                                .put("calltype", calltype)
                                .put("av_type", av_type)
                                .put("timestamp",System.currentTimeMillis())

                        )
        ).continueWithTask(task -> {
            if (task.isFaulted()) {
                Exception error = task.getError();
                RCLog.d("come--------->>>>" + "error : " + error.toString());
            } else {
                String result = task.getResult();
            }
            return null;
        }, Task.UI_THREAD_EXECUTOR);
    }


    public Task<Void> changeVideoOrAudioMsgStatusForMobile(String userId, String rid,
                                                           String status, String nType,
                                                           String time, String receiveUserId,
                                                           String mediaId) {
        RCLog.d("come--------->>>>" + "changeVideoOrAudioMsgStatusForMobile:");
        return call("changeVideoOrAudioMsgStatusForMobile", TIMEOUT_MS, () ->
                new JSONArray().put(new JSONObject()
                        .put("userId", userId)
                        .put("rid", rid)
                        .put("status", status)
                        .put("nType", nType)
                        .put("time", time)
                        .put("receiveUserId", receiveUserId)
                        .put("mediaId", mediaId)
                ))
                .continueWithTask(task -> {
                    if (task.isFaulted()) {
                        Exception error = task.getError();
                        RCLog.d("come--------->>>>" + "error : " + error.toString());
                    } else {
                        String result = null;
                        try {
                            result = task.getResult();
                            JSONObject jsonObject = new JSONObject(result);
                         /*  if(ChatStatus.CANCEL.equals(jsonObject.getString("status"))){
                               TempFileUtils.getInstance()
                                       .saveRecentVideoByMediaId(jsonObject.getString("mediaId"),jsonObject.getString("status"));
                           }*/

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        RCLog.d("come--------->>>>" + "changeVideoOrAudioMsgStatusForMobile result : " + result);
                    }
                    return null;
                }, Task.UI_THREAD_EXECUTOR);

    }

}
