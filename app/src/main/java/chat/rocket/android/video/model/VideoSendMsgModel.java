package chat.rocket.android.video.model;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/5/17/017.
 */

public class VideoSendMsgModel {

    /**
     * av_type : 2
     * media : {"video":false,"audio":true}
     * room : E4u7zi6gego9jqkhQnq9XxMzoXuLdA8PQ8
     * calluser : {"username":"黎军&48600","_id":"E4u7zi6gego9jqkhQ"}
     * from : E4u7zi6gego9jqkhQ
     * warnmsg : 正在取消音频邀请
     * calltype : single
     */

    private int av_type;
    private MediaBean media;
    private String room;
    private CalluserBean calluser;
    private String from;
    private String warnmsg;
    private String calltype;

    public static VideoSendMsgModel objectFromData(String str, String key) {

        try {
            JSONObject jsonObject = new JSONObject(str);

            return new Gson().fromJson(jsonObject.getString(str), VideoSendMsgModel.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getAv_type() {
        return av_type;
    }

    public void setAv_type(int av_type) {
        this.av_type = av_type;
    }

    public MediaBean getMedia() {
        return media;
    }

    public void setMedia(MediaBean media) {
        this.media = media;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public CalluserBean getCalluser() {
        return calluser;
    }

    public void setCalluser(CalluserBean calluser) {
        this.calluser = calluser;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getWarnmsg() {
        return warnmsg;
    }

    public void setWarnmsg(String warnmsg) {
        this.warnmsg = warnmsg;
    }

    public String getCalltype() {
        return calltype;
    }

    public void setCalltype(String calltype) {
        this.calltype = calltype;
    }

    public static class MediaBean {
        /**
         * video : false
         * audio : true
         */

        private boolean video;
        private boolean audio;

        public MediaBean(boolean video, boolean audio) {
            this.video = video;
            this.audio = audio;
        }

        public static MediaBean objectFromData(String str, String key) {

            try {
                JSONObject jsonObject = new JSONObject(str);

                return new Gson().fromJson(jsonObject.getString(str), MediaBean.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        public boolean isVideo() {
            return video;
        }

        public void setVideo(boolean video) {
            this.video = video;
        }

        public boolean isAudio() {
            return audio;
        }

        public void setAudio(boolean audio) {
            this.audio = audio;
        }
    }

    public static class CalluserBean {
        /**
         * username : 黎军&48600
         * _id : E4u7zi6gego9jqkhQ
         */

        private String username;
        private String _id;

        public CalluserBean(String username, String _id) {
            this.username = username;
            this._id = _id;
        }

        public static CalluserBean objectFromData(String str, String key) {

            try {
                JSONObject jsonObject = new JSONObject(str);

                return new Gson().fromJson(jsonObject.getString(str), CalluserBean.class);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }
    }
}
