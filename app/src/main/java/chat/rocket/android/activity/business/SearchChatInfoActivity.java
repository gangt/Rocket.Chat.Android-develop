package chat.rocket.android.activity.business;

import android.app.Activity;
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
import java.util.regex.Pattern;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.adapter.SearchChatAdapter;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.MessageRefHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.widget.emotionkeyboard.utils.EmotionUtils;
import chat.rocket.core.models.Message;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;

/**
 * Created by helloworld on 2018/3/17
 */

public class SearchChatInfoActivity extends Activity {

    private TextView tv_title;
    private ImageView iv_back, clear;
    private String roomId, hostname;
    private RecyclerView rv_chat_info;
    private EditText et_search;
    private List<Message> allMessageByRoomId, resultMessageList;
    private SearchChatAdapter searchChatAdapter;
    private RelativeLayout rl_empty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_chat);

        initViews();
        initData();
        setAdapter();
        setListener();
    }

    private void initData() {
        roomId = getIntent().getStringExtra("roomId");
        hostname = getIntent().getStringExtra("hostname");
        RealmMessageRepository messageRepository = new RealmMessageRepository(hostname);
        allMessageByRoomId = messageRepository.getAllMessageByRoomId(roomId);
        resultMessageList = new ArrayList<>();
        if (allMessageByRoomId == null)
            return;
        for (Message msg : allMessageByRoomId) {
            if (!TextUtils.isEmpty(msg.getMessage()) && msg.getMessage().
                    contains(RocketChatCache.INSTANCE.getSelectedServerHostname()) && msg.getMessage().contains("?msg=")) {
                String msgId = MessageRefHelper.getInstance().getMsgId(msg.getMessage());
                Message msg1 = MessageRefHelper.getInstance().getMsg(messageRepository, msgId);
                resultMessageList.add(Message.builder().setId(msg.getId()).setMessage(MessageRefHelper.getInstance().getReplayText(msg.getMessage(), msg1))
                        .setTimestamp(msg.getTimestamp())
                        .setRoomId(msg.getRoomId())
                        .setSyncState(msg.getSyncState())
                        .setGroupable(msg.isGroupable())
                        .setEditedAt(msg.getEditedAt())
                        .setUser(msg.getUser())
                        .build());
            } else
                resultMessageList.add(msg);
        }
    }

    private void initViews() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        clear = findViewById(R.id.clear);
        et_search = findViewById(R.id.et_search);
        rv_chat_info = findViewById(R.id.rv_chat_info);
        rl_empty = findViewById(R.id.rl_empty);
        et_search.setHint(R.string.search_chat_info);

        findViewById(R.id.tv_create).setVisibility(View.GONE);
        tv_title.setText(R.string.search_chat_info);
    }

    private void setAdapter() {
        searchChatAdapter = new SearchChatAdapter(this, resultMessageList);
        rv_chat_info.setLayoutManager(new LinearLayoutManager(this));
        rv_chat_info.setAdapter(searchChatAdapter);
        searchChatAdapter.setDatas(null, null);

        searchChatAdapter.setOnItemClickListener((view, message) -> {
            KeyboardHelper.hideSoftKeyboard(this);
            RocketChatCache.INSTANCE.setIsClickHostoryInfo(message.getTimestamp() + ";" + message.getMessage());
            setResult(Activity.RESULT_OK);
            finish();
        });
    }

    private void setListener() {
        iv_back.setOnClickListener(v -> finish());
        clear.setOnClickListener(v -> {
            et_search.setText("");
            rl_empty.setVisibility(View.GONE);
            rv_chat_info.setVisibility(View.GONE);
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String term = s.toString();
                filterSearchData(term);

            }
        });
    }

    private void filterSearchData(String term) {
        if (TextUtils.isEmpty(resultMessageList)) {
            return;
        }
        List<Message> tempDataList = new ArrayList<>();
        if (term.length() > 0) {
            clear.setVisibility(View.VISIBLE);
            new Thread(() -> {
                for (Message entity : resultMessageList) {
                    String message = entity.getMessage();
                    EmotionUtils._shortNameToUnicodes.toString();
                    for (int i = 0; i < EmotionUtils._shortNameToUnicodes.length; i++) {
                       message=message.replace(EmotionUtils._shortNameToUnicodes[i],"");
                    }
                    if (message.contains(term)) {
                        tempDataList.add(entity);
                        break;
                    }
                }
                runOnUiThread(() -> {
                    if (tempDataList.size() == 0) {
                        rl_empty.setVisibility(View.VISIBLE);
                        rv_chat_info.setVisibility(View.GONE);
                    } else {
                        rl_empty.setVisibility(View.GONE);
                        rv_chat_info.setVisibility(View.VISIBLE);
                    }
                    searchChatAdapter.setDatas(tempDataList, term);
                });
            }).start();

        } else {
            clear.setVisibility(View.GONE);
            rl_empty.setVisibility(View.GONE);
            searchChatAdapter.setDatas(tempDataList, term);
        }

    }

}
