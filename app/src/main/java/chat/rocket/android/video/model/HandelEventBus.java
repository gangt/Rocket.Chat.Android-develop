package chat.rocket.android.video.model;

/**
 * Created by Administrator on 2018/5/30/030.
 */

public class HandelEventBus {

    private int handleType ;
    private int uid ;

    public int getHandleType() {
        return handleType;
    }

    public int getUid() {
        return uid;
    }

    public HandelEventBus(int handleType, int uid) {
        this.handleType = handleType;
        this.uid = uid;
    }
}
