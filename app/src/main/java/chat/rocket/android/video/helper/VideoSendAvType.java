package chat.rocket.android.video.helper;

/**
 * Created by Administrator on 2018/5/17/017.
 */

public interface VideoSendAvType {
    /***
     * 0: //对方正在聊天中
     1: //邀请
     2: //取消
     3: //接听
     4: //拒绝
     5: //挂断
     6://切换语音
     */
    int TALKING = 7 ;
    int INVITE = 1 ;
    int CANCEL = 2 ;
    int ACCEPT = 3 ;
    int REFUSE = 4 ;
    int HANG_UP = 5 ;
    int CHANGE_AUDIO = 6 ;

}
