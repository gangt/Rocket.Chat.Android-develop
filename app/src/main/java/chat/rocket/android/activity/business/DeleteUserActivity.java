package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.DeleteUserAdapter;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.dialog.WeiNingAlertDialog;
import chat.rocket.android.log.RCLog;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import indexablerv.IndexableLayout;
import indexablerv.PinyinUtil;

public class DeleteUserActivity extends BusinessBaseActivity implements View.OnClickListener{

    private TextView tv_title,tv_create;
    private ImageView iv_back,clear;
    private EditText et_search;
    private DeleteUserAdapter mAdapter;
    private ArrayList<UserEntity> allDataList;
    private String roomId,hostname;
    private WeiNingAlertDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);

        initView();
        initData();
        setListener();
        setAdapter();
    }

    @SuppressLint("RxLeakedSubscription")
    private void initData() {
        roomId = getIntent().getStringExtra("roomId");
        hostname = getIntent().getStringExtra("hostname");
        allDataList = getIntent().getParcelableArrayListExtra("deleteUser");
    }

    private void setAdapter() {
        IndexableLayout indexableLayout = findViewById(R.id.indexableLayout);
        indexableLayout.setLayoutManager(new LinearLayoutManager(this));

        if(mAdapter == null) {
            mAdapter = new DeleteUserAdapter(this);
            indexableLayout.setAdapter(mAdapter);
            // set Material Design OverlayView
            indexableLayout.setOverlayStyle_MaterialDesign(Color.RED);

            // 全字母排序。  排序规则设置为：每个字母都会进行比较排序；速度较慢
            indexableLayout.setCompareMode(IndexableLayout.MODE_FAST);
            mAdapter.setDatas(allDataList);
            mAdapter.notifyDataSetChanged();
        }else{
            mAdapter.notifyDataSetChanged();
        }
    }

//    private Room room;
//    @SuppressLint("RxLeakedSubscription")
//    private void getRoom(){
//        RealmRoomRepository roomRepository = new RealmRoomRepository(hostname);
//
//        roomRepository.getByIdAll(roomId)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(room -> {
//                    this.room = room;
//                    setUserEntity();
//                }, Logger.INSTANCE::report);
//    }

//    private void setUserEntity() {
//        RealmUserRepository userRepository = new RealmUserRepository(hostname);
//
//        List<String> usernames = room.getUsernames();
//        String[] str = new String[]{};
//        if(usernames != null && usernames.size() > 0){
//            str = usernames.toArray(new String[0]);
//        }
//        List<User> userAll = userRepository.getByNameAll(str);
//
//        if(allDataList == null){
//            allDataList = new ArrayList<>();
//        }
//
//        if(userAll == null || userAll.isEmpty()) return;
//        String loginUsername = RocketChatCache.INSTANCE.getUserUsername();
//        for (User user : userAll) {
//            String realName = user.getRealName();
//            String username = user.getUsername();
//
//            if(loginUsername != null && loginUsername.equals(username) || "小翌".equals(realName)){
//                continue;
//            }
//
//            UserEntity entity = new UserEntity(false, realName, username);
//            String pinyin = PinyinUtil.getPingYin(realName);
//            entity.setPinyin(pinyin);
//            entity.setCompanyName(user.getCompanyName());
//            List<DeptRole> deptRole = user.getDeptRole();
//            if (deptRole != null && deptRole.size() > 0) {
//                entity.setDept(deptRole.get(0).getOrg_name());
//                entity.setZhiWei(deptRole.get(0).getPos_name());
//            }
//
//            entity.setAvatar(user.getAvatar());
//            allDataList.add(entity);
//        }
//
//        setAdapter();
//    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        tv_create.setOnClickListener(this);
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
                filterSearchData(term);
            }
        });
    }

    private void filterSearchData(String term){
        String markPingyin = PinyinUtil.getPingYin(term);
        List<UserEntity> tempDataList = new ArrayList<>();
        if(term.length() > 0){
            clear.setVisibility(View.VISIBLE);
            for(UserEntity entity : allDataList){
                String pinyin = entity.getPinyin();
                if(pinyin != null && pinyin.startsWith(term) || pinyin.startsWith(term.toLowerCase())
                        || pinyin.startsWith(term.toUpperCase()) || pinyin.startsWith(markPingyin)){
                    tempDataList.add(entity);
                }
            }
        }else{
            clear.setVisibility(View.GONE);
            tempDataList.addAll(allDataList);
        }
        mAdapter.setDatas(tempDataList);
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        tv_create = findViewById(R.id.tv_create);
        et_search = findViewById(R.id.et_search);
        clear=findViewById(R.id.clear);
        et_search.setHint(getString(R.string.input_user));

        tv_create.setText(R.string.ok_cn);
        tv_title.setText(R.string.delete_member);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clear:
                et_search.setText("");
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_create:
                ArrayList<UserEntity> selectList = mAdapter.getSelectList();
                if(chat.rocket.android.helper.TextUtils.isEmpty(selectList)){
                    return;
                }
                WeiNingAlertDialog.Builder builder = new WeiNingAlertDialog.Builder(this);
                dialog = builder.setTitle("提示")
                        .setTip("确定删除成员吗？")
                        .addViewOnclick(new WeiNingAlertDialog.OnClickListener() {
                            @Override
                            public void cancelClick() {
                                dialog.dismiss();
                            }

                            @Override
                            public void conFirmClick() {
                                deleteUser();
                                dialog.dismiss();
                            }
                        }).build();
                dialog.show();
                break;
        }
    }

    private void deleteUser() {

        showProgressDialog();
        ArrayList<UserEntity> selectList = mAdapter.getSelectList();
        MethodCallHelper methodCallHelper = new MethodCallHelper(this, hostname);

        final String[] deleteFailedName = {""};
        final String[] deleteSuccessName = {""};
        for (int i = 0; i < selectList.size(); i++) {
            UserEntity entity = selectList.get(i);
            String username = entity.getUsername();
            int finalI = i;
            methodCallHelper.removeUser(roomId, username).continueWithTask(new Continuation<String, Task<Object>>() {
                        @Override
                        public Task<Object> then(Task<String> task) throws Exception {
                            if (task.getError() != null) {
                                RCLog.e("removeUser: result=" + task.getError().getMessage(), true);
                                deleteFailedName[0] += entity.getRealName() + " ";
                            }else{
                                deleteSuccessName[0] += entity.getUsername() + " ";
                            }

                            if(finalI == selectList.size() - 1){
                                dismissProgressDialog();
                                if(TextUtils.isEmpty(deleteFailedName[0])) {
                                    ToastUtils.showToast(getResources().getString(R.string.update_finish));
                                }else{
                                    ToastUtils.showToast(deleteFailedName[0] + "删除失败");
                                }
                                Intent intent = new Intent();
                                intent.putExtra("deleteSuccessName", deleteSuccessName[0]);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                            return null;
                        }
                    }, Task.UI_THREAD_EXECUTOR);
        }
    }
}
