package chat.rocket.android.activity;

import android.content.Context;
import android.support.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.InitializeUtils;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.StringUtils;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.service.ServerConnectivity;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.core.PublicSettingsConstants;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.PublicSetting;
import chat.rocket.core.models.Session;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.PublicSettingRepository;
import chat.rocket.core.utils.Pair;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import hugo.weaving.DebugLog;
import icepick.State;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainPresenter extends BasePresenter<MainContract.View>
        implements MainContract.Presenter {

    private final CanCreateRoomInteractor canCreateRoomInteractor;
    private final RoomInteractor roomInteractor;
    private final SessionInteractor sessionInteractor;
    private final MethodCallHelper methodCallHelper;
    private final ConnectivityManagerApi connectivityManagerApi;
    private final PublicSettingRepository publicSettingRepository;
    private String sessionId;
    private boolean isRefreshRoom;
    private RealmSubscriptionRepository subscriptionRepository;
    private RealmUserRepository userRepository;

    public MainPresenter(String sessionId,
                         RoomInteractor roomInteractor,
                         CanCreateRoomInteractor canCreateRoomInteractor,
                         SessionInteractor sessionInteractor,
                         MethodCallHelper methodCallHelper,
                         ConnectivityManagerApi connectivityManagerApi,
                         PublicSettingRepository publicSettingRepository,
                         RealmSubscriptionRepository subscriptionRepository,
                         RealmUserRepository userRepository) {
        this.roomInteractor = roomInteractor;
        this.canCreateRoomInteractor = canCreateRoomInteractor;
        this.sessionInteractor = sessionInteractor;
        this.methodCallHelper = methodCallHelper;
        this.connectivityManagerApi = connectivityManagerApi;
        this.publicSettingRepository = publicSettingRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.sessionId = sessionId;
        this.isRefreshRoom = true;
        this.userRepository=userRepository;
    }

    @Override
    public void bindViewOnly(@NonNull MainContract.View view) {
        super.bindView(view);
        subscribeToUnreadCount();
        subscribeToSession();
//        setUserOnline();
    }

    @Override
    public void loadSignedInServers(@NonNull String hostname) {
        final Disposable disposable = publicSettingRepository.getById(PublicSettingsConstants.Assets.LOGO)
                .zipWith(publicSettingRepository.getById(PublicSettingsConstants.General.SITE_NAME), Pair::new)
                .map(this::getLogoAndSiteNamePair)
                .map(settings -> getServerList(hostname, settings))
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::showSignedInServers,
                        RCLog::e
                );

        addSubscription(disposable);
    }

    @Override
    public void bindView(@NonNull MainContract.View view) {
        super.bindView(view);

        if (shouldLaunchAddServerActivity()) {
            view.showAddServerScreen();
            return;
        }
        if (isRefreshRoom) {
            isRefreshRoom = !isRefreshRoom;
            openRoom();
        }

//        subscribeToNetworkChanges();
        subscribeToUnreadCount();
        subscribeToSession();
        methodCallHelper.labelsForMobile();
    }

    @Override
    public void release() {
        if (RocketChatCache.INSTANCE.getSessionToken() != null) {
//            setUserAway();
        }

        super.release();
    }

    @Override
    public void onOpenRoom(String hostname, String roomId) {
        final Disposable subscription = canCreateRoomInteractor.canCreate(roomId)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allowed -> {
                            if (allowed) {
                                view.showRoom(hostname, roomId);
                            } else {
                                view.showHome();
                            }
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    @Override
    public void onRetryLogin() {
        final Disposable subscription = sessionInteractor.retryLogin()
                .subscribe();

        addSubscription(subscription);
    }

    @DebugLog
    @Override
    public void prepareToLogout() {
        clearSubscriptions();
    }

    @Override
    public void loginUser(Context context, String sessionId) {
        methodCallHelper.loginUser(context, sessionId)
                .continueWithTask(task -> {
                    if (task.isFaulted()) {
                        RCLog.e("loginUser error: " + task.getError(), true);
                        String localizedMessage = task.getError().getLocalizedMessage();
                        if (localizedMessage.contains("Email already exists")) {
                            localizedMessage = "邮件已存在";
                        } else if (localizedMessage.contains("Not found name")) {
                            localizedMessage = "缺少用户名字段";

                        }
                        if (StringUtils.isChinese(localizedMessage)) {
                            ToastUtils.showToast(localizedMessage);
                            view.finishView();
                            return null;
                        }
                    } else {
//                        view.dismissProgressDialog();
                        view.reSubUser();
                        subscribeToUnreadCount();
                    }
                    view.refreshUser();
                    return task;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private Pair<String, String> getLogoAndSiteNamePair(Pair<Optional<PublicSetting>, Optional<PublicSetting>> settingsPair) {
        String logoUrl = "";
        String siteName = "";
        if (settingsPair.first.isPresent()) {
            logoUrl = settingsPair.first.get().getValue();
        }
        if (settingsPair.second.isPresent()) {
            siteName = settingsPair.second.get().getValue();
        }
        return new Pair<>(logoUrl, siteName);
    }

    private List<Pair<String, Pair<String, String>>> getServerList(String hostname, Pair<String, String> serverInfoPair) throws JSONException {
        JSONObject jsonObject;
        String logoUrl = null;
        if (serverInfoPair.first != null && !serverInfoPair.first.equals("")) {
            jsonObject = new JSONObject(serverInfoPair.first);
            logoUrl = (jsonObject.has("url")) ?
                    jsonObject.optString("url") : jsonObject.optString("defaultUrl");
        }
        String siteName = serverInfoPair.second;
        RocketChatCache.INSTANCE.addHostname(hostname.toLowerCase(), logoUrl, siteName);
        return RocketChatCache.INSTANCE.getServerList();
    }

    private void openRoom() {
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        String roomId = RocketChatCache.INSTANCE.getSelectedRoomId();

        if (roomId == null || roomId.length() == 0) {
            view.showHome();
            return;
        }

        onOpenRoom(hostname, roomId);
    }

    private void subscribeToUnreadCount() {
//        final Disposable subscription = Flowable.combineLatest(
//                roomInteractor.getTotalUnreadRoomsCount(),
//                roomInteractor.getTotalUnreadMentionsCount(),
//                (Pair::new)
//        )
//                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        pair -> view.showUnreadCount(pair.first, pair.second),
//                        Logger.INSTANCE::report
//                );
        Disposable subscribe1 = userRepository.getCurrent().distinctUntilChanged()
                .subscribe(userOptional -> {
                    User user = userOptional.get();
                    if (user != null) {
                        Disposable subscribe = subscriptionRepository
                                .getAllSub(user.getId()).subscribe(subscriptions -> {
                                    int count=0;
                                    for (Subscription subscription :subscriptions){
                                        count+=Integer.parseInt(subscription.getUnread());
                                    }
                                    view.showUnreadCount(0,count);
                                }, RCLog::e);
                        addSubscription(subscribe);
                    }
                }, RCLog::e);

        addSubscription(subscribe1);
    }

    public void subscribeToSession() {
        final Disposable subscription = sessionInteractor.getDefault()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sessionOptional -> {
                            Session session = sessionOptional.orNull();
                            if (session == null || session.getToken() == null) {
                                view.showLoginScreen();
                                return;
                            }

                            String error = session.getError();
                            if (error != null && error.length() != 0) {
                                view.showConnectionError();
                                RCLog.d("subscribeToSession——》》》》》error");
                                return;
                            }

                            if (!session.isTokenVerified()) {
//                                view.showConnecting();
                                RCLog.d("subscribeToSession——》》》》》connect");
                                return;
                            }
                            // TODO: Should we remove below and above calls to view?
//                            view.showConnectionOk();
                            RocketChatCache.INSTANCE.setSessionToken(session.getToken());
                        },
                        Logger.INSTANCE::report
                );

        addSubscription(subscription);
    }

    private void subscribeToNetworkChanges() {
        Disposable disposable = connectivityManagerApi.getServerConnectivityAsObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        connectivity -> {
                            if (connectivity.state == ServerConnectivity.STATE_CONNECTED) {
                                //TODO: notify almost connected or something like that.
//                                view.showConnectionOk();
                            } else if (connectivity.state == ServerConnectivity.STATE_DISCONNECTED) {
                                if (connectivity.code == DDPClient.REASON_NETWORK_ERROR) {
                                    view.showConnectionError();
                                    RCLog.d("RealmBasedConnectivityManager+subscribeToNetworkChanges——》》》》》error");
                                }
                            } else if (connectivity.state == ServerConnectivity.STATE_SESSION_ESTABLISHED) {
                                RCLog.d("RealmBasedConnectivityManager+subscribeToNetworkChanges——》》》》》STATE_SESSION_ESTABLISHED");
//                                setUserOnline();
                                view.refreshRoom();
                                view.showConnectionOk();
                            } else {
//                                view.showConnecting();
//                                RCLog.d("subscribeToNetworkChanges——》》》》》connect");
                            }
                        },
                        RCLog::e
                );

        addSubscription(disposable);
    }

    public void setUserOnline(String status) {
        methodCallHelper.setUserStatusForMobile(RocketChatCache.INSTANCE.getUserId(), status, false);
//        methodCallHelper.setUserPresence(User.STATUS_ONLINE)
//                .continueWith(new LogIfError());
    }

    public void setUserAway(String status) {
        methodCallHelper.setUserStatusForMobile(RocketChatCache.INSTANCE.getUserId(), status, true);
//        methodCallHelper.setUserPresence(User.STATUS_AWAY)
//                .continueWith(new LogIfError());
    }

    private boolean shouldLaunchAddServerActivity() {
        return connectivityManagerApi.getServerList().isEmpty();
    }

}
