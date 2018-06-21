package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hadisatrio.optional.Optional;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.GridAdapter;
import chat.rocket.android.adapter.OprationAdapter;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.dialog.WeiNingAlertDialog;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.core.PermissionsConstants;
import chat.rocket.core.models.Attendance;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.Role;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmPermissionRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import indexablerv.PinyinUtil;
import io.reactivex.Single;

public class MeetingGroupInfoActivity extends BusinessBaseActivity implements View.OnClickListener{

    private static final int SELECT_USER = 3;
    private static final int DELETE_USER = 2;
    private static final int UPDATE_GROUP = 1;
    private static final int UPDATE_HOST = 6;//修改主持人
    private static final int PERSON_INFO = 4;
    private static final int LOOK_HISTORY_MESSAGE = 5;
    private TextView tv_title,tv_count;
    private ImageView iv_back,iv_groupName,iv_zhuchiren;
    private GridView gv_opration;
    private RecyclerView rv_group;
    private TextView tv_group_name,tv_group_content,tv_group_created,tv_group_zhuciren,tv_meeting_time,tv_qiandao,tv_meeting_pause,tv_meeting_finish;
    private LinearLayout ll_groupName,ll_host,ll_loadMoreMember,ll_fujian,ll_qiandao,ll_group_content,ll_owner;
    private String roomId,hostname;
    private Room room;
    private GridAdapter mAdapter;
    private List<User> userNameAll;
    private String subscriptionDisplayName;
    private MethodCallHelper methodCallHelper;
    /**
     * 是否显示群组操作（包括设为群主，设为管理员）
     */
    private boolean showGroupOperation;
    //管理员或者群主权限
    private boolean hasOwnerModeratorPermission;
    // 添加和删除人员权限
    private boolean hasAddDeletePermission;
    private RealmUserRepository userRepository;
    private User loginUser;
    private boolean isPauseOrFinish=false;
    WeakReference<Context> contextWeakReference;
    CurrentUserObserver observer;
    private WeiNingAlertDialog dialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_meeting);
        contextWeakReference=new WeakReference<Context>(this);
        initView();
        initData();
        setListener();
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        findViewById(R.id.tv_create).setVisibility(View.GONE);

        tv_count = findViewById(R.id.tv_count);
        gv_opration = findViewById(R.id.gv_opration);
        rv_group = findViewById(R.id.rv_group);
        tv_group_name = findViewById(R.id.tv_group_name);
        tv_group_created = findViewById(R.id.tv_group_created);
        tv_group_content = findViewById(R.id.tv_group_content);
        tv_qiandao = findViewById(R.id.tv_qiandao);
        ll_owner=findViewById(R.id.ll_owner);
        tv_meeting_pause = findViewById(R.id.tv_pause);
        tv_meeting_finish = findViewById(R.id.tv_finish);
        ll_groupName = findViewById(R.id.ll_groupName);
        ll_host = findViewById(R.id.ll_host);
        ll_loadMoreMember = findViewById(R.id.ll_loadMoreMember);
        ll_fujian = findViewById(R.id.ll_fujian);
        ll_qiandao = findViewById(R.id.ll_qiandao);
        ll_group_content = findViewById(R.id.ll_group_content);

        tv_group_zhuciren = findViewById(R.id.tv_group_zhuciren);
        tv_meeting_time = findViewById(R.id.tv_meeting_time);
        iv_groupName = findViewById(R.id.iv_groupName);
        iv_zhuchiren = findViewById(R.id.iv_zhuchiren);
    }

    private void initData() {
        isPauseOrFinish=getIntent().getBooleanExtra("isPauseOrFinish",false);
        methodCallHelper = new MethodCallHelper(this, hostname);
        hostname = getIntent().getStringExtra("hostname");
        roomId = getIntent().getStringExtra("roomId");
        showGroupOperation = getIntent().getBooleanExtra("showGroupOperation", false);
        subscriptionDisplayName = getIntent().getStringExtra("subscriptionDisplayName");

        OprationAdapter oprationAdapter = new OprationAdapter(this, RocketChatConstants.M);
        gv_opration.setAdapter(oprationAdapter);

        //设置为一个6列的纵向网格布局
        GridLayoutManager mLayoutManager=new GridLayoutManager(this,6,GridLayoutManager.VERTICAL,false);
        rv_group.setLayoutManager(mLayoutManager);
        tv_title.setText(getString(R.string.meeting_manager_group));

        getRoom();
    }

    @SuppressLint("RxLeakedSubscription")
    private void getRoom() {
        RealmRoomRepository roomRepository = new RealmRoomRepository(hostname);
        this.room = roomRepository.getByRoomId(roomId);
        if (room == null) {
            ToastUtils.showToast("room == null");
            finish();
            return;
        }

        getCurrentUser().subscribe(user -> {
            loginUser = user;
        }, RCLog::e);
        setPermission();
        findUser();
        setAdapter();
        setGroupCount();
        setGroupName();
        setQianDaoType();
    }

    private void setGroupCount(){
        if(userNameAll != null) {
            tv_count.setText(getOnlineSize(userNameAll) + "/" + userNameAll.size());
        }
    }

    private void setGroupName(){
        tv_group_content.setText(room.getMeetingSubject());
        tv_group_name.setText(subscriptionDisplayName);
        String uUserName = room.getUUserName();
        String createName = chat.rocket.android.helper.TextUtils.splitUsername(uUserName);
        String host = chat.rocket.android.helper.TextUtils.splitUsername(room.getHost());
        tv_group_created.setText(createName);
        tv_group_zhuciren.setText(host);

        long roomStartTime = room.getStartTime();
        long roomEndTime = room.getEndTime();
        if(roomStartTime == 0||roomEndTime == 0){
            tv_meeting_time.setText("");
        }else {
            String startTime = DateTime.fromEpocMs(roomStartTime, DateTime.Format.DATE_TIME2);
            String endTime = DateTime.fromEpocMs(roomEndTime, DateTime.Format.DATE_TIME2);
            tv_meeting_time.setText(startTime + "——" + endTime);
        }
    }

    private void setQianDaoType() {
        if("true".equals(room.getAttendance())){
            List<Attendance> attendanceList = room.getAttendanceList();
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
                tv_qiandao.setVisibility(View.VISIBLE);
            }

            RealmSubscriptionRepository subscriptionRepository = new RealmSubscriptionRepository(hostname);
            Subscription subscriptionRid = subscriptionRepository.getByIdSub(roomId);
            List<String> roles = subscriptionRid.getRoles();
            if (roles != null && roles.size() > 0 && (roles.contains(RocketChatConstants.OWNER)
                    || roles.contains(RocketChatConstants.GROUPADMIN)|| roles.contains(RocketChatConstants.MODERATOR))) {
                if (!isPauseOrFinish)
                ll_owner.setVisibility(View.VISIBLE);
                tv_qiandao.setVisibility(View.GONE);
            }
        }
    }

    /**查找当前room里面的群成员**/
    private void findUser() {
        if(userRepository == null) {
            userRepository = new RealmUserRepository(hostname);
        }

        List<String> usernames = room.getUsernames();
        String[] str = new String[]{};
        if (usernames != null && usernames.size() > 0) {
            /*将自己创建的频道添加到列表中**/
            String userUsername = RocketChatCache.INSTANCE.getUserUsername();
            if(!usernames.contains(userUsername)){
                usernames.add(userUsername);
            }
            str = usernames.toArray(new String[0]);
        }
        List<User> temp = userRepository.getByNameAll(str);
        if(userNameAll == null){
            userNameAll = new ArrayList<>();
        }else{
            userNameAll.clear();
        }

        userNameAll.addAll(temp);
        deleteXiaoyi();

        RCLog.d("findUser userNameAll="+userNameAll.size(), true);
    }

    private void deleteXiaoyi() {
        List<User> list = new ArrayList<>();
        for (User user : userNameAll) {
            String realName = user.getRealName();
            if ("小翌".equals(realName) || "小翌".equals(user.getUsername()) ||
                    "true".equals(user.getMaster()) || isAdmin(user)) {
                list.add(user);
            }
        }
        RCLog.d("deleteXiaoyi list="+list.toString());
        userNameAll.removeAll(list);
    }

    private boolean isAdmin(User user){
        List<String> roles = user.getRoles();
        if(roles == null || roles.size() == 0) return false;
        for(String s : roles){
            if(RocketChatConstants.ADMIN.equals(s.toLowerCase())){
                RCLog.d("deleteXiaoyi list="+user);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取username数组//言慎&286
     * @return
     */
    private String[] loadLocalUserStr() {
        String[] str = new String[]{};
        List<String> usernames=new ArrayList<>();
        for (User user:userNameAll){
            usernames.add(user.getUsername());
        }
        str=usernames.toArray(new String[0]);
        return str;
    }

    private int getOnlineSize(List<User> nameAll) {
        int count = 0;
        if(nameAll.isEmpty()) return 0;
        for (User user : nameAll) {
            if(User.STATUS_ONLINE.equals(user.getStatus())){
                count++;
            }
        }

        return count;
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        ll_groupName.setOnClickListener(this);
        ll_host.setOnClickListener(this);
        ll_loadMoreMember.setOnClickListener(this);
        ll_fujian.setOnClickListener(this);
        ll_qiandao.setOnClickListener(this);
        tv_meeting_pause.setOnClickListener(this);
        tv_meeting_finish.setOnClickListener(this);

        gv_opration.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent;
            switch (position) {
                case 1://会议巢独有的总结
                    intent = new Intent(MeetingGroupInfoActivity.this, ZongJieActivity.class);
                    intent.putExtra("rawContent", room.getRawContent());
                    startActivity(intent);
                    break;
                case 0:// 搜索
                    intent = new Intent(MeetingGroupInfoActivity.this, SearchChatInfoActivity.class);
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("roomId", roomId);
                    startActivityForResult(intent, LOOK_HISTORY_MESSAGE);
                    break;
                case 2://归档
                    intent = new Intent(MeetingGroupInfoActivity.this, CunDangListActivity.class);
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("companyCode", RocketChatCache.INSTANCE.getCompanyId());
                    startActivity(intent);
                    break;
                case 3:
                    break;
            }
        });
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void onClick(View v) {
        Intent intent;
        int i = v.getId();
        if (i == R.id.iv_back) {
            finish();

        } else if (i == R.id.ll_groupName) {
            if (isPauseOrFinish){
                ToastUtils.showToast(getString(R.string.meetting_pauseorfinish_cannt_operate));
                return;
            }
            intent = new Intent(this, UpdateGroupActivity.class);
            intent.putExtra("roomId", roomId);
            intent.putExtra("subscriptionDisplayName", tv_group_name.getText().toString());
            startActivityForResult(intent, UPDATE_GROUP);

        } else if (i == R.id.ll_host) {
            if (isPauseOrFinish){
                ToastUtils.showToast(getString(R.string.meetting_pauseorfinish_cannt_operate));
                return;
            }
            intent = new Intent(this, OrgSelectUserActivity.class);
            intent.putExtra("isFromGroupInfo",true);
            intent.putExtra("roomId", roomId);
            intent.putExtra("isSingleChoose", true);
            intent.putExtra("type", RocketChatConstants.M);
            intent.putExtra("companyId", RocketChatCache.INSTANCE.getCompanyId());
            UserRepository repository = new RealmUserRepository(hostname);
            repository.getCurrent().distinctUntilChanged()
                    .subscribe(userOptional -> {
                        User user = userOptional.get();
                        if (user != null) {
                            intent.putExtra("companyId", user.getCompanyId());
                        }
                    }, RCLog::e);
            intent.putExtra("isSingleChoose", true);
            intent.putParcelableArrayListExtra("allDataList", getallMember());
            intent.putParcelableArrayListExtra("selectList", selectList);
            startActivityForResult(intent, UPDATE_HOST);

        } else if (i == R.id.ll_loadMoreMember) {
            intent = new Intent(this, MoreGroupMemberActivity.class);
            intent.putExtra("roomId", roomId);
            intent.putExtra("hostname", hostname);
            intent.putExtra("hasAddDeletePermission", hasAddDeletePermission);
            intent.putParcelableArrayListExtra("selectList", getallMember());
            intent.putExtra("showGroupOperation", showGroupOperation);
            intent.putExtra("isPauseOrFinish", isPauseOrFinish);
            startActivityForResult(intent, SELECT_USER);

        } else if (i == R.id.ll_fujian) {
            intent = new Intent(this, FuJianActivity.class);
            intent.putExtra("hostname", hostname);
            intent.putExtra("roomId", roomId);
            startActivity(intent);

        } else if (i == R.id.ll_qiandao) {
            if (isPauseOrFinish){
                ToastUtils.showToast(getString(R.string.meetting_pauseorfinish_cannt_operate));
                return;
            }
            intent = new Intent(this, QianDaoActivity.class);
            intent.putExtra("hostname", hostname);
            intent.putExtra("roomId", roomId);
            startActivity(intent);

        } else if (i == R.id.tv_pause) {
            WeiNingAlertDialog.Builder builder = new WeiNingAlertDialog.Builder(this);
            dialog = builder.setTitle("提示")
                    .setTip("确定暂停会议吗？")
                    .addViewOnclick(new WeiNingAlertDialog.OnClickListener() {
                        @Override
                        public void cancelClick() {
                            dialog.dismiss();
                        }

                        @Override
                        public void conFirmClick() {
                            pauseMeetting();
                            dialog.dismiss();
                        }
                    }).build();
            dialog.show();

        } else if (i == R.id.tv_finish) {
            WeiNingAlertDialog.Builder builder = new WeiNingAlertDialog.Builder(this);
            dialog = builder.setTitle("提示")
                    .setTip("确定结束会议吗？")
                    .addViewOnclick(new WeiNingAlertDialog.OnClickListener() {
                        @Override
                        public void cancelClick() {
                            dialog.dismiss();
                        }

                        @Override
                        public void conFirmClick() {
                            finishMeetting();
                            dialog.dismiss();
                        }
                    }).build();
            dialog.show();

        }
    }

    private void finishMeetting(){
        methodCallHelper.forceEndMeeting(roomId, RocketChatCache.INSTANCE.getUserId())
                .continueWithTask(new Continuation<String, Task<Object>>() {
                    @Override
                    public Task<Object> then(Task<String> task) throws Exception {
                        if (task.getError() != null) {
                            RCLog.e("forceEndMeeting" + task.getError().getMessage(), true);
                            if (task.getError().getMessage().equals("Invalid user [error-metting-not-begin]"))
                                ToastUtils.showToast("当前会议还未开始");
                        } else {
                            ToastUtils.showToast("会议已结束");
                            RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
                            MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
                            methodCall.getRoomSubscriptions().onSuccess(task1 -> {
                                Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                                        MeetingGroupInfoActivity.this, hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
                                listener.register();
                                return null;
                            }).continueWith(new LogIfError());
                            finish();
                        }

                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
    }
    private void pauseMeetting(){
        methodCallHelper.pauseOrRestartMeetting(roomId, RocketChatCache.INSTANCE.getUserId(), false)
                .continueWithTask(new Continuation<String, Task<Object>>() {
                    @Override
                    public Task<Object> then(Task<String> task) throws Exception {
                        if (task.getError() != null) {
                            RCLog.e("restartMeetting" + task.getError().getMessage(), true);
                            if (task.getError().getMessage().equals("Invalid user [error-metting-not-begin]"))
                                ToastUtils.showToast("当前会议还未开始");
                        } else {
                            ToastUtils.showToast("会议已暂停");
                            RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
                            MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
                            methodCall.getRoomSubscriptions().onSuccess(task1 -> {
                                Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                                        MeetingGroupInfoActivity.this, hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
                                listener.register();
                                return null;
                            }).continueWith(new LogIfError());
                            finish();
                        }

                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
    }
    private ArrayList<UserEntity> getallMember() {
        ArrayList<UserEntity> list = new ArrayList<>();
        for(User user : userNameAll){
            UserEntity entity = new UserEntity(false, user.getRealName(), user.getUsername());
            entity.setCompanyName(user.getCompanyName());
            entity.setStatus(user.getStatus());
            entity.setAvatar(user.getAvatar());
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

    private void setAdapter(){
        if(mAdapter == null){
            mAdapter = new GridAdapter(this, userNameAll, hasAddDeletePermission);
//            if(p && userNameAll != null && userNameAll.size() > 10 || (!p && userNameAll != null && userNameAll.size() > 12)){
//                ll_loadMoreMember.setVisibility(View.VISIBLE);
//            }
            rv_group.setAdapter(mAdapter);
            //实现适配器自定义的点击监听
            mAdapter.setOnItemClickListener(view -> {
                int position = rv_group.getChildAdapterPosition(view);
                if (!hasAddDeletePermission){
                    clickMyInfo(userNameAll.get(position).getUsername());
                }else {
                if(userNameAll.size() >= 10){
                    if(position == 10){// 加号
                        clickAdd();
                    }else if(position == 11){// 减号
                        clickDelete();
                    }else{
                        clickMyInfo(userNameAll.get(position).getUsername());
                    }
                }else{
                    if(position == userNameAll.size()){// 加号
                        clickAdd();
                    }else if(position == userNameAll.size() + 1){// 减号
                        clickDelete();
                    }else {
                        clickMyInfo(userNameAll.get(position).getUsername());
                    }
                }
                }
            });
        }else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void clickAdd(){
//        Intent intent = new Intent(this, OrgSelectUserActivity.class);// TODO 临时入口
//        intent.putExtra("existUser", loadLocalUserStr());
//        intent.putExtra("roomId",roomId);
//        intent.putExtra("type", RocketChatConstants.M);
//        intent.putExtra("hostname", hostname);
//        startActivityForResult(intent, SELECT_USER);
        if (isPauseOrFinish){
            ToastUtils.showToast(getString(R.string.meetting_pauseorfinish_cannt_operate));
            return;
        }
        Intent intent = new Intent(MeetingGroupInfoActivity.this, OrgSelectUserActivity.class);
        intent.putExtra("isFromGroupInfo",true);
        intent.putExtra("existUser", loadLocalUserStr());
        intent.putExtra("roomId",roomId);
        intent.putExtra("type", RocketChatConstants.M);
        intent.putExtra("companyId", RocketChatCache.INSTANCE.getCompanyId());
        startActivityForResult(intent, SELECT_USER);
    }
    private void clickDelete(){
        if (isPauseOrFinish){
            ToastUtils.showToast(getString(R.string.meetting_pauseorfinish_cannt_operate));
            return;
        }
        Intent intent = new Intent(MeetingGroupInfoActivity.this, DeleteUserActivity.class);
        intent.putExtra("roomId",roomId);
        intent.putExtra("hostname", hostname);
        intent.putParcelableArrayListExtra("deleteUser", getLocalUserList());
        startActivityForResult(intent, DELETE_USER);
    }
    private void clickMyInfo(String username){
        Intent intent = new Intent(MeetingGroupInfoActivity.this, MyInfoActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("username", username);
        intent.putExtra("showGroupOperation", true);
        intent.putExtra("roomType", RocketChatConstants.M);
        intent.putExtra("blocker", getIntent().getStringExtra("blocker"));
        intent.putExtra("isPauseOrFinish", isPauseOrFinish);
        startActivityForResult(intent, PERSON_INFO);
    }

    private ArrayList<UserEntity> getLocalUserList() {
        ArrayList<UserEntity> list = new ArrayList<>();
        String loginUsername = RocketChatCache.INSTANCE.getUserUsername();

        for (User user : userNameAll) {
            String realName = user.getRealName();
            String username = user.getUsername();
            if(loginUsername != null && loginUsername.equals(username)){
                continue;
            }

            UserEntity entity = new UserEntity(false, realName, username);
            String pinyin = PinyinUtil.getPingYin(realName);
            entity.setPinyin(pinyin);
            entity.setCompanyName(user.getCompanyName());
            List<DeptRole> deptRole = user.getDeptRole();
            if (deptRole != null && deptRole.size() > 0) {
                entity.setDept(deptRole.get(0).getOrg_name());
                entity.setZhiWei(deptRole.get(0).getPos_name().split("_")[1]);
            }

            entity.setAvatar(user.getAvatar());
            list.add(entity);
        }
        return list;
    }

    ArrayList<UserEntity> selectList;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UPDATE_GROUP && resultCode == Activity.RESULT_OK && data != null){
            String updateName = data.getStringExtra("updateName");
            tv_group_name.setText(updateName);
        }else if(requestCode == UPDATE_HOST && resultCode == Activity.RESULT_OK && data != null){
            ArrayList<UserEntity> selectList=data.getParcelableArrayListExtra("selectList");
            if (selectList==null||selectList.size()==0){
                return;
            }
            methodCallHelper.setHost(roomId, selectList.get(0).getUsername())
                    .continueWithTask(new Continuation<String, Task<Object>>() {
                        @Override
                        public Task<Object> then(Task<String> task) throws Exception {
                            if (task.getError() != null) {
                                RCLog.e("setHost" + task.getError().getMessage(), true);
                            }else {
                                tv_group_zhuciren.setText(selectList.get(0).getRealName());
                            }

                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);

        }else if ((requestCode == PERSON_INFO || requestCode == LOOK_HISTORY_MESSAGE || !TextUtils.isEmpty(RocketChatCache.INSTANCE.getIsClickInfoMsg()))
                && resultCode == Activity.RESULT_OK) {
            finish();
        }
        else if(requestCode == DELETE_USER && resultCode == Activity.RESULT_OK && data != null){
            observer = new CurrentUserObserver(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
            observer.register();
            String deleteSuccessName = data.getStringExtra("deleteSuccessName");
            String[] split = deleteSuccessName.split(" ");
            splitUserData(split);
            setGroupCount();
            mAdapter.notifyDataSetChanged();
        }
        else if(requestCode == SELECT_USER && resultCode == Activity.RESULT_OK && data != null){
            observer = new CurrentUserObserver(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
            observer.register();
            ArrayList<UserEntity> selectList = data.getParcelableArrayListExtra("selectList");
            if(selectList.isEmpty()) return;
            List<String> names=new ArrayList<>();
            for (User user:userNameAll){
                names.add(user.getUsername());
            }
            for (int i = 0; i < selectList.size(); i++) {
                UserEntity entity = selectList.get(i);
                User build = User.builder().setId(""+i).setCreatedAt(0).set_updatedAt(0).setUtcOffset(0)
                        .setRealName(entity.getRealName())
                        .setAvatar(entity.getAvatar())
                        .setUsername(entity.getUsername())
                        .setName(entity.getName())
                        .build();
                if (!userNameAll.contains(build)&&!names.contains(build.getUsername())){
                    userNameAll.add(build);
                }
            }
            deleteXiaoyi();
            setGroupCount();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void splitUserData(String[] split) {
        ArrayList<User> temp = new ArrayList<>();
        if(split == null) return;
        for (int i = 0; i < split.length; i++) {
            if(TextUtils.isEmpty(split[i])) return;
            for(User user : userNameAll){
                if(split[i].equals(user.getUsername())){
                    temp.add(user);
                }
            }
        }
        userNameAll.removeAll(temp);
        RCLog.d("splitUserData userNameAll="+userNameAll.size()+",temp="+temp.size(), true);
    }

    private void setPermission() {
        RealmSubscriptionRepository subscriptionRepository = new RealmSubscriptionRepository(hostname);
        Subscription subscriptionRid = subscriptionRepository.getByIdSub(roomId);
        if(subscriptionRid == null){
            finish();
            return;
        }
        List<String> roles = subscriptionRid.getRoles();

        if(roles != null && roles.size() > 0 && (roles.contains(RocketChatConstants.OWNER) || roles.contains(RocketChatConstants.GROUPADMIN)
                || roles.contains(RocketChatConstants.MODERATOR))) {
            hasOwnerModeratorPermission = true;
            hasAddDeletePermission = true;
            ll_host.setClickable(true);
            ll_host.setEnabled(true);
            ll_owner.setVisibility(View.VISIBLE);
            tv_qiandao.setVisibility(View.GONE);
        }

        if(hasOwnerModeratorPermission) {
            if(("true".equals(room.getAttendance())||"1".equals(room.getAttendance()))&&room.isOpen()){
                ll_qiandao.setVisibility(View.VISIBLE);
            }else{
                ll_qiandao.setVisibility(View.GONE);
            }
        }else{
            iv_groupName.setVisibility(View.GONE);
            ll_groupName.setClickable(false);
            ll_groupName.setEnabled(false);
            ll_host.setEnabled(false);
            ll_host.setClickable(false);
            iv_zhuchiren.setVisibility(View.GONE);
        }
        if(!hasAddDeletePermission){
            hasAddDeletePermission = permissionJudge();
        }
        if (isPauseOrFinish){
            ll_owner.setVisibility(View.GONE);
        }
    }

    /**
     * 1.会议巢：会议已结束，不能添加成员；
     *         需add-user-to-any-m-room或add-user-to-joined-room权限，反之，隐藏添加入口
     2.组织巢：需add-user-to-any-p-room或add-user-to-joined-room权限，反之则隐藏添加入口
     3.工作巢：需add-user-to-any-w-room或add-user-to-joined-room，反之则隐藏添加入口
     4.公共频道：需add-user-to-any-c-room或add-user-to-joined-room，反之则隐藏添加入口
     */
    @SuppressLint("RxLeakedSubscription")
    private boolean permissionJudge() {
        List<String> roles = loginUser.getRoles();
        if(roles == null || roles.size() == 0) return false;

        List<String> temp = setUserToJoinedRoomList(
                PermissionsConstants.ADD_USER_TO_JOINED_ROOM, PermissionsConstants.ADD_USER_TO_ANY_M_ROOM);
        boolean hasPermission = !Collections.disjoint(roles, temp);

        RCLog.d("permissionJudge disjoint="+hasPermission,true);
        return hasPermission;
    }

    @SuppressLint("RxLeakedSubscription")
    private List<String> setUserToJoinedRoomList(String type1, String type2){
        List<String> addUserToJoinedRoom = new ArrayList<>();
        RealmPermissionRepository permissionRepository = new RealmPermissionRepository(hostname);
        permissionRepository.getById(type1)
                .subscribe(permissionOptional -> {
                    List<Role> createWroles = permissionOptional.get().getRoles();
                    for (Role role : createWroles) {
                        addUserToJoinedRoom.add(role.getName());
                    }
                }, RCLog::e);
        permissionRepository.getById(type2)
                .subscribe(permissionOptional -> {
                    List<Role> createWroles = permissionOptional.get().getRoles();
                    for (Role role : createWroles) {
                        addUserToJoinedRoom.add(role.getName());
                    }
                }, RCLog::e);
        return addUserToJoinedRoom;
    }

    private Single<User> getCurrentUser() {
        userRepository = new RealmUserRepository(hostname);
        return userRepository.getCurrent()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (observer!=null)
            observer.unregister();
    }
}
