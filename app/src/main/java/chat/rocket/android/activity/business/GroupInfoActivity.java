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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.hadisatrio.optional.Optional;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.GridAdapter;
import chat.rocket.android.adapter.OprationAdapter;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.dialog.WeiNingAlertDialog;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.core.PermissionsConstants;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.Role;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmPermissionRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import indexablerv.PinyinUtil;
import io.reactivex.Single;

public class GroupInfoActivity extends BusinessBaseActivity implements View.OnClickListener {

    private static final int SELECT_USER = 3;
    private static final int DELETE_USER = 2;
    private static final int UPDATE_GROUP = 1;
    private static final int UPDATE_TITLE = 5;
    private static final int PERSON_INFO = 4;
    private static final int LOOK_HISTORY_MESSAGE = 5;
    private TextView tv_title, tv_count;
    private ImageView iv_back, iv_groupName, iv_groupTitle;
    private GridView gv_opration;
    private RecyclerView rv_group;
    private TextView tv_group_name, tv_group_content, tv_group_created,tv_groupTitle;
    private Button btn_delete;
    private LinearLayout ll_groupName, ll_groupTitle, ll_loadMoreMember;
    private String roomId, companyId,hostname, roomType;
    private Room room;
    private GridAdapter mAdapter;
    private List<User> userNameAll;
    private String subscriptionDisplayName;
    /**
     * 是否显示群组操作（包括设为群主，设为管理员）
     */
    private boolean showGroupOperation;
    private boolean hasOwnerModeratorPermission;//管理员或者群主权限
    // 添加和删除人员权限
    private boolean hasAddDeletePermission;
    private Switch mSNoDisturbing;
    private Switch mSisBanned;
    private Button mBtnDelete;
    private Button mBtnExit;
    private RealmSubscriptionRepository subscriptionRepository;
    private MethodCallHelper methodCallHelper;
    private Subscription subscriptionRid;
    private String mnd;
    private RealmRoomRepository roomRepository;
    private boolean ro;
    private RealmUserRepository userRepository;
    private User loginUser;
    private WeiNingAlertDialog dialog;
    private LinearLayout lLisBanned;
    WeakReference<Context> contextWeakReference ;
    CurrentUserObserver observer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        contextWeakReference =new WeakReference<Context>(this);
        initView();
        initData();
        setListener();
    }

    @SuppressLint("RxLeakedSubscription")
    private void initData() {
        roomType = getIntent().getStringExtra("roomType");
        hostname = getIntent().getStringExtra("hostname");
        roomId = getIntent().getStringExtra("roomId");
        showGroupOperation = getIntent().getBooleanExtra("showGroupOperation", false);
        subscriptionDisplayName = getIntent().getStringExtra("subscriptionDisplayName");

        OprationAdapter oprationAdapter = new OprationAdapter(this, roomType);
        gv_opration.setAdapter(oprationAdapter);

        //设置为一个6列的纵向网格布局
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 6, GridLayoutManager.VERTICAL, false);
        rv_group.setLayoutManager(mLayoutManager);
        methodCallHelper = new MethodCallHelper(this, hostname);
        initTitle();

        getCurrentUser().subscribe(user -> {
            loginUser = user;
        }, RCLog::e);

        setPermission();
        getRoom();
        mBtnDelete.setVisibility(hasAddDeletePermission?View.VISIBLE:View.GONE);
        mSisBanned.setClickable(hasAddDeletePermission);
        lLisBanned.setVisibility(hasAddDeletePermission?View.VISIBLE:View.GONE);
        subscriptionRepository.getSubscription(roomId).subscribe(subscriptionOptional -> {
                    mnd = subscriptionOptional.get().getMnd();
                    mSNoDisturbing.setChecked(Boolean.parseBoolean(mnd));
                }
                , RCLog::e);
        roomRepository.getById(roomId).subscribe(roomOptional -> {
            ro = roomOptional.get().isRo();
            mSisBanned.setChecked(ro);
        },RCLog::e);

    }

    private void initTitle() {
        String texts = "";
        if (RocketChatConstants.W.equals(roomType)) {//工作巢
            texts = getString(R.string.work_community_group);
        } else if (RocketChatConstants.P.equals(roomType)) {//组织巢
            texts = getString(R.string.organization_control_group);
        }
        tv_title.setText(texts);
    }

    private void setPermission() {
        roomRepository = new RealmRoomRepository(hostname);
        subscriptionRepository = new RealmSubscriptionRepository(hostname);
        subscriptionRid = subscriptionRepository.getSubscriptionRid(roomId);
        if(subscriptionRid==null)
            return;
        List<String> roles = null;
        try {
            roles = subscriptionRid.getRoles();
        } catch (Exception e) {
        }

        if (roles != null && roles.size() > 0 && (roles.contains(RocketChatConstants.OWNER) || roles.contains(RocketChatConstants.MODERATOR))) {
            hasOwnerModeratorPermission = true;
            hasAddDeletePermission = true;
        }

        if (!hasOwnerModeratorPermission) {
            iv_groupName.setVisibility(View.GONE);
            ll_groupName.setClickable(false);
            ll_groupName.setEnabled(false);

            iv_groupTitle.setVisibility(View.GONE);
            ll_groupTitle.setClickable(false);
            ll_groupTitle.setEnabled(false);
        }

        if (!hasAddDeletePermission) {
            hasAddDeletePermission = permissionJudge();
        }
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new GridAdapter(this, userNameAll, hasAddDeletePermission);
//            if(p && userNameAll != null && userNameAll.size() > 10 || (!p && userNameAll != null && userNameAll.size() > 12)){
//                ll_loadMoreMember.setVisibility(View.VISIBLE);
//            }
            if (RocketChatConstants.D.equals(roomType)) {
                ll_loadMoreMember.setVisibility(View.GONE);
            } else {
                ll_loadMoreMember.setVisibility(View.VISIBLE);
            }
            rv_group.setAdapter(mAdapter);
            //实现适配器自定义的点击监听
            mAdapter.setOnItemClickListener(view -> {
                int position = rv_group.getChildAdapterPosition(view);
                if (!hasAddDeletePermission){
                    clickMyInfo(userNameAll.get(position).getUsername());
                }else {
                if (userNameAll.size() >= 10) {
                    if (position == 10) {// 加号
                        clickAdd();
                    } else if (position == 11) {// 减号
                        clickDelete();
                    } else {
                        clickMyInfo(userNameAll.get(position).getUsername());
                    }
                } else {
                    if (position == userNameAll.size()) {// 加号
                        clickAdd();
                    } else if (position == userNameAll.size() + 1) {// 减号
                        clickDelete();
                    } else {
                        clickMyInfo(userNameAll.get(position).getUsername());
                    }
                }}
            });
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
    private ArrayList<UserEntity> selectList;
    private void clickAdd() {
//        Intent intent = new Intent(this, SelectUserActivity.class);
        Intent intent = new Intent(this, OrgSelectUserActivity.class);// TODO 临时入口
        intent.putExtra("existUser", loadLocalUserStr());
        intent.putExtra("roomId", roomId);
        intent.putExtra("type", roomType);
        intent.putExtra("hostname", hostname);
        intent.putExtra("companyId", subscriptionRepository.getByIdSub(roomId).getCompanyId());
        intent.putParcelableArrayListExtra("selectList", selectList);
        intent.putExtra("isFromGroupInfo",true);
        startActivityForResult(intent, SELECT_USER);
    }

    private void clickDelete() {
        Intent intent = new Intent(this, DeleteUserActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("hostname", hostname);
        intent.putParcelableArrayListExtra("deleteUser", getLocalUserList());
        startActivityForResult(intent, DELETE_USER);
    }

    private void clickMyInfo(String username) {
        Intent intent = new Intent(this, MyInfoActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("username", username);
        intent.putExtra("showGroupOperation", true);
        intent.putExtra("roomType", roomType);
        intent.putExtra("blocker", getIntent().getStringExtra("blocker"));
        startActivityForResult(intent, PERSON_INFO);
    }

    private void setGroupCount() {
        if (userNameAll != null) {
            tv_count.setText(getOnlineSize(userNameAll) + "/" + userNameAll.size());
        }
    }

    private void setGroupName() {
        tv_group_content.setText(room.getDescription());
        tv_groupTitle.setText(room.getTopic());
        tv_group_name.setText(subscriptionDisplayName);
        String uUserName = room.getUUserName();
        if (!TextUtils.isEmpty(uUserName) && uUserName.contains("&")) {
            tv_group_created.setText(uUserName.split("&")[0]);
        }
    }

    @SuppressLint("RxLeakedSubscription")
    private void getRoom() {
        this.room = roomRepository.getByRoomId(roomId);
        if (room == null) {
            ToastUtils.showToast("数据加载异常");
            finish();
            return;
        }

        findUser();
        setAdapter();
        setGroupCount();
        setGroupName();
    }

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
        if (userNameAll == null) {
            userNameAll = new ArrayList<>();
        } else {
            userNameAll.clear();
        }

        userNameAll.addAll(temp);
        filterUser();

        RCLog.d("findUser userNameAll=" + userNameAll.size(), true);
    }

    /**将需要去除的用户过滤**/
    private void filterUser() {
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
     *
     * @return
     */
    private String[] loadLocalUserStr() {
        String[] str = new String[]{};
        List<String> usernames = new ArrayList<>();
        for (User user : userNameAll) {
            usernames.add(user.getUsername());
        }
        str = usernames.toArray(new String[0]);
        return str;
    }

    private ArrayList<UserEntity> getLocalUserList() {
        ArrayList<UserEntity> list = new ArrayList<>();
        String loginUsername = RocketChatCache.INSTANCE.getUserUsername();

        for (User user : userNameAll) {
            String realName = user.getRealName();
            String username = user.getUsername();
            if (loginUsername != null && loginUsername.equals(username)) {
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

    private int getOnlineSize(List<User> nameAll) {
        int count = 0;
        if (nameAll.isEmpty()) return 0;
        for (User user : nameAll) {
            if (User.STATUS_ONLINE.equals(user.getStatus())) {
                count++;
            }
        }

        return count;
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        ll_groupName.setOnClickListener(this);
        ll_groupTitle.setOnClickListener(this);
        ll_loadMoreMember.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnExit.setOnClickListener(this);
        mSisBanned.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(ro!=isChecked){
                methodCallHelper.setReadonly(roomId,isChecked).continueWithTask(task -> {
                    if(task.isFaulted()){
                        ToastUtils.showToast("操作失败");
                        mSisBanned.setChecked(!isChecked);
                    }else {
                        ToastUtils.showToast("操作成功");
                    }
                    return null;
                },Task.UI_THREAD_EXECUTOR);
            }
        });
        mSNoDisturbing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Boolean.parseBoolean(mnd) != isChecked) {
                methodCallHelper.roomMND(roomId, subscriptionRid.getUId(), isChecked).continueWithTask(
                        task -> {
                            if(task.isFaulted()){
                                ToastUtils.showToast("操作失败");
                                mSNoDisturbing.setChecked(!isChecked);
                            }else {
                                ToastUtils.showToast("操作成功");
                            }
                            return null;
                        }, Task.UI_THREAD_EXECUTOR);
            }
        });
        gv_opration.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent;
            switch (position) {
                case 1:
                    intent = new Intent(GroupInfoActivity.this, FuJianActivity.class);
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("roomId", roomId);
                    startActivity(intent);
                    break;
                case 0:// 搜索
                    intent = new Intent(GroupInfoActivity.this, SearchChatInfoActivity.class);
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("roomId", roomId);
                    startActivityForResult(intent, LOOK_HISTORY_MESSAGE);
                    break;
                case 2://归档
                    intent = new Intent(GroupInfoActivity.this, CunDangListActivity.class);
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("companyCode", RocketChatCache.INSTANCE.getCompanyId());
                    startActivity(intent);
                    break;
                case 3:
                    break;
            }
        });
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
        btn_delete = findViewById(R.id.btn_delete);
        ll_groupName = findViewById(R.id.ll_groupName);
        ll_groupTitle = findViewById(R.id.ll_groupTitle);
        ll_loadMoreMember = findViewById(R.id.ll_loadMoreMember);
        iv_groupName = findViewById(R.id.iv_groupName);
        iv_groupTitle = findViewById(R.id.iv_groupTitle);
        tv_groupTitle = findViewById(R.id.tv_group_title);
        mSNoDisturbing = findViewById(R.id.s_no_disturbing);
        mSisBanned = findViewById(R.id.s_isBanned);
        mBtnDelete = findViewById(R.id.btn_delete);
        mBtnExit = findViewById(R.id.btn_exit);
        lLisBanned=findViewById(R.id.ll_isBanned);
    }

    @Override
    public void onClick(View v) {
                WeiNingAlertDialog.Builder builder = new WeiNingAlertDialog.Builder(this);
        Intent intent;
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.ll_groupName:
                intent = new Intent(this, UpdateGroupActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("subscriptionDisplayName", tv_group_name.getText().toString());
                startActivityForResult(intent, UPDATE_GROUP);
                break;
            case R.id.ll_groupTitle:
                Intent intent3 = new Intent(this, UpdateGroupActivity.class);
                intent3.putExtra("roomTopic", tv_groupTitle.getText().toString());
                intent3.putExtra("roomId", roomId);
                startActivityForResult(intent3, UPDATE_TITLE);
                break;
            case R.id.ll_loadMoreMember:
                intent = new Intent(this, MoreGroupMemberActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("hostname", hostname);
                intent.putExtra("hasAddDeletePermission", hasAddDeletePermission);
                intent.putParcelableArrayListExtra("selectList", getallMember());
                intent.putExtra("showGroupOperation", showGroupOperation);
                startActivityForResult(intent, SELECT_USER);
                break;
            case R.id.btn_delete://删除
                dialog = builder.setTitle("提示")
                        .setTip("确定删除该频道吗？")
                        .addViewOnclick(new WeiNingAlertDialog.OnClickListener() {
                            @Override
                            public void cancelClick() {
                                dialog.dismiss();
                            }

                            @Override
                            public void conFirmClick() {
                                deleteOrLeaveRoom(0);
                                dialog.dismiss();
                            }
                        }).build();
                dialog.show();
                break;
            case R.id.btn_exit:// 退出
                dialog = builder.setTitle("提示")
                        .setTip("确定退出该频道吗？")
                        .addViewOnclick(new WeiNingAlertDialog.OnClickListener() {
                            @Override
                            public void cancelClick() {
                                dialog.dismiss();
                            }

                            @Override
                            public void conFirmClick() {
                                deleteOrLeaveRoom(1);
                                dialog.dismiss();
                            }
                        }).build();
                dialog.show();
                break;
        }
    }

    private void deleteOrLeaveRoom(int i) {
        methodCallHelper.deleteOrLeaveRoom(roomId,subscriptionRid.getUUserName(),i).continueWithTask(task -> {
            if(task.isFaulted()){
                String localizedMessage = task.getError().getLocalizedMessage();
                if(localizedMessage.equals(getString(R.string.error_you_are_last_owner))){
                    ToastUtils.showToast(getString(R.string.last_owner));
                }else {
                    ToastUtils.showToast("操作失败");
                }

            }else {
                ToastUtils.showToast("操作成功");
                RocketChatCache.INSTANCE.setIsClickInfoMsg("showHome");
                finish();
            }
            return null;
        },Task.UI_THREAD_EXECUTOR);
    }

    private ArrayList<UserEntity> getallMember() {
        ArrayList<UserEntity> list = new ArrayList<>();
        for (User user : userNameAll) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_GROUP && resultCode == Activity.RESULT_OK && data != null) {
            String updateName = data.getStringExtra("updateName");
            tv_group_name.setText(updateName);
        }
        else if (requestCode == UPDATE_TITLE && resultCode == Activity.RESULT_OK && data != null) {
            String updateName = data.getStringExtra("updateName");
            tv_groupTitle.setText(updateName);
        }
        else if ((requestCode == PERSON_INFO || requestCode == LOOK_HISTORY_MESSAGE || !TextUtils.isEmpty(RocketChatCache.INSTANCE.getIsClickInfoMsg()))
                && resultCode == Activity.RESULT_OK) {
            finish();
        } else if (requestCode == DELETE_USER && resultCode == Activity.RESULT_OK && data != null) {
            observer = new CurrentUserObserver(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
            observer.register();
            String deleteSuccessName = data.getStringExtra("deleteSuccessName");
            String[] split = deleteSuccessName.split(" ");
            splitUserData(split);
            setGroupCount();
            mAdapter.notifyDataSetChanged();
        } else if (requestCode == SELECT_USER && resultCode == Activity.RESULT_OK && data != null) {
            observer = new CurrentUserObserver(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
            observer.register();
            ArrayList<UserEntity> selectList = data.getParcelableArrayListExtra("selectList");
            if (selectList.isEmpty()) return;
            List<String> names=new ArrayList<>();
            for (User user:userNameAll){
                names.add(user.getUsername());
            }
            for (int i = 0; i < selectList.size(); i++) {
                UserEntity entity = selectList.get(i);
                User build = User.builder().setId("" + i).setCreatedAt(0).set_updatedAt(0).setUtcOffset(0)
                        .setRealName(entity.getRealName())
                        .setAvatar(entity.getAvatar())
                        .setUsername(entity.getUsername())
                        .setName(entity.getName())
                        .build();
                if (!userNameAll.contains(build)&&!names.contains(build.getUsername())){
                userNameAll.add(build);
                }
            }
            filterUser();
            setGroupCount();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void splitUserData(String[] split) {
        ArrayList<User> temp = new ArrayList<>();
        if (split == null) return;
        for (int i = 0; i < split.length; i++) {
            if (TextUtils.isEmpty(split[i])) return;
            for (User user : userNameAll) {
                if (split[i].equals(user.getUsername())) {
                    temp.add(user);
                }
            }
        }
        userNameAll.removeAll(temp);
        RCLog.d("splitUserData userNameAll=" + userNameAll.size() + ",temp=" + temp.size(), true);
    }

    /**
     * 1.会议巢：会议已结束，不能添加成员；
     * 需add-user-to-any-m-room或add-user-to-joined-room权限，反之，隐藏添加入口
     * 2.组织巢：需add-user-to-any-p-room或add-user-to-joined-room权限，反之则隐藏添加入口
     * 3.工作巢：需add-user-to-any-w-room或add-user-to-joined-room，反之则隐藏添加入口
     * 4.公共频道：需add-user-to-any-c-room或add-user-to-joined-room，反之则隐藏添加入口
     */
    @SuppressLint("RxLeakedSubscription")
    private boolean permissionJudge() {
        List<String> roles = loginUser.getRoles();
        if(roles == null || roles.size() == 0) return false;
        boolean hasPrimission = false;

        if (RocketChatConstants.W.equals(roomType)) {
            List<String> temp = setUserToJoinedRoomList(
                    PermissionsConstants.ADD_USER_TO_JOINED_ROOM, PermissionsConstants.ADD_USER_TO_ANY_W_ROOM);
            hasPrimission = !Collections.disjoint(roles, temp);

        } else if (RocketChatConstants.P.equals(roomType)) {
            hasPrimission = !Collections.disjoint(roles, setUserToJoinedRoomList(
                    PermissionsConstants.ADD_USER_TO_JOINED_ROOM, PermissionsConstants.ADD_USER_TO_ANY_P_ROOM));
        }

        RCLog.d("permissionJudge disjoint=" + hasPrimission, true);
        return hasPrimission;
    }

    @SuppressLint("RxLeakedSubscription")
    private List<String> setUserToJoinedRoomList(String type1, String type2) {
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
