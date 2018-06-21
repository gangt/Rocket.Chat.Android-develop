package chat.rocket.android.activity.business;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import chat.rocket.android.R;
import chat.rocket.android.adapter.OrgDeleteAdapter;
import chat.rocket.persistence.realm.models.ddp.UserEntity;

/**
 * Created by helloworld on 2018/4/4
 */

public class OrgDeleteUserActivity extends BusinessBaseActivity implements View.OnClickListener {

    private RecyclerView rv_user;
    private ArrayList<UserEntity> allSelectUserEntityList;
    private OrgDeleteAdapter adapter;
    TextView tv_title;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_delete_user);
        initView();
        initData();
    }

    private void initData() {
        allSelectUserEntityList = new ArrayList<>();
        ArrayList<UserEntity> alreadySelectList = getIntent().getParcelableArrayListExtra("selectList");
        if (alreadySelectList != null) {
            allSelectUserEntityList.addAll(alreadySelectList);
        }

        adapter = new OrgDeleteAdapter(this, allSelectUserEntityList);
        rv_user.setLayoutManager(new LinearLayoutManager(this));
        rv_user.setAdapter(adapter);
    }


    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        ArrayList<UserEntity> alreadySelectList = getIntent().getParcelableArrayListExtra("selectList");
        if (alreadySelectList != null)
        tv_title.setText(String.format(getString(R.string.select_member_format), ""+alreadySelectList.size()));
        rv_user = findViewById(R.id.rv_user);
        TextView sure = findViewById(R.id.tv_create);
        sure.setText(getString(R.string.ok));
        ImageView iv_back = findViewById(R.id.iv_back);
        sure.setOnClickListener(this);
        iv_back.setOnClickListener(this);
    }

    public void setTitle(int count){
        tv_title.setText(String.format(getString(R.string.select_member_format), ""+count));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_create:
                Intent intent=new Intent();
                intent.putParcelableArrayListExtra("selectList",adapter.getData());
                setResult(RESULT_OK,intent);
                onBackPressed();
                break;
        }
    }
}
