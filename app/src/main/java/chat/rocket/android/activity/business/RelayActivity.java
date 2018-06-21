package chat.rocket.android.activity.business;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bolts.Task;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.adapter.RelayAdapter;
import chat.rocket.android.adapter.RelayImagAdapter;
import chat.rocket.android.api.FileUploadingHelper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.DownloadUtil;
import chat.rocket.android.helper.FileUploadHelper;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.MessageRefHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.widget.emotionkeyboard.utils.EmotionUtils;
import chat.rocket.android.widget.emotionkeyboard.utils.SpanStringUtils;
import chat.rocket.android.widget.helper.FileUtils;
import chat.rocket.core.interactors.MessageInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.AttachmentTitle;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by jumper_C on 2018/5/11.
 */

public class RelayActivity extends BusinessBaseActivity {
    private TextView tv_title;
    private TextView tv_create;
    private ImageView iv_back, iv_clear;
    private RecyclerView recyclerView;
    private EditText et_search;
    private List<Subscription> datas;
    private RelayAdapter adapter;
    private MethodCallHelper methodCallHelper;
    private FileUploadingHelper methodCall;
    RealmUserRepository userRepository;
    RealmSubscriptionRepository realmSubscriptionRepository;
    RealmRoomRepository roomRepository;
    RealmMessageRepository messageRepository;
    RocketChatAbsoluteUrl absoluteUrl;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relay);
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
        methodCall = new FileUploadingHelper(realmHelper);
        methodCallHelper = new MethodCallHelper(this, hostname);
        initViews();
        initData();
        setAdapter();
        setListener();
    }

    String msgId;
    String hostname;
    boolean isFile;

    @SuppressLint("RxLeakedSubscription")
    private void initData() {
        msgId = getIntent().getStringExtra("msgId");
        isFile = getIntent().getBooleanExtra("isFile", false);
        hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        realmSubscriptionRepository = new RealmSubscriptionRepository(hostname);
        userRepository = new RealmUserRepository(hostname);
        roomRepository = new RealmRoomRepository(hostname);
        messageRepository = new RealmMessageRepository(hostname);
        userRepository.getCurrent().distinctUntilChanged()
                .subscribe(userOptional -> {
                    User user = userOptional.get();
                    if (user != null) {
                        realmSubscriptionRepository.getAllSub(user.getId()).subscribe(subscriptions -> {
                            if (subscriptions.size() > 0) {
                                datas = new ArrayList<>();
                                datas.addAll(subscriptions);
                            }
                        }, RCLog::e);
                    }
                }, RCLog::e);
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

    private void initViews() {
        tv_title = findViewById(R.id.tv_title);
        iv_back = findViewById(R.id.iv_back);
        iv_clear = findViewById(R.id.clear);
        et_search = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recyclerView);
        et_search.setHint(R.string.relay_hint);

        tv_create = findViewById(R.id.tv_create);
        tv_create.setText("完成");
        tv_create.setOnClickListener(view ->{
            if(getSelectedDatas().size()>0)
                showSendRelayDialog(getSelectedDatas());
        }
        );
        tv_title.setText(R.string.relay_title);
    }

    public void showSendRelayDialog(List<Subscription> datas) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.quick_option_dialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_send_relay, null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        TextView sure = view.findViewById(R.id.tv_sure);
        TextView cancle = view.findViewById(R.id.tv_cancel);
        TextView content = view.findViewById(R.id.tv_content);
        LinearLayout ll_content = view.findViewById(R.id.ll_content);
//        EditText editText = view.findViewById(R.id.edit_relay);
        List<String> stringList=new ArrayList<>();
        for (Subscription subscription:datas){
            try {
                stringList.add(userRepository.getAvatarByUsername(subscription.getName()));
            } catch (Exception e) {
                stringList.add("");
            }
        }
        sure.setText("发送（"+datas.size()+"）");
//        String[] strs = {"", "", ""};
        RelayImagAdapter adapter = new RelayImagAdapter(this, stringList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        Dialog alertDialog = builder.show();
        alertDialog.setContentView(view);
        Window dialogWindow = alertDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        Display d = getWindowManager().getDefaultDisplay();
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.height = dip2px(this,150);
        lp.width = (int) (d.getWidth() * 0.75);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.CENTER);
        List<Message> msgs = messageRepository.getAllMessageByMessageId(msgId);
        String text="";
        String msgText=msgs.get(0).getMessage();
        if (!TextUtils.isEmpty(msgText)){
            if (msgText.contains("?msg=")&&msgText.contains(hostname)){
                String msgId = MessageRefHelper.getInstance().getMsgId(msgText);
                Message msg1= MessageRefHelper.getInstance().getMsg(messageRepository,msgId);
                if (msg1==null)
                {
                    if (msgText.contains("@")&&msgText.contains("&"))
                        text=(msgText.split(msgId+"\\) ")[1].split(" +")[1]);//@的方法
                    else
                        text=(msgText.split(msgId+"\\) ")[1]);//@的方法
                }
                else
                    text=  MessageRefHelper.getInstance().getReplayText(msgText,msg1);
            }else
            text= msgText;
        }else {
            text=msgs.get(0).getAttachments().get(0).getAttachmentTitle().getTitle();
        }
        SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,this, content, text);
        content.setText(spannableString);
        sure.setOnClickListener(view1 -> {
                    if (isFile) {
                        try {
                            List<Subscription> rooms=getSelectedDatas();
                            for (int i = 0; i <rooms.size() ; i++) {
                                sendFile(msgs.get(0), rooms.get(i));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        sendMessage(msgs.get(0).getMessage(), getSelectedDatas());
                    }
                    setResult(RESULT_OK);
                    onBackPressed();
                }
        );
        ll_content.setOnClickListener(view1 -> {
            showSendRelayDialog2(msgs.get(0));
        });
        cancle.setOnClickListener(view1 -> alertDialog.dismiss());
    }
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public void showSendRelayDialog2(Message msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.quick_option_dialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_send_relay_show, null);
        TextView title = view.findViewById(R.id.title);
        ImageView imageView = view.findViewById(R.id.image);
        ImageView imageFile = view.findViewById(R.id.image_file);
        TextView textView = view.findViewById(R.id.text);
        TextView back = view.findViewById(R.id.back);
        String text="";
        String msgText=msg.getMessage();
        if (!TextUtils.isEmpty(msgText)){
            textView.setVisibility(View.VISIBLE);
                if (msgText.contains("?msg=")&&msgText.contains(hostname)){
                    String msgId = MessageRefHelper.getInstance().getMsgId(msgText);
                    Message msg1= MessageRefHelper.getInstance().getMsg(messageRepository,msgId);
                    text=  MessageRefHelper.getInstance().getReplayText(msgText,msg1);
                }else
                    text= msgText;
            SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,this, textView, text);
            textView.setText(spannableString);
        }else if (msg.getAttachments()!=null){
            String fileTitle=msg.getAttachments().get(0).getAttachmentTitle().getTitle();
            String imageUrl=msg.getAttachments().get(0).getAttachmentTitle().getLink();
            title.setVisibility(View.VISIBLE);
            title.setText(fileTitle);
            boolean isPhoto=FileUtils.isPhoto(fileTitle);
            if (isPhoto){
                imageView.setVisibility(View.VISIBLE);
                RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                        .error(R.drawable.default_hd_avatar);
                Glide.with(RocketChatApplication.getInstance())
                        .load(absoluteUrl.from(imageUrl))
                        .apply(options)
                        .into(imageView);
            }else {
                imageFile.setVisibility(View.VISIBLE);
                showTitleIcon(fileTitle,imageFile);
            }
        }
        Dialog alertDialog = builder.show();
        alertDialog.setContentView(view);
        Window dialogWindow = alertDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        back.setOnClickListener(view1 -> alertDialog.dismiss());
    }

    public void showUnderSexHint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.quick_option_dialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_send_relay_undersex, null);
        TextView back = view.findViewById(R.id.tv_sure);
        Dialog alertDialog = builder.show();
        alertDialog.setContentView(view);
        Window dialogWindow = alertDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        Display d = getWindowManager().getDefaultDisplay();
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.height = dip2px(this,100);
        lp.width = (int) (d.getWidth() * 0.6);
        dialogWindow.setAttributes(lp);
        dialogWindow.setGravity(Gravity.CENTER);
        back.setOnClickListener(view1 -> alertDialog.dismiss());
    }



    public List<Subscription> getSelectedDatas() {
        HashMap<String, Boolean> selectList = adapter.getIsSelected();
        List<Subscription> rooms = new ArrayList<>();

        for (int i = 0; i < selectList.size(); i++) {
            rooms.add(realmSubscriptionRepository.getByIdSub((String) selectList.keySet().toArray()[i]));
//            rooms.add(adapter.getTempDataList().get((Integer) selectList.keySet().toArray()[i]));
        }
        return rooms;
    }

    private void setAdapter() {
        adapter = new RelayAdapter(this, datas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((view, tempData) -> {
            KeyboardHelper.hideSoftKeyboard(this);
        });

    }

    public void setRightCount(int count) {
        tv_create.setText("完成(" + count + ")");
    }

    private void setListener() {
        iv_clear.setOnClickListener(view -> {
            et_search.setText("");
        });
        iv_back.setOnClickListener(v -> {
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
                } else {
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

    private void filterSearchData(String term) {
        List<Subscription> tempDataList = new ArrayList<>();
        for (Subscription data : datas) {
            if (data.getName().contains(term))
                tempDataList.add(data);
        }
        adapter.setDatas(tempDataList, term);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        KeyboardHelper.hideSoftKeyboard(this);
    }


    public void sendFile(Message msg, Subscription room) throws JSONException {
        String filedId=msg.getFile().getId();
        String rid = room.getRid();
        methodCall.copyFile(filedId,rid).onSuccessTask(task -> {
            JSONObject result=customizeJson(task.getResult());
            return  methodCall.sendFileMessage(rid, null, result);
        }).onSuccessTask(task -> {
            ToastUtils.showToast("转发成功！");
            return null;
        }, Task.UI_THREAD_EXECUTOR).continueWithTask(task -> {
            if (task.isFaulted()){
                ToastUtils.showToast("转发失败！");
            }
            return null;
        },Task.UI_THREAD_EXECUTOR);
//        String currentFileName = DownloadUtil.get().getFileName(attachmentTitle.getTitle(), msg.getTimestamp());
//        String appDownloadDir = FileUtils.getAppDownloadDir(RocketChatApplication.getInstance());
//        File currentFile = new File(appDownloadDir + currentFileName);
//        if (currentFile.exists()) {
//            uploadFile(Uri.fromFile(currentFile),room.getRid());
//        } else {
//            DownloadUtil.get().download(absoluteUrl.from(attachmentTitle.getLink()), appDownloadDir, currentFileName, new DownloadUtil.OnDownloadListener() {
//                @Override
//                public void onDownloadSuccess() {
//                    RCLog.d(currentFileName + "下载成功");
//                    uploadFile(Uri.fromFile(currentFile),room.getRid());
//                }
//
//                @Override
//                public void onDownloading(int progress) {
//                    //                progressBar.setProgress(progress);
//                }
//
//                @Override
//                public void onDownloadFailed() {
//                    RCLog.d(currentFileName + "下载失败");
//                }
//            });
//        }
//

    }

    private void uploadFile(Uri uri,String roomId) {
        String uplId = new FileUploadHelper(this, RealmStore.get(hostname))
                .requestUploading(roomId, uri);
        if (!TextUtils.isEmpty(uplId)) {
//            DialogFragment dialogFragment= FileUploadProgressDialogFragment.create(hostname, roomId, uplId).show();
//            dialogFragment.show(getFragmentManager(), "FileUploadProgressDialogFragment");
        } else {
            // show error.
        }
    }

    public void sendMessage(String messageText, List<Subscription> rooms) {
        MessageInteractor messageInteractor = new MessageInteractor(
                new RealmMessageRepository(hostname),
                realmSubscriptionRepository
        );
        for (Subscription room : rooms) {
            final Disposable subscription = getRoomUserPair(room.getRid())
                    .flatMap(pair -> messageInteractor.send(pair.first, pair.second, messageText))
                    .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            success -> {
                                if (success) {
                                    //成功发送
                                }
                            },
                            throwable -> {
                                Logger.INSTANCE.report(throwable);
                            }
                    );

            compositeSubscription.add(subscription);
        }
    }

    @SuppressLint("RxLeakedSubscription")
    private Single<Pair<Subscription, User>> getRoomUserPair(String roomId) {

        getSingleRoom(roomId).subscribe(subscription -> {
                    subscription.getName();
                }
                , RCLog::e);

        getCurrentUser().subscribe(user -> {
            user.getName();
        }, RCLog::e);
        return Single.zip(
                getSingleRoom(roomId),
                getCurrentUser(),
                (Subscription first, User second) -> {
                    return new Pair<Subscription, User>(first, second);
                }
        );
    }

    private Single<Subscription> getSingleRoom(String roomId) {
        return realmSubscriptionRepository.getById(roomId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }

    private Single<User> getCurrentUser() {
        return userRepository.getCurrent()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .firstElement()
                .toSingle();
    }

    private CompositeDisposable compositeSubscription = new CompositeDisposable();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    public static JSONObject customizeJson(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.isNull("path")) {
            try {
                String path = jsonObject.getString("path");
                jsonObject.remove("path");
                jsonObject.put("path", Uri.decode(path));
            } catch (JSONException e) {
            }
        }
        if (!jsonObject.isNull("url")) {
            try {
                String url = jsonObject.getString("url");
                jsonObject.remove("url");
                jsonObject.put("url", Uri.decode(url));
            } catch (JSONException e) {
            }
        }
        return jsonObject;
    }

    private void showTitleIcon(String title, ImageView view) {
        if (FileUtils.isWord(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_word);
        } else if (FileUtils.isExcel(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_excel);
        } else if (FileUtils.isPPT(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_ppt);
        } else if (FileUtils.isMp3(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_mp3);
        } else if (FileUtils.isTxt(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_txt);
        } else if (FileUtils.isZip(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_zip);
        } else if (FileUtils.isPDF(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_pdf);
        }
    }
}
