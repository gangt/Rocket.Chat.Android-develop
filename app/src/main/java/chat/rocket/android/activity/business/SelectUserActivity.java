package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.ContactAdapter;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.dialog.WeiNingAlertDialog;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.ddp.base.RelationUserDataSubscriber;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmUserEntityRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import indexablerv.IndexableLayout;

/**
 * Created by zhangxiugao on 2018/1/8
 * 选择全部人员
 */

public class SelectUserActivity extends BusinessBaseActivity {

    private TextView tv_title,tv_create;
    private ImageView iv_back;
    private EditText et_search;
    private ImageView clear;
    private ContactAdapter mAdapter;
    private ArrayList<UserEntity> selectList;
    /**
     * 请求接口获取到的所有数据
     */
    private List<UserEntity> allDataList;
    private String companyId, type,roomId,hostname;
    private String[] filterUsernames;
    private MethodCallHelper methodCallHelper;
    private boolean isSingleChoose,isCreateSingleChat;
    WeakReference<Context> contextWeakReference ;
    CurrentUserObserver observer;
    RelationUserDataSubscriber relationUserDataSubscriber;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_person);
        contextWeakReference=new WeakReference<Context>(this);
        initView();
        initData();
        setAdapter();
        setListener();
    }

    private void setListener() {
        iv_back.setOnClickListener(v -> {
            KeyboardHelper.hideSoftKeyboard(this);
            onBackPressed();
        });
        tv_create.setOnClickListener(v -> {
            onClickOk();
        });
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
                if(TextUtils.isEmpty(allDataList)) return;
                filterSearchData(term);
            }
        });
    }

    private void onClickOk() {
        ArrayList<UserEntity> selectList = mAdapter.getSelectList();
        if(filterUsernames == null){
            Intent intent = new Intent();
//            intent.putExtra("selectList", mAdapter.getSelectList());
            if(isCreateSingleChat && selectList.size() == 1){
                intent.putExtra("username", selectList.get(0).getUsername());
            }else{
                intent.putParcelableArrayListExtra("selectList", selectList);
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        }else{
            if(selectList.isEmpty()){
                return;
            }
            showProgressDialog();
            JSONArray array = new JSONArray();
            for (UserEntity entity : selectList) {
                array.put(entity.getUsername());
            }

            methodCallHelper.addMembers(roomId, array).continueWithTask(new Continuation<String, Task<Object>>() {
                @Override
                public Task<Object> then(Task<String> task) throws Exception {
                    dismissProgressDialog();
                    if (task.isFaulted() || task.getError() != null) {
                        ToastUtils.showToast(task.getError()+"");
                    }else{
                        ToastUtils.showToast(getString(R.string.update_finish));
                        observer = new CurrentUserObserver(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
                        observer.register();
                        relationUserDataSubscriber = new RelationUserDataSubscriber(contextWeakReference.get(), hostname, RealmStore.getOrCreate(hostname));
                        relationUserDataSubscriber.register();
                        Intent intent = new Intent();
                        intent.putParcelableArrayListExtra("selectList", selectList);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }

                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);

        }
    }

    @Override
    public void onBackPressed() {
        if(mAdapter != null) {
            ArrayList<UserEntity> selectList = mAdapter.getSelectList();
            if(selectList != null && selectList.size() > 0){
                showDialog();
            }else{
                super.onBackPressed();
                finish();
            }
        }

    }

    private void filterSearchData(String term){
        List<UserEntity> tempDataList = new ArrayList<>();
        if(term.length() > 0){
            clear.setVisibility(View.VISIBLE);
            for(UserEntity entity : allDataList){
                String pinyin = entity.getPinyin();
                String realName = entity.getRealName();
                if(pinyin == null || realName == null) continue;
                if(pinyin.toLowerCase().contains(term.toLowerCase())||realName.contains(term)){
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
        tv_title.setText(R.string.all_person);
        tv_create.setText(R.string.ok_cn);
        clear.setOnClickListener(v ->
        et_search.setText(""));
    }

    private void initData(){
        companyId = getIntent().getStringExtra("companyId");
        roomId = getIntent().getStringExtra("roomId");
        type = getIntent().getStringExtra("type");
        isSingleChoose = getIntent().getBooleanExtra("isSingleChoose", false);
        isCreateSingleChat = getIntent().getBooleanExtra("isCreateSingleChat", false);
        filterUsernames = getIntent().getStringArrayExtra("existUser");
        selectList = getIntent().getParcelableArrayListExtra("selectList");
    }

    @SuppressLint("RxLeakedSubscription")
    private void setAdapter() {
        IndexableLayout indexableLayout = (IndexableLayout) findViewById(R.id.indexableLayout);
        indexableLayout.setLayoutManager(new LinearLayoutManager(this));

        hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        methodCallHelper = new MethodCallHelper(this, hostname);

        mAdapter = new ContactAdapter(this, selectList, isSingleChoose);
        indexableLayout.setAdapter(mAdapter);
        // set Material Design OverlayView
        indexableLayout.setOverlayStyle_MaterialDesign(Color.RED);
        // 全字母排序。  排序规则设置为：每个字母都会进行比较排序；速度较慢
        indexableLayout.setCompareMode(IndexableLayout.MODE_FAST);

        // 选择主持人只能单选，所列人员为已经选择的会议人员
        if(isSingleChoose){
            if(isCreateSingleChat){// 侧滑栏有选择人员入口，只能单选， isCreateSingleChat=true表示侧滑栏加人
                RealmUserEntityRepository userEntityRepository = new RealmUserEntityRepository(hostname);
                userEntityRepository.getUserEntityAllFlow(type, companyId).subscribe(userEntities -> {
                    if (userEntities.size() > 0) {
                        allDataList = userEntities;
                        mAdapter.setDatas(userEntities);
                        mAdapter.notifyDataSetChanged();
                        if(!RocketChatApplication.isCacheInvalid){
                            getUserEntity(type, companyId, true);
                        }
                    }else{
                        if(this.isFinishing()){
                            return;
                        }
                        getUserEntity(type, companyId, false);
                        RCLog.e("getUserEntity");
                    }
                }, RCLog::e);

            }else {// 添加主持人
                allDataList = getIntent().getParcelableArrayListExtra("allDataList");
                mAdapter.setDatas(allDataList);
                mAdapter.notifyDataSetChanged();
            }
        }else {
            if (filterUsernames == null) {//过滤条件，区分是频道增加人员还是创建频道
                getUserEntity(type, companyId, false);
            } else {
                getUserEntityForUsernames(type, companyId, filterUsernames);
            }
        }
    }

    private void getUserEntity(String type, String companyId, boolean isBackgroundRun) {
        if(!isBackgroundRun) {
            showProgressDialog();
        }
        allDataList = new ArrayList<>();
        methodCallHelper.userForCreateRoomToRealm(type, companyId)
                .continueWithTask((Continuation<Void, Task<Void>>) task -> {
                    dismissProgressDialog();
                    if (task.isFaulted() || task.getError() != null) {
                        RCLog.e("getUserEntity: result=" +task.getError().getMessage() , true);
                        ToastUtils.showToast(task.getError() + "");
                        return null;
                    }

                    if(isCreateSingleChat) {
                        RocketChatApplication.isCacheInvalid = true;
                    }
                    return null;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private void getUserEntityForUsernames(String type, String companyId, String[] filterUsernames) {
        showProgressDialog();
        allDataList = new ArrayList<>();
        methodCallHelper.userForCreateRoom(type, companyId, filterUsernames)
                .continueWithTask((Continuation<String, Task<String>>) task -> {
                    SelectUserActivity.this.dismissProgressDialog();
                    if (task.isFaulted() || task.getError() != null) {
                        RCLog.e("getUserEntity: result=" +task.getError().getMessage() , true);
                        ToastUtils.showToast(task.getError() + "");
                        return null;
                    }
                    String result = task.getResult();
//                    RCLog.d("getUserEntity: result=" + result, true);
                    JsonParser parser = new JsonParser();
                    JsonArray jsonArray = parser.parse(result).getAsJsonArray();
                    Gson gson = new Gson();

                    //加强for循环遍历JsonArray
                    RealmUserRepository userRepository=new RealmUserRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
                    for (JsonElement user : jsonArray) {
                        //使用GSON，直接转成Bean对象
                        UserEntity userBean = gson.fromJson(user, UserEntity.class);
                        if (userBean.getDeptRole()!=null)
                        {
                        userBean.setZhiWei(userBean.getDeptRole().getJob_name());
                        userBean.setDept(userBean.getDeptRole().getOrg_name());
                        }
                        //                        try {
//                            User myUser= userRepository.getUserByUsername(userBean.getUsername());
//                            List<DeptRole> deptRoles=myUser.getDeptRole();
//                            if (deptRoles!=null&&deptRoles.size()>0){
//                                userBean.setZhiWei(deptRoles.get(0).getPos_name().split("_")[1]);
//                                userBean.setDept(deptRoles.get(0).getOrg_name());
//                            }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
                        allDataList.add(userBean);
                    }
                    if(allDataList.isEmpty()){
                        ToastUtils.showToast(getResources().getString(R.string.no_data));
                    }else {
                        mAdapter.setDatas(allDataList);
                        mAdapter.notifyDataSetChanged();
                    }
                    return null;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private WeiNingAlertDialog dialog;
    private void showDialog(){
        WeiNingAlertDialog.Builder builder = new WeiNingAlertDialog.Builder(this);
        dialog = builder.setTitle(getString(R.string.exit_edit))
                .addViewOnclick(new WeiNingAlertDialog.OnClickListener() {
                    @Override
                    public void cancelClick() {
                        dialog.dismiss();
                    }

                    @Override
                    public void conFirmClick() {
                        finish();
                    }
                })
                .build();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (observer!=null)
            observer.unregister();
        if (relationUserDataSubscriber!=null)
            relationUserDataSubscriber.unregister();
    }
}
