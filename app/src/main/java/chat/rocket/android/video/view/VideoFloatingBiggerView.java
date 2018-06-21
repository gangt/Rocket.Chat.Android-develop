package chat.rocket.android.video.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.video.helper.ChatStatus;
import chat.rocket.android.video.helper.CountTimeEngine;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.helper.VideoSendAvType;
import chat.rocket.android.video.presenter.BasePresenter;


public class VideoFloatingBiggerView extends LinearLayout implements View.OnClickListener{
    public static final String TAG = "FloatingView";

    private BasePresenter presenter ;
    private final Context mContext;
    public FrameLayout container ;
    public FrameLayout local_container;
    public ViewManager viewManager ;
    public LoadingTextView inviteStateMessage ;
    public ImageView vedioAvar;
    public TextView  inviteUsername ;
    public LinearLayout remote_call_ll;
    public LinearLayout titleBar;
    public LinearLayout requestCallLl;
    public ImageView smallImg;
    public LinearLayout answer_call_ll;
    public TextView videoTimeCount;
    public CustomShowTextView customTx;
    public boolean isScaled;
    public boolean isClicked = false ;


    public VideoFloatingBiggerView(ViewManager manager , Context context) {
        super(context);
        this.viewManager = manager ;
        mContext = context;
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.activity_invite_video_local, this);
        findView();
        initListener();
        initPresenter();

    }

    private void initPresenter() {
        presenter = viewManager.presenter;
        presenter.bindData(viewManager.methodCallHelper, mContext, viewManager.mRtcEventHandler, viewManager.channel,this);
        presenter.initAgoraEngineAndJoinChannel();
        presenter.setStateView(viewManager.isCall ? 2 : 0);
    }

    private void initListener() {
        findViewById(R.id.jump_ll).setOnClickListener(this);
        findViewById(R.id.cancel_call).setOnClickListener(this);
        findViewById(R.id.request_call_ll_cancel).setOnClickListener(this);

        findViewById(R.id.canccel_invite_ll).setOnClickListener(this);
        findViewById(R.id.camera_reverse_ll).setOnClickListener(this);
        findViewById(R.id.answer_call).setOnClickListener(this);

        findViewById(R.id.small_img).setOnClickListener(this);
        findViewById(R.id.local_video_view_container).setOnClickListener(this);

    }

    private void findView() {
        container = findViewById(R.id.remote_video_view_container);
        local_container = findViewById(R.id.local_video_view_container);
        inviteStateMessage = findViewById(R.id.invite_state_message);

        vedioAvar = findViewById(R.id.vedio_avar);
        inviteUsername = findViewById(R.id.invite_username);
        remote_call_ll = findViewById(R.id.remote_call_ll);

        titleBar = findViewById(R.id.title_bar);
        requestCallLl = findViewById(R.id.request_call_ll);
        smallImg = findViewById(R.id.small_img);

        answer_call_ll = findViewById(R.id.answer_call_ll);
        videoTimeCount = findViewById(R.id.video_time_count);

        customTx = findViewById(R.id.custom_tx);
    }

    @Override
    public void onClick(View view) {
        if(isClicked){
            return;
        }
        int i = view.getId();
        if (i == R.id.jump_ll) {
            CustomToast.showToastInfo(R.string.change2audio);
            presenter.change2AudioChat();  // 切换语音聊天

        } else if (i == R.id.cancel_call) {
            isClicked = true ;
            presenter.sendVideo(VideoSendAvType.REFUSE, "");
            presenter.cancelTalk(R.string.call_done);
            if (viewManager.isCall) {
                presenter.setStatus(ChatStatus.REFUSE, "");
            }

        } else if (i == R.id.request_call_ll_cancel) {
            isClicked = true ;
            presenter.sendVideo(VideoSendAvType.CANCEL, "");
            presenter.cancelTalk(R.string.call_cancel);
            if (viewManager.isCall) {
                presenter.setStatus(ChatStatus.CANCEL, "");
            }

        } else if (i == R.id.canccel_invite_ll) {
            isClicked = true ;
            presenter.cancelTalk(R.string.call_done);
            presenter.sendVideo(VideoSendAvType.HANG_UP, "");
            if (viewManager.isCall) {
                CountTimeEngine.getInstance().stopIfNeccessary();
                presenter.setStatus(ChatStatus.END, TempFileUtils.getInstance().getTime());
            }
        } else if (i == R.id.camera_reverse_ll) {
            presenter.switchCameraClicked();

        } else if (i == R.id.answer_call) {
            presenter.answerCall(true);
            presenter.sendVideo(VideoSendAvType.ACCEPT, "");

        }else if (i == R.id.small_img) {
            onSmallImgClicked();

        } else if (i == R.id.audio_jump_ll) {
            presenter.onLocalAudioMuteClicked();

        } else if (i == R.id.audio_speakphone_ll) {
            presenter.onSwitchSpeakerphoneClicked();

        } else if (i == R.id.local_video_view_container) {
            presenter.exChangeView();

        }
    }

    public void onSmallImgClicked() {
        if (isScaled) {
            biggerImg();
            isScaled = !isScaled;
        } else {
            scaleImg();
            isScaled = !isScaled;
        }
    }

    public void scaleImg(){
        viewManager.addSmallView();
        presenter.scaleImg(true);
    }

    public void biggerImg(){
        viewManager.addView();
        presenter.scaleImg(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    /*    switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                preP = new Point((int)event.getRawX(), (int)event.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:
                curP = new Point((int)event.getRawX(), (int)event.getRawY());
                int dx = curP.x - preP.x,
                        dy = curP.y - preP.y;

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
                layoutParams.x += dx;
                layoutParams.y += dy;
                mWindowManager.updateViewLayout(this, layoutParams);

                preP = curP;
                break;
        }*/

        return false;
    }
}
