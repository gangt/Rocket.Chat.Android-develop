package chat.rocket.android.activity.business;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.log.RCLog;

/**
 * Created by helloworld on 2018/2/2
 */

public class UpdateGroupActivity extends BusinessBaseActivity implements View.OnClickListener{

    private TextView tv_title,tv_create;
    private ImageView iv_back,iv_delete;
    private EditText et_groupName;
    private String roomId;
    private String roomTopic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group);

        roomId = getIntent().getStringExtra("roomId");
        initView();
        setListener();
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        tv_create.setOnClickListener(this);
        iv_delete.setOnClickListener(this);
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        tv_create = findViewById(R.id.tv_create);
        iv_delete = findViewById(R.id.iv_delete);
        et_groupName = findViewById(R.id.et_groupName);

        tv_create.setText(R.string.finish);
        tv_title.setText(R.string.update_group_name);

        et_groupName.setFocusable(true);
        et_groupName.setFocusableInTouchMode(true);
        et_groupName.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        String subscriptionDisplayName = getIntent().getStringExtra("subscriptionDisplayName");
        roomTopic = getIntent().getStringExtra("roomTopic");
        String name = subscriptionDisplayName == null ? roomTopic : subscriptionDisplayName;
        et_groupName.setText(name);
        et_groupName.setSelection(name.length());//将光标移至文字末尾
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_delete:
                et_groupName.setText("");
                break;
            case R.id.tv_create:
                blockUserForMobile();
                break;
        }
    }

    private void blockUserForMobile(){
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        String name = et_groupName.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            ToastUtils.showToast(getResources().getString(R.string.please_input_content));
            return;
        }
        showProgressDialog();
        MethodCallHelper methodCallHelper = new MethodCallHelper(this, hostname);
        if(roomTopic == null){
            methodCallHelper.modifyRoomInfo(roomId, et_groupName.getText().toString())
                    .continueWithTask(task -> {
                        dismissProgressDialog();
                        if (task.isFaulted()) {
                            RCLog.e("blockUserForMobile: result=" + task.getError(), true);
                            return null;
                        }
                        ToastUtils.showToast(getResources().getString(R.string.update_finish));
                        Intent intent = new Intent();
                        intent.putExtra("updateName", name);
                        setResult(Activity.RESULT_OK, intent);
                        UpdateGroupActivity.this.finish();
                        return null;
                    }, Task.UI_THREAD_EXECUTOR);
        }else {
            methodCallHelper.modifyRoomInfo2(roomId, et_groupName.getText().toString())
                    .continueWithTask(task -> {
                        dismissProgressDialog();
                        if (task.isFaulted()) {
                            RCLog.e("blockUserForMobile: result=" + task.getError(), true);
                            return null;
                        }
                        ToastUtils.showToast(getResources().getString(R.string.update_finish));
                        Intent intent = new Intent();
                        intent.putExtra("updateName", name);
                        setResult(Activity.RESULT_OK, intent);
                        UpdateGroupActivity.this.finish();
                        return null;
                    }, Task.UI_THREAD_EXECUTOR);
        }
    }
}
