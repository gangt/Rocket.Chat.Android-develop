package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.adapter.SelectedAlertAdapter;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import indexablerv.IndexableLayout;
import indexablerv.SelectedAlertHeadAdapter;

/**
 * Created by zhangxiugao on 2018/1/8
 * 选择全部人员
 */

public class SelectedAlertActivity extends BusinessBaseActivity implements View.OnClickListener {

    private static final int SELECT_USER = 1;
    private TextView tv_title;
    private ImageView iv_back;
    private EditText et_search;
    private ImageView clear;
    private SelectedAlertAdapter mAdapter;
    private ArrayList<UserEntity> allDataList;
    private List<UserEntity> managerDataList;
    private IndexableLayout indexableLayout;
    private RecyclerView rv_list;
    private int currentSortType = 0;
    private boolean isRemind;
    private boolean isAlert;
    private String roomId, hostname;
    private Room room;
    private List<String> mutedUsers;
    WeakReference<Context> contextWeakReference;
    private SelectedAlertHeadAdapter selectedAlertHeadAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_alert);
        contextWeakReference = new WeakReference<Context>(this);
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        clear.setOnClickListener(this);
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
                filterSearchData(term, allDataList);
            }
        });
    }

    private void filterSearchData(String term, List<UserEntity> dataList) {
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
            indexableLayout.removeHeaderAdapter(selectedAlertHeadAdapter);
        } else {
            clear.setVisibility(View.GONE);
            tempDataList.addAll(dataList);
            indexableLayout.addHeaderAdapter(selectedAlertHeadAdapter);
        }
        if (mAdapter != null && currentSortType == 0) {
            mAdapter.setDatas(tempDataList);
        }
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        et_search = findViewById(R.id.et_search);
        clear = findViewById(R.id.clear);
        indexableLayout = findViewById(R.id.indexableLayout);
        rv_list = findViewById(R.id.rv_list);
        et_search.setHint("搜索姓名");
    }

    private void initData() {
        hostname = getIntent().getStringExtra("hostname");
        roomId = getIntent().getStringExtra("roomId");
        isRemind = getIntent().getBooleanExtra("remind", false);
        isAlert = getIntent().getBooleanExtra("isAlert", false);

        tv_title.setText(R.string.select_remind);
        rv_list.setVisibility(View.VISIBLE);
        allDataList = getIntent().getParcelableArrayListExtra("selectList");
        getRoom();

    }

    @SuppressLint("RxLeakedSubscription")
    private void getRoom() {
        RealmRoomRepository roomRepository = new RealmRoomRepository(hostname);
        this.room = roomRepository.getByRoomId(roomId);
        if (room == null) return;
        mutedUsers = room.getMuted();
        setOtherDataList();
        setDefaultAdapter();
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


    private void setOtherDataList(List<User> temp) {
        allDataList = new ArrayList<>();

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
            entity.setUsername(user.getUsername());
            List<DeptRole> deptRole = user.getDeptRole();
            if (deptRole != null && deptRole.size() > 0) {
                entity.setDept(deptRole.get(0).getOrg_name());
                entity.setZhiWei(deptRole.get(0).getPos_name().split("_")[1]);
            }
            if (mutedUsers != null && mutedUsers.contains(user.getUsername())) {
                entity.setMute(true);
            }

            allDataList.add(entity);
        }
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
            mAdapter = new SelectedAlertAdapter(this);
            indexableLayout.setAdapter(mAdapter);
            // set Material Design OverlayView
            indexableLayout.setOverlayStyle_MaterialDesign(Color.RED);
            // 全字母排序。  排序规则设置为：每个字母都会进行比较排序；速度较慢
            indexableLayout.setCompareMode(IndexableLayout.MODE_FAST);
            List<String> data = new ArrayList<>();
            data.add("所有成员");
            data.add("在线成员");
            selectedAlertHeadAdapter = new SelectedAlertHeadAdapter("", null, data);
            indexableLayout.addHeaderAdapter(selectedAlertHeadAdapter);
//            SimpleHeaderAdapter<UserEntity> mHotCityAdapter = new SimpleHeaderAdapter<UserEntity>(mAdapter, "", "群组、管理员", managerDataList);
//            indexableLayout.addHeaderAdapter(mHotCityAdapter);
            mAdapter.setDatas(allDataList);
        }
        mAdapter.notifyDataSetChanged();
        selectedAlertHeadAdapter.setOnItemHeaderClickListener((v, currentPosition, entity) -> {
            String data = (String) entity;
            if (data.equals("所有成员")) {
                Intent intent2 = new Intent();
                UserEntity userEntity = new UserEntity();
                userEntity.setUsername("all");
                userEntity.setRealName("all");
                intent2.putExtra("isAlert", isAlert);
                intent2.putExtra("user", userEntity);
                setResult(1000, intent2);
                finish();
            } else {
                Intent intent3 = new Intent();
                UserEntity userEntity = new UserEntity();
                userEntity.setUsername("here");
                userEntity.setRealName("here");
                intent3.putExtra("isAlert", isAlert);
                intent3.putExtra("user", userEntity);
                setResult(1000, intent3);
                finish();
            }
        });
        mAdapter.setOnItemClickListener((v, entity) -> {
            Intent intent = new Intent();
            intent.putExtra("user", entity);
            intent.putExtra("isAlert", isAlert);
            setResult(1000, intent);
            finish();
        });

        indexableLayout.setVisibility(View.VISIBLE);
        rv_list.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.clear:
                et_search.setText("");
                break;
        }
    }

}
