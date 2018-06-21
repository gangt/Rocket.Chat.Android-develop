package chat.rocket.persistence.realm.models.ddp;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.core.JsonConstants;
import chat.rocket.core.models.Auth;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user on 2018/1/24.
 */

public class RealmAuth extends RealmObject {
    public static final String TOKENEXPIRES="tokenExpires";
    @PrimaryKey
    private String id;
    private String token;
    private long tokenExpires;


    public static JSONObject customizeJson(JSONObject jsonObject) throws JSONException {
        if(!jsonObject.isNull(TOKENEXPIRES)){
            long tokenExpires=jsonObject.getJSONObject(TOKENEXPIRES).getLong(JsonConstants.DATE);
            jsonObject.remove(TOKENEXPIRES);
            jsonObject.put(TOKENEXPIRES,tokenExpires);
        }
            return jsonObject;
    }

    public Auth asAuth(){
        return Auth.builder()
                .setId(id)
                .setToken(token)
                .setTokenExpires(tokenExpires)
                .builder();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTokenExpires() {
        return tokenExpires;
    }

    public void setTokenExpires(long tokenExpires) {
        this.tokenExpires = tokenExpires;
    }
}
