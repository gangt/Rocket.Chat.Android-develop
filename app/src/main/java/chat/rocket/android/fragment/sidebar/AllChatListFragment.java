package chat.rocket.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.activity.business.MeetingManagerGroupActivity;
import chat.rocket.android.activity.business.MeettingHistoryActivity;
import chat.rocket.android.activity.business.OrganizationControlGroupActivity;
import chat.rocket.android.activity.business.WorkCommunicationGroupActivity;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.dialog.WeiNingAlertDialog;
import chat.rocket.android.helper.LogIfError;
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
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserRoomChanged;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.core.PublicSettingsConstants;
import chat.rocket.core.models.Labels;
import chat.rocket.core.models.PublicSetting;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.PermissionRepository;
import chat.rocket.core.repositories.PublicSettingRepository;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmLabelsRepository;
import chat.rocket.persistence.realm.repositories.RealmPermissionRepository;
import chat.rocket.persistence.realm.repositories.RealmPublicSettingRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.disposables.Disposable;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewAdapter;

/**
 * Created by lyq on 2018/5/12.
 */

@SuppressLint("ValidFragment")
public class AllChatListFragment extends BaseFragment implements ConversationNodeBinder.OnItemClickListener, RootGroupNodeBinder.OnItemClickListener, GroupChatNodeBinder.OnClickListener {
    @BindView(R.id.rv)
    RecyclerView recyclerView;
    private TreeViewAdapter adapter;
    private List<TreeNode> displayNodes;
    private User myUser;
    private RealmLabelsRepository labelsRepository;
    private String hostname;
    private String paersonlValue;
    private String meetingValue;
    private PublicSetting layoutPersonTitle;
    private PublicSetting layoutMeetingTitle;
    private PublicSetting layoutChannelTitle;
    private PublicSetting otherMeetingTitle;
    private PublicSetting ownMeetingTitle;
    private PublicSettingRepository publicSettingRepository;
    private PermissionRepository permissionRepository;
    private PublicSetting superviseCompanyTile;
    private PublicSetting workSuperviseCompanyTitle;
    private RealmUserRepository userRepository;
    private RealmSubscriptionRepository subscriptionRepository;
    List<String> names = new ArrayList<>();
    private WeiNingAlertDialog dialog;
    private MethodCallHelper methodCallHelper;
    private PanChange panChange;

    @SuppressLint("ValidFragment")
    public AllChatListFragment(PanChange panChange, String hostname) {
        this.hostname = hostname;
        this.panChange = panChange;
    }

    public  AllChatListFragment(){}
    @Override
    protected void initView() {
        super.initView();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    public void setPanChange(PanChange panChange) {
        this.panChange = panChange;
    }

    public interface PanChange {
        void closePane();
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    protected void initData() {
        super.initData();
        publicSettingRepository = new RealmPublicSettingRepository(hostname);
        permissionRepository = new RealmPermissionRepository(hostname);
        methodCallHelper = new MethodCallHelper(getContext(), hostname);
        userRepository = new RealmUserRepository(hostname);
        labelsRepository = new RealmLabelsRepository(hostname);
        subscriptionRepository = new RealmSubscriptionRepository(hostname);
        userRepository.getCurrent().distinctUntilChanged()
                .subscribe(userOptional -> {
                    User user = userOptional.get();
                    if (user != null) {
                        show(user);
                        this.myUser = user;
                    }
                }, RCLog::e);
        labelsRepository.getAll().subscribe(labels -> {
            if (myUser != null && myUser.getCompanyId() != null) {
                showRecyclerTree(myUser);
            }
        }, RCLog::e);
        subscriptionRepository.getAll().subscribe(subscriptions -> {
            if (myUser != null && myUser.getCompanyId() != null) {
                showRecyclerTree(myUser);
                if (names != null) {
                    userListener(myUser);
                }
            }

        }, RCLog::e);
    }

    @SuppressLint("RxLeakedSubscription")
    public void userListener(User user) {
        if (user.getCompanyId() != null) {
            List<Subscription> d = subscriptionRepository.getByTypeAll("d", user.getId());
            if (d != null && d.size() > 0) {
                for (Subscription subscription : d) {
                    names.add(subscription.getName());
                }
                String[] nameArray = names.toArray(new String[names.size()]);
                names = null;
                userRepository.getByNameToFlowable(nameArray).subscribe(users -> {
                    showRecyclerTree(user);
                }, RCLog::e);
            }

        }
    }

    public void setHint(FragmentActivity activity) {
        if (displayNodes == null || displayNodes.size() <= 3) {
            RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
            MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
            methodCall.getRooms().onSuccess(task -> {
                Registrable listener = new StreamNotifyUserRoomChanged(
                        activity, hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
                listener.register();
                return null;
            }).continueWith(new LogIfError());
            methodCall.getRoomSubscriptions().onSuccess(task -> {
                Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                        activity, hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
                listener.register();
                return null;
            }).continueWith(new LogIfError());
            if (userRepository != null) {
                Disposable subscribe = userRepository.getCurrent().distinctUntilChanged()
                        .subscribe(userOptional -> {
                            User user = userOptional.get();
                            if (user != null) {
                                this.myUser = user;
                                show(user);
                            }
                        }, RCLog::e);
            }
        }
    }

    private void show(User user) {
        if (user.getCompanyId() != null) {
            showRecyclerTree(user);
            if (names != null)
                userListener(user);
        }
    }

    /**
     * 判断mainactivity是否处于栈顶
     *
     * @return true在栈顶false不在栈顶
     */
    private boolean isMainActivityTop() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(ChatMainActivity.class.getName());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_all_chat_list;
    }

    @SuppressLint("RxLeakedSubscription")
    private void showRecyclerTree(User user) {
        displayNodes = new ArrayList<>();
        /**
         * 获取权限和设置
         */
        publicSettingRepository
                .getById(PublicSettingsConstants.Nest.PAERSONL_NEST_ENABLE)
                .subscribe(publicSettingOptional -> {
                    paersonlValue = publicSettingOptional.get().getValue();
                }, RCLog::e);
        publicSettingRepository
                .getById(PublicSettingsConstants.Nest.MEETING_NEST_ENABLE)
                .subscribe(publicSettingOptional -> {
                    meetingValue = publicSettingOptional.get().getValue();
                }, RCLog::e);
        publicSettingRepository.getById(PublicSettingsConstants.Nest.LAYOUT_PERSON_TITLE)
                .subscribe(publicSettingOptional -> {
                    layoutPersonTitle = publicSettingOptional.get();
                }, RCLog::e);
        publicSettingRepository.getById(PublicSettingsConstants.Nest.LAYOUT_MEETING_TITLE)
                .subscribe(publicSettingOptional -> {
                    layoutMeetingTitle = publicSettingOptional.get();
                }, RCLog::e);
        publicSettingRepository.getById(PublicSettingsConstants.Nest.LAYOUT_CHANNEL_TITLE)
                .subscribe(publicSettingOptional -> {
                    layoutChannelTitle = publicSettingOptional.get();
                }, RCLog::e);
        publicSettingRepository.getById(PublicSettingsConstants.Nest.OWN_MEETING_TITLE)
                .subscribe(publicSettingOptional -> {
                    ownMeetingTitle = publicSettingOptional.get();
                }, RCLog::e);
        publicSettingRepository.getById(PublicSettingsConstants.Nest.OTHER_MEETING_TITLE)
                .subscribe(publicSettingOptional -> {
                    otherMeetingTitle = publicSettingOptional.get();
                }, RCLog::e);
        publicSettingRepository.getById(PublicSettingsConstants.Nest.LAYOUT_WORKING_SUPERVISE_COMPANY_TITLE)
                .subscribe(publicSettingOptional -> {
                    workSuperviseCompanyTitle = publicSettingOptional.get();
                }, RCLog::e);
        publicSettingRepository.getById(PublicSettingsConstants.Nest.LAYOUT_SUPERVISE_COMPANY_TITLE)
                .subscribe(publicSettingOptional -> {
                    superviseCompanyTile = publicSettingOptional.get();
                }, RCLog::e);
        String selectedRoomId = RocketChatCache.INSTANCE.getSelectedRoomId();
        Subscription selectedSub = subscriptionRepository.getByIdSub(selectedRoomId);
        if (selectedSub == null) {
            RocketChatCache.INSTANCE.setSelectedRoomId("");
            if (!isMainActivityTop())
                RocketChatCache.INSTANCE.setIsClickInfoMsg("showHome");
        }
        /**
         * 工作巢
         */
        if (paersonlValue.equals("true")) {
            /**
             * 增加工作巢
             */
//            if(first){
//                expands.add(layoutPersonTitle.getValue());
//                expands.add(layoutMeetingTitle.getValue());
//                expands.add(layoutChannelTitle.getValue());
//                first=false;
//            }
            int wCount = subscriptionRepository.getByTypeCount("w", user.getId());
            int dCount = subscriptionRepository.getByTypeCount("d", user.getId());
            List<Subscription> w1 = subscriptionRepository.getByTypeAll("w", user.getId());
            List<Subscription> d1 = subscriptionRepository.getByTypeAll("d", user.getId());
            int remindCount = 0;
            for (Subscription subscription : w1) {
                remindCount += Integer.parseInt(subscription.getUnread());
            }
            for (Subscription subscription : d1) {
                remindCount += Integer.parseInt(subscription.getUnread());
            }
            TreeNode<RootGroup> work = new TreeNode<>(new RootGroup(layoutPersonTitle, wCount + dCount, remindCount > 0));
            if (RocketChatApplication.expands.contains(layoutPersonTitle.getValue())) {
                work.expand();
            }
            displayNodes.add(work);
            /**
             *  增加没有挂靠体系的频道
             *
             */
            List<Subscription> noLeafs = subscriptionRepository.getByTypeNoLeafAll("w", user.getId(), user.getCompanyId());
            if (noLeafs != null) {
                for (int i = 0; i < noLeafs.size(); i++) {
                    Subscription subscription = noLeafs.get(i);
                    List<Subscription> subscriptions = subscriptionRepository.getByGIdAll(subscription.getCId(), subscription.getName(), user.getId());
                    TreeNode<GroupChat> groupChat = new TreeNode<>(new GroupChat(subscription));
                    if (RocketChatApplication.expands.contains(subscription.getId())) {
                        groupChat.expand();
                    }
                    work.addChild(groupChat);
                    if (subscriptions != null && subscriptions.size() != 0) {
                        recursionLeaf(subscriptions, groupChat, user.getId(), 0);
                    }
                }
            }


            /**
             * 增加工作巢下有频道的体系
             */
            List<Labels> w = labelsRepository.getByType("w", user.getCompanyId());
            if (w != null) {
                for (int i = 0; i < w.size(); i++) {
                    Labels labels = w.get(i);
                    List<Subscription> subscriptions = subscriptionRepository.getByLabelAll(labels.getId(), labels.getName(), user.getId());
                    if (subscriptions != null && subscriptions.size() != 0) {
                        TreeNode<Group> label = new TreeNode<>(new Group(labels));
                        int count = recursionLeaf(subscriptions, label, user.getId(), 0);
                        if (RocketChatApplication.expands.contains(labels.getId())) {
                            label.expand();
                        }
                        Group content = label.getContent();
                        content.setUnRead(count);
                        work.addChild(label);
                    }
                }
            }
            /**
             * 增加点对点会话体系
             */
            List<Labels> sd = labelsRepository.getByType("sd", user.getCompanyId());
            List<Subscription> d = subscriptionRepository.getByTypeAll("d", user.getId());
            if (d != null && d.size() != 0) {
                if (sd != null && sd.size() != 0) {
                    TreeNode<Group> groupTreeNode = new TreeNode<>(new Group(sd.get(0)));
                    if (RocketChatApplication.expands.contains(sd.get(0).getId())) {
                        groupTreeNode.expand();
                    }
                    work.addChild(groupTreeNode);
                    int count = 0;
                    for (int i = 0; i < d.size(); i++) {
                        Subscription subscription = d.get(i);
                        count += Integer.parseInt(subscription.getUnread());
                        TreeNode<Conversation> conversationTreeNode = new TreeNode<>(new Conversation(subscription));
                        groupTreeNode.addChild(conversationTreeNode);
                    }

                    Group content = groupTreeNode.getContent();
                    content.setUnRead(count);
                }
            }

            /**
             *工作巢中其他公司频道（各监管单位）
             */
            int notCompanySize = subscriptionRepository.getByTypeNotCompanyIdAll("w", user.getCompanyId(), user.getId()).size();
            if (notCompanySize > 0) {
                TreeNode<GroupMeeting> notCompany = new TreeNode<>(new GroupMeeting(workSuperviseCompanyTitle.getValue()));
                if (RocketChatApplication.expands.contains(workSuperviseCompanyTitle.getValue())) {
                    notCompany.expand();
                }
                work.addChild(notCompany);
                /**
                 *  增加没有挂靠体系的频道
                 *
                 */
                List<Subscription> noLeafAndNoCompanyIdAll = subscriptionRepository.getByTypeNoLeafAndNoCompanyIdAll("w", user.getId(), user.getCompanyId());
                if (noLeafAndNoCompanyIdAll != null) {
                    for (int i = 0; i < noLeafAndNoCompanyIdAll.size(); i++) {
                        Subscription subscription = noLeafAndNoCompanyIdAll.get(i);
                        List<Subscription> subscriptions = subscriptionRepository.getByGIdAll(subscription.getCId(), subscription.getName(), user.getId());
                        TreeNode<GroupChat> groupChat = new TreeNode<>(new GroupChat(subscription));
                        if (RocketChatApplication.expands.contains(subscription.getId())) {
                            groupChat.expand();
                        }
                        notCompany.addChild(groupChat);
                        if (subscriptions != null && subscriptions.size() != 0) {
                            recursionLeaf(subscriptions, groupChat, user.getId(), 0);
                        }
                    }
                }

                /**
                 * 增加工作巢下有频道的体系
                 */
                List<Labels> wNoCompany = labelsRepository.getByTypeNoCompanyId("w", user.getCompanyId());
                if (wNoCompany != null) {
                    for (int i = 0; i < wNoCompany.size(); i++) {
                        Labels labels = wNoCompany.get(i);
                        List<Subscription> subscriptions = subscriptionRepository.getByLabelAll(labels.getId(), labels.getName(), user.getId());
                        if (subscriptions != null && subscriptions.size() != 0) {
                            TreeNode<Group> label = new TreeNode<>(new Group(labels));
                            int count = recursionLeaf(subscriptions, label, user.getId(), 0);
                            if (RocketChatApplication.expands.contains(labels.getId())) {
                                label.expand();
                            }
                            Group content = label.getContent();
                            content.setUnRead(count);
                            notCompany.addChild(label);
                        }
                    }
                }

            }
        } else {
            /**
             * 没有工作巢时增加点对点会话体系
             */
            List<Labels> sd = labelsRepository.getByType("sd", user.getCompanyId());
            List<Subscription> d = subscriptionRepository.getByTypeAll("d", user.getId());
            if (d != null && d.size() != 0) {
                if (sd != null && sd.size() != 0) {
                    TreeNode<Group> groupTreeNode = new TreeNode<>(new Group(sd.get(0)));
                    displayNodes.add(groupTreeNode);
                    for (int i = 0; i < d.size(); i++) {
                        Subscription subscription = d.get(i);
                        User userByUsername = userRepository.getUserByUsername(subscription.getName());
                        if (userByUsername != null) {
                            Subscription build = Subscription
                                    .builder()
                                    .setStatus(userByUsername.getStatus())
                                    .setName(subscription.getName())
                                    .setUnread(subscription.getUnread())
                                    .setUpdatedAt(subscription.getUpdatedAt())
                                    .setTs(subscription.getTs())
                                    .setLs(subscription.getLs())
                                    .setSortTime(subscription.getSortTime())
                                    .setRid(subscription.getRid())
                                    .build();
                            TreeNode<Conversation> conversationTreeNode = new TreeNode<>(new Conversation(build));
                            groupTreeNode.addChild(conversationTreeNode);
                        }
                    }
                }
            }
        }

        /**
         * 会议巢
         */
        if (meetingValue.equals("true") && user.getCompanyId() != null) {
            /**
             * 增加会议巢
             */
            int mCount = subscriptionRepository.getByTypeCount("m", user.getId());
            List<Subscription> m1 = subscriptionRepository.getByTypeAll("m", user.getId());
            int remindCount = 0;
            for (Subscription subscription : m1) {
                remindCount += Integer.parseInt(subscription.getUnread());
            }
            TreeNode<RootGroup> conference = new TreeNode<>(new RootGroup(layoutMeetingTitle, mCount, remindCount > 0));
            if (RocketChatApplication.expands.contains(layoutMeetingTitle.getValue())) {
                conference.expand();
            }
            displayNodes.add(conference);
            /**
             * 判断是否有本公司会议显示体系
             *
             */
            List<Subscription> subscriptions = subscriptionRepository.getByTypeAll("m", user.getCompanyId(), user.getId());
            List<Subscription> m = subscriptionRepository.getByTypeNotCompanyIdAll("m", user.getCompanyId(), user.getId());
            if (subscriptions != null && subscriptions.size() != 0) {
                TreeNode<GroupMeeting> companyMeeting = new TreeNode<>(new GroupMeeting(ownMeetingTitle.getValue()));
                if (RocketChatApplication.expands.contains(ownMeetingTitle.getValue())) {
                    companyMeeting.expand();
                }
                conference.addChild(companyMeeting);
                int count = 0;
                for (int i = 0; i < subscriptions.size(); i++) {
                    Subscription subscription = subscriptions.get(i);
                    count += Integer.parseInt(subscription.getUnread());
                    TreeNode<GroupChat> groupChatTreeNode = new TreeNode<>(new GroupChat(subscription));
                    companyMeeting.addChild(groupChatTreeNode);
                }
                GroupMeeting content = companyMeeting.getContent();
                content.setUnRead(count);
            }
            /**
             * 其他公司会议体系
             */
            if (m != null && m.size() != 0) {
                TreeNode<GroupMeeting> notCompany = new TreeNode<>(new GroupMeeting(otherMeetingTitle.getValue()));
                if (RocketChatApplication.expands.contains(otherMeetingTitle.getValue())) {
                    notCompany.expand();
                }
                conference.addChild(notCompany);
                int count = 0;
                for (int i = 0; i < m.size(); i++) {
                    Subscription subscription = m.get(i);
                    count += Integer.parseInt(subscription.getUnread());
                    TreeNode<GroupChat> groupChatTreeNode = new TreeNode<>(new GroupChat(subscription));
                    notCompany.addChild(groupChatTreeNode);
                }
                GroupMeeting content = notCompany.getContent();
                content.setUnRead(count);
            }

        }
        /**
         * 组织巢
         * 增加组织巢
         */
        int pCount = subscriptionRepository.getByTypeCount("p", user.getId());
        List<Subscription> p1 = subscriptionRepository.getByTypeAll("p", user.getId());
        int remindCount = 0;
        for (Subscription subscription : p1) {
            remindCount += Integer.parseInt(subscription.getUnread());
        }
        TreeNode<RootGroup> organization = new TreeNode<>(new RootGroup(layoutChannelTitle, pCount, remindCount > 0));
        if (RocketChatApplication.expands.contains(layoutChannelTitle.getValue())) {
            organization.expand();
        }
        displayNodes.add(organization);
        /**
         * 增加没有挂靠体系的频道
         */
        List<Subscription> noLeafs = subscriptionRepository.getByTypeNoLeafAll("p", user.getId(), user.getCompanyId());
        if (noLeafs != null) {
            for (int i = 0; i < noLeafs.size(); i++) {
                Subscription subscription = noLeafs.get(i);
                List<Subscription> subscriptions = subscriptionRepository.getByGIdAll(subscription.getCId(), subscription.getName(), user.getId());
                TreeNode<GroupChat> groupChat = new TreeNode<>(new GroupChat(subscription));
                if (RocketChatApplication.expands.contains(subscription.getId())) {
                    groupChat.expand();
                }
                organization.addChild(groupChat);
                if (subscriptions != null && subscriptions.size() != 0) {
                    recursionLeaf(subscriptions, groupChat, user.getId(), 0);
                }
            }
        }
        /**
         * 增加组织巢体系
         */
        List<Labels> p = labelsRepository.getByType("p", user.getCompanyId());
        if (p != null) {
            for (int i = 0; i < p.size(); i++) {
                Labels labels = p.get(i);
                List<Subscription> subscriptions = subscriptionRepository.getByLabelAll(labels.getId(), labels.getName(), user.getId());
                if (subscriptions != null && subscriptions.size() != 0) {
                    TreeNode<Group> label = new TreeNode<>(new Group(labels));
                    int count = recursionLeaf(subscriptions, label, user.getId(), 0);
                    if (RocketChatApplication.expands.contains(labels.getId())) {
                        label.expand();
                    }
                    Group content = label.getContent();
                    content.setUnRead(count);
                    organization.addChild(label);
                }
            }
        }
        /**
         *组织巢中其他公司频道
         */
        int pNotCompanySize = subscriptionRepository.getByTypeNotCompanyIdAll("p", user.getCompanyId(), user.getId()).size();
        if (pNotCompanySize > 0) {
            TreeNode<GroupMeeting> notCompany = new TreeNode<>(new GroupMeeting(superviseCompanyTile.getValue()));
            if (RocketChatApplication.expands.contains(superviseCompanyTile.getValue())) {
                notCompany.expand();
            }
            organization.addChild(notCompany);
            /**
             *  增加没有挂靠体系的频道
             *
             */
            List<Subscription> noLeafAndNoCompanyIdAll = subscriptionRepository.getByTypeNoLeafAndNoCompanyIdAll("p", user.getId(), user.getCompanyId());
            if (noLeafAndNoCompanyIdAll != null) {
                for (int i = 0; i < noLeafAndNoCompanyIdAll.size(); i++) {
                    Subscription subscription = noLeafAndNoCompanyIdAll.get(i);
                    List<Subscription> subscriptions = subscriptionRepository.getByGIdAll(subscription.getCId(), subscription.getName(), user.getId());
                    TreeNode<GroupChat> groupChat = new TreeNode<>(new GroupChat(subscription));
                    if (RocketChatApplication.expands.contains(subscription.getId())) {
                        groupChat.expand();
                    }
                    notCompany.addChild(groupChat);
                    if (subscriptions != null && subscriptions.size() != 0) {
                        recursionLeaf(subscriptions, groupChat, user.getId(), 0);
                    }
                }
            }

            /**
             * 增加有频道的体系
             */
            List<Labels> wNoCompany = labelsRepository.getByTypeNoCompanyId("p", user.getCompanyId());
            if (wNoCompany != null) {
                for (int i = 0; i < wNoCompany.size(); i++) {
                    Labels labels = wNoCompany.get(i);
                    List<Subscription> subscriptions = subscriptionRepository.getByLabelAll(labels.getId(), labels.getName(), user.getId());
                    if (subscriptions != null && subscriptions.size() != 0) {
                        TreeNode<Group> label = new TreeNode<>(new Group(labels));
                        int count = recursionLeaf(subscriptions, label, user.getId(), 0);
                        if (RocketChatApplication.expands.contains(labels.getId())) {
                            label.expand();
                        }
                        Group content = label.getContent();
                        content.setUnRead(count);
                        notCompany.addChild(label);
                    }
                }
            }
        }

        if (adapter == null) {
            adapter = new TreeViewAdapter(displayNodes, Arrays.asList(new RootGroupNodeBinder(this, permissionRepository, user)
                    , new ConversationNodeBinder(this), new GroupChatNodeBinder(this), new GroupNodeBinder(), new GroupMeetingNodeBinder()));
            adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
                @Override
                public boolean onClick(TreeNode node, RecyclerView.ViewHolder holder) {
                    String selectedRoomId1 = RocketChatCache.INSTANCE.getSelectedRoomId();
                    if (holder instanceof GroupChatNodeBinder.ViewHolder) {
//                    (GroupChat)node.getContent();
                        //频道的响应点击事件
                        GroupChat content = (GroupChat) node.getContent();
                        Subscription subscription = content.getSubscription();
                        String id = subscription.getRid();
                        if (id.equals(selectedRoomId1)) {
                            if (panChange != null) {
                                panChange.closePane();
                            }
                        }
                        RocketChatCache.INSTANCE.setSelectedRoomId(id);
//                        showRoomFragment(id);
                        return true;
                    } else if (holder instanceof ConversationNodeBinder.ViewHolder) {
                        // 点对点响应事件
                        Conversation content = (Conversation) node.getContent();
                        Subscription subscription = content.getSubscription();
                        String id = subscription.getRid();
                        if (id.equals(selectedRoomId1)) {
                            if (panChange != null) {
                                panChange.closePane();
                            }
                        }
                        RocketChatCache.INSTANCE.setSelectedRoomId(id);
//                        showRoomFragment(id);
                        return true;
                    }
                    if (!node.isLeaf()) {
                        //Update and toggle the node.
                        //TODO
                        String key = null;
                        if (holder instanceof GroupNodeBinder.ViewHolder) {
                            Group content = (Group) node.getContent();
                            key = content.getLabel().getId();

                        } else if (holder instanceof GroupMeetingNodeBinder.ViewHolder) {
                            GroupMeeting content = (GroupMeeting) node.getContent();
                            key = content.getGroupMeetingName();
                        } else if (holder instanceof RootGroupNodeBinder.ViewHolder) {
                            RootGroup content = (RootGroup) node.getContent();
                            key = content.getRootGroupName().getValue();
                        }
                        if (!node.isExpand()) {
                            RocketChatApplication.expands.add(key);
                        } else {
                            RocketChatApplication.expands.remove(key);
                        }
                        onToggle(!node.isExpand(), holder);
                    }


                    return false;
                }

                @Override
                public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
                    if (holder instanceof GroupNodeBinder.ViewHolder) {
                        GroupNodeBinder.ViewHolder dirViewHolder = (GroupNodeBinder.ViewHolder) holder;
                        final ImageView ivArrow = dirViewHolder.getArrow();
                        int rotateDegree = isExpand ? 90 : -90;
                        ivArrow.animate().rotationBy(rotateDegree)
                                .start();
                    } else if (holder instanceof GroupMeetingNodeBinder.ViewHolder) {
                        GroupMeetingNodeBinder.ViewHolder dirViewHolder = (GroupMeetingNodeBinder.ViewHolder) holder;
                        final ImageView ivArrow = dirViewHolder.getArrow();
                        int rotateDegree = isExpand ? 90 : -90;
                        ivArrow.animate().rotationBy(rotateDegree)
                                .start();
                    }


                }
            });
            DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
            divider.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.treeitem_divideritemdecoration));
            recyclerView.addItemDecoration(divider);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.refresh(displayNodes);
        }

    }

    /**
     * 递归频道叶子节点
     */
    private int recursionLeaf(List<Subscription> list, TreeNode treeNode, String uId, int unRead) {
        int count = unRead;
        for (int i = 0; i < list.size(); i++) {
            Subscription subscription = list.get(i);
            TreeNode subscriptoinNode = new TreeNode(new GroupChat(subscription));
            count += Integer.parseInt(subscription.getUnread());
            if (RocketChatApplication.expands.contains(subscription.getId())) {
                subscriptoinNode.expand();
            }
            treeNode.addChild(subscriptoinNode);
            List<Subscription> subscriptions = subscriptionRepository.getByGIdAll(subscription.getCId(), subscription.getName(), uId);
            if (subscription != null) {
                recursionLeaf(subscriptions, subscriptoinNode, uId, unRead);
            }
        }
        return count;
    }

    /**
     * 删除会话
     *
     * @param holder
     * @param position
     * @param node
     */
    @Override
    public void deteleConversation(ConversationNodeBinder.ViewHolder holder, int position, TreeNode node) {
        Conversation content = (Conversation) node.getContent();
        Subscription subscription = content.getSubscription();
        String name = subscription.getName();
        int i = name.indexOf("&");
        if (i != -1) {
            name = name.substring(0, i);
        }
        WeiNingAlertDialog.Builder builder = new WeiNingAlertDialog.Builder(getActivity());
        dialog = builder.setTitle("你确定要关闭")
                .setTip("与 \"" + name + "\" 的对话吗？")
                .addViewOnclick(new WeiNingAlertDialog.OnClickListener() {
                    @Override
                    public void cancelClick() {
                        dialog.dismiss();
                    }

                    @Override
                    public void conFirmClick() {
                        hideRoomForMobile(myUser, subscription.getRid());
                        String selectedRoomId = RocketChatCache.INSTANCE.getSelectedRoomId();
                        if (subscription.getRid().equals(selectedRoomId)) {
                            RocketChatCache.INSTANCE.setSelectedRoomId("");
//                            RocketChatCache.INSTANCE.removeSelectedRoomId(hostname);
                        }
//                        TreeNode parent = node.getParent();
//                        List childList = parent.getChildList();
//                        childList.remove(node);
//                        parent.collapse();
//                        adapter.getDisplayNodes().remove(node);
//                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                })
                .build();
        dialog.show();
    }

    /**
     * 聊天展开叶子节点
     *
     * @param holder
     * @param position
     * @param node
     */
    @Override
    public void onToggle(GroupChatNodeBinder.ViewHolder holder, int position, TreeNode node) {
        adapter.onToggle(node);
        boolean isExpand = node.isExpand();
        //TODO
        GroupChat content = (GroupChat) node.getContent();
        String key = content.getSubscription().getId();
        if (!node.isExpand()) {
            RocketChatApplication.expands.add(key);
        } else {
            RocketChatApplication.expands.remove(key);
        }
        final ImageView ivArrow = holder.getArrow();
        int rotateDegree = isExpand ? 90 : -90;
        ivArrow.animate().rotationBy(rotateDegree)
                .start();
    }

    /**
     * 增加频道
     *
     * @param holder
     * @param position
     * @param node
     */
    @Override
    public void addGroupChat(RootGroupNodeBinder.ViewHolder holder, int position, TreeNode node) {
        RootGroup content = (RootGroup) node.getContent();
        switch (content.getRootGroupName().getId()) {
            case "Layout_Person_Title":
                startActivity(WorkCommunicationGroupActivity.class);
                break;
            case "Layout_Meeting_Title":
                startActivity(MeetingManagerGroupActivity.class);
                break;
            case "Layout_Channel_Title":
                startActivity(OrganizationControlGroupActivity.class);
                break;
        }
    }

    /**
     * 历史会议
     */
    @Override
    public void historySetting() {
        startActivity(new Intent(getActivity(), MeettingHistoryActivity.class));
    }

    private void startActivity(Class activity) {
        Intent intent = new Intent(getActivity(), activity);
        if (myUser != null) {
            intent.putExtra("companyId", myUser.getCompanyId());
            intent.putExtra("userId", myUser.getId());
        }
        startActivity(intent);
    }


    public void hideRoomForMobile(User user, String rid) {
        methodCallHelper.hideRoomForMobile(user.getId(), rid);
    }
}
