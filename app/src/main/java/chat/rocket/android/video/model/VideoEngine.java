package chat.rocket.android.video.model;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import chat.rocket.android.R;
import chat.rocket.android.log.RCLog;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by Tidom on 2018/4/28/026.
 */

public class VideoEngine {

    private Context context ;
    private  IRtcEngineEventHandler mRtcEventHandler ;
    public RtcEngine mRtcEngine;
    private String channel ;
    FrameLayout container;
    FrameLayout local_container ;
    private int uid ;
    private boolean isExchanged ;

    public VideoEngine(Context context, IRtcEngineEventHandler mRtcEventHandler, FrameLayout container, FrameLayout local_container, String channel) {
        this.context = context;
        this.mRtcEventHandler = mRtcEventHandler;
        this.container = container;
        this.local_container = local_container;
        this.channel = channel ;
    }

    public void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(context, context.getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.enableLastmileTest();
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    public void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
    }

    public void setupLocalVideo() {
        SurfaceView surfaceView = RtcEngine.CreateRendererView(context);
        surfaceView.setZOrderMediaOverlay(true);
        container.setVisibility(View.VISIBLE);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    public void setupLocalVideo(FrameLayout view) {
        try {
            view.setVisibility(View.VISIBLE);
            if(view.getChildCount() > 0){
                view.removeAllViews();
            }
            SurfaceView surfaceView = RtcEngine.CreateRendererView(context);
            if(!isExchanged){
                surfaceView.setZOrderMediaOverlay(true);
            }
            view.addView(surfaceView);
            mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN , 0));
            mRtcEngine.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinChannel() {
        RCLog.d("come--------->>>>" + "joinChannel  "+channel); //GKzmawzeEXBNL8FBALCfLsjiWmu234ezpk
        mRtcEngine.joinChannel(null, channel, "Extra Optional Data", 0);
    }

    public void setupRemoteVideo( int uid) {

        try {
            this.uid = uid ;
            container.setVisibility(View.VISIBLE);
            if (container.getChildCount() > 0) {
                container.removeAllViews();
            }
            setupLocalVideo(local_container);
            SurfaceView surfaceView = RtcEngine.CreateRendererView(context);
            container.addView(surfaceView);
            mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
            surfaceView.setTag(uid); // for mark purpose
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupRemoteVideo2(FrameLayout frameLayout ) {
        frameLayout.removeAllViews();
        SurfaceView surfaceView = RtcEngine.CreateRendererView(context);
        if(isExchanged){
            surfaceView.setZOrderMediaOverlay(true);
        }
        frameLayout.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        surfaceView.setTag(uid); // for mark purpose
    }

    public void exChangeView() {
        isExchanged = !isExchanged ;
        if(isExchanged){
            setupRemoteVideo2(local_container);
            setupLocalVideo(container);
        }else{
            setupLocalVideo(local_container);
            setupRemoteVideo2(container);
        }
    }

    public void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    public void onRemoteUserLeft() {
        container.removeAllViews();
    }

    public void onSwitchCameraClicked() {
        mRtcEngine.switchCamera();
    }

    public void onLocalVideoMuteClicked() {
        try {
            mRtcEngine.muteLocalVideoStream(true);
            SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
            surfaceView.setZOrderMediaOverlay(false);
            surfaceView.setVisibility( View.GONE);
        } catch (Exception e) {
            RCLog.d("come--------->>>>" + "onLocalVideoMuteClicked  "+ e.toString()); //GKzmawzeEXBNL8FBALCfLsjiWmu234ezpk
            e.printStackTrace();
        }
    }

    public void onRemoteUserVideoMuted(int uid) {
        try {
            SurfaceView surfaceView = (SurfaceView) local_container.getChildAt(0);
            Object tag = surfaceView.getTag();
            if (tag != null && (Integer) tag == uid) {
                surfaceView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            RCLog.d("come--------->>>>" + "onRemoteUserVideoMuted  "+ e.toString());
        }
    }

    public void pauseAudio(){
        mRtcEngine.pauseAudio();
    }

    public void resumeAudio(){
        mRtcEngine.resumeAudio();
    }
}
