package chat.rocket.android.video.model;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import chat.rocket.android.R;
import chat.rocket.android.log.RCLog;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

/**
 * Created by Administrator on 2018/4/28/026.
 */

public class AudioEngine {

    private Context context ;
    private IRtcEngineEventHandler mRtcEventHandler ;
    private String channel ;
    public RtcEngine mRtcEngine;

    public AudioEngine(Context context, IRtcEngineEventHandler mRtcEventHandler, String channel) {
        this.context = context;
        this.mRtcEventHandler = mRtcEventHandler;
        this.channel = channel;
    }

    public void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(context, context.getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setDefaultAudioRoutetoSpeakerphone(false);
            mRtcEngine.enableLastmileTest();
            mRtcEngine.setEnableSpeakerphone(false);
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    public void joinChannel() {
        int res = mRtcEngine.joinChannel(null, this.channel, "Extra Optional Data", 0);
        RCLog.d("come--------->>>>joinChannel" + " "+res);
    }

    public void leaveChannel() {
        if(mRtcEngine !=null){
            mRtcEngine.leaveChannel();
        }
    }


    private boolean speak = false;
    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        speak = !speak ;
        mRtcEngine.setEnableSpeakerphone(speak);
        if (speak) {
            iv.setImageResource(R.mipmap.speakphone_on);
        } else {
            iv.setImageResource(R.mipmap.speakphone);
        }
    }

    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.setImageResource(R.mipmap.micro_mute);
        } else {
            iv.setSelected(true);
            iv.setImageResource(R.mipmap.silence_on);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    public void pauseVoice(){
        mRtcEngine.pauseAudio();
    }

    public void resumeAudio(){
        mRtcEngine.resumeAudio();
    }
}
