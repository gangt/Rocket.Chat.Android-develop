package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.FuJianAdapter;
import chat.rocket.android.entity.VideoBean;
import chat.rocket.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.CallOtherOpeanFile;
import chat.rocket.android.helper.DownloadUtil;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.PermissionsUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.widget.helper.FileUtils;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.Message;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by helloworld on 2018/3/8
 */

public class FuJianActivity extends BusinessBaseActivity {

    private static final int DOWNLOAD_FAILED = 3;
    private static final int DOWNLOAD_SUCCESS = 4;
    private TextView tv_title;
    private ImageView iv_back;
    private String roomId,hostname;
    private RecyclerView rv_cundang;
    private List<Message> allMessageByRoomId;
    private RocketChatAbsoluteUrl absoluteUrl;
    private EditText et_search;
    private ImageView clear;
    private FuJianAdapter fuJianAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cundang_list);

        initHandler();
        initViews();
        initData();
        setAdapter();
        setListener();
    }

    @SuppressLint("RxLeakedSubscription")
    private void initData() {
        roomId = getIntent().getStringExtra("roomId");
        hostname = getIntent().getStringExtra("hostname");
        RealmMessageRepository messageRepository = new RealmMessageRepository(hostname);
        allMessageByRoomId = messageRepository.getAllAttachmentByRoomId(roomId);

        RealmUserRepository userRepository = new RealmUserRepository(hostname);
        AbsoluteUrlHelper absoluteUrlHelper = new AbsoluteUrlHelper(
                hostname,
                new RealmServerInfoRepository(),
                userRepository,
                new SessionInteractor(new RealmSessionRepository(hostname)));

        absoluteUrlHelper.getRocketChatAbsoluteUrl()
                .cache()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        serverUrl -> {
                            if (serverUrl.isPresent()) {
                                absoluteUrl = serverUrl.get();
                            }
                        },
                        Logger.INSTANCE::report
                );
    }

    private void setListener() {
        iv_back.setOnClickListener(v -> {
            finish();
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

    private void filterSearchData(String term){
        if (chat.rocket.android.helper.TextUtils.isEmpty(allMessageByRoomId)) return;
        List<Message> tempDataList = new ArrayList<>();
        if(term.length() > 0){
            clear.setVisibility(View.VISIBLE);
            for(Message entity : allMessageByRoomId){
                Attachment attachment = entity.getAttachments().get(0);
                String title = attachment.getAttachmentTitle().getTitle();
                String pinyin = entity.getUser().getRealName();
                if(title.toLowerCase().contains(term)||(pinyin != null && pinyin.contains(term))){
                    tempDataList.add(entity);
                }
            }
        }else{
            clear.setVisibility(View.GONE);
            tempDataList.addAll(allMessageByRoomId);
        }
        fuJianAdapter.setDatas(tempDataList);
    }

    private void initViews() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        et_search = findViewById(R.id.et_search);
        clear=findViewById(R.id.clear);
        rv_cundang = findViewById(R.id.rv_cundang);
        et_search.setHint(R.string.search_name);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        findViewById(R.id.tv_create).setVisibility(View.GONE);
        tv_title.setText(R.string.fujian_list);
        clear.setOnClickListener(v ->
            et_search.setText("")
        );
    }

    private ArrayList<VideoBean> videoPicList;
    private void setAdapter() {
        if(allMessageByRoomId == null || allMessageByRoomId.size() == 0){
            ToastUtils.showToast(getString(R.string.no_data));
            return;
        }

        fuJianAdapter = new FuJianAdapter(this, allMessageByRoomId);
        rv_cundang.setLayoutManager(new LinearLayoutManager(this));
        rv_cundang.setAdapter(fuJianAdapter);

        fuJianAdapter.setOnItemClickListener((view, message) -> {
            videoPicList = new ArrayList<>();
            Attachment attachment = message.getAttachments().get(0);
            long timestamp = message.getTimestamp();
            String title = attachment.getAttachmentTitle().getTitle();
            String link = attachment.getAttachmentTitle().getLink();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(link)) {
                RCLog.e("title or link is null", true);
                return;
            }

            int selectPosition = 0;//点击图片或视频 整个图片视频集合列表中的位置
            boolean isSelectOk = false;
            for (int i = 0; i < allMessageByRoomId.size(); i++) {
                Attachment att = allMessageByRoomId.get(i).getAttachments().get(0);
                String attLink = att.getAttachmentTitle().getLink();

                if (TextUtils.isEmpty(attLink)) continue;
                if (FileUtils.isPhoto(attLink)) {
                    VideoBean bean = new VideoBean(VideoBean.PICTURE, absoluteUrl.from(attLink));
                    videoPicList.add(bean);
                    if (attLink.equals(link) && !isSelectOk) {
                        selectPosition = videoPicList.size() - 1;
                        isSelectOk = true;
                    }
                } else if (FileUtils.isVideo(attLink)) {
                    VideoBean bean = new VideoBean(VideoBean.VIDEO, absoluteUrl.from(attLink));
                    videoPicList.add(bean);
                    if (attLink.equals(link) && !isSelectOk) {
                        selectPosition = videoPicList.size() - 1;
                        isSelectOk = true;
                    }
                }
            }

            if (FileUtils.isVideo(title) || FileUtils.isPhoto(title)) {
                Intent intent = new Intent(FuJianActivity.this, PlayerViewActivity.class);
                intent.putParcelableArrayListExtra("video_url", videoPicList);
                intent.putExtra("selectPosition", selectPosition);
                FuJianActivity.this.startActivity(intent);
                return;
            }
            FuJianActivity.this.postDataWithParame(absoluteUrl.from(link), DownloadUtil.get().getFileName(title, timestamp));
        });
    }

    private void postDataWithParame(String url, String fileName) {
        String appDownloadDir = FileUtils.getAppDownloadDir(RocketChatApplication.getInstance());
        File file = new File(appDownloadDir+fileName);
        if(file.exists()){
            handler.obtainMessage(DOWNLOAD_SUCCESS, appDownloadDir+fileName).sendToTarget();
            RCLog.d("file.exists() open file", true);
            return;
        }
        showProgressDialog();
        PermissionsUtils.verifyStoragePermissions(this);
        DownloadUtil.get().download(url, appDownloadDir, fileName, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                handler.obtainMessage(DOWNLOAD_SUCCESS, appDownloadDir+fileName).sendToTarget();
            }
            @Override
            public void onDownloading(int progress) {
//                progressBar.setProgress(progress);
            }

            @Override
            public void onDownloadFailed() {
                handler.sendEmptyMessage(DOWNLOAD_FAILED);
            }
        });
    }

    private Handler handler;
    @SuppressLint("HandlerLeak")
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                dismissProgressDialog();
                switch (msg.what){
                    case DOWNLOAD_FAILED:
                        ToastUtils.showToast("下载失败");
                        break;
                    case DOWNLOAD_SUCCESS:
                        String fileName = (String) msg.obj;
                        handlerFile(fileName);
                        break;
                }
            }
        };
    }

    private void handlerFile(String fileName) {
        File file = new File(fileName);
        CallOtherOpeanFile.openFile(FuJianActivity.this, file);
    }

}
