//package chat.rocket.android.activity.business;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import chat.rocket.android.R;
//
///**
// * Created by zhangxiugao on 2018/1/10
// */
//
//public class SearchGroupOrUserActivity extends Activity {
//
//    private EditText et_search;
//    private TextView tv_cancel;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_search_group_user);
//
//        initView();
//        setListener();
//    }
//
//    private void setListener() {
//        tv_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//    }
//
//    private void initView() {
//        et_search = findViewById(R.id.et_search);
//        tv_cancel = findViewById(R.id.tv_cancel);
//    }
//
//}
