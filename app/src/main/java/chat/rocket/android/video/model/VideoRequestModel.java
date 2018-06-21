package chat.rocket.android.video.model;

/**
 * Created by Administrator on 2018/6/11/011.
 */


/****
 * _id, username, getAvar(), json.getString("room"), false, isvideo
 */
public class VideoRequestModel {

    public String _id ;
    public String username ;
    public String avar ;
    public String roomId ;
    public boolean isCall  ;
    public boolean  isVideo ;
    public String mediaId ;

    public VideoRequestModel(String _id, String username, String avar, String roomId, boolean isCall, boolean isVideo,String mediaId) {
        this._id = _id;
        this.username = username;
        this.avar = avar;
        this.roomId = roomId;
        this.isCall = isCall;
        this.isVideo = isVideo;
        this.mediaId = mediaId ;
    }


}
