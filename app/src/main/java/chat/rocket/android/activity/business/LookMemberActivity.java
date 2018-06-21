package chat.rocket.android.activity.business;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.adapter.OrgMemberAdapter;
import chat.rocket.android.recyclertreeview.selectuserbinder.OrgMember;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;

/**
 * Created by helloworld on 2018/4/11
 */

public class LookMemberActivity extends BusinessBaseActivity {

    private RecyclerView rv_list;
    private List<OrgMember> allDataList;
    private RealmUserRepository realmUserRepository;
    private String hostname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_member);

        initView();
        allDataList = (List<OrgMember>) getIntent().getSerializableExtra("selectList");
        hostname = getIntent().getStringExtra("hostname");
        realmUserRepository = new RealmUserRepository(hostname);
        setAdapter();
    }

    private void setAdapter(){
        OrgMemberAdapter memberAdapter = new OrgMemberAdapter(this, allDataList,realmUserRepository);
        //设置为一个6列的纵向网格布局
        LinearLayoutManager mLayoutManager=new LinearLayoutManager(this, GridLayoutManager.VERTICAL,false);
        rv_list.setLayoutManager(mLayoutManager);
        rv_list.setAdapter(memberAdapter);
    }

    private void initView() {
        TextView tv_title = findViewById(R.id.tv_title);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.tv_create).setVisibility(View.GONE);
        rv_list = findViewById(R.id.rv_list);

        tv_title.setText(R.string.select_member);
    }
}
