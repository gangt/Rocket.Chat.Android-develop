package chat.rocket.persistence.realm.models.ddp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.log.RCLog;
import chat.rocket.core.JsonConstants;
import chat.rocket.core.models.Attendance;
import chat.rocket.core.models.Room;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Chat Room(Subscription).
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
        "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class RealmRoom extends RealmObject {

    public static final String ID = "_id";
    public static final String ROOM_ID = "rid";
    public static final String NAME = "name";
    public static final String TYPE = "t";
    public static final String OPEN = "isMeetingOpen";
    public static final String PAUSE = "isPause";
    public static final String ALERT = "alert";
    public static final String UNREAD = "unread";
    public static final String UPDATED_AT = "_updatedAt";
    public static final String LAST_SEEN = "ls";
    public static final String FAVORITE = "f";
    public static final String CUSTOMFIELDS = "customFields";
    public static final String ORG = "org";
    public static final String ORGNAME = "orgName";
    public static final String GROUP = "group";
    public static final String GID = "gId";
    public static final String LEVEL = "level";
    public static final String GNAME = "gName";
    public static final String U = "u";
    public static final String UID = "uId";
    public static final String USERNAME = "username";
    public static final String UUSERNAME = "uUserName";

    public static final String TYPE_CHANNEL = "c";
    public static final String TYPE_PRIVATE = "p";
    public static final String TYPE_DIRECT_MESSAGE = "d";
    public static final String TYPE_ATTENDANCE = "attendance";
    public static final String TYPE_ISATTENDANCE = "isAttendance";
    public static final String HOST = "host";
    public static final String RECORD = "record";// 从该字段解析出 会议总结rawContent
    public static final String RAWCONTENT = "rawContent";
    public static final String MEETINGTIME = "meetingtime";// 从该字段解析出 会议开始和结束时间starttime endtime
    public static final String STARTTIME = "starttime";
    public static final String ENDTIME = "endtime";
    public static final String COMPANY_ID = "companyId";
    public static final String USERNAMES = "usernames";
    public static final String ENCRYPT = "encrypt";
    public static final String MUTED = "muted";
    public static final String DISPLAYNAME = "displayName";


    @PrimaryKey
    private String _id;

    private String rid; //roomId
    private String name;
    //private RealmUser u; // REMARK: do not save u, because it is just me.
    private String t; //type { c: channel, d: direct message, p: private }
    private boolean isMeetingOpen;
    private String encrypt;
    private boolean isPause;
    private boolean alert;
    private int unread;
    private long _updatedAt;
    private long ls; //last seen.
    private boolean f;
    private String cid;
    private String companyId;
    private String companyName;
    private String displayName;
    private String gId;
    private String level;
    private String gName;
    private String meetingSubject;
    private String orgName;
    private boolean ro;
    private String s;
    private String uId;
    private String uUserName;
    private RealmList<String> usernames;
    private RealmList<String> muted;
    private String isAttendance;
    private String host;
    private RealmList<RealmAttendance> attendance;
    private String rawContent;
    private long starttime;
    private long endtime;
    private String topic;
    private String description;

    public static JSONObject customizeJson(JSONObject roomSubscriptionJson) throws JSONException {
        if (!roomSubscriptionJson.isNull(LAST_SEEN)) {
            try {
                long  ls = roomSubscriptionJson.getJSONObject(LAST_SEEN).getLong(JsonConstants.DATE);
                roomSubscriptionJson.remove(LAST_SEEN);
                roomSubscriptionJson.put(LAST_SEEN, ls);
            } catch (JSONException e) {
            }
        }

        if (!roomSubscriptionJson.isNull(UPDATED_AT)) {
            try {
                long  updatedAt = roomSubscriptionJson.getJSONObject(UPDATED_AT).getLong(JsonConstants.DATE);
                roomSubscriptionJson.remove(UPDATED_AT);
                roomSubscriptionJson.put(UPDATED_AT, updatedAt);
            } catch (JSONException e) {
            }

        }
        if (!roomSubscriptionJson.isNull(GROUP)) {
            String gId = roomSubscriptionJson.getJSONObject(GROUP).getString(GID);
            String level = roomSubscriptionJson.getJSONObject(GROUP).getString(LEVEL);
            String gName = roomSubscriptionJson.getJSONObject(GROUP).getString(NAME);
            roomSubscriptionJson.remove(GROUP);
            roomSubscriptionJson.put(GID, gId);
            roomSubscriptionJson.put(LEVEL, level);
            roomSubscriptionJson.put(GNAME, gName);
        }
        if (!roomSubscriptionJson.isNull(U)) {
            String uId = roomSubscriptionJson.getJSONObject(U).getString(ID);
            String userName = roomSubscriptionJson.getJSONObject(U).getString(USERNAME);
            roomSubscriptionJson.remove(U);
            roomSubscriptionJson.put(UID, uId);
            roomSubscriptionJson.put(UUSERNAME, userName);
        }

        if (!roomSubscriptionJson.isNull(HOST)) {
            try {
                Object o = roomSubscriptionJson.get(HOST);
                if(o instanceof JSONArray){
                    JSONArray roleStrings = roomSubscriptionJson.getJSONArray(HOST);
                    if(roleStrings.length() > 0){
                        roomSubscriptionJson.put(HOST, roleStrings.get(0));
                    }
                }else if(o instanceof String){
                    String host = roomSubscriptionJson.getString(HOST);
                    roomSubscriptionJson.put(HOST, host);
                }
            } catch (Exception e) {
            }
        }
//        if(!roomSubscriptionJson.isNull(ENCRYPT)){
//            try {
//                Object o = roomSubscriptionJson.get(ENCRYPT);
//                if(o instanceof Integer){
//                    int encrypt = roomSubscriptionJson.getInt(ENCRYPT);
//                    if(encrypt == 1){
//                        roomSubscriptionJson.put(ENCRYPT, "true");
//                    }else {
//                        roomSubscriptionJson.put(ENCRYPT, "false");
//                    }
//                }else if(o instanceof Boolean){
//                    boolean encrypt = roomSubscriptionJson.getBoolean(ENCRYPT);
//                    roomSubscriptionJson.put(ENCRYPT, encrypt?"true":"false");
//                }else
//                    roomSubscriptionJson.put(ENCRYPT, roomSubscriptionJson.getString(ENCRYPT));
//            } catch (JSONException e) {
//            }
//        }

        if (!roomSubscriptionJson.isNull(TYPE_ATTENDANCE)) {
            try{
                JSONArray roleStrings = roomSubscriptionJson.getJSONArray(TYPE_ATTENDANCE);
                JSONArray atts = new JSONArray();
                for (int i = 0, size = roleStrings.length(); i < size; i++) {
                    atts.put(RealmAttendance.customizeJson(roleStrings.getString(i)));
                }

                roomSubscriptionJson.remove(TYPE_ATTENDANCE);
                roomSubscriptionJson.put(TYPE_ATTENDANCE, atts);

            }catch (Exception e){
            }
        }

        try{
            if (!roomSubscriptionJson.isNull(RECORD)) {
                JSONObject jsonObject = roomSubscriptionJson.getJSONObject(RECORD);
                if (!jsonObject.isNull(RAWCONTENT)) {
                    roomSubscriptionJson.put(RAWCONTENT, jsonObject.get(RAWCONTENT));
                }
                roomSubscriptionJson.remove(RECORD);
            }
        }catch (Exception e){
        }
        if (!roomSubscriptionJson.isNull(MEETINGTIME)) {
            JSONObject jsonObject = roomSubscriptionJson.getJSONObject(MEETINGTIME);

            roomSubscriptionJson.remove(MEETINGTIME);
            roomSubscriptionJson.put(STARTTIME, jsonObject.getJSONObject(STARTTIME).getLong(JsonConstants.DATE));
            roomSubscriptionJson.put(ENDTIME, jsonObject.getJSONObject(ENDTIME).getLong(JsonConstants.DATE));
        }
        if(!roomSubscriptionJson.isNull(USERNAMES)){
            JSONArray jsonArray = roomSubscriptionJson.getJSONArray(USERNAMES);
            if(jsonArray.length()==0){
                roomSubscriptionJson.remove(USERNAMES);
            }
        }
        if(!roomSubscriptionJson.has(MUTED)){
            roomSubscriptionJson.put(MUTED, new JSONArray());
        }
        return roomSubscriptionJson;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getRoomId() {
        return rid;
    }

    public void setRoomId(String rid) {
        this.rid = rid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return t;
    }

    public void setType(String t) {
        this.t = t;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    public boolean isMeetingOpen() {
        return isMeetingOpen;
    }

    public void setMeetingOpen(boolean meetingOpen) {
        isMeetingOpen = meetingOpen;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public String isAttendance() {
        return isAttendance;
    }

    public void setAttendance(String attendance) {
        isAttendance = attendance;
    }
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public RealmList<RealmAttendance> getAttendanceList() {
        return attendance;
    }

    public void setAttendanceList(RealmList<RealmAttendance> attendance) {
        this.attendance = attendance;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public long getUpdatedAt() {
        return _updatedAt;
    }

    public void setUpdatedAt(long _updatedAt) {
        this._updatedAt = _updatedAt;
    }

    public long getLastSeen() {
        return ls;
    }

    public void setLastSeen(long ls) {
        this.ls = ls;
    }

    public boolean isFavorite() {
        return f;
    }

    public void setFavorite(boolean f) {
        this.f = f;
    }

    public static String getID() {
        return ID;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
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

    public boolean isF() {
        return f;
    }

    public void setF(boolean f) {
        this.f = f;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
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

    public String getgName() {
        return gName;
    }

    public void setgName(String gName) {
        this.gName = gName;
    }

    public String getMeetingSubject() {
        return meetingSubject;
    }

    public void setMeetingSubject(String meetingSubject) {
        this.meetingSubject = meetingSubject;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public boolean isRo() {
        return ro;
    }

    public void setRo(boolean ro) {
        this.ro = ro;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuUserName() {
        return uUserName;
    }

    public void setuUserName(String uUserName) {
        this.uUserName = uUserName;
    }

    public RealmList<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(RealmList<String> usernames) {
        this.usernames = usernames;
    }

    public RealmList<String> getMuted() {
        return muted;
    }

    public void setMuted(RealmList<String> muted) {
        this.muted = muted;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public long getEndtime() {
        return endtime;
    }

    public void setEndtime(long endtime) {
        this.endtime = endtime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Room asRoom() {
        final int total = usernames != null ? usernames.size() : 0;
        final List<String> userNames = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            userNames.add(usernames.get(i));
        }

        final int mutedT = muted != null ? muted.size() : 0;
        final List<String> mutes = new ArrayList<>(mutedT);
        for (int i = 0; i < mutedT; i++) {
            mutes.add(muted.get(i));
        }

        int size = attendance != null ? attendance.size() : 0;
        List<Attendance> attendanceList = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            attendanceList.add(attendance.get(i).asAttendance());
        }

        return Room.builder()
                .setId(_id)
                .setRoomId(rid)
                .setCompanyName(companyName)
                .setDisplayName(displayName)
                .setGId(gId).setLevel(level)
                .setGName(gName)
                .setCompanyId(companyId)
                .setMeetingSubject(meetingSubject)
                .setOrgName(orgName)
                .setRo(ro)
                .setS(s)
                .setUId(uId)
                .setUUserName(uUserName)
                .setName(name == null ? "" : name)
                .setType(t)
                .setOpen(isMeetingOpen)
                .setEncrypt(encrypt)
                .setPause(isPause)
                .setAlert(alert)
                .setUnread(unread)
                .setUpdatedAt(_updatedAt)
                .setLastSeen(ls)
                .setFavorite(f)
                .setCid(cid)
                .setUsernames(userNames)
                .setMuted(mutes)
                .setHost(host)
                .setAttendance(isAttendance)
                .setAttendanceList(attendanceList)
                .setRawContent(rawContent)
                .setTopic(topic)
                .setDescription(description)
                .setStartTime(starttime)
                .setEndTime(endtime)
                .build();
    }
}
