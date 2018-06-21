package chat.rocket.android.video.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.video.helper.ChatStatus;
import chat.rocket.android.video.helper.CountTimeEngine;
import chat.rocket.android.video.helper.MessageSendEngine;
import chat.rocket.android.video.helper.ReInvitedTimeEngine;
import chat.rocket.android.video.helper.RequestTimeEngine;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.helper.VideoSendAvType;
import chat.rocket.android.video.model.RemoteUser;
import chat.rocket.android.video.model.RemoteUserRender;
import chat.rocket.android.video.model.VideoEngine;
import chat.rocket.android.video.model.VideoSendMsgModel;
import chat.rocket.android.video.view.CustomToast;
import chat.rocket.android.video.view.VideoFloatingBiggerView;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

/**
 * Created by Administrator on 2018/4/24/024.
 */

public class VideoPresenter extends BasePresenter {
    private VideoEngine videoEngine;
    //    private WeakReference<VideoActivity> mVideoActivity;
    private VideoFloatingBiggerView view;
    private CountTimeEngine timeEngine;
    private boolean hasChanged2Audio;
    private MessageSendEngine messageSendEngine;
    private Context context;
    private String currentMediaId ;

    @Override
    public void attatchView(View view) {

    }

    @Override
    public void bindData(MethodCallHelper methodCallHelper, Context context, IRtcEngineEventHandler mRtcEventHandler, String channel, View view) {
        this.view = (VideoFloatingBiggerView) view;
        this.context = context;
        videoEngine = new VideoEngine(context, mRtcEventHandler, this.view.container, this.view.local_container, channel);
        messageSendEngine = new MessageSendEngine(methodCallHelper);
    }

    @Override
    public void initAgoraEngineAndJoinChannel() {
        TempFileUtils.getInstance().saveCallingUserId(view.viewManager.id);
        TempFileUtils.getInstance().setTalkingStatus(true);
        videoEngine.initializeAgoraEngine();     // Tutorial Step 1
        videoEngine.setupVideoProfile();         // Tutorial Step 2
        if (view.viewManager.isCall) {
            videoEngine.setupLocalVideo();           // Tutorial Step 3
            videoEngine.joinChannel();               // Tutorial Step 4
            setStatus(ChatStatus.WAIT, "");
        }
        initTimeEngine(view.viewManager.isCall);

    }


    @Override
    public void setupLocalVideo(FrameLayout container) {

        videoEngine.setupLocalVideo(container);
    }

    @Override
    public void onRemoteUserVideoMuted(int uid) {
        if (hasChanged2Audio) {
            return;
        }
        CustomToast.showToastInfo(R.string.remote_change2audio);
        videoEngine.onRemoteUserVideoMuted(uid);
        change2AudioChat();

    }

    @Override
    public void setupRemoteVideo(int uid) {
        videoEngine.setupRemoteVideo(uid);
        answerCall(false);
    }


    @Override
    public void onRemoteUserLeft() {
        videoEngine.onRemoteUserLeft();
    }

    @Override
    public void joinChannel() {
        RCLog.d("come--------->>>>" + "joinChannel  "); //AFJAnJZApMTLtXEFHirMrmj6hhXKzSkLG5
        videoEngine.joinChannel();
    }

    @Override
    public void hangUp() {
        videoEngine.leaveChannel();
        RtcEngine.destroy();
        videoEngine.mRtcEngine = null;
        videoEngine = null;
        hasChanged2Audio = false;
        TempFileUtils.getInstance().setTalkingStatus(false);
        stopAllEngineIfNeccessary();
        ReInvitedTimeEngine.getInstance().stopIfNeccessary();
        CountTimeEngine.getInstance().stopIfNeccessary();
    }

    public void stopAllEngineIfNeccessary() {
        RequestTimeEngine.getInstance().stopIfNeccessary();
        if(view == null){
            return;
        }
        if(null != view.inviteStateMessage){
            view.inviteStateMessage.stopIfNeccessary();
        }
        if( view.customTx != null){
            view.customTx.stop();
        }
    }

    @Override
    public void switchCameraClicked() {
        videoEngine.onSwitchCameraClicked();
    }

    Disposable disposable;

    @Override
    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    public void cancelTalk(int msgId) {
        RocketChatApplication.stopSound();
        stopAllEngineIfNeccessary();
        ReInvitedTimeEngine.getInstance().stopIfNeccessary();
        disposable = Single.create((SingleOnSubscribe<String>)
                e -> {
                    CustomToast.showToastInfo(msgId);
                    e.onSuccess("");
                }).delay(1000, TimeUnit.MILLISECONDS).subscribe(s -> {
            view.viewManager.finish();
            disposable.dispose();
        });
    }

   public void scaleImg(boolean isScale){
           view.local_container.setVisibility(isScale ? View.GONE :View.VISIBLE);
           view.answer_call_ll.setVisibility(isScale ? View.GONE : View.VISIBLE);
           view.smallImg.setVisibility(isScale ? View.GONE :View.VISIBLE);
           view.viewManager.add2Window();
   }


    @Override
    public void change2AudioChat() {
        sendVideo(VideoSendAvType.CHANGE_AUDIO, "");
        hasChanged2Audio = true;
        videoEngine.onLocalVideoMuteClicked();
        view.viewManager.gotoAudio(true);
        view.viewManager.add2Window();
    }


    @Override
    public void setStateView(int bottomType) {
        RemoteUserRender userRender = new RemoteUserRender(new RemoteUser(view.viewManager.userName, view.viewManager.avar));
        userRender.showAvatar(view.vedioAvar);
        userRender.showUsername(view.inviteUsername);

        switch (bottomType) {
            case 0:
                view.remote_call_ll.setVisibility(View.VISIBLE);
                view.titleBar.setVisibility(View.VISIBLE);
                view.inviteStateMessage.setText(context.getString(R.string.remote_call_text));
                break;
            case 1:
                view.remote_call_ll.setVisibility(View.GONE);
                view.titleBar.setVisibility(View.GONE);
                view.inviteStateMessage.setVisibility(View.GONE);
                view.inviteStateMessage.stopIfNeccessary();
                view.requestCallLl.setVisibility(View.GONE);
                view.smallImg.setVisibility(View.VISIBLE);
                view.answer_call_ll.setVisibility(View.VISIBLE);
                view.videoTimeCount.setVisibility(View.VISIBLE);
                break;
            case 2:
                view.requestCallLl.setVisibility(View.VISIBLE);
                view.titleBar.setVisibility(View.VISIBLE);
                view.inviteStateMessage.show(context.getString(R.string.request_call_text));
                break;

        }
    }


    @Override
    public void answerCall(boolean fromLocal) {
        RocketChatApplication.stopSound();
        stopAllEngineIfNeccessary();
        if (fromLocal) {
            setupLocalVideo(view.local_container);
            joinChannel();
            startCountTime();
        }
        setStateView(1);
        if (timeEngine == null) {
            timeEngine = CountTimeEngine.getInstance();
            timeEngine.updateView(view.videoTimeCount);
            timeEngine.start();
        }
    }

    private void startCountTime() {
        ReInvitedTimeEngine reInvitedTimeEngine = ReInvitedTimeEngine.getInstance();
        reInvitedTimeEngine.bindData(this, 10);
        reInvitedTimeEngine.start();
    }

    @Override
    public void remindCall(int seconds) {
        if (seconds == 30) {
            view.customTx.setVisibility(View.VISIBLE);
            view.customTx.show();
        }
        if (seconds == 55) {
            CustomToast.showToastInfo(R.string.no_response);
        }
        if (seconds == 60) {
            setStatus(ChatStatus.CANCEL, "");
            cancelTalk(R.string.call_done);
            sendVideo(VideoSendAvType.CANCEL, "");
        }
    }

/*    public void setStatus(String status, String time, String mediaId) {
        String userId = view.viewManager.id;
        String rid = RocketChatCache.INSTANCE.getSelectedRoomId();
        messageSendEngine.setStatus(RocketChatCache.INSTANCE.getUserId(), rid, status, "video", time, userId, mediaId);
    }*/


    @Override
    public void setStatus(String status, String time) {
        String userId = view.viewManager.id;
        String rid = RocketChatCache.INSTANCE.getSelectedRoomId();
//        String rid = userId +RocketChatCache.INSTANCE.getUserId() ;
        if(status .equals(ChatStatus.WAIT)){
           currentMediaId = UUID.randomUUID().toString();
        }
        messageSendEngine.setStatus(RocketChatCache.INSTANCE.getUserId(), rid, status, "video", time, userId, currentMediaId);
    }

    @Override
    public void closeActivityImediately() {
        view.viewManager.finish();
    }


    /***
     *
     * @param av_type
     * @param warnmsg
     */
    public void sendVideo(int av_type, String warnmsg) {
//        String roomId = RocketChatCache.INSTANCE.getUserId() + view.viewManager.id;
        String roomId = view.viewManager.channel;
        try {
            messageSendEngine.sendVideo(view.viewManager.id,
                    new VideoSendMsgModel.MediaBean(true, false), roomId,
                    new VideoSendMsgModel.CalluserBean(RocketChatCache.INSTANCE.getUserUsername(), RocketChatCache.INSTANCE.getUserId()),
                    RocketChatCache.INSTANCE.getUserId(), warnmsg, "single", av_type
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     *     username = json.getJSONObject("calluser").getString("username")
     remoteUserId = json.getString("from")
     val _id = json.getJSONObject("calluser").getString("_id")
     val isvideo = json.getJSONObject("media").getBoolean("video")
     * @param json
     */


    @Override
    public void sendVideo(JSONObject json) {
        try {
            String roomId = json.getJSONObject("calluser").getString("_id");
            String id = json.getJSONObject("calluser").getString("_id") ;
            messageSendEngine.sendVideo(id,
                    new VideoSendMsgModel.MediaBean(true, false), roomId,
                    new VideoSendMsgModel.CalluserBean(RocketChatCache.INSTANCE.getUserUsername(), RocketChatCache.INSTANCE.getUserId()),
                    RocketChatCache.INSTANCE.getUserId(), "", "single", VideoSendAvType.TALKING
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void exChangeView() {
        videoEngine.exChangeView();
    }

    @Override
    public void pauseAudio() {
        videoEngine.pauseAudio();
    }

    @Override
    public void resumeAudio() {
        videoEngine.resumeAudio();
    }


    /***
     * 0:video   1:audio
     * @param type
     */

    public void preStart(int type) {
        switch (type) {
            case 0:
//                LaunchUtil.showActivity(VideoActivity.class,"","");
                break;

            case 1:
                break;
        }
    }
}
