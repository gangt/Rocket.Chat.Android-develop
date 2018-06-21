package chat.rocket.android.activity.business;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.adapter.QianDaoAdapter;
import chat.rocket.android.adapter.QianDaoLeftAdapter;
import chat.rocket.android.entity.QianDaoAttendance;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.core.models.Attendance;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;

/**
 * Created by helloworld on 2018/3/29
 */

public class QianDaoActivity extends BusinessBaseActivity{

    private ImageView iv_back;
    private String hostname;
    private List<Attendance> hasSignAttendanceList;
    private List<QianDaoAttendance> noSignAttendanceList;
    private RecyclerView rv_qiandao;
    private TextView tv_right, tv_left;
    private int currentSelect = 1;
    private static final int CLICK_LEFT = 0;
    private static final int CLICK_RIGHT = 1;
    private QianDaoAdapter adapter;
    private Room currentRoom;
    private QianDaoLeftAdapter leftAdapter;
    private RealmUserRepository userRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qiandao);

        initViews();
        initData();
        setAdapter();
        setListener();
    }

    private void setAdapter() {
        adapter = new QianDaoAdapter(this, hasSignAttendanceList, userRepository);
        rv_qiandao.setLayoutManager(new LinearLayoutManager(this));
        rv_qiandao.setAdapter(adapter);
    }

    private void setLeftAdapter() {
        leftAdapter = new QianDaoLeftAdapter(this, noSignAttendanceList);
        rv_qiandao.setLayoutManager(new LinearLayoutManager(this));
        rv_qiandao.setAdapter(leftAdapter);
    }

    private void initData() {
        hostname = getIntent().getStringExtra("hostname");
        String roomId = getIntent().getStringExtra("roomId");
        userRepository = new RealmUserRepository(hostname);

        RealmRoomRepository roomRepository = new RealmRoomRepository(hostname);
        currentRoom = roomRepository.getRoomByRoomId(roomId);
        searchHasSignAttendanceList();
    }

    private void searchHasSignAttendanceList() {
        if(hasSignAttendanceList == null){
            hasSignAttendanceList = new ArrayList<>();
        }else{
            hasSignAttendanceList.clear();
        }
        List<Attendance> attendanceList = currentRoom.getAttendanceList();
        if(!TextUtils.isEmpty(attendanceList)) {
            hasSignAttendanceList.addAll(attendanceList);
        }
    }

    private void searchNoSignAttendanceList(){
        List<String> usernames = currentRoom.getUsernames();

        String[] str = new String[]{};
        if(usernames != null && usernames.size() > 0){
            str = usernames.toArray(new String[0]);
        }
        List<User> userList = userRepository.getByNameAll(str);

        if(noSignAttendanceList == null) {
            noSignAttendanceList = new ArrayList<>();
        }else{
            noSignAttendanceList.clear();
        }
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if(!contains(user)) {
                QianDaoAttendance attendance = new QianDaoAttendance(user.getId(), user.getUsername(), user.getAvatar());
                noSignAttendanceList.add(attendance);
            }
        }
    }

    private boolean contains(User user){
        if(TextUtils.isEmpty(hasSignAttendanceList)) return false;
        for(Attendance attendance : hasSignAttendanceList){
            if(user.getUsername().equals(attendance.getUserName())){
                return true;
            }
        }
        return false;
    }

    private void initViews() {
        TextView tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        findViewById(R.id.tv_create).setVisibility(View.GONE);
        rv_qiandao = findViewById(R.id.rv_qiandao);
        tv_left = findViewById(R.id.tv_left);
        tv_right = findViewById(R.id.tv_right);

        tv_title.setText(R.string.meeting_qiandao);
    }

    private void setListener() {
        iv_back.setOnClickListener(v -> finish());

        tv_left.setOnClickListener(v -> {
            if(currentSelect == CLICK_LEFT) return;
            selectTextViewStatus(CLICK_LEFT);
            currentSelect = CLICK_LEFT;
            searchNoSignAttendanceList();
            setLeftAdapter();
        });

        tv_right.setOnClickListener(v -> {
            if(currentSelect == CLICK_RIGHT) return;
            selectTextViewStatus(CLICK_RIGHT);
            currentSelect = CLICK_RIGHT;
            searchHasSignAttendanceList();
            setAdapter();
        });
    }

    private void selectTextViewStatus(int status) {
        // click left
        if(status == CLICK_LEFT){
            tv_left.setBackgroundResource(R.drawable.qiandao_left_lan_bg);
            tv_left.setTextColor(getResources().getColor(R.color.white));
            tv_right.setBackgroundResource(R.drawable.qiandao_right_bai_bg);
            tv_right.setTextColor(getResources().getColor(R.color.color_39c3fa));
        }else{
            // click right
            tv_left.setBackgroundResource(R.drawable.qiandao_left_bai_bg);
            tv_left.setTextColor(getResources().getColor(R.color.color_39c3fa));
            tv_right.setBackgroundResource(R.drawable.qiandao_right_lan_bg);
            tv_right.setTextColor(getResources().getColor(R.color.white));
        }
    }

}
