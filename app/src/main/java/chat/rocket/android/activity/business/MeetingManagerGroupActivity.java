package chat.rocket.android.activity.business;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.DateTimePickDialogUtil;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.base.RelationUserDataSubscriber;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.disposables.Disposable;
import io.realm.RealmUserRealmProxy;

/**
 * Created by zhangxiugao on 2018/1/8
 */

public class MeetingManagerGroupActivity extends BusinessBaseActivity implements View.OnClickListener {

    private static final int SELECT_USER = 1;
    private static final int SELECT_ZHUCHIREN = 201;
    private static final String TAG = "MeetingManagerActivity";
    private TextView tv_title, tv_create, tv_endTime, tv_startTime, tv_remind, tv_addPerson, tv_zhuchiren;
    private ImageView iv_back, iv_endTime, iv_startTime, iv_addPerson, iv_zhuchiren;
    private EditText et_theme, et_meeting_issue;
    private Switch s_encryption, s_qiandao;
    private ArrayList<UserEntity> selectList;
    private ArrayList<UserEntity> zhuchirenList;
    private String userId, companyId, hostname;
    private LinearLayout ll_startTime, ll_endTime, ll_add_zhuchiren, ll_add_member;
    private RealmUserRepository userRepository;
    private User user;
    //    private RealmUserRepository userRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_manager);

        initView();
        setListener();
        initData();
    }

    private void initData() {
        userId = getIntent().getStringExtra("userId");
        companyId = getIntent().getStringExtra("companyId");
        hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        userRepository = new RealmUserRepository(hostname);
        Disposable subscribe = userRepository.getCurrent().subscribe(userOptional -> {
            user = userOptional.get();
        }, RCLog::e);

//        userRepository = new RealmUserRepository(hostname);
        Log.d(TAG, "initData: userId=" + userId);

    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        tv_create.setOnClickListener(this);
        ll_startTime.setOnClickListener(this);
        ll_endTime.setOnClickListener(this);
        ll_add_member.setOnClickListener(this);
        ll_add_zhuchiren.setOnClickListener(this);
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        tv_create = findViewById(R.id.tv_create);
        iv_endTime = findViewById(R.id.iv_endTime);
        iv_startTime = findViewById(R.id.iv_startTime);
        iv_addPerson = findViewById(R.id.iv_addPerson);
        tv_create = findViewById(R.id.tv_create);
        tv_startTime = findViewById(R.id.tv_startTime);
        tv_endTime = findViewById(R.id.tv_endTime);
        tv_remind = findViewById(R.id.tv_meeting_remind);
        tv_addPerson = findViewById(R.id.tv_addPerson);
        tv_zhuchiren = findViewById(R.id.tv_zhuchiren);

        iv_zhuchiren = findViewById(R.id.iv_zhuchiren);
        s_qiandao = findViewById(R.id.s_qiandao);

        et_theme = findViewById(R.id.et_theme);
        et_meeting_issue = findViewById(R.id.et_meeting_issue);
        s_encryption = findViewById(R.id.s_encryption);

        ll_startTime = findViewById(R.id.ll_startTime);
        ll_endTime = findViewById(R.id.ll_endTime);
        ll_add_member = findViewById(R.id.ll_add_member);
        ll_add_zhuchiren = findViewById(R.id.ll_add_zhuchiren);

        tv_title.setText(R.string.meeting_manager_group);
        // 讲首字符标记为红颜色
        SpannableString spannableString = new SpannableString(tv_remind.getText());
//        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_remind.setText(spannableString);
//        tv_meeting_type.setText(getString(R.string.meeting_word));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                KeyboardHelper.hideSoftKeyboard(this);
                finish();
                break;
            case R.id.tv_create:
                createMeetingGroup();
                break;
            case R.id.ll_endTime:
//                showDateTimePickDialog(tv_endTime);
                KeyboardHelper.hideSoftKeyboard(this);
                initTimePicker(1);
                pvTime.show();
                break;
            case R.id.ll_startTime:
//                showDateTimePickDialog(tv_startTime);
                KeyboardHelper.hideSoftKeyboard(this);
                initTimePicker(0);
                pvTime.show();
                break;

            case R.id.ll_add_member:
                Intent intent2 = new Intent(this, OrgSelectUserActivity.class);
                if (selectList != null) {
                    UserEntity userEntity = new UserEntity();
                    userEntity.setUsername(user.getUsername());
                    userEntity.setRealName(user.getRealName());
                    userEntity.setCompanyName(user.getCompanyName());
                    if (selectList.contains(userEntity)) {
                        selectList.remove(userEntity);
                    }
                }
                //会议巢m
                intent2.putExtra("type", RocketChatConstants.M);
                intent2.putExtra("companyId", companyId);
                intent2.putParcelableArrayListExtra("selectList", selectList);
                startActivityForResult(intent2, SELECT_USER);
                break;
            case R.id.ll_add_zhuchiren:
                if (selectList == null || selectList.size() < 1) {
                    ToastUtils.showToast("请先添加成员");
                    return;
                }
                UserEntity userEntity1 = new UserEntity();
                userEntity1.setUsername(user.getUsername());
                userEntity1.setRealName(user.getRealName());
                userEntity1.setCompanyName(user.getCompanyName());
                if(!selectList.contains(userEntity1))
                selectList.add(userEntity1);
                Intent intent = new Intent(this, SelectUserActivity.class);
                //会议巢m
                intent.putExtra("type", RocketChatConstants.M);
                intent.putExtra("companyId", companyId);
                intent.putExtra("isSingleChoose", true);
                intent.putParcelableArrayListExtra("allDataList", selectList);
                intent.putParcelableArrayListExtra("selectList", zhuchirenList);
                startActivityForResult(intent, SELECT_ZHUCHIREN);
                break;
//            case R.id.tv_meeting_type:
//                listDialog();
//                break;
        }
    }

    private void createMeetingGroup() {
        if (TextUtils.isEmpty(et_theme.getText().toString().trim())) {
            ToastUtils.showToast(getString(R.string.input_meeting_theme));
            return;
        }
//        if(TextUtils.isEmpty(tv_meeting_type.getText())){
//            Toast.makeText(this, getString(R.string.select_meeting_type), Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (TextUtils.isEmpty(tv_addPerson.getText())) {
            ToastUtils.showToast(getString(R.string.select_add_member));
            return;
        }
        if (TextUtils.isEmpty(tv_startTime.getText())) {
            ToastUtils.showToast(getString(R.string.select_start_time));
            return;
        }
        if (TextUtils.isEmpty(tv_endTime.getText())) {
            ToastUtils.showToast(getString(R.string.select_end_time));
            return;
        }
        showProgressDialog();
        JSONArray jsonArray = getSelectListToJsonArray(selectList);
        JSONArray zhuchirenJsonArray = getSelectListToJsonArray(zhuchirenList);
        Date startDate = DateTime.fromStringToDate(tv_startTime.getText().toString());
        Date endDate = DateTime.fromStringToDate(tv_endTime.getText().toString());

        MethodCallHelper methodCallHelper = new MethodCallHelper(this, hostname);
        methodCallHelper.createMeetingGroupForMobile(userId, et_theme.getText().toString(), jsonArray, zhuchirenJsonArray, startDate, endDate,
                "text", s_encryption.isChecked(), s_qiandao.isChecked(), et_meeting_issue.getText().toString())
                .continueWithTask(task -> {
                    dismissProgressDialog();
                    if (task.isFaulted() || task.getError() != null) {
                        RCLog.e(TAG, "createMeetingGroup: result=" + task.getError(), true);
                        ToastUtils.showToast(task.getError() + "");
                        return null;
                    }
                    String result = task.getResult();
                    RCLog.d("createMeetingGroup: result=" + result, true);
                    if (!TextUtils.isEmpty(result)) {
                        // 创建成功
                        RelationUserDataSubscriber relationUserDataSubscriber = new RelationUserDataSubscriber(MeetingManagerGroupActivity.this, hostname, RealmStore.getOrCreate(hostname));
                        relationUserDataSubscriber.register();
                        ToastUtils.showToast(getString(R.string.create_success));
                        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
                        MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
                        methodCall.getRoomSubscriptions().onSuccess(task1 -> {
                            Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                                    MeetingManagerGroupActivity.this, hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
                            listener.register();
                            return null;
                        }).continueWith(new LogIfError());
                        finish();
                    }
                    return task;
                }, Task.UI_THREAD_EXECUTOR);
    }

    private JSONArray getSelectListToJsonArray(ArrayList<UserEntity> list) {
        JSONArray jsonArray = new JSONArray();
        if (!chat.rocket.android.helper.TextUtils.isEmpty(list)) {
            for (UserEntity entity : list) {
                jsonArray.put(entity.getUsername());
            }
        }
        return jsonArray;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_USER && resultCode == Activity.RESULT_OK && data != null) {//正常搜索人员列表
            selectList = data.getParcelableArrayListExtra("selectList");

            String selectListToName = getSelectListToName(selectList);
            tv_addPerson.setText(selectListToName);
            if (!selectListToName.contains(tv_zhuchiren.getText().toString())) {
                tv_zhuchiren.setText("");
            }
        } else if (requestCode == SELECT_ZHUCHIREN && resultCode == Activity.RESULT_OK) {
            zhuchirenList = data.getParcelableArrayListExtra("selectList");
            tv_zhuchiren.setText(getSelectListToName(zhuchirenList));
        }
    }

    private String getSelectListToName(ArrayList<UserEntity> list) {
        StringBuilder nick = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (UserEntity entity : list) {
                nick.append(entity.getRealName() + ";");
            }
            return nick.substring(0, nick.length() - 1);
        }
        return "";
    }

    private void showDateTimePickDialog(TextView tv) {
        DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(this);
        dateTimePicKDialog.dateTimePicKDialog(tv);
    }

    /**
     * 选择会议类型
     */
//    private void listDialog(){
//        String[] strs = new String[]{getString(R.string.meeting_word)};
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        //设置标题
//        builder.setTitle("请选择");
//        builder.setItems(strs, (dialogInterface, i) -> {
//            tv_meeting_type.setText(strs[i]);
//            dialogInterface.dismiss();
//        });
//        builder.create();
//        builder.show();
//    }

    TimePickerView pvTime;

    public void initTimePicker(int startOrEnd) {
        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        //startDate.set(2013,1,1);
        Calendar endDate = Calendar.getInstance();
        //endDate.set(2020,1,1);

        //正确设置方式 原因：注意事项有说明
//       startDate.set(2018,0,1);
        startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DATE),
                startDate.get(Calendar.HOUR), startDate.get(Calendar.MINUTE) + 1);
        endDate.set(2030, 11, 31, 23, 59);

        pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {

            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                if (startOrEnd == 0) {
                    if (selectedDate.getTimeInMillis() - date.getTime() >= 0) {
                        ToastUtils.showToast("开始会议时间不能小于当前时间");
                    } else
                        tv_startTime.setText(DATE_TIME_FORMAT.format(date));
                } else
                    tv_endTime.setText(DATE_TIME_FORMAT.format(date));
            }
        })
                .setType(new boolean[]{true, true, true, true, true, false})// 默认全部显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确定")//确认按钮文字
                .setContentTextSize(18)//滚轮文字大小
                .setTitleSize(20)//标题文字大小
//               .setTitleText("时间")//标题文字
                .setOutSideCancelable(false)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(false)//是否循环滚动
//               .setTitleColor(Color.BLACK)//标题文字颜色
//               .setSubmitColor(Color.BLUE)//确定按钮文字颜色
//               .setCancelColor(Color.BLUE)//取消按钮文字颜色
//               .setTitleBgColor(0xFF666666)//标题背景颜色 Night mode
//               .setBgColor(0xFF333333)//滚轮背景颜色 Night mode
                .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setRangDate(startDate, endDate)//起始终止年月日设定
                .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(false)//是否显示为对话框样式
                .build();
    }
}
