package chat.rocket.persistence.realm.models.ddp;

import android.os.Parcel;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.log.RCLog;
import indexablerv.PinyinUtil;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class RealmUserEntity extends RealmObject {

    public static final String COMPANY_ID = "companyId";
    public static final String ROOM_TYPE = "type";
    public static final String PINYIN = "pinyin";
    public static final String REALNAME = "realName";
    public static final String USERNAME = "username";

    private String pinyin;
    private String type;

    @PrimaryKey
    private String _id;
    private String name;
    private String         realName;
    private String         username;
    private String avatar;
    private String         companyId;
    private String companyName;


    public static JSONObject customizeJson(JSONObject userEntityJson, String type) {
        try {
            if (!userEntityJson.isNull(REALNAME)) {
                userEntityJson.put(PINYIN, PinyinUtil.getPingYin(userEntityJson.getString(REALNAME)));
            }
            userEntityJson.put(ROOM_TYPE, type);
        } catch (JSONException e) {
            RCLog.e(e.toString());
        }
        return userEntityJson;
    }
    
    public RealmUserEntity(){
    }

    public RealmUserEntity(String realName, String username) {
        this.realName = realName;
        this.username = username;
    }

    protected RealmUserEntity(Parcel in) {
        pinyin = in.readString();
        _id = in.readString();
        name = in.readString();
        realName = in.readString();
        username = in.readString();
        avatar = in.readString();
        companyId = in.readString();
        companyName = in.readString();
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRealName() {
        if(TextUtils.isEmpty(realName)){
            return name;
        }
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserEntity asUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.set_id(_id);
        userEntity.setAvatar(avatar);
        userEntity.setCompanyId(companyId);
        userEntity.setName(name);
        userEntity.setUsername(username);
        userEntity.setPinyin(pinyin);
        userEntity.setCompanyName(companyName);
        userEntity.setRealName(realName);

        return userEntity;
    }
}