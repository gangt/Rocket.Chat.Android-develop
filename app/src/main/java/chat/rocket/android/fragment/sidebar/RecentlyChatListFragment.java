package chat.rocket.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.recyclertreeview.bean.Conversation;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserRoomChanged;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.disposables.Disposable;
import tellh.com.recyclertreeview_lib.TreeNode;

/**
 * Created by lyq on 2018/5/12.
 */

@SuppressLint("ValidFragment")
public class RecentlyChatListFragment extends BaseFragment implements ChatListAdapter.OnItemClickListener {
    @BindView(R.id.rv)
    RecyclerView mRecycler;
    @BindView(R.id.tv_empty)
    TextView mTvEmpty;
    private LinearLayoutManager linearLayoutManager;
    private ChatListAdapter chatListAdapter;
    private RealmSubscriptionRepository subscriptionRepository;
    private String hostname;
    private RealmUserRepository userRepository;
    private AllChatListFragment.PanChange panChange;
    List<String> names = new ArrayList<>();
    private User user;
    private List<Subscription> subscriptions;

    @SuppressLint("ValidFragment")
    public RecentlyChatListFragment(AllChatListFragment.PanChange panChange, String hostname) {
        this.hostname = hostname;
        this.panChange = panChange;
    }

    public RecentlyChatListFragment(){}
    @Override
    protected void initData() {
        super.initData();
        subscriptionRepository = new RealmSubscriptionRepository(hostname);
        userRepository = new RealmUserRepository(hostname);
        loadData();
    }

    @Override
    protected void initView() {
        super.initView();
        linearLayoutManager = new LinearLayoutManager(mContext);
        mRecycler.setLayoutManager(linearLayoutManager);
        chatListAdapter = new ChatListAdapter(this);
        mRecycler.setAdapter(chatListAdapter);

    }

    public void setHint( FragmentActivity activity) {
        if (chatListAdapter!=null&&chatListAdapter.getItemCount()==0) {
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
            loadData();
        }
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
                    setData(subscriptions);
                }, RCLog::e);
            }

        }
    }
    private void loadData() {
        Disposable subscribe1 = userRepository.getCurrent().distinctUntilChanged()
                .subscribe(userOptional -> {
                    user = userOptional.get();
                    if (user != null) {
                        Disposable subscribe = subscriptionRepository
                                .getAllSub(user.getId()).subscribe(subscriptions -> {
                                    if (subscriptions.size() > 0) {
                                        mTvEmpty.setVisibility(View.GONE);
                                    } else {
                                        mTvEmpty.setVisibility(View.VISIBLE);
                                    }
                                    this.subscriptions=subscriptions;
                                    setData(subscriptions);
                                    if(names!=null)
                                    userListener(user);
                                }, RCLog::e);
                    }
                }, RCLog::e);
    }

    private void setData(List<Subscription> subscriptions) {
        chatListAdapter.setData(subscriptions);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_recently_chat_list;
    }

    @Override
    public void onItemClick(Subscription subscription) {
        String selectedRoomId1 = RocketChatCache.INSTANCE.getSelectedRoomId();
        //频道的响应点击事件
        String id = subscription.getRid();
        if (id.equals(selectedRoomId1)) {
            if (panChange != null) {
                panChange.closePane();
            }
        }
        RocketChatCache.INSTANCE.setSelectedRoomId(id);
    }
}
