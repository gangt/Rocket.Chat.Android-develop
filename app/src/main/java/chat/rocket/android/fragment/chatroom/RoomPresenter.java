package chat.rocket.android.fragment.chatroom;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.Handler;

import bolts.Task;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.SyncState;
import chat.rocket.core.interactors.MessageInteractor;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Settings;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RoomPresenter extends BasePresenter<RoomContract.View>
        implements RoomContract.Presenter {

    private final String roomId;
    private final MessageInteractor messageInteractor;
    private final UserRepository userRepository;
//    private final RoomRepository roomRepository;
    private final RealmSubscriptionRepository realmSubscriptionRepository;
    private final AbsoluteUrlHelper absoluteUrlHelper;
    private final MethodCallHelper methodCallHelper;
    private final ConnectivityManagerApi connectivityManagerApi;
    private Subscription currentRoom;
    String channel;
    private boolean isFirst=true;
    private boolean isFirstEvent;

    /* package */RoomPresenter(String roomId,
                               UserRepository userRepository,
                               MessageInteractor messageInteractor,
                               RealmSubscriptionRepository realmSubscriptionRepository,
                               AbsoluteUrlHelper absoluteUrlHelper,
                               MethodCallHelper methodCallHelper,
                               ConnectivityManagerApi connectivityManagerApi) {
        this.roomId = roomId;
        this.userRepository = userRepository;
        this.messageInteractor = messageInteractor;
//        this.roomRepository = roomRepository;
        this.realmSubscriptionRepository = realmSubscriptionRepository;
        this.absoluteUrlHelper = absoluteUrlHelper;
        this.methodCallHelper = methodCallHelper;
        this.connectivityManagerApi = connectivityManagerApi;
    }

    @Override
    public void bindView(@NonNull RoomContract.View view) {
        super.bindView(view);
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> refreshRoom(false),500);

    }

    @Override
    public void refreshRoom(boolean isNeedRefresh) {
        getRoomRoles();
        getRoomInfo();
        getRoomHistoryStateInfo(isNeedRefresh);
//        getMessages(false);
        getUserPreferences();
    }

    @Override
    public void loadMessages() {
        final Disposable subscription = getSingleRoom()
                .flatMap(messageInteractor::loadMessages)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (!success) {
                                connectivityManagerApi.keepAliveServer();
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @Override
    public void loadMoreMessages() {
        final Disposable subscription = getSingleRoom()
                .flatMap(messageInteractor::loadMoreMessages)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (!success) {
                                connectivityManagerApi.keepAliveServer();
                            }

                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @Override
    public void onMessageSelected(@Nullable Message message) {
        if (message == null) {
            return;
        }

        if (message.getSyncState() == SyncState.DELETE_FAILED) {
            view.showMessageDeleteFailure(message);
        } else if (message.getSyncState() == SyncState.FAILED) {
            view.showMessageSendFailure(message);
        } else if (message.getType() == null && message.getSyncState() == SyncState.SYNCED) {
            // If message is not a system message show applicable actions.
            if(!TextUtils.isEmpty(message.getNType()) ){
                return;
            }
            view.showMessageActions(message);
        }
    }

    @Override
    public void onMessageTap(@Nullable Message message) {
        if (message == null) {
            return;
        }

        if (message.getSyncState() == SyncState.FAILED) {
            view.showMessageSendFailure(message);
        }
    }

    /**转发*/
    @Override
    public void relayMessage() {

    }

    @Override
    public void replyMessage(@NonNull Message message, boolean justQuote) {
        RealmSubscriptionRepository repository=new RealmSubscriptionRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
        String type=repository.getByIdSub(message.getRoomId()).getT();
        /**点对点：direct
        会议： meeting
        组织巢: group
        工作巢: working*/
        if (type.equals(RocketChatConstants.M)){
            channel="meeting";
        }else if (type.equals(RocketChatConstants.D)){
            channel="direct";
        }
        else if (type.equals(RocketChatConstants.P)){
            channel="group";
        }
        else if (type.equals(RocketChatConstants.W)){
            channel="working";
        }
        final Disposable subscription = this.absoluteUrlHelper.getRocketChatAbsoluteUrl()
                .cache()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        serverUrl -> {
                            if (serverUrl.isPresent()) {
                                RocketChatAbsoluteUrl absoluteUrl = serverUrl.get();
                                String baseUrl = absoluteUrl.getBaseUrl();
                                view.onReply(absoluteUrl, buildReplyOrQuoteMarkdown(baseUrl,channel, message, justQuote), message);
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    public void acceptMessageDeleteFailure(Message message) {
        final Disposable subscription = messageInteractor.acceptDeleteFailure(message)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        addSubscription(subscription);
    }

    @Override
    public void loadMissedMessages(boolean isNeedRefresh) {
//        RocketChatApplication appContext = RocketChatApplication.getInstance();
        JSONObject openedRooms = RocketChatCache.INSTANCE.getOpenedRooms();
        if (openedRooms.has(roomId)) {
            try {
                JSONObject room = openedRooms.getJSONObject(roomId);
                String rid = room.optString("rid");
                long ls = room.optLong("ls");
                methodCallHelper.loadMissedMessages(rid, ls)
                        .continueWithTask(task ->{
                            if (!task.isFaulted()&&isNeedRefresh)
                                    refreshRoom(isNeedRefresh);
                               return Task.forResult(null);
                        })
                        .continueWith(new LogIfError());
            } catch (JSONException e) {
                RCLog.e(e);
            }
        }
    }

    String roomName="";
    private String buildReplyOrQuoteMarkdown(String baseUrl,String channel, Message message, boolean justQuote) {
        if (currentRoom == null || message.getUser() == null) {
            return "";
        }
        if (currentRoom.isDirectMessage()) {
            return String.format("[ ](%s/direct/%s?msg=%s) ", baseUrl,
                    message.getUser().getUsername(),
                    message.getId());
        } else {
            return String.format("[ ](%s/%s/%s?msg=%s) %s", baseUrl,channel,
                    roomName.equals("")?currentRoom.getName():roomName,
                    message.getId(),
                    justQuote ? "" : "@" + message.getUser().getUsername() + " ");
        }
    }

    @Override
    public void sendMessage(String messageText) {
        view.disableMessageInput();
        final Disposable subscription = getRoomUserPair()
                .flatMap(pair -> messageInteractor.send(pair.first, pair.second, messageText))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (success) {
                                view.onMessageSendSuccessfully();
                            }
                            view.enableMessageInput();
                        },
                        throwable -> {
                            view.enableMessageInput();
                            Logger.INSTANCE.report(throwable);
                        }
                );

        addSubscription(subscription);
    }

    @Override
    public void resendMessage(@NonNull Message message) {
        final Disposable subscription = getCurrentUser()
                .flatMap(user -> messageInteractor.resend(message, user))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        addSubscription(subscription);
    }

    @Override
    public void updateMessage(@NonNull Message message, String content) {
        view.disableMessageInput();
        final Disposable subscription = getCurrentUser()
                .flatMap(user -> messageInteractor.update(message, user, content))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (success) {
                                view.onMessageSendSuccessfully();
                            }
                            view.enableMessageInput();
                        },
                        throwable -> {
                            view.enableMessageInput();
                            Logger.INSTANCE.report(throwable);
                        }
                );

        addSubscription(subscription);
    }

    @Override
    public void deleteMessage(@NonNull Message message) {
        final Disposable subscription = messageInteractor.delete(message)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        addSubscription(subscription);
    }

    @Override
    public void deleteMessage(@NonNull String msgId) {
     messageInteractor.delete(msgId);

    }
    @Override
    public void onUnreadCount() {
        final Disposable subscription = getRoomUserPair()
                .flatMap(roomUserPair -> messageInteractor
                        .unreadCountFor(roomUserPair.first, roomUserPair.second))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        count -> view.showUnreadCount(count),
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @Override
    public void onMarkAsRead() {
        final Disposable subscription = realmSubscriptionRepository.getById(roomId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .filter(room -> room.getAlert().equals("true"))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        subscription1 -> methodCallHelper.readMessages(subscription1.getRid())
                                .continueWith(new LogIfError()),
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void getRoomRoles() {
        methodCallHelper.getRoomRoles(roomId);
    }

    public void getRoomInfo() {
        final Disposable subscription = realmSubscriptionRepository.getById(roomId)
                .distinctUntilChanged()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processRoom, Logger.INSTANCE::report);
        addSubscription(subscription);
    }

    private void processRoom(Subscription subscription) {
        this.currentRoom = subscription;
        view.render(subscription);

        if (subscription.isDirectMessage()) {
            getUserByUsername(subscription.getName());
        }
    }

    private void getUserByUsername(String username) {
        final Disposable disposable = userRepository.getByUsername(username)
                .distinctUntilChanged()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showUserStatus, Logger.INSTANCE::report);
        addSubscription(disposable);
    }

    private void getRoomHistoryStateInfo(boolean isNeedRefresh) {
        final Disposable subscription = realmSubscriptionRepository.getHistoryStateByRoomId(roomId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        roomHistoryState -> {
                            int syncState = roomHistoryState.getSyncState();
                            view.updateHistoryState(
                                    !roomHistoryState.isComplete(),
                                    syncState == SyncState.SYNCED || syncState == SyncState.FAILED
                            );
                            if(isFirst) {
                                getMessages(isNeedRefresh);
                                if(syncState == SyncState.SYNCED) {
                                    isFirst=false;
                                }
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
        getMessages(isNeedRefresh);
    }

    @SuppressLint("RxLeakedSubscription")
    private void getMessages(boolean isNeedRefresh) {
        RCLog.d("RoomFragment----getMessages");
        isFirstEvent=isNeedRefresh;
        RealmMessageRepository repository=new RealmMessageRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
        if (view!=null&&(repository.getAllMessageByRoomId(roomId)==null||repository.getAllMessageByRoomId(roomId).size()==0))
        view.dismissProgressDialog();
        final Disposable subscription = Flowable.zip(realmSubscriptionRepository.getById(roomId),
                absoluteUrlHelper.getRocketChatAbsoluteUrl().toFlowable().cache(), Pair::new)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .map(pair -> {
                    view.setupWith(pair.second.orNull());
                    return pair.first;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(room -> {
                    RocketChatCache.INSTANCE.addOpenedRoom(room.getRid(), room.getLs());
                    return room;
                })
                .flatMap(messageInteractor::getAllFrom)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (List<Message> messages) -> {
                            view.showMessages(messages, isFirstEvent);
                            RCLog.d("RoomFragment----showMessages");
                            if (isFirstEvent==true)
                                isFirstEvent=false;
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void getUserPreferences() {
        final Disposable subscription = userRepository.getCurrent()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(user -> user.getSettings() != null)
                .map(User::getSettings)
                .filter(settings -> settings.getPreferences() != null)
                .map(Settings::getPreferences)
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        preferences -> {
                            if (preferences.isAutoImageLoad()) {
                                view.autoloadImages();
                            } else {
                                view.manualLoadImages();
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void getAbsoluteUrl() {
        final Disposable subscription = absoluteUrlHelper.getRocketChatAbsoluteUrl()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        it -> view.setupWith(it.orNull()),
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @SuppressLint("RxLeakedSubscription")
    private Single<Pair<Subscription, User>> getRoomUserPair() {

         getSingleRoom().subscribe(subscription -> {
                    subscription.getName();
                 }
         ,RCLog::e);

        getCurrentUser().subscribe(user -> {
            user.getName();
        },RCLog::e);
        return Single.zip(
                getSingleRoom(),
                getCurrentUser(),
                (Subscription first, User second) -> {
                    return new Pair<Subscription, User>(first, second);
                }
        );
    }

    private Single<Subscription> getSingleRoom() {
        return realmSubscriptionRepository.getById(roomId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }

    private Single<User> getCurrentUser() {
        return userRepository.getCurrent()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }

}
