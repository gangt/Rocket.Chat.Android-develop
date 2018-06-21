package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.GroupOnlineAdapter;
import chat.rocket.android.adapter.MoreMemberAdapter;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.Role;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomRole;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRoleRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import indexablerv.IndexableLayout;
import indexablerv.PinyinUtil;
import indexablerv.SimpleHeaderAdapter;

/**
 * Created by zhangxiugao on 2018/1/8
 * 选择全部人员
 */

public class MoreGroupMemberActivity extends BusinessBaseActivity implements View.OnClickListener {

    private static final int SELECT_USER = 1;
    private TextView tv_title, tv_sort, tv_count, tv_manager_count;
    private ImageView iv_back, iv_sort, iv_add;
    private EditText et_search;
    private ImageView clear;
    private MoreMemberAdapter mAdapter;
    private ArrayList<UserEntity> allDataList;
    private List<UserEntity> managerDataList;
    private IndexableLayout indexableLayout;
    private RecyclerView rv_list;
    private GroupOnlineAdapter onlineAdapter;
    private int currentSortType = 0;
    private boolean isRemind;
    private boolean isAlert;
    private boolean showGroupOperation;
    private String roomId, hostname;
    private Room room;
    private List<String> mutedUsers;
    private ArrayList<UserEntity> selectListResult;
    private List<UserEntity> otherDataList;
    private boolean isPauseOrFinish;
    private LinearLayout ll_alt, ll_alt_all, ll_alt_here;
    WeakReference<Context> contextWeakReference;
    CurrentUserObserver observer;
    private SimpleHeaderAdapter<UserEntity> mHotCityAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_search_person);
        contextWeakReference = new WeakReference<Context>(this);
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        iv_sort.setOnClickListener(this);
        iv_add.setOnClickListener(this);
        clear.setOnClickListener(this);
        ll_alt_all.setOnClickListener(this);
        ll_alt_here.setOnClickListener(this);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String term = s.toString();
                if (currentSortType == 0) {
                    filterSearchData(term, otherDataList,managerDataList);
                } else {
                    filterSearchData(term, allDataList,managerDataList);
                }
            }
        });
    }

    private void filterSearchData(String term, List<UserEntity> dataList, List<UserEntity> managerDataList) {
        List<UserEntity> tempDataList = new ArrayList<>();
        if (term.length() > 0) {
            clear.setVisibility(View.VISIBLE);
            for (UserEntity entity : dataList) {
                String pinyin = entity.getPinyin();
                String realName = entity.getRealName();
                if (pinyin == null || realName == null) continue;
                if (pinyin.toLowerCase().contains(term.toLowerCase()) || realName.contains(term)) {
                    tempDataList.add(entity);
                }
            }
            for (UserEntity entity : managerDataList) {
                String pinyin = entity.getPinyin();
                String realName = entity.getRealName();
                if (pinyin == null || realName == null) continue;
                if (pinyin.toLowerCase().contains(term.toLowerCase()) || realName.contains(term)) {
                    tempDataList.add(entity);
                }
            }
            if (mHotCityAdapter != null && indexableLayout != null) {
                indexableLayout.removeHeaderAdapter(mHotCityAdapter);
            }

        } else {
            if (mHotCityAdapter != null && indexableLayout != null) {
                indexableLayout.removeHeaderAdapter(mHotCityAdapter);
                indexableLayout.addHeaderAdapter(mHotCityAdapter);
            }
            clear.setVisibility(View.GONE);
            tempDataList.addAll(dataList);
        }
        if (mAdapter != null && currentSortType == 0) {
            mAdapter.setDatas(tempDataList);
        }
        if (onlineAdapter != null && (currentSortType == 1 || currentSortType == 2)) {
            onlineAdapter.setDatas(tempDataList);
            onlineAdapter.notifyDataSetChanged();
        }
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        tv_sort = findViewById(R.id.tv_sort);
        tv_count = findViewById(R.id.tv_count);
        tv_manager_count = findViewById(R.id.tv_manager_count);
        iv_sort = findViewById(R.id.iv_sort);
        iv_add = findViewById(R.id.iv_add);
        et_search = findViewById(R.id.et_search);
        clear = findViewById(R.id.clear);
        indexableLayout = findViewById(R.id.indexableLayout);
        ll_alt = findViewById(R.id.ll_alt);
        ll_alt_all = findViewById(R.id.ll_alt_all);
        ll_alt_here = findViewById(R.id.ll_alt_here);
        rv_list = findViewById(R.id.rv_list);
        et_search.setHint(R.string.search_current_user);
    }

    private void initData() {
        isPauseOrFinish = getIntent().getBooleanExtra("isPauseOrFinish", false);
        hostname = getIntent().getStringExtra("hostname");
        roomId = getIntent().getStringExtra("roomId");
        isRemind = getIntent().getBooleanExtra("remind", false);
        isAlert = getIntent().getBooleanExtra("isAlert", false);
        boolean hasAddDeletePermission = getIntent().getBooleanExtra("hasAddDeletePermission", false);
        showGroupOperation = getIntent().getBooleanExtra("showGroupOperation", false);
        if (isRemind) {
            ll_alt.setVisibility(View.VISIBLE);
            iv_add.setVisibility(View.GONE);
            iv_sort.setVisibility(View.GONE);
            tv_title.setText(R.string.select_remind);
            tv_sort.setVisibility(View.GONE);
            rv_list.setVisibility(View.VISIBLE);
            tv_manager_count.setVisibility(View.VISIBLE);
            setManagerList();
            String indexTitle = getString(R.string.group_member_count, managerDataList.size() + "");
            tv_manager_count.setText(indexTitle);
            otherDataList = getIntent().getParcelableArrayListExtra("selectList");
            allDataList = getIntent().getParcelableArrayListExtra("selectList");
            setOnlineCount();
            setDefaultAdapter();
            setObjAdapter();
        } else {
            tv_sort.setVisibility(View.VISIBLE);
            iv_add.setVisibility(View.VISIBLE);
            iv_sort.setVisibility(View.VISIBLE);
            tv_title.setText(R.string.group_member);

            getRoom();
        }

        //判断权限iv_add
        if (!hasAddDeletePermission) {
            iv_add.setVisibility(View.GONE);
        }
    }

    @SuppressLint("RxLeakedSubscription")
    private void getRoom() {
        RealmRoomRepository roomRepository = new RealmRoomRepository(hostname);
        this.room = roomRepository.getByRoomId(roomId);
        if (room == null) return;
        mutedUsers = room.getMuted();

        setManagerList();
        setOtherDataList();
        setAllDataList();
        setOnlineCount();
        setDefaultAdapter();

//        roomRepository.getByIdAll(roomId)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(room -> {
//                    this.room = room;
//
//                }, Logger.INSTANCE::report);
    }

    private void setAllDataList() {
        if (allDataList == null) {
            allDataList = new ArrayList<>();
        } else {
            allDataList.clear();
        }

        allDataList.addAll(managerDataList);
        allDataList.addAll(otherDataList);
    }

    /**
     * 搜索所有的user，去掉群主，管理员
     */
    private void setOtherDataList() {
        RealmUserRepository userRepository = new RealmUserRepository(hostname);
        List<String> usernames = room.getUsernames();
        String[] str = new String[]{};
        if (usernames != null && usernames.size() > 0) {
            /*将自己的频道添加到列表中**/
            String userUsername = RocketChatCache.INSTANCE.getUserUsername();
            if (!usernames.contains(userUsername) && !managerDataList.contains(new UserEntity(userUsername))) {
                usernames.add(userUsername);
            }
            str = usernames.toArray(new String[0]);
        }
        List<User> temp = userRepository.getByNameAll(str);
        setOtherDataList(temp);
    }

    private void setManagerList() {
        RealmRoomRoleRepository realmRoomRoleRepository = new RealmRoomRoleRepository(hostname);
        List<RoomRole> roomRoleAll = realmRoomRoleRepository.getRoomRoleAll(roomId);
        if (managerDataList == null) {
            managerDataList = new ArrayList<>();
        } else {
            managerDataList.clear();
        }
        if (roomRoleAll == null || roomRoleAll.isEmpty()) return;
        for (RoomRole roomRole : roomRoleAll) {
            User user = roomRole.getUser();
            UserEntity entity = new UserEntity(false, user.getRealName(), user.getUsername());
            entity.setCompanyName(user.getCompanyName());
            entity.setStatus(user.getStatus());
            entity.setAvatar(user.getAvatar());
            entity.setLastOnlineTime(user.get_updatedAt());
            entity.setPinyin(PinyinUtil.getPingYin(user.getRealName()));
            List<DeptRole> deptRole = user.getDeptRole();
            if (deptRole != null && deptRole.size() > 0) {
                entity.setDept(deptRole.get(0).getOrg_name());
                entity.setZhiWei(deptRole.get(0).getPos_name().split("_")[1]);
            }
            List<Role> roles = roomRole.getRoles();
            if (roles != null && roles.size() > 0) {
                String str = "";
                for (int i = 0; i < roles.size(); i++) {
                    str += roles.get(i).getName();
                }
                if (str.contains(RocketChatConstants.OWNER)) {
                    entity.setOwner(true);
                }
                if (str.contains(RocketChatConstants.MODERATOR)) {
                    entity.setManager(true);
                }
            }
            if (mutedUsers != null && mutedUsers.contains(user.getUsername())) {
                entity.setMute(true);
            }

            if (!isAdmin(user) || "true".equals(user.getMaster()))
                managerDataList.add(entity);
        }
    }

    private void setOtherDataList(List<User> temp) {
        otherDataList = new ArrayList<>();

        if (TextUtils.isEmpty(temp)) return;
        for (User user : temp) {
            String realName = user.getRealName();
            if ("小翌".equals(realName) || "小翌".equals(user.getUsername()) || isAdmin(user) || "true".equals(user.getMaster())) {
                continue;
            }

            UserEntity entity = new UserEntity(false, user.getRealName(), user.getUsername());
            entity.setCompanyName(user.getCompanyName());
            entity.setStatus(user.getStatus());
            entity.setAvatar(user.getAvatar());
            entity.setLastOnlineTime(user.get_updatedAt());
            List<DeptRole> deptRole = user.getDeptRole();
            if (deptRole != null && deptRole.size() > 0) {
                entity.setDept(deptRole.get(0).getOrg_name());
                entity.setZhiWei(deptRole.get(0).getPos_name().split("_")[1]);
            }
            if (mutedUsers != null && mutedUsers.contains(user.getUsername())) {
                entity.setMute(true);
            }

            otherDataList.add(entity);
        }
        otherDataList.removeAll(managerDataList);
    }

    private boolean isAdmin(User user) {
        List<String> roles = user.getRoles();
        if (roles == null || roles.size() == 0) return false;
        for (String s : roles) {
            if (RocketChatConstants.ADMIN.equals(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void setDefaultAdapter() {
        currentSortType = 0;
        et_search.setText("");
        indexableLayout.setLayoutManager(new LinearLayoutManager(this));

        if (mAdapter == null) {
            mAdapter = new MoreMemberAdapter(this);
            indexableLayout.setAdapter(mAdapter);
            // set Material Design OverlayView
            indexableLayout.setOverlayStyle_MaterialDesign(Color.RED);
            // 全字母排序。  排序规则设置为：每个字母都会进行比较排序；速度较慢
            indexableLayout.setCompareMode(IndexableLayout.MODE_FAST);

            if (!isRemind) {
                String indexTitle = getString(R.string.group_member_count, managerDataList.size() + "");
                mHotCityAdapter = new SimpleHeaderAdapter<UserEntity>(mAdapter, "", indexTitle, managerDataList);
                indexableLayout.addHeaderAdapter(mHotCityAdapter);
            }

            mAdapter.setDatas(otherDataList);
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyDataSetChanged();
        }
        mAdapter.setOnItemClickListener((v, entity) -> {
            if (isRemind) {
                Intent intent = new Intent();
                intent.putExtra("user", entity);
                intent.putExtra("isAlert", isAlert);
                setResult(1000, intent);
                finish();
            } else {
                Intent intent = new Intent(MoreGroupMemberActivity.this, MyInfoActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("username", entity.getUsername());
                intent.putExtra("blocker", getIntent().getStringExtra("blocker"));
                intent.putExtra("showGroupOperation", showGroupOperation);
                startActivityForResult(intent, 1);
            }
        });

        tv_sort.setText(R.string.default_sort);
        indexableLayout.setVisibility(View.VISIBLE);
        rv_list.setVisibility(View.GONE);
    }

    private void setOnlineCount() {
        int onlineCount = 0;
        for (int i = 0; i < allDataList.size(); i++) {
            if (User.STATUS_ONLINE.equals(allDataList.get(i).getStatus())) {
                onlineCount++;
            }
        }
        tv_count.setText(onlineCount + "/" + allDataList.size());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add:
                if (isPauseOrFinish) {
                    ToastUtils.showToast(getString(R.string.meetting_pauseorfinish_cannt_operate));
                    return;
                }
                Intent intent = new Intent(this, OrgSelectUserActivity.class);
                intent.putExtra("isFromGroupInfo",true);
                intent.putExtra("hostname", hostname);
                intent.putExtra("existUser", loadLocalUserStr());
                intent.putExtra("roomId", roomId);
                intent.putExtra("type", room.getType());
                intent.putExtra("companyId", RocketChatCache.INSTANCE.getCompanyId());
                startActivityForResult(intent, SELECT_USER);
                break;
            case R.id.iv_sort:
                showPopwindow();
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.clear:
                et_search.setText("");
                break;
            case R.id.ll_alt_all:
                Intent intent2 = new Intent();
                intent2.putExtra("isAlert", isAlert);
                intent2.putExtra("isAll", true);
                setResult(1000, intent2);
                finish();
                break;
            case R.id.ll_alt_here:
                Intent intent3 = new Intent();
                intent3.putExtra("isAlert", isAlert);
                intent3.putExtra("isHere", true);
                setResult(1000, intent3);
                finish();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if (selectListResult == null || selectListResult.size() == 0) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("selectList", selectListResult);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private String[] loadLocalUserStr() {
        String[] str = new String[allDataList.size()];
        if (TextUtils.isEmpty(allDataList)) return str;
        for (int i = 0; i < allDataList.size(); i++) {
            str[i] = allDataList.get(i).getUsername();
        }
        return str;
    }

    private PopupWindow popupWindow;

    private void showPopwindow() {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(this);
            popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            View view = LayoutInflater.from(this).inflate(R.layout.group_sort_pop, null);
            view.findViewById(R.id.tv_defaultSort).setOnClickListener(v -> {
                if (currentSortType != 0) {
                    tv_manager_count.setVisibility(View.GONE);
//                    setManagerList();
//                    setOtherUser();
                    setDefaultAdapter();
                }
                popupWindow.dismiss();
            });
            view.findViewById(R.id.tv_onlineSort).setOnClickListener(v -> {
                if (currentSortType != 1) {
                    setOnlineAdapter();
                }
                popupWindow.dismiss();
            });
            view.findViewById(R.id.tv_timeSort).setOnClickListener(v -> {
                if (currentSortType != 2) {
                    setTimeSortAdapter();
                }
                popupWindow.dismiss();
            });
            popupWindow.setContentView(view);
        }
        setBackgroundAlpha(0.6f);//设置屏幕透明度
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setFocusable(true);
        popupWindow.setOnDismissListener(() -> {
            // popupWindow隐藏时恢复屏幕正常透明度
            setBackgroundAlpha(1.0f);
        });
        popupWindow.showAsDropDown(iv_sort);
    }

    private void setOnlineAdapter() {
        tv_sort.setText(R.string.online_sort);
        currentSortType = 1;
        et_search.setText("");
        List<UserEntity> onlineList = new ArrayList<>();
        List<UserEntity> offlineList = new ArrayList<>();
        List<UserEntity> otherList = new ArrayList<>();
        for (UserEntity entity : allDataList) {
            if (User.STATUS_ONLINE.equals(entity.getStatus())) {
                onlineList.add(entity);
            } else if (User.STATUS_OFFLINE.equals(entity.getStatus())) {
                offlineList.add(entity);
            } else {
                otherList.add(entity);
            }
        }
        allDataList.clear();
        allDataList.addAll(onlineList);
        allDataList.addAll(otherList);
        allDataList.addAll(offlineList);
        setObjAdapter();
    }

    private void setObjAdapter() {
        allDataList.removeAll(managerDataList);
        allDataList.addAll(0, managerDataList);

        tv_manager_count.setVisibility(View.VISIBLE);
        String indexTitle = getString(R.string.group_member_count, managerDataList.size() + "");
        tv_manager_count.setText(indexTitle);

        indexableLayout.setVisibility(View.GONE);
        rv_list.setVisibility(View.VISIBLE);

//        if(onlineAdapter == null) {
        onlineAdapter = new GroupOnlineAdapter(this, allDataList);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
        rv_list.setAdapter(onlineAdapter);
        onlineAdapter.setOnItemClickListener(new GroupOnlineAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MoreGroupMemberActivity.this, MyInfoActivity.class);
                intent.putExtra("showGroupOperation", showGroupOperation);
                intent.putExtra("roomId", roomId);
                intent.putExtra("username", allDataList.get(position).getUsername());
                intent.putExtra("blocker", getIntent().getStringExtra("blocker"));
                intent.putExtra("isPauseOrFinish", isPauseOrFinish);
                startActivityForResult(intent, 1);
            }
        });
//        }else{
//            onlineAdapter.notifyDataSetChanged();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_USER && resultCode == Activity.RESULT_OK && data != null) {
            observer = new CurrentUserObserver(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
            observer.register();
            selectListResult = data.getParcelableArrayListExtra("selectList");
            if (selectListResult.isEmpty()) return;
            List<String> names=new ArrayList<>();
            for (UserEntity user:allDataList){
                names.add(user.getUsername());
            }
            for (UserEntity entity:selectListResult){
                if (!allDataList.contains(entity)&&!names.contains(entity.getUsername())){
                    otherDataList.add(entity);
                }
            }
            setAllDataList();

            setOnlineCount();
            if (currentSortType == 0) {
                tv_manager_count.setVisibility(View.GONE);
                setDefaultAdapter();
            } else if (currentSortType == 1) {
                setOnlineAdapter();
            } else if (currentSortType == 2) {
                setTimeSortAdapter();
            }
        } else if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void setTimeSortAdapter() {
        currentSortType = 2;
        et_search.setText("");
        tv_sort.setText(R.string.time_sort);
        sortByTimeList();
        setObjAdapter();
    }

    private void sortByTimeList() {
        Collections.sort(allDataList, (o1, o2) -> {
            if (o1.getLastOnlineTime() > o2.getLastOnlineTime()) {
                return -1;
            } else {
                return 1;
            }
        });
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha 屏幕透明度0.0-1.0 1表示完全不透明
     */
    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (observer != null)
            observer.unregister();
    }
}
