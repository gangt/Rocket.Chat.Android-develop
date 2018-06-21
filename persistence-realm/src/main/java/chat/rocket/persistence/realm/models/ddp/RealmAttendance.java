package chat.rocket.persistence.realm.models.ddp;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.core.JsonConstants;
import chat.rocket.core.models.Attendance;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 签到人员列表
 */
public class RealmAttendance extends RealmObject {

    public interface Columns {
        String ID = "_id";
        String NAME = "username";
        String UPDATE = "update";
    }

    @PrimaryKey
    private String _id;
    private String username;
    private long update;

    public static JSONObject customizeJson(String roleString) throws JSONException {
        //{"username":"流芳&44009","_id":"hPDngiRa7hmw8eMXc","update":{"$date":1522144207000}}
        JSONObject roleObject = new JSONObject();
        JSONObject jsonObject = new JSONObject(roleString);

        roleObject.put(Columns.ID, jsonObject.get(Columns.ID));
        roleObject.put(Columns.NAME, jsonObject.get(Columns.NAME));

        if (!jsonObject.isNull(Columns.UPDATE)) {
            try {
                long updatedAt = jsonObject.getJSONObject(Columns.UPDATE).getLong(JsonConstants.DATE);
                roleObject.put(Columns.UPDATE, updatedAt);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return roleObject;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getUpdate() {
        return update;
    }

    public void setUpdate(long update) {
        this.update = update;
    }

    public Attendance asAttendance() {
        return Attendance.builder()
                .setId(_id)
                .setUserName(username)
                .setUpdate(update)
                .build();
    }
}
