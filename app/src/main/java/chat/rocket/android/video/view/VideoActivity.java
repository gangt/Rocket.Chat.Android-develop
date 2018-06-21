package chat.rocket.android.video.view;//package chat.rocket.android.video.view;
//
//import android.Manifest;
//import android.app.Activity;
//import android.app.Service;
//import android.content.pm.PackageManager;
//import android.media.AudioManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.view.Display;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//
//import java.lang.ref.WeakReference;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import butterknife.Unbinder;
//import chat.rocket.android.R;
//import chat.rocket.android.RocketChatApplication;
//import chat.rocket.android.RocketChatCache;
//import chat.rocket.android.api.MethodCallHelper;
//import chat.rocket.android.log.RCLog;
//import chat.rocket.android.video.helper.ChatStatus;
//import chat.rocket.android.video.helper.TempFileUtils;
//import chat.rocket.android.video.helper.VideoSendAvType;
//import chat.rocket.android.video.model.VideoBusEvent;
//import chat.rocket.android.video.presenter.AudioPresenter;
//import chat.rocket.android.video.presenter.BasePresenter;
//import chat.rocket.android.video.presenter.PresenterFactory;
//import chat.rocket.android.video.presenter.VideoPresenter;
//import chat.rocket.persistence.realm.repositories.RealmUserRepository;
//import io.agora.rtc.IRtcEngineEventHandler;
//
///**
// * Created by Tidom on 2018/4/26/026.
// */
//
//public class VideoActivity extends Activity {
//
//
//    @BindView(R.id.remote_video_view_container)
//    public FrameLayout container;
//    @BindView(R.id.local_video_view_container)
//    public FrameLayout local_container;
//    @BindView(R.id.vedio_avar)
//    public ImageView vedioAvar;
//    @BindView(R.id.invite_username)
//    public TextView inviteUsername;
//    @BindView(R.id.invite_state_message)
//    public LoadingTextView inviteStateMessage;
//    @BindView(R.id.invite_local_user_ll)
//    LinearLayout inviteLocalUserLl;
//    @BindView(R.id.cancel_call)
//    ImageView cancelCall;
//    @BindView(R.id.answer_call)
//    ImageView answerCall;
//    @BindView(R.id.remote_call_ll)
//    public LinearLayout remote_call_ll;
//    @BindView(R.id.jump)
//    ImageView jump;
//    @BindView(R.id.jump_ll)
//    LinearLayout jumpLl;
//    @BindView(R.id.invite_cancle)
//    ImageView inviteCancle;
//    @BindView(R.id.canccel_invite_ll)
//    LinearLayout canccelInviteLl;
//    @BindView(R.id.camesa_reverse)
//    ImageView camesaReverse;
//    @BindView(R.id.camera_reverse_ll)
//    LinearLayout cameraReverseLl;
//    @BindView(R.id.answer_call_ll)
//    public LinearLayout answer_call_ll;
//    @BindView(R.id.request_call_ll_cancel)
//    ImageView requestCallLlCancel;
//    @BindView(R.id.request_call_ll)
//    public LinearLayout requestCallLl;
//    @BindView(R.id.small_img)
//    public ImageView smallImg;
//    @BindView(R.id.title_bar)
//    public LinearLayout titleBar;
//    @BindView(R.id.audio_small_img)
//    public ImageView audio_small_img;
//    @BindView(R.id.audio_user_avar)
//    public ImageView audio_user_avar;
//    @BindView(R.id.audio_username_center)
//    public TextView audio_username_center;
//    @BindView(R.id.invite_state_center)
//    public LoadingTextView inviteStateCenter;
//    @BindView(R.id.invite_center_show_ll)
//    public LinearLayout inviteCenterShowLl;
//    @BindView(R.id.audio_canccel_invite)
//    public LinearLayout audio_canccel_invite;
//    @BindView(R.id.hang_up)
//    public ImageView hangUp;
//    @BindView(R.id.audio_remote_call_ll)
//    public LinearLayout audioRemoteCallLl;
//    @BindView(R.id.audio_answer_call_ll)
//    public LinearLayout audioAnswerCallLl;
//    public String channel;
//    public String userName;
//    public String avar;
//    public boolean isCall;
//    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
//    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
//    @BindView(R.id.audio_cancel_call)
//    ImageView audioCancelCall;
//    @BindView(R.id.audio_answer_call)
//    ImageView audioAnswerCall;
//    @BindView(R.id.audio_jump)
//    public ImageView audioJump;
//    @BindView(R.id.audio_jump_ll)
//    LinearLayout audioJumpLl;
//    @BindView(R.id.audio_canccel_invite_ll)
//    LinearLayout audioCanccelInviteLl;
//    @BindView(R.id.audio_speakphone_img)
//    public ImageView audioSpeakphoneImg;
//    @BindView(R.id.audio_speakphone_ll)
//    LinearLayout audioSpeakphoneLl;
//    @BindView(R.id.video_time_count)
//    public TextView videoTimeCount;
//    @BindView(R.id.audio_time_count)
//    public TextView audioTimeCount;
//    @BindView(R.id.custom_tx)
//    public CustomShowTextView customTx;
//    @BindView(R.id.audio_custom_tx)
//    public CustomShowTextView audioCustomTx;
//    @BindView(R.id.audio_scale_time)
//    public TextView audioScaleTime;
//    @BindView(R.id.audio_scale_view)
//    public FrameLayout audioScaleView;
//    @BindView(R.id.audio_open_img)
//    public ImageView audioOpenImg;
//    @BindView(R.id.audio_invite_cancle)
//    ImageView audioInviteCancle;
//    private BasePresenter presenter;
//    public boolean isScaled;
//    public MethodCallHelper methodCallHelper;
//    Display display;
//    Window window;
//    WindowManager.LayoutParams windowLayoutParams;
//    public boolean isVideo;
//    private Unbinder unbinder;
//    public String id;
//    public String mediaId;
//    private AudioManager aManger;
//    SuneeOnAudioFocusChangeListener focusChangeListener;
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();
//        initData();
//        EventBus.getDefault().register(this);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void initData() {
//        initPresenterAndStateView();
//        aManger = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
//        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
//            requestFocus();
//            presenter.initAgoraEngineAndJoinChannel();
//        }
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void requestFocus() {
//        try {
//            focusChangeListener = new SuneeOnAudioFocusChangeListener();
//            aManger.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private class SuneeOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
//
//        @Override
//        public void onAudioFocusChange(int focusChange) {
//            switch (focusChange) {
//                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://Pause playback
//                    presenter.pauseAudio();
//                    break;
//                case AudioManager.AUDIOFOCUS_GAIN://Resume playback
//                    presenter.resumeAudio();
//                    break;
//                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://
//
//                    break;
//                case AudioManager.AUDIOFOCUS_LOSS://Stop playback
//                    presenter.cancelTalk(R.string.call_done);
//                    break;
//            }
//        }
//    }
//
//
//    private void initView() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        Window window = getWindow();
//        // | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//      /*  window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                | WindowManager.LayoutParams.FLAG_FULLSCREEN
//        );*/
//        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//        );
//        window.setType(WindowManager.LayoutParams.TYPE_PHONE);
//
//        initLayoutView();
//        display = getWindowManager().getDefaultDisplay(); // 为获取屏幕宽、高
//        this.window = getWindow();
//        windowLayoutParams = this.window.getAttributes(); // 获取对话框当前的参数值
//
//        //used when finished
////        RocketChatApplication .playSounde(0);
//
//    }
//
//    public void getIntentData() {
//        isVideo = getIntent().getBooleanExtra("isVideo", true);
//        userName = getIntent().getStringExtra("name");
//        avar = getIntent().getStringExtra("avar");
//        if ("".equals(avar)) {
//            RealmUserRepository repository = new RealmUserRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
//            avar = repository.getAvatarByUsername(userName);
//        }
//        if (userName.indexOf("&") != -1) {
//            userName = userName.substring(0, userName.indexOf("&"));
//        }
//        channel = getIntent().getStringExtra("channel");
//        isCall = getIntent().getBooleanExtra("isCall", true);
//        id = getIntent().getStringExtra("id");
//        mediaId = getIntent().getStringExtra("mediaId");
//    }
//
//
//    public void initLayoutView() {
//        getIntentData();
//        setContentView(R.layout.activity_chat_video);
//        findViewById(isVideo ? R.id.video_layout : R.id.audio_layout).setVisibility(View.VISIBLE);
//        unbinder = ButterKnife.bind(this);
//    }
//
//    private void initPresenter() {
//        methodCallHelper = new MethodCallHelper(new WeakReference<>(this).get(), RocketChatCache.INSTANCE.getSelectedServerHostname());
//        presenter = PresenterFactory.getInstance(isVideo ? VideoPresenter.class : AudioPresenter.class);
////        presenter.bindData(methodCallHelper, new WeakReference<>(this), mRtcEventHandler, channel);
//    }
//
//    public void initPresenterAndStateView() {
//        initPresenter();
//        if (getIntent().getExtras() == null) {
//            return;
//        }
//        presenter.setStateView(isCall ? 2 : 0);
//    }
//
//
//    public boolean checkSelfPermission(String permission, int requestCode) {
//        if (ContextCompat.checkSelfPermission(this,
//                permission)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{permission},
//                    requestCode);
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String permissions[], @NonNull int[] grantResults) {
//
//        switch (requestCode) {
//            case PERMISSION_REQ_ID_RECORD_AUDIO: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
//                } else {
//                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
//                    finish();
//                }
//                break;
//            }
//            case PERMISSION_REQ_ID_CAMERA: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // initAgoraEngineAndJoinChannel();
//                    presenter.initAgoraEngineAndJoinChannel();
//                } else {
//                    showLongToast("No permission for " + Manifest.permission.CAMERA);
//                    finish();
//                }
//                break;
//            }
//        }
//    }
//
//    public final void showLongToast(final String msg) {
//        this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        aManger.abandonAudioFocus(focusChangeListener);
//        presenter.hangUp();
//        unbinder.unbind();
//        RocketChatApplication.stopSound();
//        EventBus.getDefault().unregister(this);
//        super.onDestroy();
//    }
//
//    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
//        @Override
//        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
//            RCLog.d("come--------->>>>" + "onFirstRemoteVideoDecoded");
//            presenter.closeReInviteTimeEngine();
//            runOnUiThread(() -> presenter.setupRemoteVideo(uid));
//        }
//
//        @Override
//        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
//            RCLog.d("VideoActivtiy--------->>>>" + "onUserOffline");
//            runOnUiThread(() -> {
//                presenter.cancelTalk(R.string.remote_cancel_done);
//                if(isCall){
//                     String time = TempFileUtils.getInstance().getTime();
//                     presenter.setStatus(ChatStatus.END, time);
//                 }
//            });
//        }
//
//        @Override
//        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
//            RCLog.d("VideoActivtiy--------->>>>", "onUserMuteVideo");
//            runOnUiThread(() ->
//                    presenter.onRemoteUserVideoMuted(uid));
//        }
//
//        @Override
//        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
//            RCLog.d("VideoActivtiy--------->>>>", "onUserMuteAudio");
//            runOnUiThread(() -> presenter.onRemoteUserAudioMuted());
//        }
//
//
//        @Override
//        public void onFirstRemoteAudioFrame(int uid, int elapsed) {
//            RCLog.d("VideoActivtiy--------->>>>", "onFirstRemoteAudioFrame");
//            presenter.closeReInviteTimeEngine();
//            super.onFirstLocalAudioFrame(elapsed);
//        }
//
//        @Override
//        public void onConnectionLost() {
//            finish();
//            super.onConnectionLost();
//        }
//
//        @Override
//        public void onAudioRouteChanged(int routing) {
//            RCLog.d("VideoActivtiy--------->>>>", "routing");
//            super.onAudioRouteChanged(routing);
//
//        }
//    };
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
//            return false;
//        }
//        return super.onTouchEvent(event);
//    }
//
//
//    public void setOpenScreenSize() {
//        windowLayoutParams.width = (display.getWidth());
//        windowLayoutParams.height = (display.getHeight());
//        window.setAttributes(windowLayoutParams);
//    }
//
//    public void setScaleAudioScreenSize() {
//        windowLayoutParams.width = (int) (display.getWidth() * 0.2);
//        windowLayoutParams.height = (int) (display.getHeight() * 0.2);
//        windowLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
//        window.setAttributes(windowLayoutParams);
//    }
//
//    private void openScreen() {
//        setOpenScreenSize();
//        local_container.setVisibility(View.VISIBLE);
//        answer_call_ll.setVisibility(View.VISIBLE);
//    }
//
//    private void scaleScreen() {
//        setScaleAudioScreenSize();
//        local_container.setVisibility(View.GONE);
//        answer_call_ll.setVisibility(View.GONE);
//
//    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//    }
//
//    public void onSmallImgClicked() {
//        if (isScaled) {
//            openScreen();
//            isScaled = !isScaled;
//        } else {
//            scaleScreen();
//            isScaled = !isScaled;
//        }
//    }
//
//    @OnClick({
//            R.id.jump_ll, R.id.cancel_call, R.id.request_call_ll_cancel,
//            R.id.camera_reverse_ll, R.id.answer_call, R.id.small_img,
//            R.id.canccel_invite_ll, R.id.audio_jump_ll, R.id.audio_canccel_invite_ll,
//            R.id.audio_speakphone_ll, R.id.audio_cancel_call, R.id.audio_answer_call, R.id.audio_invite_cancle,
//            R.id.audio_open_img, R.id.audio_small_img, R.id.local_video_view_container
//    })
//    public void onViewClicked(View view) {
//        int i = view.getId();
//        if (i == R.id.jump_ll) {
//            CustomToast.showToastInfo(R.string.change2audio);
//            presenter.change2AudioChat();  // 切换语音聊天
//
//        } else if (i == R.id.cancel_call || i == R.id.audio_cancel_call) {
//            presenter.sendVideo(VideoSendAvType.REFUSE, "");
//            presenter.cancelTalk(R.string.call_done);
//            if (isCall) {
//                presenter.setStatus(ChatStatus.REFUSE, "");
//            }
//
//        } else if (i == R.id.request_call_ll_cancel || i == R.id.audio_invite_cancle) {
//            presenter.sendVideo(VideoSendAvType.CANCEL, "");
//            presenter.cancelTalk(R.string.call_cancel);
//            if (isCall) {
//                presenter.setStatus(ChatStatus.CANCEL, "");
//            }
//
//        } else if (i == R.id.canccel_invite_ll || i == R.id.audio_canccel_invite_ll) {
//            presenter.cancelTalk(R.string.call_done);
//            presenter.sendVideo(VideoSendAvType.HANG_UP, "");
//            if (isCall) {
//                presenter.setStatus(ChatStatus.END, TempFileUtils.getInstance().getTime());
//            }
//
//
//        } else if (i == R.id.camera_reverse_ll) {
//            presenter.switchCameraClicked();
//
//        } else if (i == R.id.answer_call || i == R.id.audio_answer_call) {
//
//            presenter.answerCall(true);
//            presenter.sendVideo(VideoSendAvType.ACCEPT, "");
//
//        } else if (i == R.id.audio_open_img) {
//            presenter.scaleImg(false);
//
//        } else if (i == R.id.audio_small_img) {
//            presenter.scaleImg(true);
//
//        } else if (i == R.id.small_img) {
//            onSmallImgClicked();
//
//        } else if (i == R.id.audio_jump_ll) {
//            presenter.onLocalAudioMuteClicked();
//
//        } else if (i == R.id.audio_speakphone_ll) {
//            presenter.onSwitchSpeakerphoneClicked();
//
//        } else if (i == R.id.local_video_view_container) {
//            presenter.exChangeView();
//
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//     /*   if (isScaled) {
//            return;
//        }*/
//
//        if (!isScaled) {
//            if (presenter instanceof VideoPresenter) {
//                return;
//            }
//            presenter.scaleImg(true);
//            isScaled = !isScaled;
//            return;
//        }
//        super.onBackPressed();
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(VideoBusEvent event) {
//        switch (event.getType()) {
//            case VideoSendAvType.ACCEPT: // 接通
//                if (presenter instanceof AudioPresenter) {
//                    presenter.answerCall(false);
//                }
//                break;
//
//            case VideoSendAvType.HANG_UP: //挂断
//            /*    presenter.cancelTalk(R.string.remote_cancel_done);
//              *//*  if (isCall) {
//                    presenter.setStatus(ChatStatus.END, TempFileUtils.getInstance().getTime());
//                }*/
//                break;
//
//            case VideoSendAvType.CANCEL: //取消
//                presenter.cancelTalk(R.string.remote_cancel_done);
//                if (isCall) {
//                    presenter.setStatus(ChatStatus.CANCEL, "");
//                }
//                break;
//
//            case VideoSendAvType.REFUSE: //拒绝
//                int msgId = presenter instanceof VideoPresenter ? R.string.remote_refuse_video_done : R.string.remote_refuse_audio_done;
//                presenter.cancelTalk(msgId);
//                if (isCall) {
//                    presenter.setStatus(ChatStatus.REFUSE, "");
//                }
//                break;
//            case VideoSendAvType.TALKING:
//                presenter.sendVideo(VideoSendAvType.TALKING, "");
//
//                break;
//
//
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        checkRecentVideoMsg();
//        super.onStart();
//    }
//
//    private void checkRecentVideoMsg() {
//        if(null ==mediaId){
//            return;
//        }
//        String status = TempFileUtils.getInstance().getMsgByMediaId(mediaId);
//        if(status !=null  && status .equals(ChatStatus.CANCEL) ){
//            presenter.cancelTalk(R.string.remote_cancel_done);
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        inviteStateCenter.stopIfNeccessary();
//        super.onStop();
//    }
//}
//
//
//
