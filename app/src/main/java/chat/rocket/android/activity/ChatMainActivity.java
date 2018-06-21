package chat.rocket.android.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.ConnectionStatusManager;
import chat.rocket.android.InitializeUtils;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.api.rest.DefaultServerPolicyApi;
import chat.rocket.android.api.rest.ServerPolicyApi;
import chat.rocket.android.fragment.chatroom.HomeFragment;
import chat.rocket.android.fragment.chatroom.RoomFragment;
import chat.rocket.android.fragment.sidebar.SidebarMainFragment;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.NetWorkStateReceiver;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.ServerPolicyApiValidationHelper;
import chat.rocket.android.helper.ServerPolicyHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.login.LoginProgressDialog1;
import chat.rocket.android.login.SharedPreferencesUtil;
import chat.rocket.android.service.ChatConnectivityManager;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.base.RelationUserDataSubscriber;
import chat.rocket.android.service.ddp.base.UserDataSubscriber;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.android.service.ddp.stream.VideoMsgObsever;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.android.service.observer.SessionObserver;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.model.VideoRequestModel;
import chat.rocket.android.video.model.ViewEvent;
import chat.rocket.android.widget.RoomToolbar;
import chat.rocket.android.widget.helper.DebouncingOnClickListener;
import chat.rocket.android.widgets.MyAutoSlidingPaneLayout;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.PublicSettingRepository;
import chat.rocket.core.repositories.SubscriptionRepository;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.core.utils.Pair;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import chat.rocket.persistence.realm.repositories.RealmPublicSettingRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import hugo.weaving.DebugLog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class ChatMainActivity extends AbstractAuthedActivity implements MainContract.View {
    private static final String TAG = "ChatMainActivity";

    private RoomToolbar toolbar;
    private TextView mTitle;
    private MyAutoSlidingPaneLayout pane;
    private MainContract.Presenter presenter;
    private volatile AtomicReference<Crouton> croutonStatusTicker = new AtomicReference<>();
    private View croutonView;
    private ImageView croutonTryAgainImage;
    private TextView croutonText;
    private AnimatedVectorDrawableCompat tryAgainSpinnerAnimatedDrawable;
    //a7e4994f9ba342babdfba38892fa9810 冯俊
    //c24a926ab04144f29957aba8e25a9551 叶小琳
    private String sessionId="a7e4994f9ba342babdfba38892fa9810";//古小宁;878c0d8da81446268bd0e7a037d47f1d
    private String userId ="" ;//古小宁;
    private String companyId="WEINING";
    public static final String IS_DESTROY = "destroy";
    UserDataSubscriber subscriber;
    CurrentUserObserver currentUserObserver;
    Registrable listener;
    SessionObserver sessionObserver;
//    VideoMsgObsever videoMsgObsever ;
    WeakReference<Context> contextWeakReference;
    NetWorkStateReceiver netWorkStateReceiver;
    private boolean isActive = false;
    private static  WeakReference<ChatMainActivity> context ;

    @Override
    public int getLayoutContainerForFragment() {
        return R.id.activity_main_container;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
//        userId=getIntent().getStringExtra("userId");
//        sessionId=getIntent().getStringExtra("sessionId");
//        companyId=getIntent().getStringExtra("companyId");
        final Window win = getWindow();//自动解锁
        win.addFlags(
                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        registerReceiver();
        RocketChatCache.INSTANCE.setSessionId(sessionId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);
        toolbar = findViewById(R.id.activity_main_toolbar);
        pane = findViewById(R.id.sliding_pane);
        mTitle = findViewById(R.id.tv_title);
        contextWeakReference = new WeakReference<>(this);
        loadCroutonViewIfNeeded();
        if (sessionId == null) {
//            ToastUtils.showToast("sessionId为空");
            finish();
        }
        MethodCallHelper methodCall = new MethodCallHelper(RealmStore.getOrCreate(hostname));
        if (!TextUtils.isEmpty(RocketChatCache.INSTANCE.getUserId())) {
            methodCall.setUserPushTokenForMobile(RocketChatCache.INSTANCE.getUserId(), RocketChatCache.INSTANCE.getOrCreatePushId(), RocketChatCache.INSTANCE.getAliyunDeviceId());
        }
        setupToolbar();
        if (!TextUtils.isEmpty(getIntent().getStringExtra("roomId"))) {
            roomId = getIntent().getStringExtra("roomId");
        }
        context = new WeakReference<ChatMainActivity>(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferencesUtil.saveBooleanData(this,IS_DESTROY,false);
    }

    private void registerReceiver() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    super.onReceive(context, intent);
                    //获得ConnectivityManager对象
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    //获取ConnectivityManager对象对应的NetworkInfo对象
                    //获取WIFI连接的信息
                    NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    //获取移动数据连接的信息
                    NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                        Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
                    } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
//                        Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
                    } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
//                        Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "网络已断开", Toast.LENGTH_SHORT).show();
                        BaseEvent event = new BaseEvent();
                        event.setCode(EventTags.SHOW_DIALOG);
                        EventBus.getDefault().postSticky(event);

                        showConnectionError();
                    }
                }
            };
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    protected void onStop() {
        super.onStop();
        RealmUserRepository userRepository = new RealmUserRepository(hostname);
        User user =  userRepository.getMySelf();
        if (!isAppOnForeground()) {
            //app 进入后台
            isActive = false;
            //全局变量isActive = false 记录当前已经进入后台
            if (presenter != null)
            if (user != null) {
                if (user.getStatus().equals(User.STATUS_ONLINE))
                    presenter.setUserAway(User.STATUS_AWAY);
                else
                    presenter.setUserAway(user.getStatus());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManagerApi connectivityManager = ChatConnectivityManager.getInstance(getApplicationContext());
        if (hostname == null || presenter == null) {
            String previousHostname = hostname;
            hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
            if (hostname == null) {
                showAddServerScreen();
            } else {
                if (!hostname.equalsIgnoreCase(previousHostname)) {
                    onHostnameUpdated();
                    connectivityManager.resetConnectivityStateList();
                    connectivityManager.keepAliveServer();
                }
            }
        } else {
            connectivityManager.keepAliveServer();
            presenter.bindView(this);
            presenter.loadSignedInServers(hostname);
            roomId = RocketChatCache.INSTANCE.getSelectedRoomId();
            if (!isActive) {
//        //        app 从后台唤醒，进入前台
                isActive = true;
                if (presenter!=null){
                    RealmUserRepository userRepository = new RealmUserRepository(hostname);
                    User user =  userRepository.getMySelf();
                    if (user!=null&&user.getStatus()!=null){
                        if (user.getStatus().equals(User.STATUS_AWAY))
                            presenter.setUserOnline(User.STATUS_ONLINE);
                        else
                            presenter.setUserOnline(user.getStatus());
                    }
                }

            }
        }

        if (!TextUtils.isEmpty(RocketChatCache.INSTANCE.getIsClickInfoMsg())) {
            gotoChatMessage(RocketChatCache.INSTANCE.getIsClickInfoMsg());
        }

    /*    if(TempFileUtils.getInstance().getCallingRequest() !=null){
            VideoRequestModel model = TempFileUtils.getInstance().getCallingRequest();
            if(model.mediaId !=null  &&  model.mediaId.equals(TempFileUtils.getInstance().getMediaId())){
                return;
            }
            LaunchUtil.showVideoActivity(model._id,model.username,model.avar,model.roomId,model.isCall,model.isVideo);
            //   LaunchUtil.showVideoActivity(_id, username, getAvar(), json.getString("room"), false, isvideo)
        }*/

//        if (hostname == null) {
//            showAddServerScreen();
//            hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
//        }
//        if (presenter == null) {
//            onHostnameUpdated();
//        }
//        connectivityManager.keepAliveServer();
//        presenter.bindView(this);
////        presenter.loadSignedInServers(hostname);
//        roomId = RocketChatCache.INSTANCE.getSelectedRoomId();
    }
    boolean first;
    @SuppressLint("RxLeakedSubscription")
    private void gotoChatMessage(String msg) {
        first=true;
        if (msg.equals("showHome")) {
            showFragment(new HomeFragment());
            RocketChatCache.INSTANCE.setIsClickInfoMsg(null);
        }
        if (!msg.contains(";")) return;
        String username = msg.split(";")[0];
        String uid = msg.split(";")[1];
        MethodCallHelper methodCallHelper = new MethodCallHelper(this, hostname);
        RealmSubscriptionRepository subscriptionRepository = new RealmSubscriptionRepository(hostname);
        Subscription subscription = subscriptionRepository.getByName(username, uid);
        if (subscription == null) {
            methodCallHelper.createDirectMessageForMobile(username);
            subscriptionRepository.getByUserName(username, uid).subscribe(subscriptions -> {
                    if (subscriptions.size() > 0) {
                        if (first) {
                            synchronized (SidebarMainFragment.class) {
                                if (first){
                                    first = false;
                                    if (!RocketChatCache.INSTANCE.getSelectedRoomId().equals(subscriptions.get(0).getRid())){
                                        RelationUserDataSubscriber relationUserDataSubscriber = new RelationUserDataSubscriber(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
                                        relationUserDataSubscriber.register();
                                        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
                                        MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
                                        methodCall.getRoomSubscriptions().onSuccess(task -> {
                                            Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                                                    ChatMainActivity.this, hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
                                            listener.register();
                                            return null;
                                        }).continueWith(new LogIfError());
                                        enterRoom(subscriptions.get(0));
                                    }

                                }
                        }
                    }

                }
            }, RCLog::e);
        } else {
            RocketChatCache.INSTANCE.setSelectedRoomId(subscription.getRid());
        }

        RocketChatCache.INSTANCE.setIsClickInfoMsg(null);
    }

    private void enterRoom(Subscription subscription) {
        RocketChatCache.INSTANCE.setSelectedRoomId(subscription.getRid());
    }

    @Override
    protected void onPause() {
        if (presenter != null) {
            presenter.release();
        }
        Crouton.cancelAllCroutons();
        super.onPause();
    }


    private void setupToolbar() {
        if (pane != null) {
            pane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(@NonNull View view, float v) {
                    //Ref: ActionBarDrawerToggle#setProgress
                    toolbar.setNavigationIconProgress(v);
                }

                @Override
                public void onPanelOpened(@NonNull View view) {
                    toolbar.setNavigationIconVerticalMirror(true);
                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentById(R.id.sidebar_fragment_container);
                    if (fragment != null && fragment instanceof SidebarMainFragment) {
                        SidebarMainFragment sidebarMainFragment = (SidebarMainFragment) fragment;
                        sidebarMainFragment.setHint(false);
                    }
                }

                @Override
                public void onPanelClosed(@NonNull View view) {
                    toolbar.setNavigationIconVerticalMirror(false);
                    Fragment fragment = getSupportFragmentManager()
                            .findFragmentById(R.id.sidebar_fragment_container);
                    if (fragment != null && fragment instanceof SidebarMainFragment) {
                        SidebarMainFragment sidebarMainFragment = (SidebarMainFragment) fragment;
                        sidebarMainFragment.toggleUserActionContainer(false);
                        sidebarMainFragment.showUserActionContainer(false);
                        sidebarMainFragment.setHint(true);
                    }
                }
            });

            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(view -> {
                    if (pane.isSlideable() && !pane.isOpen()) {
                        pane.openPane();
                    }
                });
            }
        }
        closeSidebarIfNeeded();
    }

    private boolean closeSidebarIfNeeded() {
        // REMARK: Tablet UI doesn't have SlidingPane!
        if (pane != null && pane.isSlideable() && pane.isOpen()) {
            pane.closePane();
            return true;
        }
        return false;
    }

    //    @DebugLog
    @Override
    protected void onHostnameUpdated() {
        super.onHostnameUpdated();

        if (presenter != null) {
            presenter.release();
        }

        RoomInteractor roomInteractor = new RoomInteractor(new RealmRoomRepository(hostname));

        CanCreateRoomInteractor createRoomInteractor = new CanCreateRoomInteractor(
                new RealmUserRepository(hostname),
                new SessionInteractor(new RealmSessionRepository(hostname))
        );

        SessionInteractor sessionInteractor = new SessionInteractor(
                new RealmSessionRepository(hostname)
        );
        PublicSettingRepository publicSettingRepository = new RealmPublicSettingRepository(hostname);
        RealmSubscriptionRepository realmSubscriptionRepository = new RealmSubscriptionRepository(hostname);
        presenter = new MainPresenter(
                sessionId,
                roomInteractor,
                createRoomInteractor,
                sessionInteractor,
                new MethodCallHelper(this, hostname),
                ChatConnectivityManager.getInstance(getApplicationContext()),
                publicSettingRepository,
                realmSubscriptionRepository,
                new RealmUserRepository(hostname)
        );
//        if (TextUtils.isEmpty(RocketChatCache.INSTANCE.getUserId())&&RocketChatCache.INSTANCE.getDownLine()||!companyId.equals(RocketChatCache.INSTANCE.getCompanyId())||!userId.equals(RocketChatCache.INSTANCE.getFrameUserId()))
//        {
        RocketChatCache.INSTANCE.setHomePageVisiable(false);
        gotoChatMessage("showHome");
        if (!TextUtils.isEmpty(RocketChatCache.INSTANCE.getFrameUserId()) && !userId.equals(RocketChatCache.INSTANCE.getFrameUserId())) {
            InitializeUtils.getInstance().clearSession();
            InitializeUtils.getInstance().clearUser();
        }
        presenter.subscribeToSession();
//        }
        updateSidebarMainFragment();

        presenter.bindView(this);
        presenter.loadSignedInServers(hostname);
        ChatConnectivityManager.getInstance(this).keepAliveServer();
        roomId = RocketChatCache.INSTANCE.getSelectedRoomId();
    }

    private void updateSidebarMainFragment() {
        closeSidebarIfNeeded();
        String selectedServerHostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        Fragment sidebarFragment = findFragmentByTag(selectedServerHostname);
        if (sidebarFragment == null) {
            sidebarFragment = SidebarMainFragment.create(selectedServerHostname);
            ((SidebarMainFragment) sidebarFragment).setPanChange(() ->
                    closeSidebarIfNeeded()
            );
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sidebar_fragment_container, sidebarFragment, selectedServerHostname)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    protected void onRoomIdUpdated() {
        super.onRoomIdUpdated();
        presenter.onOpenRoom(hostname, roomId);
    }

    @Override
    protected boolean onBackPress() {
        return closeSidebarIfNeeded() || super.onBackPress();
    }

    @Override
    public void showHome() {
        showFragment(new HomeFragment());
    }

    @Override
    public void showRoom(String hostname, String roomId) {
        showFragment(RoomFragment.create(hostname, roomId));
        closeSidebarIfNeeded();
        KeyboardHelper.hideSoftKeyboard(this);
    }

    @Override
    public void showUnreadCount(long roomsCount, int mentionsCount) {
        if(toolbar == null){
            return;
        }
        toolbar.setUnreadBadge((int) roomsCount, mentionsCount);
    }

    @Override
    public void showAddServerScreen() {
        LaunchUtil.showAddServerActivity(this);

    }

    @Override
    public void showLoginScreen() {
        showConnectionOk();
        if (TextUtils.isEmpty(RocketChatCache.INSTANCE.getFrameUserId())) {
            showProgressDialog(getString(R.string.first_login));
        } else if (!userId.equals(RocketChatCache.INSTANCE.getFrameUserId())) {
            showProgressDialog(getString(R.string.switch_login));
        } else if (!TextUtils.isEmpty(RocketChatCache.INSTANCE.getCompanyId()) && !companyId.equals(RocketChatCache.INSTANCE.getCompanyId())) {
            showProgressDialog(getString(R.string.company_switch_login));
        }
        presenter.loginUser(this, sessionId);
    }

    @Override
    public void reSubUser() {
        List<RealmSession> sessions = RealmStore.getOrCreate(hostname).executeTransactionForReadResults(realm ->
                realm.where(RealmSession.class)
                        .isNotNull(RealmSession.TOKEN)
                        .equalTo(RealmSession.TOKEN_VERIFIED, true)
                        .isNull(RealmSession.ERROR)
                        .findAll());
        if (sessions != null && sessions.size() > 0) {
            RCLog.d("reSubUser：session!=null");
            sessionObserver = new SessionObserver(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
            sessionObserver.register();
            subscriber = new UserDataSubscriber(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
            subscriber.register();
            currentUserObserver = new CurrentUserObserver(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
            currentUserObserver.register();

            MethodCallHelper methodCall = new MethodCallHelper(RealmStore.getOrCreate(hostname));
            methodCall.getRoomSubscriptions().onSuccess(task -> {
                listener = new StreamNotifyUserSubscriptionsChanged(
                        contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname), RocketChatCache.INSTANCE.getUserId());
                listener.register();
                return null;
            }).continueWith(new LogIfError());
            RocketChatCache.INSTANCE.setDownLine(false);
            RocketChatCache.INSTANCE.setFrameUserId(userId);
            RocketChatCache.INSTANCE.setCompanyId(companyId);
            RocketChatCache.INSTANCE.setHomePageVisiable(true);
            String pushId = RocketChatCache.INSTANCE.getOrCreatePushId();
            String deviceId = RocketChatCache.INSTANCE.getAliyunDeviceId();
            gotoChatMessage("showHome");
            dismissProgressDialog();
//            new RaixPushHelper(realmHelper)
//                    .pushUpdate(pushId, deviceId, RocketChatCache.INSTANCE.getUserId());
            methodCall.setUserPushTokenForMobile(RocketChatCache.INSTANCE.getUserId(), pushId, deviceId);

        }
    }

    @Override
    public void finishView() {
        finish();
    }

    @Override
    public void showConnectionError() {
        ConnectionStatusManager.INSTANCE.setConnectionError(this::showConnectionErrorCrouton);
    }

    @Override
    public void showConnecting() {
        ConnectionStatusManager.INSTANCE.setConnecting(this::showConnectingCrouton);
    }

    @Override
    public void showConnectionOk() {
        ConnectionStatusManager.INSTANCE.setOnline(this::dismissStatusTickerIfShowing);
    }

    private void showConnectingCrouton(boolean success) {
//        if (success) {
        croutonText.setText(R.string.server_config_activity_authenticating);
        croutonTryAgainImage.setOnClickListener(null);
        tryAgainSpinnerAnimatedDrawable.start();
        Crouton.cancelAllCroutons();
        updateCrouton();
        croutonStatusTicker.get().show();
//        }
    }

    private void showConnectionErrorCrouton(boolean success) {
//        if (success) {
        tryAgainSpinnerAnimatedDrawable.stop();
        croutonText.setText(R.string.fragment_retry_login_error_title);

        croutonTryAgainImage.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                retryConnection();
            }
        });

        Crouton.cancelAllCroutons();
        updateCrouton();
        croutonStatusTicker.get().show();
//        }
    }

    private void loadCroutonViewIfNeeded() {
        if (croutonView == null) {
            croutonView = LayoutInflater.from(this).inflate(R.layout.crouton_status_ticker, null);
            croutonTryAgainImage = croutonView.findViewById(R.id.try_again_image);
            croutonText = croutonView.findViewById(R.id.text_view_status);
            tryAgainSpinnerAnimatedDrawable =
                    AnimatedVectorDrawableCompat.create(contextWeakReference.get(), R.drawable.ic_loading_animated);
            croutonTryAgainImage.setImageDrawable(tryAgainSpinnerAnimatedDrawable);

            updateCrouton();
        }
    }

    private void updateCrouton() {
        Configuration configuration = new Configuration.Builder()
                .setDuration(Configuration.DURATION_INFINITE).build();
        dismissStatusTickerIfShowing(true);
        Crouton crouton = Crouton.make(this, croutonView, getLayoutContainerForFragment())
                .setConfiguration(configuration);

        croutonStatusTicker.set(crouton);
    }

    private void dismissStatusTickerIfShowing(boolean success) {
        if (croutonStatusTicker.get() != null) {
            croutonStatusTicker.get().hide();
        }
    }

    private void retryConnection() {
//        croutonStatusTicker.set(null);
        showConnecting();
        RCLog.d("retryConnection——》》》》》connect");
        ChatConnectivityManager.getInstance(getApplicationContext()).keepAliveServer();
    }

    @Override
    public void showSignedInServers(List<Pair<String, Pair<String, String>>> serverList) {
//        final SlidingPaneLayout subPane = findViewById(R.id.sub_sliding_pane);
//        if (subPane != null) {
//            LinearLayout serverListContainer = subPane.findViewById(R.id.server_list_bar);
//            View addServerButton = subPane.findViewById(R.id.btn_add_server);
//            addServerButton.setOnClickListener(view -> showAddServerActivity());
//            serverListContainer.removeAllViews();
//            for (Pair<String, Pair<String, String>> server : serverList) {
//                String serverHostname = server.first;
//                Pair<String, String> serverInfoPair = server.second;
//                String logoUrl = serverInfoPair.first;
//                String siteName = serverInfoPair.second;
//                View serverView = serverListContainer.findViewWithTag(serverHostname);
//                if (serverView == null) {
//                    View newServerView = LayoutInflater.from(this).inflate(R.layout.server_row, serverListContainer, false);
//                    SimpleDraweeView serverButton = newServerView.findViewById(R.id.drawee_server_button);
//                    TextView hostnameLabel = newServerView.findViewById(R.id.text_view_server_label);
//                    TextView siteNameLabel = newServerView.findViewById(R.id.text_view_site_name_label);
//                    ImageView dotView = newServerView.findViewById(R.id.selected_server_dot);
//
//                    newServerView.setTag(serverHostname);
//                    hostnameLabel.setText(serverHostname);
//                    siteNameLabel.setText(siteName);
//
//                    // Currently selected server
//                    if (hostname.equalsIgnoreCase(serverHostname)) {
//                        newServerView.setSelected(true);
//                        dotView.setVisibility(View.VISIBLE);
//                    } else {
//                        newServerView.setSelected(false);
//                        dotView.setVisibility(View.GONE);
//                    }
//
//                    newServerView.setOnClickListener(view -> changeServerIfNeeded(serverHostname));
//
//                    Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher);
//                    if (drawable == null) {
//                        int id = getResources().getIdentifier(
//                                "rocket_chat_notification", "drawable", getPackageName());
//                        drawable = ContextCompat.getDrawable(this, id);
//                    }
//                    FrescoHelper.INSTANCE.loadImage(serverButton, logoUrl, drawable);
//
//                    serverListContainer.addView(newServerView);
//                }
//            }
//            serverListContainer.addView(addServerButton);
//        }
    }

    @Override
    public void refreshRoom() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(getLayoutContainerForFragment());
        if (fragment != null && fragment instanceof RoomFragment) {
            RoomFragment roomFragment = (RoomFragment) fragment;
            roomFragment.loadMissedMessages();
        }
    }

    @Override
    public void refreshUser() {
        List<RealmSession> sessions = RealmStore.getOrCreate(hostname).executeTransactionForReadResults(realm ->
                realm.where(RealmSession.class)
                        .isNotNull(RealmSession.TOKEN)
                        .equalTo(RealmSession.TOKEN_VERIFIED, true)
                        .isNull(RealmSession.ERROR)
                        .findAll());
        if (sessions != null && sessions.size() > 0) {

        } else {
            presenter.loginUser(this, this.sessionId);
        }
    }


    @DebugLog
    public void onLogout() {
        presenter.prepareToLogout();
        if (RocketChatCache.INSTANCE.getSelectedServerHostname() == null) {
            finish();
            LaunchUtil.showMainActivity(this);
        } else {
            onHostnameUpdated();
        }
    }


    protected LoginProgressDialog1 mProgressDialog;

    /**
     * 显示自定义进度框
     */
    @Override
    public void showProgressDialog(String msg) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog = new LoginProgressDialog1(this, R.style.CustomProgressDialog1, msg);
        mProgressDialog.show();
    }

    /**
     * 取消自定义进度框
     */
    @Override
    public void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!TextUtils.isEmpty(getIntent().getStringExtra("roomId"))) {
            roomId = getIntent().getStringExtra("roomId");
            String rId = getIntent().getStringExtra("rId");
            if(!TextUtils.isEmpty(rId)&&!TextUtils.isEmpty(hostname)){
                showRoom(hostname,rId);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        SharedPreferencesUtil.saveBooleanData(this,IS_DESTROY,true);
        if (sessionObserver != null)
            sessionObserver.unregister();
        if (listener != null)
            listener.unregister();
        if (subscriber != null)
            subscriber.unregister();
        if (currentUserObserver != null){
            try {
                currentUserObserver.unregister();
            } catch (Exception e) {
            }
        }
        dismissProgressDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleMessage(BaseEvent event) {
        if (event.getCode() == EventTags.STATE_SESSION_ESTABLISHED) {
            presenter.setUserOnline(User.STATUS_ONLINE);
            refreshRoom();
            showConnectionOk();
        } else if (event.getCode() == EventTags.STATE_DISCONNECTED) {
            showConnectionError();
        }

    }


    long exitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && TempFileUtils.getInstance().getTalkingStatus()) {
            EventBus.getDefault().post(new ViewEvent());
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (System.currentTimeMillis() - exitTime > 1500) {
                ToastUtils.showToast("确定退出聊天界面");
                exitTime = System.currentTimeMillis();
            } else {
                this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }


    public static  WeakReference<ChatMainActivity> getChatMainActivity(){
        return context ;
    }
}
