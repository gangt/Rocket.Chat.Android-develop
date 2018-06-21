package chat.rocket.android.video.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Task;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.video.model.VideoSendMsgModel;

/**
 * Created by Administrator on 2018/5/6/006.
 */

public class MessageSendEngine {
    private MethodCallHelper methodCallHelper;
    public static final  String DEFINE =  "########";

    public MessageSendEngine(MethodCallHelper methodCallHelper) {
        this.methodCallHelper = methodCallHelper;
    }


    /***
     * set status
     * @param userId
     */
    public void setStatus(String userId ,String rid,String status,String nType,String time,String receiveUserId,String mediaId){
        methodCallHelper.changeVideoOrAudioMsgStatusForMobile(userId,rid,status,nType,time,receiveUserId,mediaId);
    }

    /**
     * user when test
     * @param params
     */

    /***
     *
     * @param reCallId
     */
    public void sendVideo(String reCallId ,
                          VideoSendMsgModel.MediaBean mediaBean ,
                          String roomId ,
                          VideoSendMsgModel.CalluserBean calluserBean,
                          String from ,String warnmsg ,String calltype,int av_type ){
        methodCallHelper.sendVideo(reCallId+"/webrtc",
                mediaBean,roomId,calluserBean,from,warnmsg,calltype,av_type);
    }


}
