package chat.rocket.android.video.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.video.helper.ChatStatus;
import chat.rocket.android.video.helper.CountTimeEngine;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.helper.VideoSendAvType;
import chat.rocket.android.video.presenter.BasePresenter;


public class AudioFloatingBiggerView extends LinearLayout implements View.OnClickListener{
    public static final String TAG = "FloatingView";

    public  Context mContext;
    public ViewManager viewManager ;
    private BasePresenter presenter ;
    public View layoutView ;
    public View audioRemoteCallLl;
    public LoadingTextView invite_state_center;
    public View audio_canccel_invite;
    public View audio_answer_call_ll;
    public TextView audio_time_count;
    public ImageView audio_user_avar;
    public TextView audio_username_center;
    public CustomShowTextView customShowTextView;
    public TextView audioScaleTime;
    public  View inviteCenterShowLl ;
    public  View audio_small_img ;
    public  View audio_open_img ;
    public View audio_scale_view ;
    public boolean fromVideo ;
    public boolean isClicked = false ;

    public AudioFloatingBiggerView(ViewManager manager , Context context, boolean fromVideo) {
        super(context);
        this.viewManager = manager ;
        mContext = context;
        this.fromVideo = fromVideo ;
        initView();
    }

    public void initView() {

        layoutView = inflate(mContext, R.layout.activity_invite_audio_local, this);
        findView();
        initListener();
        presenter = viewManager.presenter ;
        presenter.bindData(viewManager.methodCallHelper,mContext,viewManager.mRtcEventHandler,viewManager.channel,this);
        presenter.initAgoraEngineAndJoinChannel();
        if(fromVideo){
            presenter.setStateView(3);
            return;
        }
        presenter.setStateView(viewManager.isCall ? 2 : 0);

    }

    private void initListener() {
        audio_small_img.setOnClickListener(this);
        audio_open_img.setOnClickListener(this);
        findViewById(R.id.audio_cancel_call).setOnClickListener(this);
        findViewById(R.id.audio_invite_cancle).setOnClickListener(this);
        findViewById(R.id.audio_canccel_invite_ll).setOnClickListener(this);
        findViewById(R.id.audio_jump_ll).setOnClickListener(this);
        findViewById(R.id.audio_speakphone_ll).setOnClickListener(this);
        findViewById(R.id.audio_answer_call).setOnClickListener(this);
    }

    public void findView() {
        audioRemoteCallLl = findViewById(R.id.audio_remote_call_ll);
        invite_state_center = findViewById(R.id.invite_state_center);
        audio_canccel_invite = findViewById(R.id.audio_canccel_invite);
        audio_answer_call_ll = findViewById(R.id.audio_answer_call_ll);
        audio_time_count = findViewById(R.id.audio_time_count);
        audio_user_avar = findViewById(R.id.audio_user_avar);
        audio_username_center = findViewById(R.id.audio_username_center);
        customShowTextView = findViewById(R.id.audio_custom_tx);
        audioScaleTime = findViewById(R.id.audio_scale_time);
        inviteCenterShowLl = findViewById(R.id.invite_center_show_ll);
        audio_small_img = findViewById(R.id.audio_small_img);
        audio_scale_view = findViewById(R.id.audio_scale_view);
        audio_open_img = findViewById(R.id.audio_open_img);
    }

    public void scaleImg(){
        viewManager.isVideo = false;
        viewManager.addSmallView();
        presenter.scaleImg(true);
    }

    public void biggerImg(){
        viewManager.isVideo = false;
        viewManager.addView();
        presenter.scaleImg(false);
    }

    @Override
    public void onClick(View view) {
        if(isClicked){
            return;
        }
        int i = view.getId();
       if (i == R.id.audio_cancel_call) {
           isClicked = true ;
            presenter.sendVideo(VideoSendAvType.REFUSE, "");
            presenter.cancelTalk(R.string.call_done);
            if (viewManager.isCall) {
                presenter.setStatus(ChatStatus.REFUSE, "");
            }
        } else if (i == R.id.audio_invite_cancle) {
           isClicked = true ;
           presenter.cancelTalk(R.string.call_cancel);
           presenter.sendVideo(VideoSendAvType.CANCEL, "");
            if (viewManager.isCall) {
                RCLog.d("VideoActivtiy--------->>>>", "VideoSendAvType.CANCEL  onClick");
                presenter.setStatus(ChatStatus.CANCEL, "");
            }

        } else if (i == R.id.audio_canccel_invite_ll) {
           isClicked = true ;
            presenter.cancelTalk(R.string.call_done);
            presenter.sendVideo(VideoSendAvType.HANG_UP, "");
            if (viewManager.isCall) {
                CountTimeEngine.getInstance().stopIfNeccessary();
                presenter.setStatus(ChatStatus.END, TempFileUtils.getInstance().getTime());
            }

        } else if (i == R.id.audio_answer_call) {
            presenter.answerCall(true);
            presenter.sendVideo(VideoSendAvType.ACCEPT, "");

        }  else if (i == R.id.audio_open_img) {
//            biggerImg();

       }  else if (i == R.id.audio_small_img) {
             scaleImg();

        }  else if (i == R.id.audio_jump_ll) {
            presenter.onLocalAudioMuteClicked();

        } else if (i == R.id.audio_speakphone_ll) {
            presenter.onSwitchSpeakerphoneClicked();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(KeyEvent.KEYCODE_BACK == event.getAction()){
            if (!viewManager.isScaled) {
                presenter.scaleImg(true);
                viewManager.isScaled = !viewManager.isScaled;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
