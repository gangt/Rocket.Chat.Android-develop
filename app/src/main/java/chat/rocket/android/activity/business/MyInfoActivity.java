package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
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

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.OprationAdapter;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.dialog.WeiNingAlertDialog;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.renderer.UserRenderer;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.core.PermissionsConstants;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.Role;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomRole;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmPermissionRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRoleRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Single;

public class MyInfoActivity extends BusinessBaseActivity implements View.OnClickListener {

    private static final int LOOK_HISTORY_MESSAGE = 5;
    private TextView tv_name, tv_phone,tv_landline, tv_company, tv_dept, tv_position, tv_role, tv_current_time, tv_create_time, tv_last_login,tv_remove_from_group;
    private ImageView iv_back;
    private User clickUser,loginUser;
    private String hostname;
    private Switch s_lock,s_muteUser,s_set_group,s_set_manager;
    private LinearLayout ll_lock;
    private String username,roomId,roomType;
    WeakReference<Context> weakReference;
    CurrentUserObserver observer;
    /**
     * 锁定用户标记
     */
    private String blocker;

    private String telePhone;
    /**
     * 为true表示需要显示锁定用户UI，只有从点对点聊天右上角点击进入才显示
     */
    private boolean showLockUser;
    /**
     * 争对群的操作，包括设置群主，管理员，禁止发言（true表示群操作）
     * 点对点查看个人信息不显示，该值为false
     */
    private boolean showGroupOperation;
    private MethodCallHelper methodCallHelper;
    private LinearLayout ll_group_message,ll_user_info;
    private GridView gv_opration;
    private View include_info;
    private LinearLayout ll_video ;
    private LinearLayout ll_audio ;
    private boolean isPauseOrFinish;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);
        weakReference=new WeakReference<Context>(this);
        initView();
        initData();
        setListener();
    }

    @SuppressLint("RxLeakedSubscription")
    private void initData() {
        isPauseOrFinish=getIntent().getBooleanExtra("isPauseOrFinish",false);
        username = getIntent().getStringExtra("username");
        roomId = getIntent().getStringExtra("roomId");
        roomType = getIntent().getStringExtra("roomType");
        blocker = getIntent().getStringExtra("blocker");
        showLockUser = getIntent().getBooleanExtra("showLockUser", false);
        showGroupOperation = getIntent().getBooleanExtra("showGroupOperation", false);
        hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        methodCallHelper = new MethodCallHelper(this, hostname);
        getUserByUsername().subscribe(user -> {
            clickUser = user;
        }, RCLog::e);
        getCurrentUser().subscribe(user -> {
            loginUser = user;
        }, RCLog::e);

        onRenderCurrentUser();
        showInfoMsg();
        showLockAndGroupUI();
        if(showLockUser){
            gv_opration.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 3.如所查用户active为false，音视频会话按钮灰色不可点击
     4.设置群主需set-owner权限
     5.禁言需mute-user权限
     6.删除用户需remove-user权限
     7.设置管理员需set-moderator权限
     */
    private void showLockAndGroupUI() {
        String username = loginUser.getUsername();
        // 不需要显示音视频聊天按钮
        if(username == null || username.equals(clickUser.getUsername()) || showLockUser ){
            include_info.setVisibility(View.GONE);
        }
        if(showLockUser){
            ll_lock.setVisibility(View.VISIBLE);
            if("true".equals(blocker)){
                s_lock.setChecked(true);
            }
            findViewById(R.id.include_group_opr).setVisibility(View.GONE);
            return;
        }

        if(showGroupOperation) {
            findViewById(R.id.include_group_opr).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.include_group_opr).setVisibility(View.GONE);
            return;
        }

        setOwnerAndManager();
        setMuted();

        // 通过sub表查看是否有群主 管理员权限-----设置群主 管理员 禁言
        RealmSubscriptionRepository subscriptionRepository = new RealmSubscriptionRepository(hostname);
        Subscription subscriptionRid = subscriptionRepository.getByIdSub(roomId);
        List<String> roles = subscriptionRid.getRoles();

        View ll_set_group = findViewById(R.id.ll_set_group);
        View ll_set_manager = findViewById(R.id.ll_set_manager);
        View ll_lock_speak = findViewById(R.id.ll_lock_speak);
        if(roles != null && roles.size() > 0 && (roles.contains(RocketChatConstants.OWNER) || roles.contains(RocketChatConstants.MODERATOR))) {
            ll_set_group.setVisibility(View.VISIBLE);
            ll_set_manager.setVisibility(View.VISIBLE);
            ll_lock_speak.setVisibility(View.VISIBLE);
            if (!clickUser.getUsername().equals(username))
                tv_remove_from_group.setVisibility(View.VISIBLE);
        }else {
            ll_set_group.setVisibility(hasPermission(PermissionsConstants.SET_OWNER, roles) ? View.VISIBLE : View.GONE);
            ll_set_manager.setVisibility(hasPermission(PermissionsConstants.SET_MODERATOR, roles) ? View.VISIBLE : View.GONE);
            ll_lock_speak.setVisibility(hasPermission(PermissionsConstants.MUTE_USER, roles) ? View.VISIBLE : View.GONE);
            tv_remove_from_group.setVisibility((hasPermission(PermissionsConstants.DELETE_USER, roles)&&
                    !clickUser.getUsername().equals(username))?View.VISIBLE:View.GONE);
        }
    }

    private void setMuted() {
        RealmRoomRepository realmRoomRepository = new RealmRoomRepository(hostname);
        Room roomByRoomId = realmRoomRepository.getRoomByRoomId(roomId);
        if(roomByRoomId == null)return;
        List<String> muted = roomByRoomId.getMuted();
        if(muted != null && muted.contains(username)){
            s_muteUser.setChecked(true);
        }
    }

    /**
     * //设置是否是群主和管理员
     */
    private void setOwnerAndManager() {
        RealmRoomRoleRepository realmRoomRoleRepository = new RealmRoomRoleRepository(hostname);
        List<RoomRole> roomRoleAll = realmRoomRoleRepository.getRoomRoleAll(roomId);
        if(roomRoleAll == null || roomRoleAll.isEmpty()) return;

        for(RoomRole roomRoles : roomRoleAll){
            String id = roomRoles.getUser().getId();
            if(!TextUtils.isEmpty(id) && id.equals(clickUser.getId())){
                List<Role> roles = roomRoles.getRoles();
                if(roles == null) continue;
                String str = "";
                for (int i = 0; i < roles.size(); i++) {
                    str += roles.get(i).getName();
                }
                if(str.contains(RocketChatConstants.OWNER)){
                    s_set_group.setChecked(true);
                }
                if(str.contains(RocketChatConstants.MODERATOR)){
                    s_set_manager.setChecked(true);
                }
                break;
            }
        }
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        tv_phone.setOnClickListener(this);
        tv_landline.setOnClickListener(this);
        ll_group_message.setOnClickListener(this);
        if (isPauseOrFinish){
            s_lock.setClickable(false);
            s_set_manager.setClickable(false);
            s_set_group.setClickable(false);
            s_muteUser.setClickable(false);
        }
        ll_video.setOnClickListener(this);
        ll_audio.setOnClickListener(this);

        s_lock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(ignoreCurrentChecked) {
                ignoreCurrentChecked = false;
                return;
            }
            blockUserForMobile(isChecked);
        });
        s_set_manager.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(ignoreCurrentChecked) {
                ignoreCurrentChecked = false;
                return;
            }
            setModerator(isChecked);
        });
        s_set_group.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(ignoreCurrentChecked) {
                ignoreCurrentChecked = false;
                return;
            }
            setOwner(isChecked);
        });
        s_muteUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(ignoreCurrentChecked) {
                ignoreCurrentChecked = false;
                return;
            }
            muteUser(isChecked);
        });

        OprationAdapter oprationAdapter = new OprationAdapter(this, RocketChatConstants.D);
        gv_opration.setAdapter(oprationAdapter);

        gv_opration.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent;
            switch (position) {
                case 1:
                    intent = new Intent(MyInfoActivity.this, FuJianActivity.class);
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("roomId", roomId);
                    startActivity(intent);
                    break;
                case 0:// 搜索
                    intent = new Intent(MyInfoActivity.this, SearchChatInfoActivity.class);
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("roomId", roomId);
                    startActivityForResult(intent, LOOK_HISTORY_MESSAGE);
                    break;
                case 2:
                    intent = new Intent(MyInfoActivity.this, CunDangListActivity.class);
                    intent.putExtra("hostname", hostname);
                    intent.putExtra("companyCode", RocketChatCache.INSTANCE.getCompanyId());
                    startActivity(intent);
                    break;
            }
        });
        tv_remove_from_group.setOnClickListener(view -> {
            if(isPauseOrFinish){
                ToastUtils.showToast(getString(R.string.meetting_pauseorfinish_cannt_operate));
                return;
            }
            WeiNingAlertDialog.Builder builder = new WeiNingAlertDialog.Builder(this);

            dialog = builder.setTitle("提示")
                    .setTip("确定从频道中移除吗？")
                    .addViewOnclick(new WeiNingAlertDialog.OnClickListener() {
                        @Override
                        public void cancelClick() {
                            dialog.dismiss();
                        }

                        @Override
                        public void conFirmClick() {
                            methodCallHelper.removeUser(roomId, username).continueWithTask(new Continuation<String, Task<Object>>() {
                                @Override
                                public Task<Object> then(Task<String> task) throws Exception {
                                    if (task.getError() != null) {
                                        RCLog.e("removeUser: result=" + task.getError().getMessage(), true);
                                    }else{
                                        ToastUtils.showToast("操作成功");
                                        observer = new CurrentUserObserver(weakReference.get(), hostname, RealmStore.getOrCreate(hostname));
                                        observer.register();
                                        setResult(Activity.RESULT_OK);
                                        finish();

                                    }

                                    return null;
                                }
                            }, Task.UI_THREAD_EXECUTOR);
                            dialog.dismiss();

                        }
                    }).build();
            dialog.show();
        });
    }
    WeiNingAlertDialog dialog;
    private void muteUser(boolean isChecked){
        if(clickUser == null) return;

        methodCallHelper.muteUser(roomId, clickUser.getId(), username, isChecked)
                .continueWithTask(task -> {
                    if (task.isFaulted() || task.getError() != null) {
                        ToastUtils.showToast(task.getError()+"");
                        ignoreCurrentChecked = true;
                        s_muteUser.setChecked(!isChecked);
                        return null;
                    }
                    ToastUtils.showToast(getString(R.string.update_finish));
                    return task;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private void setModerator(boolean isChecked){
        if(clickUser == null) return;
        methodCallHelper.setModerator(roomId, clickUser.getId(), isChecked)
                .continueWithTask(task -> {
                    if (task.isFaulted() || task.getError() != null) {
                        ToastUtils.showToast(getString(R.string.update_fail));
                        ignoreCurrentChecked = true;
                        s_set_manager.setChecked(!isChecked);
                        return null;
                    }else {
                        methodCallHelper.getRoomRoles(roomId);
                        ToastUtils.showToast(getString(R.string.update_finish));
                    }

                    return task;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private void setOwner(boolean isChecked){
        if(clickUser == null) return;
        methodCallHelper.setOwner(roomId, clickUser.getId(), isChecked)
                .continueWithTask(task -> {
                    if (task.isFaulted() || task.getError() != null) {
                        ignoreCurrentChecked = true;
                        s_set_group.setChecked(!isChecked);
                        ToastUtils.showToast(getString(R.string.update_fail));
                        return null;
                    }else {
                        methodCallHelper.getRoomRoles(roomId);
                        ToastUtils.showToast(getString(R.string.update_finish));
                    }
                    return task;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private boolean ignoreCurrentChecked;
    private void blockUserForMobile(boolean isChecked){
        if(clickUser == null) return;
        methodCallHelper.blockUserForMobile(roomId, clickUser.getId(), isChecked)
                .continueWithTask(task -> {
                    if (task.isFaulted() || task.getError() != null) {
                        ToastUtils.showToast(task.getError()+"");
                        ignoreCurrentChecked = true;
                        s_lock.setChecked(!isChecked);
                        return null;
                    }
                    ToastUtils.showToast(getString(R.string.update_finish));
                    return task;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private void initView() {
        TextView tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        findViewById(R.id.tv_create).setVisibility(View.GONE);

        tv_name = findViewById(R.id.tv_name);
        tv_phone = findViewById(R.id.tv_phone);
        tv_company = findViewById(R.id.tv_company);
        tv_dept = findViewById(R.id.tv_dept);//部门
        tv_position = findViewById(R.id.tv_position);//职位
        tv_role = findViewById(R.id.tv_role);//角色
        tv_current_time = findViewById(R.id.tv_current_time);
        tv_create_time = findViewById(R.id.tv_create_time);
        tv_last_login = findViewById(R.id.tv_last_login);
        tv_remove_from_group = findViewById(R.id.tv_remove_from_group);
        s_lock = findViewById(R.id.s_lock);
        ll_lock = findViewById(R.id.ll_lock);
        tv_landline = findViewById(R.id.tv_landline);

        s_set_manager = findViewById(R.id.s_set_manager);
        s_set_group = findViewById(R.id.s_set_group);
        s_muteUser = findViewById(R.id.s_lock_speak);//禁言

        ll_group_message = findViewById(R.id.ll_message);
        ll_user_info = findViewById(R.id.ll_user_info);
        include_info = findViewById(R.id.include_info);
        gv_opration = findViewById(R.id.gv_opration);

        ll_video = findViewById(R.id.ll_video);
        ll_audio = findViewById(R.id.ll_audio);

        tv_title.setText(R.string.info_owner);
    }

    private void showInfoMsg() {
        if (clickUser == null){
            ll_user_info.setVisibility(View.GONE);
            return;}
        tv_name.setText(clickUser.getRealName());

        showMobile(clickUser.getMobile(), findViewById(R.id.ll_phone), tv_phone);//手机
        showMobile(clickUser.getPhone(), findViewById(R.id.ll_landline), tv_landline);//座机
        showMobile(clickUser.getCompanyName(), findViewById(R.id.ll_company), tv_company);

        showMobile(DateTime.fromEpocMs(clickUser.get_updatedAt(), DateTime.Format.DATE_TIME3), findViewById(R.id.ll_last_login), tv_last_login);
        showMobile(DateTime.fromEpocMs(clickUser.getCreatedAt(), DateTime.Format.DATE_TIME3), findViewById(R.id.ll_create_time), tv_create_time);

        List<DeptRole> deptRole = clickUser.getDeptRole();
        String companyId = loginUser.getCompanyId();

        boolean hasMatchOk = false;
//        if (deptRole != null && deptRole.size() > 0) {
//            for (DeptRole dept : deptRole){
//                if(companyId!=null&&companyId.equalsIgnoreCase(dept.getOrg_code())){
//                    tv_dept.setText(dept.getOrg_name());
//                    String zhiWei = dept.getPos_name();
//                    if(zhiWei != null && zhiWei.contains("_")){
//                        zhiWei = zhiWei.split("_")[1];
//                    }
//                    tv_position.setText(zhiWei);
//                    hasMatchOk = true;
//                }
//            }
//            if(!hasMatchOk){
//                tv_dept.setText(deptRole.get(0).getOrg_name());
//                String zhiWei = deptRole.get(0).getPos_name();
//                if(zhiWei != null && zhiWei.contains("_")){
//                    zhiWei = zhiWei.split("_")[1];
//                }
//                tv_position.setText(zhiWei);
//            }
//        }else{
//            findViewById(R.id.ll_dept).setVisibility(View.GONE);
//            findViewById(R.id.ll_position).setVisibility(View.GONE);
//        }
        if (!TextUtils.isEmpty(clickUser.getOrgName()))
            tv_dept.setText(clickUser.getOrgName());
        else
            findViewById(R.id.ll_dept).setVisibility(View.GONE);

        if (!TextUtils.isEmpty(clickUser.getJobName()))
            tv_position.setText(clickUser.getJobName());
        else
            findViewById(R.id.ll_position).setVisibility(View.GONE);
        List<String> roles = clickUser.getRoles();
        String str = "";
        if (roles != null && roles.size() > 0) {
            for (String s : roles) {
                str += transRole(s) + "/";
            }
            str = str.substring(0, str.length() - 1);
            tv_role.setText(str);
        }else{
            findViewById(R.id.ll_role).setVisibility(View.GONE);
        }
    }

    private String transRole(String role){
        String s="";
        if (role.equalsIgnoreCase(RocketChatConstants.USER)){
            s="普通用户";
        }else if(role.equalsIgnoreCase(RocketChatConstants.ADMIN)){
            s="超级管理员";
        }else if(role.equalsIgnoreCase(RocketChatConstants.OWNER)){
            s="群主";
        }else if(role.equalsIgnoreCase(RocketChatConstants.CADMIN) || role.equalsIgnoreCase(RocketChatConstants.COMPANYADMIN)){
            s="子公司管理员";
        }else if(role.equalsIgnoreCase(RocketChatConstants.GADMIN) || role.equalsIgnoreCase(RocketChatConstants.GROUPADMIN)){
            s="集团管理员";
        }else if(role.equalsIgnoreCase(RocketChatConstants.MODERATOR)){
            s="管理员";
        }else if(role.equalsIgnoreCase(RocketChatConstants.FACILITATORADMIN) || role.equalsIgnoreCase(RocketChatConstants.FADMIN)){
            s="分公司管理员";
        }else if(role.equalsIgnoreCase(RocketChatConstants.EXECUTIVE)){
            s="行政人员";
        }else if(role.equalsIgnoreCase(RocketChatConstants.ASSIGN_ROLES_ADMIN)){
            s="可分配此角色的管理员";
        }else if(role.equalsIgnoreCase(RocketChatConstants.BOT)){
            s="系统用户";
        }
        return s;
    }
    private void showMobile(String str, View viewById, TextView textView) {
        if (TextUtils.isEmpty(str)) {
            viewById.setVisibility(View.GONE);
        }else{
            viewById.setVisibility(View.VISIBLE);
            textView.setText(str);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_phone:
                telePhone = tv_phone.getText().toString();
                call(telePhone);
                break;
            case R.id.tv_landline:
                telePhone = tv_landline.getText().toString();
                call(telePhone);
                break;
            case R.id.ll_message://建立点对点消息
                RocketChatCache.INSTANCE.setIsClickInfoMsg(username+";"+loginUser.getId());
                setResult(Activity.RESULT_OK);
                finish();
                break;
            case R.id.ll_video:
                if(!TempFileUtils.getInstance().getTalkingStatus()){
                    RocketChatCache.INSTANCE.setIsClickInfoMsg(username+";"+loginUser.getId());
                    setResult(Activity.RESULT_OK);
                    LaunchUtil.showVideoActivity(clickUser.getId(),clickUser.getUsername(),clickUser.getAvatar(),roomId,true,true);
                }
                finish();
                break;
            case R.id.ll_audio:
                if(!TempFileUtils.getInstance().getTalkingStatus()){
                    RocketChatCache.INSTANCE.setIsClickInfoMsg(username+";"+loginUser.getId());
                    setResult(Activity.RESULT_OK);
                    LaunchUtil.showVideoActivity(clickUser.getId(),clickUser.getUsername(),clickUser.getAvatar(),roomId,true,false);
                }
                finish();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOOK_HISTORY_MESSAGE && resultCode == Activity.RESULT_OK){
            finish();
        }
    }

    /**
     * 调用拨号功能
     * @param phone 电话号码
     */
    private void call(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void onRenderCurrentUser() {
        if (clickUser != null) {
            UserRenderer userRenderer = new UserRenderer(clickUser);
            userRenderer.showAvatar(findViewById(R.id.current_user_avatar), hostname);
            userRenderer.showUsername(findViewById(R.id.current_user_name));
            userRenderer.showStatusColor(findViewById(R.id.current_user_status));
            userRenderer.showStatusInfo(findViewById(R.id.tv_user_status));
        }
    }

    private Single<User> getCurrentUser() {
        RealmUserRepository userRepository = new RealmUserRepository(hostname);
        return userRepository.getCurrent()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }

    private Single<User> getUserByUsername() {
        RealmUserRepository userRepository = new RealmUserRepository(hostname);
        return userRepository.getByUsername(username)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }

    @SuppressLint("RxLeakedSubscription")
    private boolean hasPermission(String permissionType, List<String> roles) {
        if(roles == null || roles.isEmpty()) return false;

        List<String> temp = setUserToJoinedRoomList(permissionType);
        return !Collections.disjoint(roles, temp);
    }

    @SuppressLint("RxLeakedSubscription")
    private List<String> setUserToJoinedRoomList(String permissionType){
        List<String> addUserToJoinedRoom = new ArrayList<>();
        RealmPermissionRepository permissionRepository = new RealmPermissionRepository(hostname);
        permissionRepository.getById(permissionType)
                .subscribe(permissionOptional -> {
                    List<Role> createWroles = permissionOptional.get().getRoles();
                    for (Role role : createWroles) {
                        addUserToJoinedRoom.add(role.getName());
                    }
                }, RCLog::e);
        return addUserToJoinedRoom;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (observer!=null)
            observer.unregister();
    }
}
