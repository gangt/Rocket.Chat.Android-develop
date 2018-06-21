package chat.rocket.android.video.presenter;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.json.JSONObject;

import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.video.helper.ReInvitedTimeEngine;
import chat.rocket.android.video.helper.RequestTimeEngine;
import io.agora.rtc.IRtcEngineEventHandler;

/**
 * Created by Administrator on 2018/4/28/026.
 */

public abstract class BasePresenter {
    public RequestTimeEngine requestTimeEngine;
    public ReInvitedTimeEngine reInvitedTimeEngine;


    public abstract  void attatchView(View view);

    public abstract void bindData(MethodCallHelper methodCallHelper, Context context, IRtcEngineEventHandler mRtcEventHandler, String channel, View view);

    public abstract void initAgoraEngineAndJoinChannel();

    public abstract void hangUp();

    public abstract void cancelTalk(int msgId);

    public abstract void sendVideo(int av_type, String warnmsg) ;

//    public abstract void sendMethod2(String msg);
//    public abstract  void sendWithDataMethod2(String status, String receiveMsg,String fromMsg);

    public abstract void joinChannel();

    public abstract void onRemoteUserLeft() ;

    public abstract  void setStateView(int type);

    public abstract  void  answerCall(boolean fromLocal);

    public abstract  void remindCall(int seconds);

    public abstract void setStatus(String status ,String time);

    public abstract  void closeActivityImediately();

    public void onRemoteUserVideoMuted(int uid){}

    public void onRemoteUserAudioMuted (){}

    public void setupLocalVideo(FrameLayout container) {
    }

    public void setupRemoteVideo(int uid) {

    }

    public void switchCameraClicked() {
    }

    public void   change2AudioChat(){
    }

    public void onLocalAudioMuteClicked(){

    }

    public void onSwitchSpeakerphoneClicked() {

    }

    public void scaleImg(boolean isScale){

    }

    public void exChangeView(){

    }

    public  abstract void pauseAudio();

    public  abstract void resumeAudio();

    public abstract  void sendVideo(JSONObject json);

    public void initTimeEngine(boolean isCall){
       if(isCall){
           getRequestTiemEngine();
           return;
       }
        getReInvitedTiemEngine();
    }


    public void getRequestTiemEngine(){
        requestTimeEngine = RequestTimeEngine.getInstance();
        requestTimeEngine.attatchData(this);
        requestTimeEngine.start();
    }

    public void getReInvitedTiemEngine(){
        reInvitedTimeEngine = ReInvitedTimeEngine.getInstance();
        reInvitedTimeEngine.bindData(this,60);
        reInvitedTimeEngine.start();
    }

    public void closeReInviteTimeEngine(){
        ReInvitedTimeEngine.getInstance().stopIfNeccessary();
    }

}
