package chat.rocket.android.fragment.chatroom;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.os.BuildCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hadisatrio.optional.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bolts.Task;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.activity.business.GroupInfoActivity;
import chat.rocket.android.activity.business.MeetingGroupInfoActivity;
import chat.rocket.android.activity.business.MoreGroupMemberActivity;
import chat.rocket.android.activity.business.MyInfoActivity;
import chat.rocket.android.activity.business.RelayActivity;
import chat.rocket.android.activity.business.SelectedAlertActivity;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.chatroom.dialog.FileUploadProgressDialogFragment;
import chat.rocket.android.fragment.sidebar.SidebarMainFragment;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.FileUploadHelper;
import chat.rocket.android.helper.FileUtils;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.LoadMoreScrollListener;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.OnBackPressListener;
import chat.rocket.android.helper.PermissionsUtils;
import chat.rocket.android.helper.RecyclerViewAutoScrollManager;
import chat.rocket.android.helper.RecyclerViewScrolledToBottomListener;
import chat.rocket.android.helper.RxTimerUtil;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.layouthelper.chatroom.AbstractNewMessageIndicatorManager;
import chat.rocket.android.layouthelper.chatroom.MessageFormManager;
import chat.rocket.android.layouthelper.chatroom.MessageListAdapter;
import chat.rocket.android.layouthelper.chatroom.MessagePopup;
import chat.rocket.android.layouthelper.chatroom.ModelListAdapter;
import chat.rocket.android.layouthelper.chatroom.PairedMessage;
import chat.rocket.android.layouthelper.extra_action.AbstractExtraActionItem;
import chat.rocket.android.layouthelper.extra_action.MessageExtraActionBehavior;
import chat.rocket.android.layouthelper.extra_action.upload.AbstractUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.AudioUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.FileUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.ImageUploadActionItem;
import chat.rocket.android.layouthelper.extra_action.upload.VideoUploadActionItem;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.login.CustomProgressDialog;
import chat.rocket.android.renderer.RocketChatUserStatusProvider;
import chat.rocket.android.service.ChatConnectivityManager;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserRoomChanged;
import chat.rocket.android.service.temp.DeafultTempSpotlightRoomCaller;
import chat.rocket.android.service.temp.DefaultTempSpotlightUserCaller;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.model.VideoRefreshBusEvent;
import chat.rocket.android.video.view.ChooseVideoDialog;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.RoomToolbar;
import chat.rocket.android.widget.emotionkeyboard.adapter.NoHorizontalScrollerVPAdapter;
import chat.rocket.android.widget.emotionkeyboard.fragment.EmotiomComplateFragment;
import chat.rocket.android.widget.emotionkeyboard.fragment.FragmentFactory;
import chat.rocket.android.widget.emotionkeyboard.utils.EmotionUtils;
import chat.rocket.android.widget.emotionkeyboard.utils.GlobalOnItemClickManagerUtils;
import chat.rocket.android.widget.helper.AudioHelper;
import chat.rocket.android.widget.internal.ExtraActionPickerDialogFragment;
import chat.rocket.android.widget.message.ImageKeyboardEditText;
import chat.rocket.android.widget.message.MessageFormLayout;
import chat.rocket.android.widget.message.autocomplete.AutocompleteManager;
import chat.rocket.android.widget.message.autocomplete.channel.ChannelSource;
import chat.rocket.android.widget.message.autocomplete.user.UserSource;
import chat.rocket.core.interactors.AutocompleteChannelInteractor;
import chat.rocket.core.interactors.AutocompleteUserInteractor;
import chat.rocket.core.interactors.MessageInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Attendance;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightUserRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.utils.ImageCaptureManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;

/**
 * Chat room screen.
 */
@RuntimePermissions
public class RoomFragment extends AbstractChatRoomFragment implements
        OnBackPressListener,

        ExtraActionPickerDialogFragment.Callback,
        ModelListAdapter.OnItemLongClickListener<PairedMessage>,
        ModelListAdapter.OnItemClickListener<PairedMessage>,
        MessageListAdapter.OnMessageFailedClickListener<PairedMessage>,
        MessageListAdapter.OnImageClickListener<PairedMessage>,
        MessageListAdapter.OnImageLongClickListener<PairedMessage>,
        MessageListAdapter.OnAttachItemLongClickListener<PairedMessage>,
        RoomContract.View {

    private static final int DIALOG_ID = 1;
    private static final String HOSTNAME = "hostname";
    private static final String ROOM_ID = "roomId";
    private static final String SUBSCRIPTION_T = "subscriptionT";

    private String hostname;
    private String token;
    private String userId;
    private String roomId, roomType, subscriptionDisplayName, blocked, blocker;
    private LoadMoreScrollListener scrollListener;
    private MessageFormManager messageFormManager;
    private RecyclerView messageRecyclerView;
    // messageRecyclerView需要的manager
    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewAutoScrollManager recyclerViewAutoScrollManager;
    protected AbstractNewMessageIndicatorManager newMessageIndicatorManager;
    protected Snackbar unreadIndicator;
    private boolean previousUnreadMessageExists;
    private MessageListAdapter messageListAdapter;

    private AutocompleteManager autocompleteManager;

    private List<AbstractExtraActionItem> extraActionItems;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected RoomContract.Presenter presenter;
    private RealmSubscriptionRepository realmSubscriptionRepository;
    private RealmUserRepository userRepository;
    private MethodCallHelper methodCallHelper;

    private AbsoluteUrlHelper absoluteUrlHelper;

    private Message editingMessage = null;

    private RoomToolbar toolbar;
    private Optional<SlidingPaneLayout> optionalPane;
    private SidebarMainFragment sidebarFragment;
    private MessageFormLayout messageFormLayout;

    /**
     * 雷厉&46196
     */
    private String sub_name;
    private RealmRoomRepository realmRoomRepository;
    private List<UserEntity> remindUsers;
    /** 目标项是否在最后一个可见项之后*/
    private boolean mShouldScroll;
    /** 记录目标项位置*/
    private int mToPosition;
    private boolean first = true;

    private LinearLayout ll_unread;
    private TextView tv_unread;
    private ImageCaptureManager captureManager;//用于管理相机
    private boolean canDeleteMySelf=false;
    Button btn_qiandao;
    private static Handler handler;

    public RoomFragment() {
    }

    /**
     * build fragment with roomId.
     */
    public static RoomFragment create(String hostname, String roomId) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);
        args.putString(ROOM_ID, roomId);

        RoomFragment fragment = new RoomFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        hostname = args.getString(HOSTNAME);
        roomId = args.getString(ROOM_ID);

//        roomRepository = new RealmRoomRepository(hostname);
        realmSubscriptionRepository = new RealmSubscriptionRepository(hostname);
        realmRoomRepository = new RealmRoomRepository(hostname);

        MessageInteractor messageInteractor = new MessageInteractor(
                new RealmMessageRepository(hostname),
                realmSubscriptionRepository
        );

        userRepository = new RealmUserRepository(hostname);

        absoluteUrlHelper = new AbsoluteUrlHelper(
                hostname,
                new RealmServerInfoRepository(),
                userRepository,
                new SessionInteractor(new RealmSessionRepository(hostname))
        );

        methodCallHelper = new MethodCallHelper(getContext(), hostname);

        presenter = new RoomPresenter(
                roomId,
                userRepository,
                messageInteractor,
                realmSubscriptionRepository,
                absoluteUrlHelper,
                methodCallHelper,
                ChatConnectivityManager.getInstance(getContext())
        );

        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
        MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
        methodCall.getRooms().onSuccess(task -> {
            Registrable listener = new StreamNotifyUserRoomChanged(
                    getActivity(), hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
            listener.register();
            return null;
        }).continueWith(new LogIfError());
        if (savedInstanceState == null) {
            presenter.loadMessages();
        }

//        PermissionsUtils.verifyStoragePermissions(getActivity());

    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_room;
    }

    @Override
    protected void onSetupView() {
        captureManager = new ImageCaptureManager(getActivity());
        handler = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                if(msg.what==1){
                    ToastUtils.showToast("归档成功");
                }else if(msg.what==2){
                    ToastUtils.showToast("归档失败");
                }
            }
        };
        ll_unread=rootView.findViewById(R.id.ll_room_unread);
        tv_unread=rootView.findViewById(R.id.tv_room_unread);
        optionalPane = Optional.ofNullable(getActivity().findViewById(R.id.sliding_pane));
        messageRecyclerView = rootView.findViewById(R.id.messageRecyclerView);
        btn_qiandao = rootView.findViewById(R.id.btn_qiandao);
        messageListAdapter = new MessageListAdapter(getContext(), hostname);
        messageListAdapter.setOnItemLongClickListener(this);
        messageListAdapter.setOnItemClickListener(this);
        messageListAdapter.setOnMessageFailedClickListener(this);
        messageListAdapter.setOnImageClickListener(this);
        messageListAdapter.setOnImageLongClickListener(this);
        messageListAdapter.setOnAttachItemLongClickListener(this);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageRecyclerView.setAdapter(messageListAdapter);
        messageRecyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom+300 < oldBottom) {
                messageRecyclerView.postDelayed(() -> messageRecyclerView.scrollToPosition(0), 100);
            }
        });
        recyclerViewAutoScrollManager = new RecyclerViewAutoScrollManager(linearLayoutManager) {
            @Override
            protected void onAutoScrollMissed() {
                if (newMessageIndicatorManager != null) {
                    presenter.onUnreadCount();
                }
            }
        };
        messageListAdapter.registerAdapterDataObserver(recyclerViewAutoScrollManager);

        // 判断用户是否签到，是否应该显示签到按钮 //
        Room currentRoom = realmRoomRepository.getRoomByRoomId(roomId);
        if(currentRoom != null &&("true".equals(currentRoom.getAttendance())||"1".equals(currentRoom.getAttendance()))&&currentRoom.isOpen()){
            List<Attendance> attendanceList = currentRoom.getAttendanceList();
            boolean isQianDaoFinish = false;
            String userUsername = RocketChatCache.INSTANCE.getUserUsername();
            if(attendanceList != null && attendanceList.size() > 0){
                for(Attendance attendance : attendanceList){
                    if(userUsername != null && userUsername.equals(attendance.getUserName())){
                        isQianDaoFinish = true;
                        break;
                    }
                }
            }

            if(isQianDaoFinish){
                btn_qiandao.setVisibility(View.GONE);
            }else{
                btn_qiandao.setVisibility(View.VISIBLE);
                btn_qiandao.setOnClickListener(v -> {
                    methodCallHelper.attendanceFromMobile(roomId, userUsername,btn_qiandao);
                });
            }
        }

        scrollListener = new LoadMoreScrollListener(linearLayoutManager, 10) {
            @Override
            public void requestMoreItem() {
                presenter.loadMoreMessages();
            }
        };
        messageRecyclerView.addOnScrollListener(scrollListener);
//        messageRecyclerView.addOnScrollListener(new RecyclerViewScrolledToBottomListener(linearLayoutManager, 1, this::markAsReadIfNeeded));
        messageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /* new State
                0（SCROLL_STATE_IDLE）表示recyclerview是不动的
                1（SCROLL_STATE_DRAGGING）表示recyclerview正在被拖拽
                2（SCROLL_STATE_SETTLING）表示recyclerview正在惯性下滚动
                 */
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        KeyboardHelper.hideSoftKeyboard(getActivity(),messageFormLayout);
                        try {
                            messageFormLayout.hideEmotionLayout(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                // 目的：点击搜索历史消息，需要滚动对应的消息到RecyclerView的top位置
//

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        newMessageIndicatorManager = new AbstractNewMessageIndicatorManager() {
            @Override
            protected void onShowIndicator(int count, boolean onlyAlreadyShown) {
                if ((onlyAlreadyShown && unreadIndicator != null && unreadIndicator.isShown()) || !onlyAlreadyShown) {
                    unreadIndicator = getUnreadCountIndicatorView(count);
                    unreadIndicator.show();
                }
            }

            @Override
            protected void onHideIndicator() {
                if (unreadIndicator != null && unreadIndicator.isShown()) {
                    unreadIndicator.dismiss();
                }
            }
        };
        RoomFragmentPermissionsDispatcher.storagePermissionWithCheck(RoomFragment.this);
    }

    protected boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager.findFirstVisibleItemPosition()==1)
            return true;
        return false;
    }

    private void setupMessageActions() {
        extraActionItems = new ArrayList<>(4); // fixed number as of now
        extraActionItems.add(new ImageUploadActionItem());
        extraActionItems.add(new AudioUploadActionItem());
        extraActionItems.add(new VideoUploadActionItem());
        extraActionItems.add(new FileUploadActionItem());
    }

    private void scrollToLatestMessage() {
        if (messageListAdapter.getItemData()==null||messageListAdapter.getItemData().size()==0){
            presenter.refreshRoom(false);
        }
//        if (messageListAdapter.getItemData().get(messageListAdapter.getItemData().size()-1).target.getMessage().
//                contains(RocketChatCache.INSTANCE.getSelectedServerHostname())&&
//                messageListAdapter.getItemData().get(messageListAdapter.getItemData().size()-1).target.getAttachmentsJson()!=null){
//            presenter.refreshRoom();
////            presenter.loadMissedMessages(true);
//        }
        if (messageRecyclerView != null&&messageListAdapter.getItemData() != null && messageListAdapter.getItemData().size()!=0)
//            messageRecyclerView.scrollToPosition(messageListAdapter.getItemCount() - 1);
            messageRecyclerView.scrollToPosition(0);
    }

    protected Snackbar getUnreadCountIndicatorView(int count) {
        // TODO: replace with another custom View widget, not to hide message composer.
        final String caption = getResources().getQuantityString(
                R.plurals.fmt_dialog_view_latest_message_title, count, count);

        return Snackbar.make(rootView, caption, Snackbar.LENGTH_LONG)
                .setAction(R.string.dialog_view_latest_message_action, view -> scrollToLatestMessage());
    }

    @Override
    public void onDestroyView() {
        RecyclerView.Adapter adapter = messageRecyclerView.getAdapter();
        if (adapter != null)
            adapter.unregisterAdapterDataObserver(recyclerViewAutoScrollManager);

        compositeDisposable.clear();

        if (autocompleteManager != null) {
            autocompleteManager.dispose();
            autocompleteManager = null;
        }

        super.onDestroyView();
    }

    @Override
    public boolean onItemLongClick(PairedMessage pairedMessage) {
        KeyboardHelper.hideSoftKeyboard(getActivity(),messageFormLayout);
        presenter.onMessageSelected(pairedMessage.target);
        return true;
    }

    @Override
    public void onAttachItemLongClick(PairedMessage pairedMessage) {
        KeyboardHelper.hideSoftKeyboard(getActivity(),messageFormLayout);
        presenter.onMessageSelected(pairedMessage.target);
    }

    @Override
    public void onMessageFailedClick(PairedMessage pairedMessage) {
        KeyboardHelper.hideSoftKeyboard(getActivity(),messageFormLayout);
        presenter.onMessageTap(pairedMessage.target);
    }

    /**
     * 点击用户头像
     */
    @Override
    public void onImageClick(PairedMessage pairedMessage) {
        KeyboardHelper.hideSoftKeyboard(getActivity(),messageFormLayout);
        Intent intent = new Intent(getActivity(), MyInfoActivity.class);
        User user = pairedMessage.target.getUser();
        intent.putExtra("roomId", roomId);
        intent.putExtra("roomType", roomType);
        if(RocketChatConstants.D.equals(roomType)){
            intent.putExtra("showGroupOperation", false);
            intent.putExtra("blocker", blocker);
        }else{
            intent.putExtra("showGroupOperation", true);
        }
        intent.putExtra("username", user == null ? "" : user.getUsername());
        startActivity(intent);
    }

    /**长按用户头像*/
    @Override
    public void onImageLongClick(PairedMessage pairedMessage) {
        KeyboardHelper.hideSoftKeyboard(getActivity(),messageFormLayout);
        User user = pairedMessage.target.getUser();
        messageFormLayout.setTextUserName( "@" + user.getRealName() + " " );
        UserEntity userEntity = new UserEntity();
        userEntity.setRealName(user.getRealName());
        userEntity.setUsername(user.getUsername());
        if (remindUsers == null) {
            remindUsers = new ArrayList<>();
        }
        remindUsers.add(userEntity);
    }
    /**
     * 点击整个item
     *
     * @param pairedMessage
     */
    @Override
    public void onItemClick(PairedMessage pairedMessage) {
        KeyboardHelper.hideSoftKeyboard(getActivity(),messageFormLayout);
//        presenter.onMessageTap(pairedMessage.target);
    }

    private void setupToolbar() {
        toolbar = getActivity().findViewById(R.id.activity_main_toolbar);
        toolbar.getMenu().clear();
        setToobarIcon();

        optionalPane.ifPresent(pane -> toolbar.setNavigationOnClickListener(view -> {
            if (pane.isSlideable() && !pane.isOpen()) {
                pane.openPane();
            }
        }));

        toolbar.setOnMenuItemClickListener(menuItem -> {
            Intent intent;
            switch (menuItem.getItemId()) {
                case R.id.action_person:
                    intent = new Intent(getActivity(), MyInfoActivity.class);
                    intent.putExtra("roomId",roomId);
                    intent.putExtra("username", sub_name);
                    intent.putExtra("blocker", blocker);
                    intent.putExtra("showLockUser", true);
                    startActivity(intent);
                    break;
                case R.id.action_group:
                    if(RocketChatConstants.M.equals(roomType)){
                        intent = new Intent(getActivity(), MeetingGroupInfoActivity.class);
                        Room roomByRoomId = realmRoomRepository.getRoomByRoomId(roomId);
                        intent.putExtra("isPause",roomByRoomId!=null?roomByRoomId.isPause():false);
                    }else{
                        intent = new Intent(getActivity(), GroupInfoActivity.class);
                    }
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("roomType", roomType);
                    intent.putExtra("username", sub_name);
                    intent.putExtra("showGroupOperation", true);
                    intent.putExtra("subscriptionDisplayName", subscriptionDisplayName);
                    if (isRoomClosed){
                        intent.putExtra("isPauseOrFinish", true);
//                        ToastUtils.showToast("会议已经暂停或结束，无法查看！");
                    }
                        startActivity(intent);

                    break;
            }
            return true;
        });
    }

    private void setToobarIcon() {
        if (toolbar == null || roomType == null) return;
        toolbar.getMenu().clear();
        if ("d".equals(roomType)) {//点对点聊天，点击显示个人信息
            toolbar.inflateMenu(R.menu.menu_room);
        } else {
            toolbar.inflateMenu(R.menu.menu_room_group);
        }
    }

    private void setupSidebar() {
        SlidingPaneLayout subPane = getActivity().findViewById(R.id.sub_sliding_pane);
        sidebarFragment = (SidebarMainFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.sidebar_fragment_container);

        optionalPane.ifPresent(pane -> pane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(@NonNull View view, float v) {
                messageFormManager.enableComposingText(false);
                sidebarFragment.clearSearchViewFocus();
                //Ref: ActionBarDrawerToggle#setProgress
                toolbar.setNavigationIconProgress(v);
            }

            @Override
            public void onPanelOpened(@NonNull View view) {
                toolbar.setNavigationIconVerticalMirror(true);
                if(RoomFragment.this.getActivity()==null){
                    return;
                }
                KeyboardHelper.hideSoftKeyboard(RoomFragment.this.getActivity(),messageFormLayout);
            }

            @Override
            public void onPanelClosed(@NonNull View view) {
                messageFormManager.enableComposingText(true);
                toolbar.setNavigationIconVerticalMirror(false);
                subPane.closePane();
                closeUserActionContainer();
                if(RoomFragment.this.getActivity()==null){
                    return;
                }
                KeyboardHelper.hideSoftKeyboard(RoomFragment.this.getActivity(),messageFormLayout);
            }
        }));
    }

    public void closeUserActionContainer() {
        sidebarFragment.closeUserActionContainer();
    }

    private void setupMessageComposer() {
        messageFormLayout = rootView.findViewById(R.id.messageComposer);
        messageFormManager = new MessageFormManager(messageFormLayout);//, this::showExtraActionSelectionDialog
        messageFormManager.setSendMessageCallback(this::sendMessage);
        messageFormManager.setShowBiaoqingCallback(this::onShowSoftwindow);
        messageFormManager.setShowFileCallback(this::onItemSelected);
        messageFormManager.setShowAudioCallback(this::onItemSelected);
        messageFormManager.setShowVideoCallback(this::onItemSelected);
        messageFormManager.setShowCameraCallback(this::onItemSelected);
        messageFormManager.setShowGalleryCallback(this::onItemSelected);
        messageFormLayout.setShowSelectedActivityListener(() -> startSelectActivity(false));
        messageFormLayout.setShowAlertSelectListener(this::showAlert);
        messageFormLayout.setEditTextCommitContentListener(this::onCommitContent);
        messageFormLayout.setAudioListener(new MessageFormLayout.AudioChange() {
            @Override
            public void onAudioChange() {
//                isAudioCheck=checked;
                RoomFragmentPermissionsDispatcher.vudioPermissionWithCheck(RoomFragment.this);
            }
        });
        messageFormLayout.setPermissionToVerifyListener(() -> {
            boolean isDown = messageFormLayout.getAudioRecorderButtonDown();
            System.out.println(isDown + "permission______________________________");
            if (isDown) {
                messageFormLayout.setPrepareAudio();
            }
//            RoomFragmentPermissionsDispatcher.vudioPermissionWithCheck(RoomFragment.this);
        });
        messageFormLayout.setAudioFinishListener(filePath -> {
            uploadFile(Uri.fromFile(new File(filePath)));
        });
        autocompleteManager = new AutocompleteManager(rootView.findViewById(R.id.messageListRelativeLayout));

        autocompleteManager.registerSource(
                new ChannelSource(
                        new AutocompleteChannelInteractor(
                                realmSubscriptionRepository,
                                new RealmSpotlightRoomRepository(hostname),
                                new DeafultTempSpotlightRoomCaller(methodCallHelper)
                        ),
                        AndroidSchedulers.from(BackgroundLooper.get()),
                        AndroidSchedulers.mainThread()
                )
        );

        Disposable disposable = Single.zip(
                absoluteUrlHelper.getRocketChatAbsoluteUrl(),
                realmSubscriptionRepository.getById(roomId).first(Optional.absent()),
                Pair::create
        )
                .subscribe(
                        pair -> {
                            if (pair.first.isPresent() && pair.second.isPresent()) {
                                autocompleteManager.registerSource(
                                        new UserSource(
                                                new AutocompleteUserInteractor(
                                                        pair.second.get(),
                                                        userRepository,
                                                        new RealmMessageRepository(hostname),
                                                        new RealmSpotlightUserRepository(hostname),
                                                        new DefaultTempSpotlightUserCaller(methodCallHelper)
                                                ),
                                                pair.first.get(),
                                                RocketChatUserStatusProvider.INSTANCE,
                                                AndroidSchedulers.from(BackgroundLooper.get()),
                                                AndroidSchedulers.mainThread()
                                        )
                                );
                            }
                        },
                        throwable -> {
                        }
                );

        compositeDisposable.add(disposable);

//        autocompleteManager.bindTo(
//                messageFormLayout.getEditText(),
//                messageFormLayout
//        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**拍照*/
        if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (captureManager == null) {
                FragmentActivity activity = getActivity();
                captureManager = new ImageCaptureManager(activity);
            }
            String path = captureManager.getCurrentPhotoPath();
//            RCLog.d("pictrue->>"+path);
            if (!TextUtils.isEmpty(path))
            uploadFile(Uri.fromFile(new File(path)));
        }
        /**选图库*/
        if (resultCode == Activity.RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {

                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                for (String path:photos){
                    RCLog.d("pictrue->>"+path);
                    RxTimerUtil.timer(500, number -> {
                        uploadFile(Uri.fromFile(new File(path)));
                    });


                }
            }
        }else {
        if (resultCode == 1000) {
            if (remindUsers == null) {
                remindUsers = new ArrayList<>();
            }
            UserEntity userEntity = data.getParcelableExtra("user");
            boolean isAlert = data.getBooleanExtra("isAlert", false);
            remindUsers.add(userEntity);
            messageFormLayout.setTextUserName(isAlert ? " @" + userEntity.getRealName() + " " : userEntity.getRealName() + " ");
            return;
        }
        if (requestCode != AbstractUploadActionItem.RC_UPL || resultCode != Activity.RESULT_OK) {
            return;
        }

        if (data == null || data.getData() == null) {
            return;
        }
//        RCLog.d("file->>"+data.getData().toString());
            try {
                uploadFile(Uri.fromFile(new File(FileUtils.getFilePathByUri(getActivity(),data.getData()))));
//                uploadFile(file2Uri(getActivity(),new File(FileUtils.getFilePathByUri(getActivity(),data.getData()))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode==0x6&&resultCode==Activity.RESULT_OK){
            presenter.relayMessage();
        }
    }
    String uplId;
    private void uploadFile(Uri uri) {
        uplId = new FileUploadHelper(getContext(), RealmStore.get(hostname))
                .requestUploading(roomId, uri);
        if (!TextUtils.isEmpty(uplId)) {
            dialogFragment=FileUploadProgressDialogFragment.create(hostname, roomId, uplId);
            dialogFragment.show(getFragmentManager(), "FileUploadProgressDialogFragment");
        } else {
            // show error.
        }
    }
    FileUploadProgressDialogFragment dialogFragment;
    private void markAsReadIfNeeded() {
        presenter.onMarkAsRead();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter!=null)
            presenter.getRoomInfo();
        // 搜索历史消息点击跳转到对应位置
        String isClickHistoryInfo = RocketChatCache.INSTANCE.getIsClickHostoryInfo();
        if(!TextUtils.isEmpty(isClickHistoryInfo)){
            moveToPosition(isClickHistoryInfo);
            RocketChatCache.INSTANCE.setIsClickHostoryInfo(null);
        }
    }

    @Override
    public void onPause() {

        super.onPause();
    }

//    private void showExtraActionSelectionDialog() {
//        final DialogFragment fragment = ExtraActionPickerDialogFragment
//                .create(new ArrayList<>(extraActionItems));
//        fragment.setTargetFragment(this, DIALOG_ID);
//        fragment.show(getFragmentManager(), "ExtraActionPickerDialogFragment");
//    }

    public void onItemSelected(int position) {
        AbstractExtraActionItem extraActionItem = null;
        if (position == 5 ){
            /**选择图库*/
            PhotoPicker.builder()
                    .setPhotoCount(9)
                    .setShowCamera(false)
                    .setShowGif(true)
                    .setPreviewEnabled(true)
                    .start(getActivity(), this);
        }else if (position == 4){
            /**拍照*/
//            if (!me.iwf.photopicker.utils.PermissionsUtils.checkCameraPermission(this)) return;
//            if (!me.iwf.photopicker.utils.PermissionsUtils.checkWriteStoragePermission(this)) return;
            RoomFragmentPermissionsDispatcher.cameraPermissionWithCheck(this);

        }
        else if (position == 3) {
            ChooseVideoDialog dialog = new ChooseVideoDialog(getActivity());
            dialog.showDialog();
            dialog.setOnVideoDialogClickListener(position1 -> gotoVideoActivity(position1));
        }
        else {
        if (position == 1) {
            extraActionItem = new FileUploadActionItem();
        } else if (position == 2) {
            extraActionItem = new AudioUploadActionItem();
        }
        else if (position == 3) {
            extraActionItem = new VideoUploadActionItem();
        }
        RoomFragmentPermissionsDispatcher.onExtraActionSelectedWithCheck(RoomFragment.this, extraActionItem);
        }
//        for (AbstractExtraActionItem extraActionItem : extraActionItems) {
//            RoomFragmentPermissionsDispatcher.onExtraActionSelectedWithCheck(RoomFragment.this, extraActionItem);
//        }
    }

    @Override
    public boolean onBackPressed() {
        if (editingMessage != null) {
            editingMessage = null;
            messageFormManager.clearComposingText();
        }
        if (myViewPager != null && myViewPager.isShown()) {
            myViewPager.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RoomFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({ Manifest.permission.CAMERA})
    public void cameraPermission() {
        try {
            Intent intent = captureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException e) {
            Log.e("PhotoPickerFragment", "No Activity Found to handle Intent", e);
        }
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void storagePermission() {
        showProgressDialog();
        presenter.bindView(this);
        setupToolbar();
        setupSidebar();
        setupMessageComposer();
        setupMessageActions();
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void vudioPermission() {
        messageFormLayout.changeRecordStatus();
    }
    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void vudioPermissionDenied(){
        messageFormLayout.setToggle();
    }
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    protected void onExtraActionSelected(MessageExtraActionBehavior action) {
        action.handleItemSelectedOnFragment(RoomFragment.this);
    }

    private boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                    Bundle opts, String[] supportedMimeTypes) {
        boolean supported = false;
        for (final String mimeType : supportedMimeTypes) {
            if (inputContentInfo.getDescription().hasMimeType(mimeType)) {
                supported = true;
                break;
            }
        }

        if (!supported) {
            return false;
        }

        if (BuildCompat.isAtLeastNMR1()
                && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
            try {
                inputContentInfo.requestPermission();
            } catch (Exception e) {
                return false;
            }
        }

        Uri linkUri = inputContentInfo.getLinkUri();
        if (linkUri == null) {
            return false;
        }

        sendMessage(linkUri.toString());

        try {
            inputContentInfo.releasePermission();
        } catch (Exception e) {
            RCLog.e(e);
            Logger.INSTANCE.report(e);
        }

        return true;
    }

    private void showAlert() {
        startSelectActivity(true);
    }

    @SuppressLint("RxLeakedSubscription")
    private void startSelectActivity(boolean isAlert) {
//        Toast.makeText(getContext(), "跳转到@页面", Toast.LENGTH_SHORT).show();
        Room room = realmRoomRepository.getByRoomId(roomId);
        if (room==null){
            ToastUtils.showToast("数据加载异常");
            return;
        }
        findUser(room.getUsernames(), isAlert);

    }

    private void findUser(List<String> usernames, boolean isAlert) {
        System.out.println("findUser------------------------------------------");
        String[] str;
        if (usernames != null && usernames.size() > 0) {
            str = usernames.toArray(new String[0]);
        }else{
            RCLog.e("findUser usernames=null");
            return;
        }
        List<User> userNameAll = new ArrayList<>();
        List<User> temp = userRepository.getByNameAll(str);

        userNameAll.addAll(temp);
        User tempUser = null;
        for (User user : userNameAll) {
            String realName = user.getRealName();
            if ("小翌".equals(realName) || "小翌".equals(user.getUsername())) {
                tempUser = user;
            }
        }
        userNameAll.remove(tempUser);
        Intent intent = new Intent(getActivity(), SelectedAlertActivity.class);
        intent.putExtra("remind", true);
        intent.putExtra("isAlert", isAlert);
        intent.putExtra("roomId",roomId);
        intent.putExtra("hostname",hostname);
        intent.putParcelableArrayListExtra("selectList", getallMember(userNameAll));
        startActivityForResult(intent, 100);

    }

    private ArrayList<UserEntity> getallMember(List<User> userNameAll) {
        ArrayList<UserEntity> list = new ArrayList<>();
        for (User user : userNameAll) {
            UserEntity entity = new UserEntity(false, user.getRealName(), user.getUsername());
            entity.setCompanyName(user.getCompanyName());
            entity.setStatus(user.getStatus());
            entity.setAvatar(user.getAvatar());
            entity.setRealName(user.getRealName());
            entity.setLastOnlineTime(user.get_updatedAt());
            List<DeptRole> deptRole = user.getDeptRole();
            if (deptRole != null && deptRole.size() > 0) {
                entity.setDept(deptRole.get(0).getOrg_name());
                entity.setZhiWei(deptRole.get(0).getPos_name().split("_")[1]);
                entity.setStatus(user.getStatus());
            }
            list.add(entity);
        }
        return list;
    }

    private void sendMessage(String messageText) {
        if (editingMessage == null) {
            if (messageText.indexOf("@") != -1 && remindUsers != null) {
                for (UserEntity entity : remindUsers) {
                    String nameStr = "@" + entity.getRealName() + " ";
                    if (messageText.indexOf(nameStr) != -1) {
                        String userNameStr = "@" + entity.getUsername() + " ";
                        messageText = messageText.replace(nameStr, userNameStr);
                    }
                }
            }
            presenter.sendMessage(messageText);
        } else {
            presenter.updateMessage(editingMessage, messageText);
        }
    }
    RocketChatAbsoluteUrl rocketChatAbsoluteUrl;
    @Override
    public void setupWith(@NonNull RocketChatAbsoluteUrl rocketChatAbsoluteUrl) {
        if (rocketChatAbsoluteUrl != null) {
            token = rocketChatAbsoluteUrl.getToken();
            userId = rocketChatAbsoluteUrl.getUserId();
            messageListAdapter.setAbsoluteUrl(rocketChatAbsoluteUrl);
        }
        this.rocketChatAbsoluteUrl=rocketChatAbsoluteUrl;
    }
    boolean isRoomClosed=false;
    @Override
    public void render(@NonNull Subscription subscription) {
        //新创建频道不刷新
        String unread = subscription.getUnread();
        if(Integer.parseInt(unread)>0){
            List<PairedMessage> itemData = messageListAdapter.getItemData();
            if(itemData==null||itemData.size()==0){
                RealmMessageRepository repository=new RealmMessageRepository(hostname);
                List<Message> messages = repository.getAllMessageByRoomId(roomId);
                if(messages!=null&&messages.size()>0){
                    presenter.refreshRoom(false);
                }
            }
        }

        roomType = subscription.getT();
        subscriptionDisplayName = subscription.getDisplayName();
        blocker = subscription.getBlocker();
        blocked = subscription.getBlocked();
        // 锁定用户需要限制聊天（点对点）
        if (RocketChatConstants.D.equals(roomType)) {
            isRoomClosed=false;
            if ("true".equals(blocked) || "true".equals(blocker)) {
                messageFormManager.setVisibility(true,0);
            } else {
                messageFormManager.setVisibility(false,0);
            }
        }
       else {
            // 禁止发言 需要限制聊天（群聊）
            Disposable subscribe = realmRoomRepository.getById(roomId).subscribe(roomOptional -> {
                Room roomByRoomId = roomOptional.get();
                List<String> muted = roomByRoomId == null ? null : roomByRoomId.getMuted();
                if (muted != null && muted.contains(subscription.getUUserName())) {
                    if(RocketChatConstants.M.equals(roomType)){
                        isRoomClosed=true;
                        RealmRoomRepository repository=new RealmRoomRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
                         {
                            if (repository.getByRoomId(roomId).isPause())
                                messageFormManager.setVisibility(true,1);  //会议暂停
                            else if (repository.getByRoomId(roomId).isOpen()){
                                messageFormManager.setVisibility(true,4); //被禁言
                            }else
                                messageFormManager.setVisibility(true,2); //会议终止

                        }
                        btn_qiandao.setVisibility(View.GONE);
                    }
                    else
                        messageFormManager.setVisibility(true,3);
                } else {
                    isRoomClosed=false;
                    messageFormManager.setVisibility(false,0);
                }
            },RCLog::e);

        }

        sub_name = subscription.getName();
        if (!roomType.equals("d")) {
            setToolbarTitle(subscription.getDisplayName());
        } else {
            String name = sub_name;
            int i = name.indexOf("&");
            if (i != -1) {
                name = name.substring(0, i);
            }
            setToolbarTitle(name);
        }
        setToobarIcon();

        boolean unreadMessageExists = subscription.getAlert().equals("true");
        if (unreadMessageExists){
            int count=Integer.parseInt(subscription.getUnread());
            if (count>10){
                ll_unread.setVisibility(View.VISIBLE);
                tv_unread.setText(String.format(getString(R.string.unread_msg_count),count>99?"99+":count+""));
                tv_unread.setOnClickListener(view -> {
                    smoothMoveToPosition(messageRecyclerView,count>messageListAdapter.getItemCount()?messageListAdapter.getItemCount():count);
                    ll_unread.setVisibility(View.GONE);
                    markAsReadIfNeeded();
                });
            }
        }
        if (newMessageIndicatorManager != null && previousUnreadMessageExists && !unreadMessageExists) {
            newMessageIndicatorManager.reset();
        }
        previousUnreadMessageExists = unreadMessageExists;

        if (subscription.isChannel()) {
            showToolbarPublicChannelIcon();
            return;
        }

        if (subscription.isPrivate()) {
            showToolbarPrivateChannelIcon();
        }

        if (subscription.isLivechat()) {
            showToolbarLivechatChannelIcon();
        }
    }

    @Override
    public void showUserStatus(@NonNull User user) {
        showToolbarUserStatuslIcon(user.getStatus());
    }

    @Override
    public void updateHistoryState(boolean hasNext, boolean isLoaded) {
        if (messageRecyclerView == null || !(messageRecyclerView.getAdapter() instanceof MessageListAdapter)) {
            return;
        }

        MessageListAdapter adapter = (MessageListAdapter) messageRecyclerView.getAdapter();
        if (isLoaded) {
            scrollListener.setLoadingDone();
        }
        adapter.updateFooter(hasNext, isLoaded);
    }

    @Override
    public void onMessageSendSuccessfully() {
        scrollToLatestMessage();
        editingMessage = null;
        messageFormManager.onMessageSend();
    }

    @Override
    public void disableMessageInput() {
        messageFormManager.enableComposingText(false);
    }

    @Override
    public void enableMessageInput() {
        messageFormManager.enableComposingText(true);
    }

    @Override
    public void showUnreadCount(int count) {
        RCLog.d("unreadMsg->>"+count);
        newMessageIndicatorManager.updateNewMessageCount(count);
    }

    @Override
    public void showMessages(@NonNull List<? extends Message> messages,boolean isRefresh) {
        if (messageListAdapter == null) {
            return;
        }
        markAsReadIfNeeded();
        messageListAdapter.updateData((List<Message>) messages);
        dismissProgressDialog();
        if (isRefresh||isSlideToBottom(messageRecyclerView))
            scrollToLatestMessage();
//        if (first) {
//            synchronized (RoomFragment.class) {
//                if (first){
//                    scrollToLatestMessage();
//                    first = false;
//
//                }
//            }
//        }
//        doScrollToBottom();

    }

    /**
     * RecyclerView 移动到当前位置，
     */
    public void moveToPosition(String isClickHistoryInfo) {
        int indexOf = isClickHistoryInfo.indexOf(";");
        String timeSamp = isClickHistoryInfo.substring(0, indexOf);
        String msg = isClickHistoryInfo.substring(indexOf+1, isClickHistoryInfo.length());
        List<PairedMessage> itemData = messageListAdapter.getItemData();

        int position = 0;
        for(int i = 0; i < itemData.size(); i++){
            Message message = itemData.get(i).target;
            if(message == null) continue;
            if(timeSamp.equals(message.getTimestamp()+"") && msg.equals(message.getMessage())){
                position = i;
            }
        }
        smoothMoveToPosition(messageRecyclerView, position+1);
    }

    /**
     * 滑动到指定位置
     * @param mRecyclerView
     * @param position
     */
    private void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
         mRecyclerView.smoothScrollToPosition(position);
        // 第一个可见位置
//        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
//        // 最后一个可见位置
//        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
//        if (position < firstItem) {
//            // 如果跳转位置在第一个可见位置之前，就smoothScrollToPosition可以直接跳转
//            mRecyclerView.smoothScrollToPosition(position);
//        } else if (position <= lastItem) {
//            // 跳转位置在第一个可见项之后，最后一个可见项之前
//            // smoothScrollToPosition根本不会动，此时调用smoothScrollBy来滑动到指定位置
//            int movePosition = position - firstItem;
//            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
//                int top = mRecyclerView.getChildAt(movePosition).getTop();
//                mRecyclerView.smoothScrollBy(0, top);
//            }
//        }else {
//            // 如果要跳转的位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
//            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
//            mRecyclerView.smoothScrollToPosition(position);
//            mToPosition = position;
//            mShouldScroll = true;
//        }
//        RCLog.e("smoothMoveToPosition,mShouldScroll="+mShouldScroll, true);
    }

    @Override
    public void showMessageSendFailure(@NonNull Message message) {
        new AlertDialog.Builder(getContext())
                .setPositiveButton(R.string.resend,
                        (dialog, which) -> presenter.resendMessage(message))
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.discard,
                        (dialog, which) -> presenter.deleteMessage(message))
                .show();
    }

    @Override
    public void showMessageDeleteFailure(@NonNull Message message) {
        new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.failed_to_delete))
                .setMessage(getContext().getString(R.string.failed_to_delete_message))
                .setPositiveButton(R.string.ok, (dialog, which) -> presenter.acceptMessageDeleteFailure(message))
                .show();
    }

    @Override
    public void autoloadImages() {
        messageListAdapter.setAutoloadImages(true);
    }

    @Override
    public void manualLoadImages() {
        messageListAdapter.setAutoloadImages(false);
    }

    @Override
    public void onReply(@NonNull AbsoluteUrl absoluteUrl, @NonNull String markdown, @NonNull Message message) {
        messageFormManager.setReply(absoluteUrl, markdown, message);
    }

    @Override
    public void onCopy(@NonNull String message) {
//        Context context = RocketChatApplication.getInstance();
        ClipboardManager clipboardManager =
                (ClipboardManager) RocketChatApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("message", message));
    }
    boolean isFile=false;
    @Override
    public void showMessageActions(@NonNull Message message) {
        Activity context = getActivity();
        if (context != null && context instanceof ChatMainActivity&&(message.getCard()==null&&message.getReport()==null)) {
                if(message.getAttachments()!=null&&(!(message.getMessage().contains(hostname)&&message.getMessage().contains("?msg="))))
                    isFile=true;
                else
                    isFile=false;
                MessagePopup.take(message)
                    .setReplyAction(msg -> presenter.replyMessage(message, false))
//                    .setEditAction(this::onEditMessage)
                    .setCopyAction(msg -> onCopy(message.getMessage()))
                    .setQuoteAction(msg -> presenter.replyMessage(message, true))
                    .setRelayAction(msg -> startActivityForResult(new Intent(getActivity(), RelayActivity.class)
                            .putExtra("msgId",message.getId()).putExtra("isFile",isFile),0x6))
                    .setCundangAction(this::cunDang)
                    .setDeleteAction(this::onDeleteMessage)
                    .showWith(context);
        }

    }

//    String valueUrl;
    private void cunDang(Message message){
        methodCallHelper.getFileClassify("0",rocketChatAbsoluteUrl.from(message.getAttachments().get(0).getTitleLink()),
                message.getAttachments().get(0).getAttachmentTitle().getTitle(),message.getFile().getId(),RocketChatCache.INSTANCE.getSessionId())
                .continueWith(task -> {
                    if (task.getError()==null){
                        ToastUtils.showToast("归档成功");
                    }else {
                        ToastUtils.showToast("归档失败");
                    }
                    return null;
                }, Task.UI_THREAD_EXECUTOR);
//        RealmPublicSettingRepository publicSettingRepository = new RealmPublicSettingRepository(hostname);
//        PublicSetting setting = publicSettingRepository.getPublicSettingById(RealmPublicSetting.CUSTOMADDRESS_FILE_CLASSIFY_UPLOAD_ADDRESS);
//        if(setting != null) {
//            valueUrl = setting.getValue();
//            postDataWithParame(message);
//        }
    }
  /*  private void postDataWithParame(Message message) {
                OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                formBody.add("type", "4");//类型
                formBody.add("sessionId", RocketChatCache.INSTANCE.getSessionId());
                formBody.add("name",message.getAttachments().get(0).getAttachmentTitle().getTitle());
                formBody.add("flag","0");
                formBody.add("downloadUrl",rocketChatAbsoluteUrl.from(message.getAttachments().get(0).getTitleLink()));
                formBody.add("fileSaveId",message.getFile().getId());
                formBody.add("eid", RocketChatCache.INSTANCE.getCompanyId());
                Request request = new Request.Builder()//创建Request 对象。
                        .url(valueUrl)
                        .post(formBody.build())//传递请求体
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handler.sendEmptyMessage(2);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
//                            Log.d("kwwl", "response.code()==" + response.code());
//                            Log.d("kwwl", "response.message()==" + response.message());
//                            Log.d("kwwl", "res==" + response.body().string());
                            //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
                            handler.sendEmptyMessage(1);

                        }
                    }
                });//得到Response 对象

    }*/

    private void onEditMessage(Message message) {
        editingMessage = message;
        messageFormManager.setEditMessage(message.getMessage());
    }

    @SuppressLint("RxLeakedSubscription")
    public void onDeleteMessage(Message message) {
        presenter.deleteMessage(message);
//        RealmPublicSettingRepository   publicSettingRepository = new RealmPublicSettingRepository(hostname);
//        /**
//         * 获取权限和设置
//         */
//        publicSettingRepository
//                .getById(PublicSettingsConstants.Message.ALLOW_DELETING)
//                .subscribe(publicSettingOptional -> {
//                    String mValue = publicSettingOptional.get().getValue();
//                    if(mValue.equals("true"))
//                        publicSettingRepository
//                                .getById(PublicSettingsConstants.Message.ALLOW_DELETING_BLOCK_TIMEOUT)
//                                .subscribe(publicSettingOptional1 -> {
//                                    String mValue1 = publicSettingOptional1.get().getValue();
//                                    int min=(int)((Calendar.getInstance().getTimeInMillis()-message.getTimestamp())/1000/60);
//                                    if(Integer.parseInt(mValue1)>min){
//                                        canDeleteMySelf=true;
//
//                                    }
//                                    else{
//                                        canDeleteMySelf=false;
//                                        ToastUtils.showToast("超出时长，无法撤销");
//                                    }
//                                }, RCLog::e);
//                    else{
//                        canDeleteMySelf=false;
//                        ToastUtils.showToast("没有权限，无法撤销");
//                    }
//                }, RCLog::e);

    }

//    private void showRoomListFragment(int actionId) {
//        //TODO: oddly sometimes getActivity() yields null. Investigate the situations this might happen
//        //and fix it, removing this null-check
//        if (getActivity() != null) {
//            Intent intent = new Intent(getActivity(), RoomActivity.class).putExtra("actionId", actionId)
//                    .putExtra("roomId", roomId)
//                    .putExtra("roomType", roomType)
//                    .putExtra("hostname", hostname)
//                    .putExtra("token", token)
//                    .putExtra("userId", userId);
//            startActivity(intent);
//        }
//    }

    public void loadMissedMessages() {
        presenter.loadMissedMessages(false);
    }

    //    private List<Fragment> fragments=new ArrayList<>();
    private ViewPager myViewPager;

    public void onShowSoftwindow(ViewPager viewPager, ImageKeyboardEditText editText) {
        myViewPager = viewPager;

        GlobalOnItemClickManagerUtils globalOnItemClickManager = GlobalOnItemClickManagerUtils.getInstance(getActivity());
        globalOnItemClickManager.attachToEditText(editText);
        replaceFragment(viewPager);
    }

    private void replaceFragment(ViewPager viewPager) {
        FragmentFactory factory = FragmentFactory.getSingleFactoryInstance();
        EmotiomComplateFragment f1 = (EmotiomComplateFragment) factory.getFragment(EmotionUtils.EMOTION_CLASSIC_TYPE);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(f1);

        NoHorizontalScrollerVPAdapter adapter = new NoHorizontalScrollerVPAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }


//    private void doScrollToBottom() {
//        messageRecyclerView.scrollToPosition(messageListAdapter.getItemCount() - 1);
//    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            AudioHelper.getInstance().relese();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showVideoMsg(VideoRefreshBusEvent event) {
        if (presenter != null)
            presenter.refreshRoom(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(BaseEvent event){
        if (event.getCode()== EventTags.SUBMIT_REF_CALLBACK){
            RCLog.d("baseEvent>>>>>>"+event.getMsg());
            presenter.loadMissedMessages(true);
        }
        if (event.getCode()== EventTags.DELETE_SUCESS){
            RCLog.d("baseEvent>>>>>>"+event.getMsg());
//            RealmMessageRepository repository=new RealmMessageRepository(hostname);
//            Message message=null;
//            if (repository.getAllMessageByMessageId(event.getMsg())!=null&&repository.getAllMessageByMessageId(event.getMsg()).size()>0){
//             message=repository.getAllMessageByMessageId(event.getMsg()).get(0);
            presenter.deleteMessage(event.getMsg());}
//        }
        if (event.getCode()== EventTags.UPLOAD_DIALOG){
                dialogFragment.dismiss();
        }
        if (event.getCode()==EventTags.REFRESH_PIC&&event.getMsg().equals(uplId)){
            presenter.refreshRoom(true);
        }
        if (event.getCode()==EventTags.SET_LONG_CLICK){
            KeyboardHelper.hideSoftKeyboard(getActivity(),messageFormLayout);
            Message pairedMessage=(Message)event.getTarget();
            presenter.onMessageSelected(pairedMessage);
        }
    }

    public void gotoVideoActivity(int mToPosition){
        Room room = realmRoomRepository.getByRoomId(roomId);
        if (room==null){
            ToastUtils.showToast("数据加载异常");
            return;
        }
        List<String> usernames = room.getUsernames();
        if(!"d".equals(roomType)){
            ToastUtils.showToast("多人聊天暂未开放，敬请期待");
            return;
        }
//        String[] str;
//        if (usernames != null && usernames.size() > 0) {
//            str = usernames.toArray(new String[0]);
//        }else{
//            RCLog.e("findUser usernames=null");
//            return;
//        }
//        List<User> users = userRepository.getByNameAll(str);
        RealmSubscriptionRepository subscriptionRepository=new RealmSubscriptionRepository(hostname);
//        RealmMessageRepository messageRepository=new RealmMessageRepository(hostname);
        String userName = subscriptionRepository.getByIdSub(roomId).getName();
        User user=userRepository.getUserByUsername(userName);
//        User user = users.get(1);
        //点对点聊天-
        if(TempFileUtils.getInstance().getTalkingStatus()){
            return;
        }
        if(mToPosition == 0){
            LaunchUtil.showVideoActivity(user.getId(),user.getUsername(),user.getAvatar(),roomId,true,true);
        }else{
            LaunchUtil.showVideoActivity(user.getId(),user.getUsername(),user.getAvatar(),roomId,true,false);
        }
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        AudioHelper.getInstance().relese();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.release();
        AudioHelper.getInstance().relese();
        EventBus.getDefault().unregister(this);
    }


    protected CustomProgressDialog mProgressDialog;
    /**
     * 显示自定义进度框
     */
    protected void showProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog = new CustomProgressDialog(getActivity(), R.style.CustomProgressDialog1);
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

}