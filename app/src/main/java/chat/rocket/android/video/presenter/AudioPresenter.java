package chat.rocket.android.video.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.video.helper.ChatStatus;
import chat.rocket.android.video.helper.CountTimeEngine;
import chat.rocket.android.video.helper.MessageSendEngine;
import chat.rocket.android.video.helper.ReInvitedTimeEngine;
import chat.rocket.android.video.helper.RequestTimeEngine;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.helper.VideoSendAvType;
import chat.rocket.android.video.model.AudioEngine;
import chat.rocket.android.video.model.RemoteUser;
import chat.rocket.android.video.model.RemoteUserRender;
import chat.rocket.android.video.model.VideoSendMsgModel;
import chat.rocket.android.video.view.AudioFloatingBiggerView;
import chat.rocket.android.video.view.AudioFloatingView;
import chat.rocket.android.video.view.CustomToast;
import chat.rocket.android.video.view.LoadingTextView;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/28/026.
 */

public class AudioPresenter extends BasePresenter {
    private AudioEngine audioEngine;
    //    private WeakReference<VideoActivity> mVideoActivity;
    private Context context;
    private CountTimeEngine timeEngine;
    private MessageSendEngine messageSendEngine;
    private boolean isAudioTalking;
    private boolean isScaled;
    private AudioFloatingBiggerView view;
    private AudioFloatingView smallView;
    LoadingTextView inviteStateCenter;
    private String currentMediaId ;

    @Override
    public void attatchView(View view) {
        if (view instanceof AudioFloatingView) {
            smallView = (AudioFloatingView) view;
        } else {
            this.view = (AudioFloatingBiggerView) view;
        }
    }

    @Override
    public void bindData(MethodCallHelper methodCallHelper, Context context, IRtcEngineEventHandler mRtcEventHandler, String channel, View view) {
        this.context = context;
        this.view = (AudioFloatingBiggerView) view;
        audioEngine = new AudioEngine(context, mRtcEventHandler, channel);
        messageSendEngine = new MessageSendEngine(methodCallHelper);
    }

    @Override
    public void initAgoraEngineAndJoinChannel() {
        TempFileUtils.getInstance().saveCallingUserId(view.viewManager.id);
        TempFileUtils.getInstance().setTalkingStatus(true);
        audioEngine.initializeAgoraEngine();
        if (view.viewManager.isCall) {
            audioEngine.joinChannel();               // Tutorial Step 4
            if(!view.fromVideo){
                setStatus(ChatStatus.WAIT, "");
            }
        }
        if(view.fromVideo){
            return;
        }
        initTimeEngine(view.viewManager.isCall);
    }

    /***
     *
     * @param av_type
     * @param warnmsg
     */
    public void sendVideo(int av_type, String warnmsg) {
//        String roomId =  RocketChatCache.INSTANCE.getUserId()+view.viewManager.id;
        String roomId = view.viewManager.channel;
        try {
            messageSendEngine.sendVideo(view.viewManager.id,
                    new VideoSendMsgModel.MediaBean(false, true), roomId,
                    new VideoSendMsgModel.CalluserBean(RocketChatCache.INSTANCE.getUserUsername(), RocketChatCache.INSTANCE.getUserId()),
                    RocketChatCache.INSTANCE.getUserId(), "", "single", av_type
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendVideo(JSONObject json) {
        try {
            String roomId = json.getJSONObject("calluser").getString("_id");
            String id = json.getJSONObject("calluser").getString("_id");
            messageSendEngine.sendVideo(id,
                    new VideoSendMsgModel.MediaBean(true, false), roomId,
                    new VideoSendMsgModel.CalluserBean(RocketChatCache.INSTANCE.getUserUsername(), RocketChatCache.INSTANCE.getUserId()),
                    RocketChatCache.INSTANCE.getUserId(), "", "single", VideoSendAvType.TALKING
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void hangUp() {
        RtcEngine.destroy();
        if (audioEngine != null) {
            audioEngine.leaveChannel();
            audioEngine.mRtcEngine = null;
            audioEngine = null;
        }
        stopAllEngineIfNeccessary();
        CountTimeEngine.getInstance().stopIfNeccessary();
        TempFileUtils.getInstance().setTalkingStatus(false);
        isAudioTalking = false;
    }


    public void stopAllEngineIfNeccessary() {
        RequestTimeEngine.getInstance().stopIfNeccessary();
        ReInvitedTimeEngine.getInstance().stopIfNeccessary();
        if (inviteStateCenter != null) {
            inviteStateCenter.stopIfNeccessary();
        }
        if(view.invite_state_center !=null){
            view.invite_state_center.stopIfNeccessary();
        }
    }

    Disposable disposable2;

    @Override
    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    public void cancelTalk(int msgId) {
        RocketChatApplication.stopSound();
        if(reInvitedTimeEngine !=null){
            reInvitedTimeEngine.stopIfNeccessary();
        }
        if(requestTimeEngine !=null){
            requestTimeEngine.stopIfNeccessary();
        }

        if (view.invite_state_center.getVisibility() == View.VISIBLE) {
            view.invite_state_center.stopIfNeccessary();
        }
        if(view.customShowTextView !=null){
            view.customShowTextView.stop();
        }
        disposable2 = Single.create((SingleOnSubscribe<String>)
                e -> {
                    CustomToast.showToastInfo(msgId);
                    e.onSuccess("");
                }).delay(1000, TimeUnit.MILLISECONDS).subscribe(s ->
        {
            view.viewManager.finish();
            disposable2.dispose();
        });
    }

    @Override
    public void closeActivityImediately() {
        view.viewManager.finish();
    }


    public void onLocalAudioMuteClicked() {
        View audioJmp = view.layoutView.findViewById(R.id.audio_jump);
        audioEngine.onLocalAudioMuteClicked(audioJmp);
    }


    public void onSwitchSpeakerphoneClicked() {
        View audioSpeakphoneImg = view.layoutView.findViewById(R.id.audio_speakphone_img);
        audioEngine.onSwitchSpeakerphoneClicked(audioSpeakphoneImg);
    }


    @Override
    public void joinChannel() {
        audioEngine.joinChannel();
    }

    @Override
    public void onRemoteUserLeft() {
        //TODO...
    }

    @Override
    public void setStateView(int type) {
//        VideoActivity view = view.viewManager;
        RemoteUserRender userRender = new RemoteUserRender(new RemoteUser(view.viewManager.userName, view.viewManager.avar));
        userRender.showAvatar(view.audio_user_avar);
        userRender.showUsername(view.audio_username_center);
        switch (type) {

            case 0: //被叫
                view.audioRemoteCallLl.setVisibility(View.VISIBLE);
                view.invite_state_center.setText(context.getString(R.string.remote_call_audio_text));
                break;

            case 1://接通
                view.audioRemoteCallLl.setVisibility(View.GONE);
                view.audio_canccel_invite.setVisibility(View.GONE);
                view.invite_state_center.setVisibility(View.GONE);
                view.invite_state_center.stopIfNeccessary();
                view.audio_answer_call_ll.setVisibility(View.VISIBLE);
                view.audio_time_count.setVisibility(View.VISIBLE);
                break;

            case 2: //主叫
                view.audio_canccel_invite.setVisibility(View.VISIBLE);
//                view.inviteStateCenter.setText(view.getString(R.string.request_call_text));
                view.invite_state_center.setVisibility(View.VISIBLE);
                view.invite_state_center.show(context.getString(R.string.request_call_text));
                break;
            case 3:
                isAudioTalking = true;
                view.audio_time_count.setVisibility(View.VISIBLE);
                CountTimeEngine timeEngine = CountTimeEngine.getInstance();
                if (timeEngine != null) {
                    timeEngine.updateView(view.audio_time_count);
                }
                view.audio_answer_call_ll.setVisibility(View.VISIBLE);
                break;


        }
    }

    @Override
    public void answerCall(boolean fromLocal) {
        RocketChatApplication.stopSound();
        isAudioTalking = true;
        if (fromLocal) {
            joinChannel();
        }
        if(view.customShowTextView !=null){
            view.customShowTextView.stop();
        }
        view.invite_state_center.stopIfNeccessary();
        if (timeEngine == null) {
            timeEngine = CountTimeEngine.getInstance();
            timeEngine.updateView(isScaled ? smallView.audioScaleTime : view.audio_time_count);
            timeEngine.start();
        }
        if (!isScaled) {
            setStateView(1);
        }
        if (requestTimeEngine != null) {
            requestTimeEngine.stopIfNeccessary();
        }
    }

    @Override
    public void remindCall(int seconds) {
        if (seconds == 30) {
            view.customShowTextView.setVisibility(View.VISIBLE);
            view.customShowTextView.show();
        }
        if (seconds == 55) {
            CustomToast.showToastInfo(R.string.no_response);
        }
        if (seconds == 60) {
            setStatus(ChatStatus.CANCEL, "");
            cancelTalk(R.string.call_cancel);
            sendVideo(VideoSendAvType.CANCEL, "");
        }
    }

    @Override
    public void setStatus(String status, String time) {
        RCLog.d("come--------->>>>" + "time  :" + time);
        String userId = view.viewManager.id;
//        String mediaId = UUID.randomUUID().toString();
        if(status .equals(ChatStatus.WAIT) || currentMediaId == null){
            currentMediaId = UUID.randomUUID().toString();
        }
        String rid = RocketChatCache.INSTANCE.getSelectedRoomId();
        messageSendEngine.setStatus(RocketChatCache.INSTANCE.getUserId(), rid, status, "audio", time, userId, currentMediaId);
    }

    public void scaleImg(boolean isScale) {
        RCLog.d("come--------->>>>" + "scaleImg  :" + isScale);
//        VideoActivity view = view.viewManager;
        isScaled = isScale;
        if (isScale) {
            //缩放
         /*   view.inviteCenterShowLl.setVisibility(View.GONE);
            view.audio_small_img.setVisibility(View.GONE);
            view.audio_scale_view.setVisibility(View.VISIBLE);*/
            if (isAudioTalking) {
                //通话中缩放
                RCLog.d("come--------->>>>" + "通话中缩放  :");
              /*  view.audio_answer_call_ll.setVisibility(View.GONE);
                view.audio_time_count.setVisibility(View.GONE);
                view.audioScaleTime.setVisibility(View.VISIBLE);
                CountTimeEngine.getInstance().updateView(view.audioScaleTime);
                view.viewManager.add2Window();*/
                CountTimeEngine.getInstance().updateView(smallView.audioScaleTime);
                smallView.viewManager.add2Window();

            } else {
                //请求中缩放
                RCLog.d("come--------->>>>" + "请求中缩放  :");
              /*  view.audio_small_img.setVisibility(View.GONE);
                view.audio_canccel_invite.setVisibility(View.GONE);
                view.audioRemoteCallLl.setVisibility(View.GONE);
                view.invite_state_center.setVisibility(View.GONE);
                view.audioScaleTime.setVisibility(View.VISIBLE);
                view.audioScaleTime.setText("等待接听");
                view.viewManager.add2Window();*/
                smallView.audioScaleTime.setText("等待接听");
                smallView.viewManager.add2Window();
            }

        } else {
            //打开
            view.inviteCenterShowLl.setVisibility(View.VISIBLE);
//            view.audio_scale_view.setVisibility(View.GONE);
            view.audio_small_img.setVisibility(View.VISIBLE);
            if (isAudioTalking) {
                //通话中打开
                RCLog.d("come--------->>>>" + "通话中打开  :");
                view.audio_answer_call_ll.setVisibility(View.VISIBLE);
                view.audio_time_count.setVisibility(View.VISIBLE);
                CountTimeEngine.getInstance().updateView(view.audio_time_count);
                view.viewManager.add2Window();
            } else {
                //请求中打开
                RCLog.d("come--------->>>>" + "请求中打开  :");
                view.invite_state_center.setVisibility(View.VISIBLE);
                if (view.viewManager.isCall) {
                    view.audio_canccel_invite.setVisibility(View.VISIBLE);
                } else {
                    view.audioRemoteCallLl.setVisibility(View.VISIBLE);
                }
                view.invite_state_center.setText(context.getString(R.string.calling));
                view.viewManager.add2Window();
            }

        }

    }

    @Override
    public void pauseAudio() {
        audioEngine.pauseVoice();
    }

    @Override
    public void resumeAudio() {
        audioEngine.resumeAudio();
    }

    @Override
    public void onRemoteUserAudioMuted() {
        //TODO...
    }

}
