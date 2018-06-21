package chat.rocket.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hadisatrio.optional.Optional;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.BuildConfig;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.activity.business.MeetingManagerGroupActivity;
import chat.rocket.android.activity.business.MeettingHistoryActivity;
import chat.rocket.android.activity.business.MyAccountActivity;
import chat.rocket.android.activity.business.OrgSelectUserActivity;
import chat.rocket.android.activity.business.OrganizationControlGroupActivity;
import chat.rocket.android.activity.business.WorkCommunicationGroupActivity;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.dialog.WeiNingAlertDialog;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.fragment.sidebar.dialog.AddChannelDialogFragment;
import chat.rocket.android.fragment.sidebar.dialog.AddDirectMessageDialogFragment;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.layouthelper.chatroom.list.SearchAdapter;
import chat.rocket.android.layouthelper.chatroom.roomlist.ChannelRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.DirectMessageRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.FavoriteRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.LivechatRoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.RoomListHeader;
import chat.rocket.android.layouthelper.chatroom.roomlist.UnreadRoomListHeader;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.recyclertreeview.bean.Conversation;
import chat.rocket.android.recyclertreeview.bean.Group;
import chat.rocket.android.recyclertreeview.bean.GroupChat;
import chat.rocket.android.recyclertreeview.bean.GroupMeeting;
import chat.rocket.android.recyclertreeview.bean.RootGroup;
import chat.rocket.android.recyclertreeview.viewbinder.ConversationNodeBinder;
import chat.rocket.android.recyclertreeview.viewbinder.GroupChatNodeBinder;
import chat.rocket.android.recyclertreeview.viewbinder.GroupMeetingNodeBinder;
import chat.rocket.android.recyclertreeview.viewbinder.GroupNodeBinder;
import chat.rocket.android.recyclertreeview.viewbinder.RootGroupNodeBinder;
import chat.rocket.android.renderer.UserRenderer;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.base.RelationUserDataSubscriber;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.core.PublicSettingsConstants;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Labels;
import chat.rocket.core.models.PublicSetting;
import chat.rocket.core.models.RoomSidebar;
import chat.rocket.core.models.SpotlightUser;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.PermissionRepository;
import chat.rocket.core.repositories.PublicSettingRepository;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.repositories.RealmLabelsRepository;
import chat.rocket.persistence.realm.repositories.RealmPermissionRepository;
import chat.rocket.persistence.realm.repositories.RealmPublicSettingRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightUserRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import hugo.weaving.DebugLog;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewAdapter;

public class SidebarMainFragment extends AbstractFragment implements SidebarMainContract.View {
    private static final int SELECT_USER = 1;
    private SidebarMainContract.Presenter presenter;
    //    private RoomListAdapter adapter;
    //    private SearchView searchView;
    private TextView loadMoreResultsText;
    private SmartTabLayout mSmartTab;
    private ViewPager mVp;
    private List<RoomSidebar> roomSidebarList = Collections.emptyList();
    private Disposable spotlightDisposable;
    private String hostname;
    private static final String HOSTNAME = "hostname";
    private EditText mEtSearch;
    private FrameLayout mFlSearch;
    private AllChatListFragment.PanChange panChange;



    private RealmUserRepository userRepository;
    private RealmSubscriptionRepository subscriptionRepository;
    private User myUser;

    private RecyclerView mRvSearch;
    private SearchAdapter mSerachAdapter;
    //    private List<String> RocketChatApplication.expands;
    private ImageView mIvAddUser;

    private boolean first = true;

    private RelativeLayout rl_empty;
    List<Subscription> suggestionsFor;
    private FragmentPagerAdapter mFragmentAdapter;
    private List<BaseFragment> mFragments;
    private AllChatListFragment allChatListFragment;
    private RecentlyChatListFragment recentlyChatListFragment;

    public SidebarMainFragment() {
    }

    /**
     * build SidebarMainFragment with hostname.
     */
    public static SidebarMainFragment create(String hostname) {
        Bundle args = new Bundle();
        args.putString(HOSTNAME, hostname);
        SidebarMainFragment fragment = new SidebarMainFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public void setPanChange(AllChatListFragment.PanChange panChange) {
        this.panChange=panChange;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        hostname = getArguments().getString(HOSTNAME);

        userRepository = new RealmUserRepository(hostname);
        subscriptionRepository = new RealmSubscriptionRepository(hostname);

        AbsoluteUrlHelper absoluteUrlHelper = new AbsoluteUrlHelper(
                hostname,
                new RealmServerInfoRepository(),
                userRepository,
                new SessionInteractor(new RealmSessionRepository(hostname))
        );


        presenter = new SidebarMainPresenter(
                hostname,
                new RoomInteractor(new RealmRoomRepository(hostname)),
                userRepository,
                absoluteUrlHelper,
                new MethodCallHelper(getContext(), hostname),
                new RealmSpotlightRepository(hostname),
                new RealmSpotlightUserRepository(hostname)
        );

    }

    @SuppressLint("RxLeakedSubscription")
    public void setHint(boolean isVisibleToUser) {
        recentlyChatListFragment.setHint(getActivity());
        presenter.setHint(myUser);
        allChatListFragment.setHint( getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        presenter.bindView(this);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        myUser = null;
        presenter.release();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((SidebarMainPresenter) presenter).resume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleMessage(BaseEvent event) {
        if (event.getCode() == EventTags.SHOW_DIALOG) {
            setUserOffline(myUser);
        }

    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_sidebar_main;
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    protected void onSetupView() {
        rl_empty = rootView.findViewById(R.id.rl_empty);
        setupUserActionToggle();
        setupUserStatusButtons();
        setupLogoutButton();
        setupVersionInfo();
//        RocketChatApplication.expands = new ArrayList<>();
//        searchView = rootView.findViewById(R.id.search);
        mEtSearch = rootView.findViewById(R.id.et_search);
        mSmartTab = rootView.findViewById(R.id.viewpagertab);
        mVp = rootView.findViewById(R.id.viewpager);
        ImageView mIvClear = rootView.findViewById(R.id.iv_clear);
        mIvClear.setOnClickListener(view -> {
            mEtSearch.setText("");
            rl_empty.setVisibility(View.GONE);
            mVp.setVisibility(View.VISIBLE);
            mSmartTab.setVisibility(View.VISIBLE);
        });
        SpannableString ss = new SpannableString("搜索群组名或用户名");//定义hint的值
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(11, true);//设置字体大小 true表示单位是sp
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mEtSearch.setHint(new SpannedString(ss));
        mFlSearch = rootView.findViewById(R.id.search_box);
        mRvSearch = rootView.findViewById(R.id.rv_search);
        mIvAddUser = rootView.findViewById(R.id.iv_add_user);
        setOnClickIvAddUser();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRvSearch.setLayoutManager(linearLayoutManager);
        mSerachAdapter = new SearchAdapter(getContext(), new SearchAdapter.OnClickItemListener() {
            @Override
            public void joinRoom(Subscription subscription) {
                mEtSearch.setText("");
                RocketChatCache.INSTANCE.setSelectedRoomId(subscription.getRid());
            }

            boolean first;

            @Override
            public void createDirectMessage(SpotlightUser spotlightUser) {
                mEtSearch.setText("");
                Subscription subscription = subscriptionRepository.getByName(spotlightUser.getUsername(), myUser.getId());
                first = true;
                if (subscription == null) {
                    presenter.createDirectMessageForMobile(spotlightUser.getUsername());
                    subscriptionRepository.getByUserName(spotlightUser.getUsername(), myUser.getId()).subscribe(subscriptions -> {
                        if (subscriptions.size() > 0) {
                            if (first) {
                                synchronized (SidebarMainFragment.class) {
                                    if (first) {
                                        first = false;
                                        if (!RocketChatCache.INSTANCE.getSelectedRoomId().equals(subscriptions.get(0).getRid())) {
                                            RelationUserDataSubscriber relationUserDataSubscriber = new RelationUserDataSubscriber(getContext(), hostname, RealmStore.getOrCreate(hostname));
                                            relationUserDataSubscriber.register();
                                            RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
                                            MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
                                            methodCall.getRoomSubscriptions().onSuccess(task -> {
                                                Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                                                        getActivity(), hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
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
            }
        });
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.treeitem_divideritemdecoration));
        mRvSearch.addItemDecoration(divider);
        mRvSearch.setAdapter(mSerachAdapter);


        loadMoreResultsText = rootView.findViewById(R.id.text_load_more_results);


        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String term = s.toString().trim();
                if (myUser == null)
                    return;
                if (term.length() > 0) {
                    mRvSearch.setVisibility(View.VISIBLE);
                    mIvClear.setVisibility(View.VISIBLE);
                    presenter.searchSpotlight(term, myUser).toObservable()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(spotlightUsers ->
                                    showSearchSuggestions(spotlightUsers), RCLog::e);
                    suggestionsFor = subscriptionRepository.getSuggestionsFor(term, myUser.getId());
                    mSerachAdapter.setSubscriptionDatas(suggestionsFor);

                } else {
                    mVp.setVisibility(View.VISIBLE);
                    mSmartTab.setVisibility(View.VISIBLE);
                    mIvClear.setVisibility(View.GONE);
                    mRvSearch.setVisibility(View.GONE);
                    rl_empty.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mFragments=new ArrayList<>();
        recentlyChatListFragment = new RecentlyChatListFragment(panChange,hostname);
        recentlyChatListFragment.setTabName("消息");
        mFragments.add(recentlyChatListFragment);
        allChatListFragment = new AllChatListFragment(panChange,hostname);
        allChatListFragment.setTabName("群组");
        mFragments.add(allChatListFragment);
        mFragmentAdapter=new CustomViewpagerAdapter(getChildFragmentManager(),mFragments);
        mVp.setAdapter(mFragmentAdapter);
        mSmartTab.setViewPager(mVp);
    }

    private void setOnClickIvAddUser() {
        mIvAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrgSelectUserActivity.class);
            intent.putExtra("type", RocketChatConstants.D);
            intent.putExtra("isCreateSingleChat", true);
            intent.putExtra("isSingleChoose", true);
            if (myUser!=null)
            intent.putExtra("companyId", myUser.getCompanyId());
            startActivityForResult(intent, SELECT_USER);
        });
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_USER && resultCode == Activity.RESULT_OK && data != null) {
            String username = data.getStringExtra("username");
            RocketChatCache.INSTANCE.setIsClickInfoMsg(username + ";" + myUser.getId());

        }

    }

    private void enterRoom(Subscription subscription) {
        RocketChatCache.INSTANCE.setSelectedRoomId(subscription.getRid());
    }

    @Override
    public void showRoomSidebarList(@NonNull List<RoomSidebar> roomSidebarList) {
        this.roomSidebarList = roomSidebarList;
//        adapter.setRoomSidebarList(roomSidebarList);
    }

    @Override
    public void filterRoomSidebarList(CharSequence term) {
        List<RoomSidebar> filteredRoomSidebarList = new ArrayList<>();

        for (RoomSidebar roomSidebar : roomSidebarList) {
            if (roomSidebar.getRoomName().contains(term)) {
                filteredRoomSidebarList.add(roomSidebar);
            }
        }

        if (filteredRoomSidebarList.isEmpty()) {
//                    loadMoreResults();
        } else {
            loadMoreResultsText.setVisibility(View.VISIBLE);
//            adapter.setMode(RoomListAdapter.MODE_ROOM);
//            adapter.setRoomSidebarList(filteredRoomSidebarList);
        }
    }



    private void showSearchSuggestions(List<SpotlightUser> spotlightUsers) {
        if (mEtSearch.getText().toString().length() == 0)
            return;
        mSerachAdapter.setSpotlightUserDatas(spotlightUsers);
        if ((suggestionsFor != null && suggestionsFor.size() != 0) || (spotlightUsers != null && spotlightUsers.size() != 0)) {
            mRvSearch.setVisibility(View.VISIBLE);
            rl_empty.setVisibility(View.GONE);
            mVp.setVisibility(View.GONE);
            mSmartTab.setVisibility(View.GONE);
        } else {
            mRvSearch.setVisibility(View.GONE);
            rl_empty.setVisibility(View.VISIBLE);
            mVp.setVisibility(View.GONE);
            mSmartTab.setVisibility(View.GONE);
        }
    }

    @SuppressLint("RxLeakedSubscription")
    private void setupUserActionToggle() {
        final CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
        toggleUserAction.setFocusableInTouchMode(false);

        rootView.findViewById(R.id.user_info_container).setOnClickListener(view -> toggleUserAction.toggle());

        RxCompoundButton.checkedChanges(toggleUserAction)
                .compose(bindToLifecycle())
                .subscribe(
                        show -> showUserActionContainer(show),
                        Logger.INSTANCE::report
                );
    }

    public void showUserActionContainer(boolean show) {
        rootView.findViewById(R.id.search_box)
                .setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        rootView.findViewById(R.id.user_action_outer_container)
                .setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void toggleUserActionContainer(boolean checked) {
        CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
        toggleUserAction.setChecked(checked);
    }

    @Override
    public void showScreen() {
        rootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEmptyScreen() {
        rootView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void show(User user) {
        RocketChatCache.INSTANCE.setUserId(user.getId());
        RocketChatCache.INSTANCE.setUserUsername(user.getUsername());
        RocketChatCache.INSTANCE.setUserName(user.getName());
        myUser = user;
        onRenderCurrentUser(user);
    }







    private void setupUserStatusButtons() {
        rootView.findViewById(R.id.btn_status_online).setOnClickListener(view -> {
            presenter.onUserOnline();
            closeUserActionContainer();
        });
        rootView.findViewById(R.id.btn_status_away).setOnClickListener(view -> {
            presenter.onUserAway();
            closeUserActionContainer();
        });
        rootView.findViewById(R.id.btn_status_busy).setOnClickListener(view -> {
            presenter.onUserBusy();
            closeUserActionContainer();
        });
        rootView.findViewById(R.id.btn_status_invisible).setOnClickListener(view -> {
            presenter.onUserOffline();
            closeUserActionContainer();
        });
    }

    public void onRenderCurrentUser(User user) {
        TextView userName = rootView.findViewById(R.id.current_user_name);
        UserRenderer userRenderer = new UserRenderer(user);
        if (user != null && TextUtils.isEmpty(userName.getText())) {
            userRenderer.showAvatar(rootView.findViewById(R.id.current_user_avatar), hostname);
        }
        userRenderer.showUsername(rootView.findViewById(R.id.current_user_name));
        userRenderer.showStatusColor(rootView.findViewById(R.id.current_user_status));
        userRenderer.showStatus(rootView.findViewById(R.id.tv_user_status));

    }

    public void setUserOffline(User user) {
        RealmHelper realmHelper = RealmStore.get(hostname);
        realmHelper.executeTransaction(realm -> {
            realm.where(RealmUser.class)
                    .equalTo(RealmUser.ID, user.getId())
                    .findFirst()
                    .setStatus("offline");
            return null;
        });
    }

    private void updateRoomListMode() {
        final List<RoomListHeader> roomListHeaders = new ArrayList<>();

        roomListHeaders.add(new UnreadRoomListHeader(
                getString(R.string.fragment_sidebar_main_unread_rooms_title)
        ));

        roomListHeaders.add(new FavoriteRoomListHeader(
                getString(R.string.fragment_sidebar_main_favorite_title)
        ));

        roomListHeaders.add(new LivechatRoomListHeader(
                getString(R.string.fragment_sidebar_main_livechat_title)
        ));

        roomListHeaders.add(new ChannelRoomListHeader(
                getString(R.string.fragment_sidebar_main_channels_title),
                () -> showAddRoomDialog(AddChannelDialogFragment.create(hostname))
        ));
        roomListHeaders.add(new DirectMessageRoomListHeader(
                getString(R.string.fragment_sidebar_main_direct_messages_title),
                () -> showAddRoomDialog(AddDirectMessageDialogFragment.create(hostname))
        ));

//        adapter.setRoomListHeaders(roomListHeaders);
    }

    @DebugLog
    @Override
    public void onPreparedToLogOut() {
        final Activity activity = getActivity();
        if (activity != null && activity instanceof ChatMainActivity) {
            ((ChatMainActivity) activity).onLogout();
        }
    }

    private void setupLogoutButton() {
        rootView.findViewById(R.id.btn_logout).setOnClickListener(view -> {
//            closeUserActionContainer();
            // Clear relative data and set new hostname if any.
//            presenter.prepareToLogOut();
            // 这里是进入我的账户
            startActivity(MyAccountActivity.class);
        });
    }

    public void clearSearchViewFocus() {
//        searchView.clearFocus();
    }

    public void closeUserActionContainer() {
        final CompoundButton toggleUserAction = rootView.findViewById(R.id.toggle_user_action);
        if (toggleUserAction != null && toggleUserAction.isChecked()) {
            toggleUserAction.setChecked(false);
        }
    }

    private void setupVersionInfo() {
        TextView versionInfoView = rootView.findViewById(R.id.version_info);
        versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
    }

    private void showAddRoomDialog(DialogFragment dialog) {
        dialog.show(getFragmentManager(), "AbstractAddRoomDialogFragment");
    }

    private void startActivity(Class activity) {
        Intent intent = new Intent(getActivity(), activity);
        if (myUser != null) {
            intent.putExtra("companyId", myUser.getCompanyId());
            intent.putExtra("userId", myUser.getId());
        }
        startActivity(intent);
    }



}