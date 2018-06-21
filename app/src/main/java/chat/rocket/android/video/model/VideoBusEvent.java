package chat.rocket.android.video.model;

import org.json.JSONObject;

/**
 * Created by Administrator on 2018/5/2/002.
 */

public class VideoBusEvent {

    private int type ;
    private JSONObject json;

   /* public VideoBusEvent(int type ,) {
        this.type = type;
    }

    public int getType() {
        return type;
    }*/

    public VideoBusEvent(int type, JSONObject json) {
        this.type = type;
        this.json = json;
    }

    public int getType() {
        return type;
    }

    public JSONObject getJson() {
        return json;
    }
}
