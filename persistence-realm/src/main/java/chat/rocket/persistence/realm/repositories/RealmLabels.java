package chat.rocket.persistence.realm.repositories;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.core.JsonConstants;
import chat.rocket.core.models.Labels;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user on 2018/1/17.
 */

public class RealmLabels extends RealmObject {
    public interface Columns {
        String TYPE = "type";
        String COMPANYID = "companyId";
    }

    public static final String UPDATED_AT = "_updatedAt";
    public static final String COMPANY = "company";
    @PrimaryKey
    private String _id;
    private String level;
    private String name;
    private String type;
    private long _updatedAt;
    private String companyId;
    private String companyName;

    public static JSONObject customizeJson(JSONObject roomSubscriptionJson) throws JSONException {
        if (!roomSubscriptionJson.isNull(UPDATED_AT)) {
            try {
                long  updatedAt = roomSubscriptionJson.getJSONObject(UPDATED_AT).getLong(JsonConstants.DATE);
                roomSubscriptionJson.remove(UPDATED_AT);
                roomSubscriptionJson.put(UPDATED_AT, updatedAt);
            } catch (JSONException e) {
            }

        }
        if (!roomSubscriptionJson.isNull(COMPANY)) {
            String companyId = roomSubscriptionJson.getJSONObject(COMPANY).getString("companyId");
            String companyName = roomSubscriptionJson.getJSONObject(COMPANY).getString("companyName");
            roomSubscriptionJson.remove(COMPANY);
            roomSubscriptionJson.put("companyId", companyId);
            roomSubscriptionJson.put("companyName", companyName);
        }
        return roomSubscriptionJson;
    }

    public Labels asLabels() {
        return Labels.builder()
                .setCompanyId(companyId)
                .setCompanyName(companyName)
                .setId(_id)
                .setLevel(level)
                .setName(name)
                .setType(type)
                .setUpdatedAt(_updatedAt)
                .build();
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long get_updatedAt() {
        return _updatedAt;
    }

    public void set_updatedAt(long _updatedAt) {
        this._updatedAt = _updatedAt;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
