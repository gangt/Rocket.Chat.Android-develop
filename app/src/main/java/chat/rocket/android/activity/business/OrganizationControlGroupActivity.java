package chat.rocket.android.activity.business;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.base.RelationUserDataSubscriber;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.core.models.Labels;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmLabelsRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;

/**
 * Created by zhangxiugao on 2018/1/8
 */

public class OrganizationControlGroupActivity extends BusinessBaseActivity implements View.OnClickListener{

    private static final int SELECT_USER = 1;
    private static final String TAG = "OrganizationConActivity";
    private TextView tv_title,tv_create,tv_channel_remind,tv_add_person,tv_subordinate_system;
    private ImageView iv_back;
    private ArrayList<UserEntity> selectList;
    private EditText et_org_name;
    private MethodCallHelper methodCallHelper;
    private List<Labels> labelsList;
    private Switch s_superior_channel,s_isSpeak;
    private String userId,companyId;
    private String hostname;
    private LinearLayout ll_add_member,ll_system;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_control);

        initView();
        initData();
        setListener();
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
//        iv_add_person = findViewById(R.id.iv_add_person);
//        iv_subordinate_system = findViewById(R.id.iv_subordinate_system);

        et_org_name = findViewById(R.id.et_org_name);
        s_superior_channel = findViewById(R.id.s_superior_channel);
        s_isSpeak = findViewById(R.id.s_isSpeak);

        tv_create = findViewById(R.id.tv_create);
        tv_channel_remind = findViewById(R.id.tv_channel_remind);
        tv_add_person = findViewById(R.id.tv_add_person);
        tv_subordinate_system = findViewById(R.id.tv_subordinate_system);

        ll_add_member = findViewById(R.id.ll_add_member);
        ll_system = findViewById(R.id.ll_system);
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        tv_create.setOnClickListener(this);
        ll_add_member.setOnClickListener(this);
        ll_system.setOnClickListener(this);
    }

    private void initData(){
        tv_title.setText(R.string.organization_control_group);

        SpannableString spannableString = new SpannableString(tv_channel_remind.getText());
//        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_channel_remind.setText(spannableString);
        // 添加两张小图标
        ImageSpan imgSpan1 = new ImageSpan(this, R.drawable.channel_icon);
        spannableString.setSpan(imgSpan1, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tv_channel_remind.setText(spannableString);

        companyId = getIntent().getStringExtra("companyId");
        userId = getIntent().getStringExtra("userId");
        Log.d(TAG, "initData: companyId="+companyId+",userId="+userId);

        hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        methodCallHelper = new MethodCallHelper(this, hostname);

        RealmUserRepository realmUserRepository = new RealmUserRepository(hostname);
        User user = realmUserRepository.getUserByUsername(RocketChatCache.INSTANCE.getUserUsername());
        if(user != null){
            List<String> roles = user.getRoles();
            if(roles == null || roles.size() == 1 && roles.contains(RocketChatConstants.USER)){
                tv_subordinate_system.setHint("普通用户不可选择体系");
                ll_system.setClickable(false);
                ll_system.setEnabled(false);
            }
        }
    }

    /**
     * 获取体系数据（websocket订阅）
     * @param companyId
     */
    private void loadData(String companyId) {// 组织巢p
        RealmLabelsRepository labelsRepository = new RealmLabelsRepository(hostname);
        labelsList = labelsRepository.getByType(RocketChatConstants.P, companyId);
        if(labelsList == null){
            labelsList = new ArrayList<>();
        }
        Log.d(TAG, "loadData: labelsList="+labelsList.toString());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                KeyboardHelper.hideSoftKeyboard(this);
                finish();
                break;
            case R.id.tv_create:
                createOrgGroup();
                break;
            case R.id.ll_system:
                loadData(companyId);
                listDialog();
                break;
            case R.id.ll_add_member:
//                Intent intent = new Intent(this, SelectUserActivity.class);
                Intent intent = new Intent(this, OrgSelectUserActivity.class);
                // 组织巢p
                intent.putExtra("type", RocketChatConstants.P);
                intent.putExtra("companyId", companyId);
                intent.putParcelableArrayListExtra("selectList", selectList);
                startActivityForResult(intent, SELECT_USER);
                break;
        }
    }

    private void createOrgGroup() {
        if(TextUtils.isEmpty(et_org_name.getText().toString().trim())){
            ToastUtils.showToast(getString(R.string.input_organization_name));
            return;
        }
        if(TextUtils.isEmpty(tv_add_person.getText())){
            ToastUtils.showToast(getString(R.string.select_add_member));
            return;
        }
        showProgressDialog();
        JSONArray jsonArray = getSelectListToJsonArray();
        String tixiString = null;
        if(labelsList != null && labelsList.size() > 0 && selectPosition >= 0){
            Labels shuTixiClass = labelsList.get(selectPosition);
            tixiString = new Gson().toJson(shuTixiClass);
        }
        String name = et_org_name.getText().toString();
//        s_superior_channel.isChecked();

        methodCallHelper.createPrivateGroupForMobile(userId, name, jsonArray,"", tixiString, s_isSpeak.isChecked())
                .continueWithTask(task -> {
                    dismissProgressDialog();
                    if (task.isFaulted() || task.getError() != null) {
                        Log.e(TAG, "createOrgGroup: result=" + task.getError());
                        ToastUtils.showToast(task.getError() + "");
                        return null;
                    }
                    String result = task.getResult();
                    Log.d(TAG, "createOrgGroup: result=" + result);
                    if(!TextUtils.isEmpty(result)){
                        // 创建成功
                        RelationUserDataSubscriber relationUserDataSubscriber = new RelationUserDataSubscriber(OrganizationControlGroupActivity.this, hostname, RealmStore.getOrCreate(hostname));
                        relationUserDataSubscriber.register();
                        ToastUtils.showToast(getString(R.string.create_success));
                        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
                        MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
                        methodCall.getRoomSubscriptions().onSuccess(task1 -> {
                            Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                                    OrganizationControlGroupActivity.this, hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
                            listener.register();
                            return null;
                        }).continueWith(new LogIfError());
                        finish();
                    }
                    return task;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private JSONArray getSelectListToJsonArray() {
        JSONArray jsonArray = new JSONArray();
        if(selectList != null && selectList.size() > 0){
            for (UserEntity entity : selectList){
                jsonArray.put(entity.getUsername());
            }
        }
        return jsonArray;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_USER && resultCode == Activity.RESULT_OK && data != null) {
            selectList = data.getParcelableArrayListExtra("selectList");
            tv_add_person.setText(getSelectListToName());
        }
    }

    private String getSelectListToName() {
        StringBuilder nick = new StringBuilder();
        if(selectList != null && selectList.size() > 0){
            for (UserEntity entity : selectList){
                nick.append(entity.getRealName() + ";");
            }
            return nick.substring(0, nick.length() - 1);
        }
        return "";
    }

    private int selectPosition = -1;
    private void listDialog(){
        if(labelsList.size() == 0){
            ToastUtils.showToast("未搜索到所属体系");
            return;
        }
        String[] strs = new String[labelsList.size()];
        for (int i = 0; i < labelsList.size(); i++) {
            strs[i] = labelsList.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置标题
        builder.setTitle("请选择");
        builder.setItems(strs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectPosition = i;
                tv_subordinate_system.setText(strs[i]);
                dialogInterface.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

}
