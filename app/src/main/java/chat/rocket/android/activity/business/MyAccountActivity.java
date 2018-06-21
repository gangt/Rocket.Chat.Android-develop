package chat.rocket.android.activity.business;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;

/**
 * Created by zhangxiugao on 2018/1/8
 */

public class MyAccountActivity extends Activity implements View.OnClickListener{

    private TextView tv_title,tv_create;
    private ImageView iv_back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        initView();
        setListener();
    }

    private void setListener() {
        iv_back.setOnClickListener(this);
        tv_create.setOnClickListener(this);
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        tv_create = findViewById(R.id.tv_create);
        tv_create.setVisibility(View.GONE);

        tv_title.setText(R.string.my_account);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
        }

    }
}
