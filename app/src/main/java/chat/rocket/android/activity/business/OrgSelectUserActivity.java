package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.OrgContactAdapter;
import chat.rocket.android.adapter.OrgSearchAdapter;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.recyclertreeview.selectuserbinder.CompanyItemType;
import chat.rocket.android.recyclertreeview.selectuserbinder.DeptItemType;
import chat.rocket.android.recyclertreeview.selectuserbinder.MemberItemType;
import chat.rocket.android.recyclertreeview.selectuserbinder.OrgMember;
import chat.rocket.android.recyclertreeview.selectuserbinder.RootCompanyBinder;
import chat.rocket.android.recyclertreeview.selectuserbinder.RootDeptBinder;
import chat.rocket.android.recyclertreeview.selectuserbinder.RootMemberBinder;
import chat.rocket.android.service.ddp.base.RelationUserDataSubscriber;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.OrgCompany;
import chat.rocket.core.models.SpotlightUser;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmUserEntity;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmOrgCompanyRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightUserRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserEntityRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import indexablerv.IndexableLayout;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewAdapter;

/**
 * Created by helloworld on 2018/4/4
 */

public class OrgSelectUserActivity extends BusinessBaseActivity implements RootDeptBinder.MyOnItemClickListener, View.OnClickListener{

    private static final int NORMAL_SEARCH = 1;
    private static final int ORG_SEARCH = 2;
    private RecyclerView rv_user;
    private RecyclerView rv_search;
    private TreeViewAdapter orgAdapter;
    private List<TreeNode> displayNodes;
    private MethodCallHelper methodCallHelper;
    private String hostname, type,sessionId,roomId,companyId;
    private RealmOrgCompanyRepository realmOrgCompanyRepository;
    /***已勾选人员数量**/
    private List<UserEntity> allSelectUserEntityList;
    private RootMemberBinder rootMemberBinder;
    private TextView tv_count;
    private RootDeptBinder rootDeptBinder;
    private RealmUserRepository userRepository;
    private EditText et_search;
    private ImageView clear;
    private RelativeLayout rl_empty;
    /**
     * 请求接口获取到的所有数据，正常搜索人员
     */
//    private List<UserEntity> allDataList;
    private int currentSearchType = ORG_SEARCH;
    private OrgContactAdapter normalAdapter;
    private OrgSearchAdapter orgSearchAdapter;
    private IndexableLayout indexableLayout;
    private RealmUserEntityRepository userEntityRepository;
    private List<UserEntity> realmUserEntityAll;
    private View search_box;
    private ImageView iv_switch;
    private boolean isSingleChoose;
    private LinearLayout ll_oprationMember;
    WeakReference<Context> contextWeakReference ;
    CurrentUserObserver observer;
    RelationUserDataSubscriber relationUserDataSubscriber;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_select_user);
        displayNodes = new ArrayList<>();
        contextWeakReference=new WeakReference<Context>(this);
        initView();
        initData();
        searchCompanyData();
    }

    @SuppressLint("RxLeakedSubscription")
    private void searchCompanyData() {
        realmOrgCompanyRepository.getOrgCompanyAllFlow().subscribe(orgCompanies -> {
            if(orgCompanies.size() > 0){
                // displayNodes 拼装数据
                initDisplayNodes(orgCompanies);
                if(!RocketChatApplication.isCacheInvalid){
                    searchNetCompany(true);
                }
            }else {
                searchNetCompany(false);
            }
        }, RCLog::e);
    }

    @SuppressLint("RxLeakedSubscription")
    private void searchUserEntityData() {
        setInputSearch();
        userEntityRepository.getUserEntityAllFlow(type, companyId).subscribe(userEntities -> {
            if (userEntities.size() > 0 ) {
                if(realmUserEntityAll == null){
                    realmUserEntityAll = new ArrayList<>();
                }else{
                    realmUserEntityAll.clear();
                }
                for (int i=0;i<userEntities.size();i++){
                    try {
                        UserEntity userBean=userEntities.get(i);
                        User myUser= userRepository.getUserByUsername(userBean.getUsername());
                        List<DeptRole> deptRoles=myUser.getDeptRole();
                        if (deptRoles!=null&&deptRoles.size()>0){
                            userBean.setZhiWei(deptRoles.get(0).getPos_name().split("_")[1]);
                            userBean.setDept(deptRoles.get(0).getOrg_name());
                            userEntities.set(i,userBean);
                        }

                    }catch (Exception e){
                    }
                }

                realmUserEntityAll.addAll(userEntities);
                setNormalAdapter();
                if(!RocketChatApplication.isCacheInvalid){
                    searchNormalUserEntity(type, companyId, true);
                }
            }else{
                if(this.isFinishing()){
                    return;
                }
                searchNormalUserEntity(type, companyId, false);
                RCLog.e("getUserEntity");
            }
        }, RCLog::e);

    }

    private void setOrgAdapter() {
        if(orgAdapter == null) {
            rv_user.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.treeitem_divideritemdecoration));
            rv_user.addItemDecoration(divider);

            RootCompanyBinder rootCompanyBinder = new RootCompanyBinder();
            rootDeptBinder = new RootDeptBinder(this,this,isSingleChoose);
            rootMemberBinder = new RootMemberBinder(this, userRepository,isSingleChoose);
            orgAdapter = new TreeViewAdapter(displayNodes, Arrays.asList(rootCompanyBinder,
                    rootDeptBinder, rootMemberBinder));
            orgAdapter.setPadding(80);
            rv_user.setAdapter(orgAdapter);

            orgAdapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
                @Override
                public boolean onClick(TreeNode node, RecyclerView.ViewHolder holder) {
                    if (holder instanceof RootDeptBinder.ViewHolder) {
                        RCLog.d("setOnTreeNodeListener onClick");
                        //点击整个item，取消和勾选人员
                        DeptItemType content = (DeptItemType) node.getContent();
                        OrgCompany company = content.getDept();
                        List childList = node.getChildList();
                        if(childList == null || childList.size() == 0){//没有请求数据
                            searchNetUser(company.getOrgId(), node, true, company);
                        }else{
                            List<OrgCompany> selectOrgCompanyList = rootDeptBinder.getSelectOrgCompanyList();

                            if(selectOrgCompanyList.size() == 0 || !selectOrgCompanyList.contains(company)){// 没有勾选
                                selectOrgCompanyList.add(company);

                                for(Object child : childList){//将这个部门下面的用户数据都勾选上
                                    TreeNode treeNode = (TreeNode) child;
                                    MemberItemType itemType = (MemberItemType)treeNode.getContent();
                                    UserEntity member = itemType.getMember();
                                    addSelectUserEntityToList(member);
                                }
                            }else{
                                selectOrgCompanyList.remove(company);

                                for(Object child : childList){//将这个部门下面的用户数据都取消勾选
                                    TreeNode treeNode = (TreeNode) child;
                                    MemberItemType itemType = (MemberItemType)treeNode.getContent();
                                    UserEntity member = itemType.getMember();
                                    removeSelectUserEntityToList(member);
                                }
                            }

                            rootDeptBinder.setSelectDeptList(selectOrgCompanyList);
                            orgAdapter.notifyDataSetChanged();

                            setSelectMemberInfo();
                        }
                        return true;
                    }else{
                        RCLog.e("setOnTreeNodeListener onClick error"+holder);
                    }
                    return false;
                }

                @Override
                public void onToggle(boolean b, RecyclerView.ViewHolder viewHolder) {

                }
            });
        }else{
            orgAdapter.notifyDataSetChanged();
        }
    }

    //orgType:  1集团 2公司/单位 3部门 4小组 5其他公司
    private void initDisplayNodes(List<OrgCompany> orgCompanies){
        for (int i = 0; i < orgCompanies.size(); i++) {
            OrgCompany orgCompany = orgCompanies.get(i);
            TreeNode<CompanyItemType> groupTreeNode = new TreeNode<>(new CompanyItemType(orgCompany));
            if(i == 0) groupTreeNode.expand();
            displayNodes.add(groupTreeNode);
            digui(groupTreeNode,orgCompany.getOrgId());
        }

        setOrgAdapter();
    }
    public void digui(TreeNode<CompanyItemType> groupTreeNode, String orgId){
        List<OrgCompany> twoCompanyList = realmOrgCompanyRepository.getOrgCompanyListToOrgId(orgId);

        if(twoCompanyList != null && twoCompanyList.size() > 0) {
            for (int j = 0; j < twoCompanyList.size(); j++) {
                OrgCompany twoCompany = twoCompanyList.get(j);
                if(twoCompany == null) continue;
                String orgType = twoCompany.getOrgType();
                if("1".equals(orgType) || "2".equals(orgType) || "5".equals(orgType)){//公司
                    TreeNode<CompanyItemType> deptChat = new TreeNode<>(new CompanyItemType(twoCompany));
                    groupTreeNode.addChild(deptChat);
                    digui(deptChat, twoCompany.getOrgId());
                }else{
                    TreeNode<DeptItemType> deptChat = new TreeNode<>(new DeptItemType(twoCompany));
                    groupTreeNode.addChild(deptChat);
                }
            }
        }
    }

    /**点击整个item需要勾选列表中所有人员，点击前面图标展开列表，b=true勾选人员**/
    public void searchNetUser(String orgId, TreeNode node, boolean b, OrgCompany company){
        ArrayList<UserEntity> userEntities = new ArrayList<>();
        showProgressDialog();
        methodCallHelper.organizationUserForMobile(type, sessionId, orgId)
                .continueWithTask(task -> {
                    if (task.isFaulted() || task.getError() != null) {
                        RCLog.e("getData: result=" +task.getError()+"" , true);
                        ToastUtils.showToast( "暂无数据");
                        return null;
                    }
                    String result = task.getResult();
                    RCLog.d("getData: result=" + result, true);
                    JsonParser parser = new JsonParser();

                    JsonObject jsonObject = parser.parse(result).getAsJsonObject();
                    JsonElement res = jsonObject.get("res");
                    if(res == null) return null;

                    JsonArray jsonArray = res.getAsJsonArray();
                    RCLog.d("organizationUserForMobile jsonArray size="+jsonArray.size());
                    Gson gson = new Gson();
//                    //加强for循环遍历JsonArray
                    for (JsonElement user : jsonArray) {
                        //使用GSON，直接转成Bean对象
                        OrgMember userBean = gson.fromJson(user, OrgMember.class);
                        String username = userBean.getUsername();
                        if (username.contains("&")){
                            username=username.split("&")[0];
                        }
                        UserEntity entity = new UserEntity(userBean.getFullname(), userBean.getUsername());
                        entity.setZhiWei(userBean.getThisposName());
                        entity.setRealName(username);
                        try {
                            User myUser= userRepository.getUserByUsername(userBean.getUsername());
                            if(TextUtils.isEmpty(entity.getRealName()))
                            entity.setRealName(myUser.getRealName());
                            List<DeptRole> deptRoles=myUser.getDeptRole();
                            if (deptRoles!=null&&deptRoles.size()>0){
                                if(TextUtils.isEmpty(entity.getZhiWei()))
                                    entity.setZhiWei(deptRoles.get(0).getPos_name().split("_")[1]);
                                entity.setDept(deptRoles.get(0).getOrg_name());
                                entity.setCompanyName(myUser.getCompanyName());
                            }
                        }catch (Exception e){
                        }

                        userEntities.add(entity);
                    }

                    dismissProgressDialog();
                    if(userEntities.isEmpty() ){
                        ToastUtils.showToast(getResources().getString(R.string.no_data));
                    }else {
                        for (UserEntity member : userEntities){
                            TreeNode<MemberItemType> groupTreeNode = new TreeNode<>(new MemberItemType(member));
                            node.addChild(groupTreeNode);
                        }

                        if(b) {//b=true勾选人员
                            // 处理勾选部门信息
                            List<OrgCompany> selectOrgCompanyList = rootDeptBinder.getSelectOrgCompanyList();
                            selectOrgCompanyList.add(company);
                            rootDeptBinder.setSelectDeptList(selectOrgCompanyList);

                            //处理勾选人员信息
                            if(allSelectUserEntityList.size() > 0){
                                for (UserEntity entity : userEntities){
                                    addSelectUserEntityToList(entity);
                                }
                            }else {
                                allSelectUserEntityList.addAll(userEntities);
                            }
                            setSelectMemberInfo();
                        }else{//b=false展开列表
                            orgAdapter.onToggle(node);
                        }
                        setOrgAdapter();
                    }
                    return null;
                }, Task.UI_THREAD_EXECUTOR);
    }
    Disposable subscribe;
    private void initData(){
        isSingleChoose=getIntent().getBooleanExtra("isSingleChoose",false);
        if (isSingleChoose)
            ll_oprationMember.setVisibility(View.GONE);
        allSelectUserEntityList = new ArrayList<>();
        sessionId = RocketChatCache.INSTANCE.getSessionId();
        type = getIntent().getStringExtra("type");
        companyId = getIntent().getStringExtra("companyId");
        if (companyId==null)
            companyId=RocketChatCache.INSTANCE.getCompanyId();
        ArrayList<UserEntity> alreadySelectList = getIntent().getParcelableArrayListExtra("selectList");
        if(alreadySelectList != null){
            allSelectUserEntityList.addAll(alreadySelectList);
            setSelectMemberInfo();
        }

        roomId = RocketChatCache.INSTANCE.getSelectedRoomId();
        hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        methodCallHelper = new MethodCallHelper(this, hostname);
        realmOrgCompanyRepository = new RealmOrgCompanyRepository(hostname);
        userRepository = new RealmUserRepository(hostname);
        userEntityRepository = new RealmUserEntityRepository(hostname);
        subscribe = userRepository.getCurrent().distinctUntilChanged()
                .subscribe(userOptional -> {
                    User user = userOptional.get();
                    if (user != null) {
                        myUser=user;
                    }
                }, RCLog::e);
        setInputSearch();
    }

    User myUser;
    private void searchNetCompany(boolean isBackgroundRun){
        if(!isBackgroundRun) {
            showProgressDialog();
        }
        methodCallHelper.organizationForMobile(type, sessionId)
                .continueWithTask(task -> {
                    dismissProgressDialog();
                    if (task.isFaulted() || task.getError() != null) {
                        RCLog.e("getData: result=" +task.getError() , true);
                        ToastUtils.showToast( "暂无数据");
                        return null;
                    }
                    RCLog.d("result=" + task.getResult());
                    RocketChatApplication.isCacheInvalid = true;
                    return null;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private void initView() {
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(R.string.select_member);
        rv_user = findViewById(R.id.rv_user);
        rv_search = findViewById(R.id.rv_search);
        findViewById(R.id.tv_create).setVisibility(View.GONE);
        ImageView iv_back = findViewById(R.id.iv_back);
        iv_switch = findViewById(R.id.iv_switch);
        iv_switch.setVisibility(View.GONE);
        tv_count = findViewById(R.id.tv_count);
        Button btn_ok = findViewById(R.id.btn_ok);
        et_search = findViewById(R.id.et_search);
        clear=findViewById(R.id.clear);
        et_search.setHint(getString(R.string.input_user));
        search_box = findViewById(R.id.search_box);
        rl_empty = findViewById(R.id.rl_empty);
//        search_box.setVisibility(View.GONE);
        indexableLayout = findViewById(R.id.indexableLayout);
        ll_oprationMember = findViewById(R.id.ll_oprationMember);

        iv_back.setOnClickListener(this);
        iv_switch.setOnClickListener(this);
        tv_count.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        clear.setOnClickListener(this);
    }

    /***设置已选择人员数量**/
    private void setSelectMemberInfo(){
        String format = String.format(getString(R.string.org_count), allSelectUserEntityList.size()+ "");
        tv_count.setText(format);
    }

    /**获取所有的已勾选人员数量**/
    public List<UserEntity> getAllSelectUserEntityList() {
        return allSelectUserEntityList;
    }

    /**单选时清除之前的选中**/
    public void clearAllSelectUserEntityList() {
        allSelectUserEntityList.clear();
    }

    public void addSelectUserEntityToList(UserEntity entity) {
        if(!allSelectUserEntityList.contains(entity)){
            if (isSingleChoose)
                clearAllSelectUserEntityList();
            allSelectUserEntityList.add(entity);
        }
        setSelectMemberInfo();
    }

    public void removeSelectUserEntityToList(UserEntity entity) {
        if(allSelectUserEntityList.contains(entity)){
            allSelectUserEntityList.remove(entity);
        }
        setSelectMemberInfo();
    }

    /***设置已选择部门**/
    public void setSelectDeptList(List<OrgCompany> selectOrgCompanyList){
        rootDeptBinder.setSelectDeptList(selectOrgCompanyList);
        orgAdapter.notifyDataSetChanged();
    }

    public List<OrgCompany> getSelectOrgDeptList(){
        return rootDeptBinder.getSelectOrgCompanyList();
    }

    @Override
    public void onItemClickListener(RootDeptBinder.ViewHolder holder, int position, TreeNode node, ImageView ivIcon) {
        DeptItemType content = (DeptItemType) node.getContent();
        OrgCompany company = content.getDept();
        if(node.getChildList() == null || node.getChildList().size() == 0){
//            if (!isSingleChoose)
            searchNetUser(company.getOrgId(), node, false, null);
        }else{
            orgAdapter.onToggle(node);
        }
        if(node.isExpand()){
            ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.org_delete));
        }else{
            ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.org_add));
        }
    }

    /**正常选择人员 搜索接口**/
    private void searchNormalUserEntity(String type, String companyId, boolean isBackgroundRun) {
        if(!isBackgroundRun) {
            showProgressDialog();
        }
        methodCallHelper.userForCreateRoomToRealm(type, companyId)
                .continueWithTask((Continuation<Void, Task<Void>>) task -> {
                    dismissProgressDialog();
                    if (task.isFaulted() || task.getError() != null) {
                        RCLog.e("getUserEntity: result=" +task.getError().getMessage() , true);
                        ToastUtils.showToast(  "暂无数据");
                        return null;
                    }
                    RocketChatApplication.isCacheInvalid = true;
                    return null;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private void setInputSearch() {
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
//                if(TextUtils.isEmpty(realmUserEntityAll)) return;
//                if(currentSearchType == NORMAL_SEARCH) {
                if(term.length()>0) {
                    clear.setVisibility(View.VISIBLE);
                    filterSearchData(term);
                }else {
                    clear.setVisibility(View.GONE);
                    rv_search.setVisibility(View.GONE);
                    rl_empty.setVisibility(View.GONE);
                    rv_user.setVisibility(View.VISIBLE);
                }
//                }else{
//
//                }
            }
        });
    }

    @SuppressLint("RxLeakedSubscription")
    private void filterSearchData(String term){
        rv_search.setVisibility(View.VISIBLE);
        rv_user.setVisibility(View.GONE);
        subscriptionRepository=new RealmSubscriptionRepository(hostname);
        methodCallHelper.userForCreateRoomShowDeptToRealm(type,term, companyId,true)
                .onSuccessTask((Task<JSONArray> task) -> {
                    JSONArray result = task.getResult();
                    if(result == null) {
                        return null;
                    }
                            dismissProgressDialog();
                            if (task.isFaulted() || task.getError() != null) {
                                RCLog.e("getUserEntity: result=" +task.getError().getMessage() , true);
                                ToastUtils.showToast(  "暂无数据");
                                return null;
                            }else {
                                for (int i = 0; i < result.length(); i++) {
                                    RealmUserEntity.customizeJson(result.getJSONObject(i), type);
                                }
                                Gson gson=new Gson();
                                List<UserEntity> tempDataList = new ArrayList<>();
                                for (int i = 0; i < result.length(); i++) {
                                    UserEntity userEntity=gson.fromJson(result.get(i).toString(),UserEntity.class);
                                    if (userEntity!=null&&userEntity.getDeptRole()!=null){
                                    userEntity.setDept(userEntity.getDeptRole().getOrg_name());
                                    userEntity.setZhiWei(userEntity.getDeptRole().getJob_name());}
                                    tempDataList.add(userEntity);
                                }
                                if (tempDataList.size()==0){
                                rl_empty.setVisibility(View.VISIBLE);
                                rv_search.setVisibility(View.GONE);
                            }else {
                                rl_empty.setVisibility(View.GONE);
                                rv_search.setVisibility(View.VISIBLE);
                                orgSearchAdapter = new OrgSearchAdapter(this, tempDataList);
                                rv_search.setLayoutManager(new LinearLayoutManager(this));
                                rv_search.setAdapter(orgSearchAdapter);
                                orgSearchAdapter.setOnItemClickListener((view, spotlightUsers1) -> {
                                    if (isSingleChoose){
                                        addSelectUserEntityToList(spotlightUsers1);
                                        onClickOk();
                                    }else {
                                        addSelectUserEntityToList(spotlightUsers1);
                                        ToastUtils.showToast("添加成功");
                                        et_search.setText("");
                                        rv_search.setVisibility(View.GONE);
                                        rv_user.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                            }
                            return null;
                },Task.UI_THREAD_EXECUTOR)
                .continueWith(task -> {
                    if (!task.isFaulted()){
                    }
                    return null;
                });



//        searchSpotlight(term, myUser).toObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(spotlightUsers ->{
//                    List<UserEntity> tempDataList = new ArrayList<>();
//                    for (SpotlightUser user:spotlightUsers){
//                        tempDataList.add(new UserEntity(user.getRealName(),user.getUsername()));
//                    }
////                    orgSearchAdapter.setDatas(tempDataList,term);
//                    if (tempDataList.size()==0){
//                        rl_empty.setVisibility(View.VISIBLE);
//                        rv_search.setVisibility(View.GONE);
//                    }else {
//                        rl_empty.setVisibility(View.GONE);
//                        rv_search.setVisibility(View.VISIBLE);
//                        orgSearchAdapter = new OrgSearchAdapter(this, tempDataList);
//                        rv_search.setLayoutManager(new LinearLayoutManager(this));
//                        rv_search.setAdapter(orgSearchAdapter);
//                        orgSearchAdapter.setOnItemClickListener((view, spotlightUsers1) -> {
//                            if (isSingleChoose){
//                                addSelectUserEntityToList(spotlightUsers1);
//                                onClickOk();
//                            }else {
//                                addSelectUserEntityToList(spotlightUsers1);
//                                ToastUtils.showToast("添加成功");
//                                et_search.setText("");
//                                rv_search.setVisibility(View.GONE);
//                                rv_user.setVisibility(View.VISIBLE);
//                            }
//                        });
//                    }
//                } , RCLog::e);

//        List<Subscription> suggestionsFor = subscriptionRepository.getSuggestionsFor(term, myUser.getId());
//        if(term.length() > 0){
//            for(UserEntity entity : realmUserEntityAll){
//                String pinyin = entity.getPinyin();
//                String realName = entity.getRealName();
//                if(pinyin == null || realName == null) continue;
//                if(pinyin.toLowerCase().contains(term.toLowerCase())||realName.contains(term)){
//                    tempDataList.add(entity);
//                }
//            }
//        }else{
//            tempDataList.addAll(realmUserEntityAll);
//        }
//        normalAdapter.setDatas(tempDataList);
    }


    private RealmSubscriptionRepository subscriptionRepository;
    private RealmSpotlightUserRepository realmSpotlightUserRepository;
    public Flowable<List<SpotlightUser>> searchSpotlight(String term, User user) {
        realmSpotlightUserRepository=new RealmSpotlightUserRepository(hostname);
        methodCallHelper.searchSpotlight(term, user.getId());
        return realmSpotlightUserRepository.getSuggestionsFor(term);
    }

    private void setNormalAdapter() {
        if(normalAdapter == null) {
            IndexableLayout indexableLayout = (IndexableLayout) findViewById(R.id.indexableLayout);
            indexableLayout.setLayoutManager(new LinearLayoutManager(this));

            hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
            methodCallHelper = new MethodCallHelper(this, hostname);

            normalAdapter = new OrgContactAdapter(this);
            indexableLayout.setAdapter(normalAdapter);
            // set Material Design OverlayView
            indexableLayout.setOverlayStyle_MaterialDesign(Color.RED);
            // 全字母排序。  排序规则设置为：每个字母都会进行比较排序；速度较慢
            indexableLayout.setCompareMode(IndexableLayout.MODE_FAST);

            normalAdapter.setDatas(realmUserEntityAll);
        }else{
            normalAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("RxLeakedSubscription")
    public void onClickOk(){
        Intent intent = new Intent();
        ArrayList<UserEntity> selectList=new ArrayList<>();
        selectList.addAll(allSelectUserEntityList);
        if (getIntent().getBooleanExtra("isFromGroupInfo",false)){
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

        }else {
            if (isSingleChoose) {
                intent.putExtra("username", selectList.get(0).getUsername());
            }
            intent.putParcelableArrayListExtra("selectList", selectList);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_count:
                if (isSingleChoose)
                    return;
                intent = new Intent(this, OrgDeleteUserActivity.class);
                if(allSelectUserEntityList == null||allSelectUserEntityList.size()==0){
                    return;
                }
                ArrayList<UserEntity> temp=new ArrayList<>();
                temp.addAll(allSelectUserEntityList);
                intent.putExtra("selectList", temp);
                startActivityForResult(intent,0x3);

                break;
            case R.id.clear:
                et_search.setText("");
                break;
            case R.id.btn_ok:
                onClickOk();
                break;
            case R.id.iv_switch:
                et_search.setText("");
                if(currentSearchType == ORG_SEARCH){
                    rv_user.setVisibility(View.GONE);
                    indexableLayout.setVisibility(View.VISIBLE);
                    search_box.setVisibility(View.VISIBLE);
                    currentSearchType = NORMAL_SEARCH;
                    iv_switch.setImageResource(R.drawable.add_group_sort);
                    if(normalAdapter == null) {
                        searchUserEntityData();
                    }else{
                        normalAdapter.notifyDataSetChanged();
                    }
                }else if(currentSearchType == NORMAL_SEARCH){
                    rv_user.setVisibility(View.VISIBLE);
                    indexableLayout.setVisibility(View.GONE);
                    search_box.setVisibility(View.GONE);
                    iv_switch.setImageResource(R.drawable.select_member_switch);
                    currentSearchType = ORG_SEARCH;
                    setOrgAdapter();
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK&&requestCode==0x3){
            allSelectUserEntityList=data.getParcelableArrayListExtra("selectList");
            if (orgAdapter!=null)
            orgAdapter.notifyDataSetChanged();
            setSelectMemberInfo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscribe.dispose();
        if (observer!=null)
            observer.unregister();
        if (relationUserDataSubscriber!=null)
            relationUserDataSubscriber.unregister();
    }
}
