package chat.rocket.android.service.observer;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Observe messages for sending.
 */
public class NewMessageObserver extends AbstractModelObserver<RealmMessage> {

    private final MethodCallHelper methodCall;

    public NewMessageObserver(Context context, String hostname, RealmHelper realmHelper) {
        super(context, hostname, realmHelper);
        methodCall = new MethodCallHelper(realmHelper);

        realmHelper.executeTransaction(realm -> {
            // resume pending operations.
            RealmResults<RealmMessage> pendingMethodCalls = realm.where(RealmMessage.class)
                    .equalTo(RealmMessage.SYNC_STATE, SyncState.SYNCING)
                    .findAll();
            for (RealmMessage message : pendingMethodCalls) {
                message.setSyncState(SyncState.NOT_SYNCED);
            }

            return null;
        }).continueWith(new LogIfError());
    }

    @Override
    public RealmResults<RealmMessage> queryItems(Realm realm) {
        return realm.where(RealmMessage.class)
                .equalTo(RealmMessage.SYNC_STATE, SyncState.NOT_SYNCED)
                .isNotNull(RealmMessage.ROOM_ID)
                .findAll();
    }

    String msg = "";
    String attachment = "";

    @Override
    public void onUpdateResults(List<RealmMessage> results) {
        if (results.isEmpty()) {
            return;
        }

        final RealmMessage message = results.get(0);
        final String messageId = message.getId();
        final String roomId = message.getRoomId();
        if (message.getMessage().contains(RocketChatCache.INSTANCE.getSelectedServerHostname())&&message.getMessage().contains("?msg=")) {
            RealmRoomRepository repository = new RealmRoomRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
            String roomName = repository.getByRoomNameId(message.getRoomId());
            RealmMessageRepository messageRepository = new RealmMessageRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
            String messageUser = null;
            try {
                messageUser = messageRepository.getAllMessageByMessageId(message.getMessage().split("msg=")[1].split("\\)")[0]).get(0).getUser().getUsername();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(roomName))
                try {
                    msg = message.getMessage().replace(roomName, URLEncoder.encode(URLEncoder.encode(roomName, "utf-8"), "utf-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            else if (message.getMessage().contains("/direct")) {
                try {
                    msg = message.getMessage().replace(messageUser, URLEncoder.encode(URLEncoder.encode(message.getUser().getUsername(), "utf-8"), "utf-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                msg = message.getMessage();
        } else
            msg = message.getMessage();
        final long editedAt = message.getEditedAt();

        realmHelper.executeTransaction(realm ->
                realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
                        .put(RealmMessage.ID, messageId)
                        .put(RealmMessage.SYNC_STATE, SyncState.SYNCING)
                )
        ).onSuccessTask(task -> methodCall.sendMessage(messageId, roomId, msg, editedAt)
        ).continueWith(task -> {
            if (task.isFaulted()) {
                RCLog.w(task.getError());
                realmHelper.executeTransaction(realm ->
                        realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
                                .put(RealmMessage.ID, messageId)
                                .put(RealmMessage.SYNC_STATE, SyncState.FAILED)));
            } else {
                JSONObject result = new JSONObject(task.getResult().toString());
                RealmMessage.customizeJson(result);
                try {
                    attachment = result.getJSONArray("attachments").get(0).toString();
                } catch (JSONException e) {
                    attachment = "";
                }
                if (!TextUtils.isEmpty(attachment)) {
                    return realmHelper.executeTransaction(realm ->
                            realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
                                    .put(RealmMessage.ID, messageId)
                                    .put(RealmMessage.ATTACHMENTS, attachment)
                                    .put(RealmMessage.SYNC_STATE, SyncState.SYNCED)));
                } else
                    realmHelper.executeTransaction(realm ->
                            realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
                                    .put(RealmMessage.ID, messageId)
                                    .put(RealmMessage.SYNC_STATE, SyncState.SYNCED)));
            }
            return null;
        }).continueWith(task -> {
            if (!task.isFaulted()&&!TextUtils.isEmpty(attachment)) {
                BaseEvent baseEvent = new BaseEvent();
                baseEvent.setCode(EventTags.SUBMIT_REF_CALLBACK);
                EventBus.getDefault().post(baseEvent);
            }
            return null;
        });
    }
}
