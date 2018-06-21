package chat.rocket.persistence.realm.models.ddp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.JsonConstants;
import chat.rocket.core.models.Subscription;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user on 2018/1/17.
 */

public class RealmSubscription extends RealmObject {
    public interface Columns {
        String LABELID = "label_id";
        String LABELNAME = "label_name";
        String GID = "gId";
        String GNAME = "g_name";
        String TYPE = "t";
        String COMPANYID = "companyId";
        String OPEN = "open";
        String UID = "u_id";
        String DISPLAYNAME = "displayName";
        String NAME = "name";
    }

    public static final String LAST_SEEN = "ls";
    public static final String UPDATED_AT = "_updatedAt";
    public static final String SORT_TIME = "sort_time";
    public static final String TS = "ts";
    public static final String ID = "_id";
    public static final String USERNAME = "username";
    public static final String U_ID = "u_id";
    public static final String U_USERNAME = "u_username";
    public static final String USER = "u";
    public static final String GROUP = "group";
    public static final String GID = "gId";
    public static final String LEVEL = "level";
    public static final String G_NAME = "g_name";
    public static final String ROOM_ID = "rid";
    public static final String NAME = "name";
    public static final String TYPE = "t";
    public static final String TYPE_CHANNEL = "c";
    public static final String TYPE_PRIVATE = "p";
    public static final String TYPE_SETTING = "m";
    public static final String COMPANYID = "companyId";
    public static final String BLOCKER = "blocker";
    public static final String BLOCKED = "blocked";
    @PrimaryKey
    private String _id;
    private String alert;
    private String cId;
    private String companyId;
    private String companyName;
    private String displayName;
    private String encrypt;
    private String label_id;
    private String label_name;
    private String name;
    private String open;
    private String rid;
    private String s;
    private String subMeetingType;
    private String t;
    private String unread;
    private long _updatedAt;
    private long ls;
    private RealmList<String> roles;
    private long sort_time;
    private long ts;
    private String u_id;
    private String u_username;
    private String gId;
    private String level;
    private String g_name;
    private String blocker;
    private String blocked;
    private String mnd;


    public static JSONObject customizeJson(JSONObject roomSubscriptionJson) throws JSONException {
        if (!roomSubscriptionJson.isNull(LAST_SEEN)) {
            try {
                long ls = roomSubscriptionJson.getJSONObject(LAST_SEEN).getLong(JsonConstants.DATE);
                roomSubscriptionJson.remove(LAST_SEEN);
                roomSubscriptionJson.put(LAST_SEEN, ls);
            } catch (JSONException e) {
            }

        }
        if (!roomSubscriptionJson.isNull(UPDATED_AT)) {
            try {
                long updatedAt = roomSubscriptionJson.getJSONObject(UPDATED_AT).getLong(JsonConstants.DATE);
                roomSubscriptionJson.remove(UPDATED_AT);
                roomSubscriptionJson.put(UPDATED_AT, updatedAt);
            } catch (JSONException e) {
            }

        }
        if (!roomSubscriptionJson.isNull(SORT_TIME)) {
            try {
                long sortTime = roomSubscriptionJson.getJSONObject(SORT_TIME).getLong(JsonConstants.DATE);
                roomSubscriptionJson.remove(SORT_TIME);
                roomSubscriptionJson.put(SORT_TIME, sortTime);
            } catch (JSONException e) {
            }
        }
        if (!roomSubscriptionJson.isNull(TS)) {
            try {
                long ts = roomSubscriptionJson.getJSONObject(TS).getLong(JsonConstants.DATE);
                roomSubscriptionJson.remove(TS);
                roomSubscriptionJson.put(TS, ts);
            } catch (JSONException e) {
            }
        }
        if (!roomSubscriptionJson.isNull(USER)) {
            String id = roomSubscriptionJson.getJSONObject(USER).getString("_id");
            String username = roomSubscriptionJson.getJSONObject(USER).getString("username");
            roomSubscriptionJson.remove(USER);
            roomSubscriptionJson.put(U_ID, id);
            roomSubscriptionJson.put(U_USERNAME, username);
        }
        if (!roomSubscriptionJson.isNull(GROUP)) {
            String id = roomSubscriptionJson.getJSONObject(GROUP).getString("gId");
            String level = roomSubscriptionJson.getJSONObject(GROUP).getString("level");
            String name = roomSubscriptionJson.getJSONObject(GROUP).getString("name");
            roomSubscriptionJson.remove(GROUP);
            roomSubscriptionJson.put(GID, id);
            roomSubscriptionJson.put(LEVEL, level);
            roomSubscriptionJson.put(G_NAME, name);
        }
        if (roomSubscriptionJson.isNull(BLOCKER)) {
            roomSubscriptionJson.put(BLOCKER, "");
        }
        if (roomSubscriptionJson.isNull(BLOCKED)) {
            roomSubscriptionJson.put(BLOCKED, "");
        }
        return roomSubscriptionJson;
    }

    public Subscription asSubscription() {
        final int total2 = roles != null ? roles.size() : 0;
        final List<String> coreRoles = new ArrayList<>(total2);
        for (int i = 0; i < total2; i++) {
            coreRoles.add(roles.get(i));
        }
        return Subscription.builder()
                .setAlert(alert)
                .setCId(cId)
                .setCompanyId(companyId)
                .setCompanyName(companyName)
                .setDisplayName(displayName)
                .setEncrypt(encrypt)
                .setGId(gId)
                .setGName(g_name)
                .setId(_id)
                .setLabelId(label_id)
                .setLabelName(label_name)
                .setLevel(level)
                .setLs(ls)
                .setName(name)
                .setOpen(open)
                .setRid(rid)
                .setRoles(coreRoles)
                .setS(s)
                .setSortTime(sort_time)
                .setSubMeetingType(subMeetingType)
                .setT(t)
                .setTs(ts)
                .setUId(u_id)
                .setUnread(unread)
                .setUpdatedAt(_updatedAt)
                .setUUserName(u_username)
                .setBlocked(blocked)
                .setBlocker(blocker)
                .setMnd(mnd)
                .build();
    }

    public String getMnd() {
        return mnd;
    }

    public void setMnd(String mnd) {
        this.mnd = mnd;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    public String getLabel_id() {
        return label_id;
    }

    public void setLabel_id(String label_id) {
        this.label_id = label_id;
    }

    public String getLabel_name() {
        return label_name;
    }

    public void setLabel_name(String label_name) {
        this.label_name = label_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getBlocker() {
        return blocker;
    }

    public void setBlocker(String blocker) {
        this.blocker = blocker;
    }

    public String getBlocked() {
        return blocked;
    }

    public void setBlocked(String blocked) {
        this.blocked = blocked;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getSubMeetingType() {
        return subMeetingType;
    }

    public void setSubMeetingType(String subMeetingType) {
        this.subMeetingType = subMeetingType;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getUnread() {
        return unread;
    }

    public void setUnread(String unread) {
        this.unread = unread;
    }

    public long get_updatedAt() {
        return _updatedAt;
    }

    public void set_updatedAt(long _updatedAt) {
        this._updatedAt = _updatedAt;
    }

    public long getLs() {
        return ls;
    }

    public void setLs(long ls) {
        this.ls = ls;
    }

    public RealmList<String> getRoles() {
        return roles;
    }

    public void setRoles(RealmList<String> roles) {
        this.roles = roles;
    }

    public long getSort_time() {
        return sort_time;
    }

    public void setSort_time(long sort_time) {
        this.sort_time = sort_time;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public static String getLastSeen() {
        return LAST_SEEN;
    }

    public String getU_id() {
        return u_id;
    }

    public void setU_id(String u_id) {
        this.u_id = u_id;
    }

    public String getU_username() {
        return u_username;
    }

    public void setU_username(String u_username) {
        this.u_username = u_username;
    }

    public String getgId() {
        return gId;
    }

    public void setgId(String gId) {
        this.gId = gId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getG_name() {
        return g_name;
    }

    public void setG_name(String g_name) {
        this.g_name = g_name;
    }
}
