package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.adapter.MeettingHistoryAdapter;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserRoomChanged;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;

/**
 * Created by jumper on 2018/3/29
 */


public class MeettingHistoryActivity extends BusinessBaseActivity {

    private TextView tv_title;
    private ImageView iv_back,iv_clear;
    private RecyclerView recyclerView;
    private EditText et_search;
    private List<Room> datas;
    private MeettingHistoryAdapter adapter;
    private MethodCallHelper methodCallHelper;
    private RelativeLayout rl_empty;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetting_history);
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        methodCallHelper = new MethodCallHelper(this, hostname);
        initViews();
        initData();
        setAdapter();
        setListener();
    }

    @SuppressLint("RxLeakedSubscription")
    private void initData() {
        datas=new ArrayList<>();
        RealmUserRepository userRepository = new RealmUserRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
        RealmRoomRepository repository=new RealmRoomRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
        userRepository.getCurrent().distinctUntilChanged()
                .subscribe(userOptional -> {
                    User user = userOptional.get();
                    if (user!=null){
                        repository.getHistorySetting(user.getCompanyId()).subscribe(subscriptions -> {
                            if(subscriptions.size()>0){
                               List<Room> TempDatas=subscriptions;
                                    for (Room room:TempDatas){
                                        if (repository.getRoomByRoomId(room.getId()).getUUserName().equals(RocketChatCache.INSTANCE.getUserUsername()))
                                            datas.add(room);
                                    }
//                                RCLog.d("meettingHistroyDatas->>"+datas.toString());
                            }
                        },RCLog::e);
                    }
                }, RCLog::e);

    }

    private void initViews() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        iv_clear = findViewById(R.id.clear);
        et_search = findViewById(R.id.et_search);
        rl_empty = findViewById(R.id.rl_empty);
        recyclerView = findViewById(R.id.recycler_meetting_history);
        et_search.setHint(R.string.meeting_history_hint);

        findViewById(R.id.tv_create).setVisibility(View.GONE);
        tv_title.setText(R.string.meeting_history_title);
    }

    private void setAdapter() {
        if (datas!=null&&datas.size()==0)
            rl_empty.setVisibility(View.VISIBLE);
        else
            rl_empty.setVisibility(View.GONE);
        adapter = new MeettingHistoryAdapter(this, datas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((view, tempData) -> {
            KeyboardHelper.hideSoftKeyboard(this);
            Intent intent = new Intent(this, ChatMainActivity.class);
            intent.putExtra("roomId",tempData.getId());
            RocketChatCache.INSTANCE.setSelectedRoomId(tempData.getId());
            if ("false".equals(tempData.getEncrypt())||"0".equals(tempData.getEncrypt()))
            startActivity(intent);
//            finish();
        });

        adapter.setRestartListener(new MeettingHistoryAdapter.ReStartListener() {
            @Override
            public void restart(View view,int position) {
                /**重新开始会议*/
                methodCallHelper.pauseOrRestartMeetting(datas.get(position).getId(), datas.get(position).getUId(),true)
                        .continueWithTask(new Continuation<String, Task<Object>>() {
                    @Override
                    public Task<Object> then(Task<String> task) throws Exception {
                        if (task.getError() != null) {
                            RCLog.e("restartMeetting" + task.getError().getMessage(), true);
                        }else {
                            ToastUtils.showToast("重新开始会议……");
                            view.setVisibility(View.GONE);
                            String hostname=RocketChatCache.INSTANCE.getSelectedServerHostname();
                            RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
                            MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
                            methodCall.getRoomSubscriptions().onSuccess(task1 -> {
                                Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                                        MeettingHistoryActivity.this, hostname, realmHelper, RocketChatCache.INSTANCE.getUserId());
                                listener.register();
                                return null;
                            }).continueWith(new LogIfError());
                            finish();
                        }

                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);
            }
        });
    }

    private void setListener() {
        iv_clear.setOnClickListener(view ->{
            et_search.setText("");
        } );
        iv_back.setOnClickListener(v ->{
                KeyboardHelper.hideSoftKeyboard(this);
                finish();
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String term = s.toString().trim();
                if (term.length() > 0) {
                    iv_clear.setVisibility(View.VISIBLE);
                }else {
                    iv_clear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String term = s.toString();
                filterSearchData(term);
            }
        });
    }

    private void filterSearchData(String term){
        List<Room> tempDataList =new ArrayList<>();
        for (Room data:datas){
            String startTime = DateTime.fromEpocMs(data.getStartTime(), DateTime.Format.DATE_TIME2);
            String endTime = DateTime.fromEpocMs(data.getEndTime(), DateTime.Format.DATE_TIME2);
            String subtitle=data.getUUserName().split("&")[0]+"("+data.getCompanyName()+")";
            if (data.getDisplayName().contains(term)||startTime.contains(term)||endTime.contains(term)||subtitle.contains(term))
                tempDataList.add(data);
        }
        if (tempDataList.size()==0){
            rl_empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);}
        else{
            rl_empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);}
        adapter.setDatas(tempDataList, term);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        KeyboardHelper.hideSoftKeyboard(this);
    }
}
