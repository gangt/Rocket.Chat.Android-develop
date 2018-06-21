package chat.rocket.android.activity.business;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;

public class ZongJieActivity extends BusinessBaseActivity {

    private ImageView iv_back;
    private TextView tv_content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zongjie);

        initView();
        initData();
    }

    private void initData() {
        iv_back.setOnClickListener(v -> finish());

        String rawContent = getIntent().getStringExtra("rawContent");
        tv_content.setText(rawContent);
    }

    private void initView() {
        TextView tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        tv_content = findViewById(R.id.tv_content);

        findViewById(R.id.tv_create).setVisibility(View.GONE);
        tv_title.setText(R.string.meeting_zongjie);
    }


}
