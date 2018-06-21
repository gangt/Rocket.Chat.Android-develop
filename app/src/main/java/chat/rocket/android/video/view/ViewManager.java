package chat.rocket.android.video.view;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.video.Utils.LightSensorUtils;
import chat.rocket.android.video.Utils.ScreenLockUtils;
import chat.rocket.android.video.helper.ChatStatus;
import chat.rocket.android.video.helper.CountTimeEngine;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.helper.VideoSendAvType;
import chat.rocket.android.video.model.CloseBusEvent;
import chat.rocket.android.video.model.HandelEventBus;
import chat.rocket.android.video.model.ServiceStop;
import chat.rocket.android.video.model.VideoBusEvent;
import chat.rocket.android.video.model.ViewEvent;
import chat.rocket.android.video.presenter.AudioPresenter;
import chat.rocket.android.video.presenter.BasePresenter;
import chat.rocket.android.video.presenter.PresenterFactory;
import chat.rocket.android.video.presenter.VideoPresenter;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.agora.rtc.IRtcEngineEventHandler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/5/28/028.
 */

public class ViewManager {
    private Context mContext;
    WindowManager.LayoutParams params;
    public WindowManager mWindowManager;
    public static final String AUDIO_BIGGER = "audio_bigger";
    public static final String AUDIO_SMALL = "audio_small";

    public static final String VIDEO_BIGGER = "video_bigger";
    private static final String VIDEO_SMALL = "video_small"; //onUserMuteVideo


    private static final int onUserShow = 0;  //onUserOffline onUserMuteAudio onUserMuteVideo
    private static final int onUserOffline = 1;
    private static final int onUserMuteAudio = 2;
    private static final int onUserMuteVideo = 3;
    private static final int onFirstRemoteAudioFrame = 4;
    private static final int onConnectionLost = 5;
    private static final int onLastmileQuality = 6;


    private String currentKey = null;
    private HashMap<String, View> map = new HashMap<>();

    private WindowManager.LayoutParams LAYOUT_PARAMS;
    Display defaultDisplay;

    public String channel;
    public String userName;
    public String avar;
    public boolean isCall;
    public boolean isVideo;
    public String id;
    public String mediaId;
    public BasePresenter presenter;
    public MethodCallHelper methodCallHelper;
    public boolean isScaled;
    private AudioManager aManger;
    private boolean hasSend = false;
    private LightSensorUtils sensorUtils ;

    public ViewManager() {
        EventBus.getDefault().register(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void bindData(Bundle bundle) {
        this.mContext = ChatMainActivity.getChatMainActivity().get();
        getIntentData(bundle);
        mWindowManager = (WindowManager) ChatMainActivity.getChatMainActivity().get().getSystemService(Context.WINDOW_SERVICE);
        initParams();
        initPresenter();
        requestFocus();
        initSensor();
    }


    private void initPresenter() {
        methodCallHelper = new MethodCallHelper(mContext, RocketChatCache.INSTANCE.getSelectedServerHostname());
        presenter = PresenterFactory.getInstance(isVideo ? VideoPresenter.class : AudioPresenter.class);
//        presenter.bindData(methodCallHelper,mContext, mRtcEventHandler, channel);
    }


    public void getIntentData(Bundle bundle) {
        isVideo = bundle.getBoolean("isVideo", true);
        userName = bundle.getString("name");
        avar = bundle.getString("avar");
        if ("".equals(avar)) {
            RealmUserRepository repository = new RealmUserRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
            avar = repository.getAvatarByUsername(userName);
        }
        if (userName.indexOf("&") != -1) {
            userName = userName.substring(0, userName.indexOf("&"));
        }
        channel = bundle.getString("channel");
        isCall = bundle.getBoolean("isCall", true);
        id = bundle.getString("id");
        mediaId = bundle.getString("mediaId");
    }

    private void initParams() {

        aManger = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_TOAST);
        params = new WindowManager.LayoutParams();
        defaultDisplay = mWindowManager.getDefaultDisplay();
        params.x = 0;
        params.y = 0;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

       /* if(Build.VERSION.SDK_INT  < 25 ){
            params.type = WindowManager.LayoutParams.TYPE_TOAST ;
            return;
        }
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;*/
/*        IBinder windowToken = ChatMainActivity.getChatMainActivity().getWindow().getDecorView().getWindowToken();
        params.token = windowToken;*/


    }

    public void add2Window() {
        try {
            ChatMainActivity.getChatMainActivity().get().runOnUiThread(() -> {
                map.get(currentKey).setLayoutParams(LAYOUT_PARAMS);
                mWindowManager.addView(map.get(currentKey), LAYOUT_PARAMS);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addView() {
        if (isVideo) {
            gotoVideo();
        } else {
            gotoAudio(false);
        }
    }


    public void initSensor(){
        if(isVideo){
            return;
        }
        sensorUtils = LightSensorUtils.getInstance();
        sensorUtils.init(mContext);
        ScreenLockUtils.getInstance().acquireCpuWakeLock(mContext);
    }



    public void gotoVideo() {
        try {
            isScaled = false;
            openView();
            VideoFloatingBiggerView biggerView = null;
            if (map.get(VIDEO_BIGGER) == null) {
                biggerView = new VideoFloatingBiggerView(this, mContext);
                map.put(VIDEO_BIGGER, biggerView);
            } else {
                biggerView = (VideoFloatingBiggerView) map.get(VIDEO_BIGGER);
            }
            currentKey = VIDEO_BIGGER;
            TempFileUtils.getInstance().saveCurrentKey(currentKey);
            biggerView.setLayoutParams(LAYOUT_PARAMS);
            ChatMainActivity.getChatMainActivity().get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (map.get(VIDEO_SMALL) != null) {
                        mWindowManager.removeViewImmediate(map.get(VIDEO_SMALL));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void exChange2AudioPresenter() {
        presenter = null;
        presenter = PresenterFactory.getInstance(AudioPresenter.class);
        isVideo = false;
        initSensor();
    }

    public void gotoAudio(boolean fromVideo) {
        isScaled = false;
        openView();
        AudioFloatingBiggerView biggerView = null;
        if (fromVideo) {
            exChange2AudioPresenter();
        }
        if (map.get(AUDIO_BIGGER) == null) {
            biggerView = new AudioFloatingBiggerView(this, mContext, fromVideo);
            map.put(AUDIO_BIGGER, biggerView);
        } else {
            biggerView = (AudioFloatingBiggerView) map.get(AUDIO_BIGGER);
        }
        presenter.attatchView(biggerView);
        try {
            ChatMainActivity.getChatMainActivity().get().runOnUiThread(() -> {
                if (currentKey != null && map.get(currentKey) != null) {
                    mWindowManager.removeViewImmediate(map.get(currentKey));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentKey = AUDIO_BIGGER;
        TempFileUtils.getInstance().saveCurrentKey(currentKey);
        biggerView.setLayoutParams(LAYOUT_PARAMS);
    }


    public void addSmallView() {
        try {
            isScaled = true;
            scaleView();
            currentKey = isVideo ? VIDEO_SMALL : AUDIO_SMALL;
            TempFileUtils.getInstance().saveCurrentKey(currentKey);
      /*  if(!map.containsKey(currentKey)) {
            View  smallView = isVideo ? (VideoFloatingBiggerView) map.get(VIDEO_BIGGER) : (AudioFloatingBiggerView) map.get(AUDIO_BIGGER);
            map.put(currentKey, smallView);
        }*/
            if (!map.containsKey(currentKey)) {
                View smallView = isVideo ? (VideoFloatingBiggerView) map.get(VIDEO_BIGGER) : new AudioFloatingView(this, mContext);
                map.put(currentKey, smallView);
                if (!isVideo) {
                    presenter.attatchView(smallView);
                }
            }
            ChatMainActivity.getChatMainActivity()
                    .get().runOnUiThread(() -> mWindowManager.removeViewImmediate(map.get(isVideo ? VIDEO_BIGGER : AUDIO_BIGGER)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openView() {
        params.width = defaultDisplay.getWidth();
        params.height = defaultDisplay.getHeight();
        LAYOUT_PARAMS = params;
    }


    public void scaleView() {
        params.width = (int) (defaultDisplay.getWidth() * 0.25);
        params.height = (int) (defaultDisplay.getHeight() * 0.19);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.x = 20;
        params.y = 20;
        LAYOUT_PARAMS = params;
    }

    public final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            RCLog.d("come--------->>>>" + "onFirstRemoteVideoDecoded");
            EventBus.getDefault().post(new HandelEventBus(onUserShow, uid));
        }

        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            RCLog.d("VideoActivtiy--------->>>>" + "onUserOffline");
            if (hasSend) {
                return;
            }
            hasSend = true;
            EventBus.getDefault().post(new HandelEventBus(onUserOffline, uid));
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            RCLog.d("VideoActivtiy--------->>>>", "onUserMuteVideo");
            EventBus.getDefault().post(new HandelEventBus(onUserMuteVideo, uid));
        }

        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
            RCLog.d("VideoActivtiy--------->>>>", "onUserMuteAudio");
            EventBus.getDefault().post(new HandelEventBus(onUserMuteAudio, uid));
        }


        @Override
        public void onFirstRemoteAudioFrame(int uid, int elapsed) {
            RCLog.d("VideoActivtiy--------->>>>", "onFirstRemoteAudioFrame");
//            presenter.closeReInviteTimeEngine();
            EventBus.getDefault().post(new HandelEventBus(onFirstRemoteAudioFrame, uid));

        }

        @Override
        public void onConnectionLost() {
            finish();
        }

        @Override
        public void onLastmileQuality(int quality) {
            if (quality > 3) {
                EventBus.getDefault().post(new HandelEventBus(onLastmileQuality, quality));
            }
        }
    };


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(HandelEventBus event) {

        int uid = event.getUid();
        switch (event.getHandleType()) {
            case onUserShow:
                presenter.closeReInviteTimeEngine();
                presenter.setupRemoteVideo(uid);
                break;

            case onUserOffline:
                presenter.cancelTalk(R.string.remote_cancel_done);
                if (!isCall) {
                    return;
                }
                CountTimeEngine.getInstance().stopIfNeccessary();
                String time = TempFileUtils.getInstance().getTime();
                presenter.setStatus(ChatStatus.END, time);
                break;

            case onUserMuteAudio:
                presenter.onRemoteUserAudioMuted();
                break;

            case onUserMuteVideo:
                presenter.onRemoteUserVideoMuted(uid);
                break;

            case onFirstRemoteAudioFrame:
                presenter.closeReInviteTimeEngine();
                break;

            case onLastmileQuality:
                if (uid > 2) {
                    CustomToast.showToastInfo(R.string.talk_quality_low);
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VideoBusEvent event) {
        switch (event.getType()) {
            case VideoSendAvType.ACCEPT: // 接通
                if (presenter instanceof AudioPresenter) {
                    presenter.answerCall(false);
                }
                break;

            case VideoSendAvType.HANG_UP: //挂断
//                CustomToast.showToastInfo(R.string.remote_cancel_done);
                break;

            case VideoSendAvType.CANCEL: //取消
                presenter.cancelTalk(R.string.remote_cancel_done);
                if (isCall) {
                    presenter.setStatus(ChatStatus.CANCEL, "");
                }
                break;

            case VideoSendAvType.REFUSE: //拒绝
                int msgId = presenter instanceof VideoPresenter ? R.string.remote_refuse_video_done : R.string.remote_refuse_audio_done;
                presenter.cancelTalk(msgId);
                if (isCall) {
                    presenter.setStatus(ChatStatus.REFUSE, "");
                }
                break;
            case VideoSendAvType.TALKING:
                if (event.getJson() != null) {
                    //notify caller calling
                    presenter.sendVideo(event.getJson());
                    return;
                }
             /*   CustomToast.showToastInfo(R.string.is_talking);
                finish();*/
             presenter.cancelTalk(R.string.is_talking);
                break;
        }
    }


    SuneeOnAudioFocusChangeListener focusChangeListener;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestFocus() {
        try {
            focusChangeListener = new SuneeOnAudioFocusChangeListener();
            int i = aManger.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if(i == 0){
                startPhoneStateObserver();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPhoneStateObserver() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager != null) {
            try {
                telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
            } catch(Exception e) {
            }
        }
    }

    private boolean isTalking ;
    private final class PhoneListener extends PhoneStateListener {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            try {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:   //来电
                        isTalking = true ;
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK:   //接通电话
                        isTalking = true ;
                        Log.d("CALL_STATE_OFFHOOK", incomingNumber);
                        break ;

                    case TelephonyManager.CALL_STATE_IDLE :  //挂掉电话
                        Log.d("hangup", incomingNumber);
                        if(isTalking){
                            requestFocus();
                            isTalking = false ;
                        }
                        break;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private class SuneeOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://Pause playback
                    presenter.pauseAudio();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN://Resume playback
                    presenter.resumeAudio();

                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://
                    break;
                case AudioManager.AUDIOFOCUS_LOSS://Stop playback
                    presenter.cancelTalk(R.string.call_done);
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ViewEvent event) {
        if (currentKey != null && currentKey == AUDIO_BIGGER && map != null) {
            AudioFloatingBiggerView view = (AudioFloatingBiggerView) map.get(currentKey);
            view.scaleImg();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CloseBusEvent event) {
        finish();
    }

    public void unRegister() {
        EventBus.getDefault().unregister(this);
    }

    Disposable disposable;

    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    public void finish() {
        try {
            if(null != sensorUtils){
                ScreenLockUtils.getInstance().releaseCpuLock();
                sensorUtils = null ;
            }
            hasSend = false;
            RocketChatApplication.stopSound();
            if(aManger !=null){
                aManger.abandonAudioFocus(focusChangeListener);
            }
            if (presenter != null) {
                presenter.hangUp();
            }
            if (map != null && map.size() > 0 && map.get(currentKey) != null) {
                disposable = Single.create(e -> e.onSuccess("")).subscribeOn(AndroidSchedulers.mainThread()).map(o -> {
                         startAnimation();
                         return null;
                      }).delay(500, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            mWindowManager.removeViewImmediate(map.get(currentKey));
                            map.clear();
                            disposable.dispose();
                        });

            }
            EventBus.getDefault().post(new ServiceStop());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startAnimation() {
        map.get(currentKey)
                .animate().alpha(0.1f)
                .translationY(defaultDisplay.getHeight());
    }

}
