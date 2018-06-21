package chat.rocket.android.activity.business;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.CunDangAdapter;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.entity.GuidangBean;
import chat.rocket.android.entity.GuidangBean1;
import chat.rocket.android.login.CustomProgressDialog;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.PublicSetting;
import chat.rocket.persistence.realm.models.ddp.RealmPublicSetting;
import chat.rocket.persistence.realm.repositories.RealmPublicSettingRepository;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by helloworld on 2018/2/27
 */

public class CunDangListActivity extends BusinessBaseActivity {

    private String hostname, companyCode;
    private String valueUrl;
    private RecyclerView rv_cundang;
    private int pageSize = 20;
    private int page = 1;
    private Handler handler;
    private LinearLayoutManager linearLayoutManager;
    private boolean isLoading = false;
    List<GuidangBean1.DataBeanX.DataBean.VarListBean> data;
    private CunDangAdapter cunDangAdapter;
    private EditText et_search;
    private String url;
    private ImageView clear;
    private RelativeLayout rl_empty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cundang_list);

        rv_cundang = findViewById(R.id.rv_cundang);
        findViewById(R.id.iv_back).setOnClickListener(view -> onBackPressed());
        rl_empty = findViewById(R.id.rl_empty);
        TextView title=(TextView)findViewById(R.id.tv_title);
        TextView sure=(TextView)findViewById(R.id.tv_create);
        et_search=(EditText)findViewById(R.id.et_search);
        clear=findViewById(R.id.clear);
        sure.setVisibility(View.GONE);
        title.setText("归档");
        rv_cundang = findViewById(R.id.rv_cundang);
        et_search.setHint(R.string.search_file_name);
//        handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                switch (msg.what){
//                    case 1:
//                        ToastUtils.showToast("未知错误");
//                        break;
//                    case 2:
//                        setAdapter(guidangBean);
//                        break;
//                }
//            }
//        };
        initData();
        postDataWithParame(false);
        clear.setOnClickListener(v -> {
            et_search.setText("");
            rl_empty.setVisibility(View.GONE);
            rv_cundang.setVisibility(View.VISIBLE);
                }
        );
    }

    private void setAdapter(GuidangBean1.DataBeanX.DataBean result,boolean isLoadMore,String url) {
        if (result!=null)
            data=result.getVarList();
        if(data.size()==pageSize){
            isLoading=false;
        }else {
            isLoading=true;
        }
        cunDangAdapter.setData(data,isLoadMore,url);
    }

    private void loadMore() {
        page++;
        postDataWithParame(true);
    }
    MethodCallHelper methodCallHelper;
    private void initData() {
        hostname = getIntent().getStringExtra("hostname");
        companyCode = getIntent().getStringExtra("companyCode");
        methodCallHelper = new MethodCallHelper(this, hostname);
        RealmPublicSettingRepository publicSettingRepository = new RealmPublicSettingRepository(hostname);
        PublicSetting setting = publicSettingRepository.getPublicSettingById(RealmPublicSetting.CUSTOMADDRESS_FILE_CLASSIFY_LIST_ADDRESS);
        if (setting != null) {
            valueUrl = setting.getValue();
        }
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String term = s.toString().trim();
                filterSearchData(term);
            }
        });
        data=new ArrayList<>();
        cunDangAdapter = new CunDangAdapter(this, data);
        linearLayoutManager = new LinearLayoutManager(this);
        rv_cundang.setLayoutManager(linearLayoutManager);
        rv_cundang.setAdapter(cunDangAdapter);
        rv_cundang.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                int findLastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading
                        && findLastVisibleItemPosition >= totalItemCount-1
                        && visibleItemCount < totalItemCount
                        && dy>10) {
                    isLoading = true;
                    loadMore();
                }
            }
        });
    }

    private void filterSearchData(String term) throws NullPointerException {
        try {
            if (guidangBean!=null&&chat.rocket.android.helper.TextUtils.isEmpty(guidangBean.getData().getData().getVarList())) return;
        } catch (Exception e) {
            return;
        }
        List<GuidangBean1.DataBeanX.DataBean.VarListBean> tempDataList = new ArrayList<>();
        if(term.length() > 0){
            clear.setVisibility(View.VISIBLE);
            for(GuidangBean1.DataBeanX.DataBean.VarListBean entity: guidangBean.getData().getData().getVarList()){
                String title = entity.getName();
                String pinyin = entity.getUperName();
                if(title.contains(term)||(pinyin != null && pinyin.contains(term))){
                    tempDataList.add(entity);
                }
            }
            if (tempDataList.size()==0){
                rl_empty.setVisibility(View.VISIBLE);
            }else {
                rv_cundang.setVisibility(View.VISIBLE);
                rl_empty.setVisibility(View.GONE);
            }
        }else{
            clear.setVisibility(View.GONE);
            tempDataList.addAll(guidangBean.getData().getData().getVarList());
        }
        cunDangAdapter.setData(tempDataList,false,url);
    }

    /**
     * type
     * sessionId
     * name
     * pageSize
     * page
     * eid
     *  * userAccount	String	会话id		是
     name	String	文件名		否	搜索时传
     pageSize	Int	当前页数		是	默认传20条
     pageIndex	Int	页码		是	第几页
     */
    private void postDataWithParame(boolean isLoadMore) {
        if(!isLoadMore)
        showProgressDialog();
        methodCallHelper.getFileClassifyList(page+"",pageSize+"","",RocketChatCache.INSTANCE.getSessionId())
                .continueWith(task -> {
                    dismissProgressDialog();
                    if (task.getError()==null){
                     String  result= task.getResult().toString();
                        Gson gson = new Gson();
                        guidangBean = gson.fromJson(result, GuidangBean1.class);
                        url=guidangBean.getUrl();
                        setAdapter(guidangBean.getData().getData(),isLoadMore,url);
                    }else {
                        ToastUtils.showToast("未知错误");
                        page--;
                        isLoading=false;
                    }
                    return null;
                }, Task.UI_THREAD_EXECUTOR);
//        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
//        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
//        formBody.add("type", "4");//类型
//        formBody.add("sessionId", RocketChatCache.INSTANCE.getSessionId());
//        formBody.add("name", "");
//        formBody.add("pageSize", pageSize+"");
//        formBody.add("page", page+"");
//        formBody.add("eid", companyCode);//文件id
//        Request request = new Request.Builder()//创建Request 对象。
//                .url(valueUrl)
//                .post(formBody.build())//传递请求体
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                handler.sendEmptyMessage(1);
//                page=page--;
//                isLoading=false;
//                dismissProgressDialog();
//            }
//            String result;
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    dismissProgressDialog();
////                    Log.d("kwwl", "response.code()==" + response.code());
////                    Log.d("kwwl", "response.message()==" + response.message());
////                    Log.d("kwwl", "res==" + response.body().string());
//                    Reader reader = response.body().charStream();
//                    Gson gson = new Gson();
//                    guidangBean = gson.fromJson(reader, GuidangBean.class);
//                    handler.sendEmptyMessage(2);
//                }
//            }
//        });

    }


    GuidangBean1 guidangBean;
    @Override
    protected void showProgressDialog() {
        super.showProgressDialog();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }
        mProgressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialog1);
        mProgressDialog.show();
    }

    @Override
    protected void dismissProgressDialog() {
        super.dismissProgressDialog();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
