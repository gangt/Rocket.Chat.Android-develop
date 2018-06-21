//package chat.rocket.android.entity;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//import android.text.TextUtils;
//
//import indexablerv.IndexableEntity;
//
//public class UserEntity implements IndexableEntity,Parcelable{
//    private boolean isCheck;
//    private String pinyin;
//
//    private String _id;
//    private String name;
//    private String         realName;
//    private String         username;
//    private String avatar;
//    private String         companyId;
//    private String companyName;
//    private String dept;
//    private String zhiWei;
//    private String status;//是否在线
//    private long lastOnlineTime;
//
//    private boolean isOwner;
//    private boolean isManager;
//
//    public UserEntity(){
//
//    }
//
//    public UserEntity(boolean isCheck, String realName, String username) {
//        this.isCheck = isCheck;
//        this.realName = realName;
//        this.username = username;
//    }
//
//    protected UserEntity(Parcel in) {
//        isCheck = in.readByte() != 0;
//        pinyin = in.readString();
//        _id = in.readString();
//        name = in.readString();
//        realName = in.readString();
//        username = in.readString();
//        avatar = in.readString();
//        companyId = in.readString();
//        companyName = in.readString();
//        dept = in.readString();
//        zhiWei = in.readString();
//        status = in.readString();
//        lastOnlineTime = in.readLong();
//    }
//
//    public static final Creator<UserEntity> CREATOR = new Creator<UserEntity>() {
//        @Override
//        public UserEntity createFromParcel(Parcel in) {
//            return new UserEntity(in);
//        }
//
//        @Override
//        public UserEntity[] newArray(int size) {
//            return new UserEntity[size];
//        }
//    };
//
//    public boolean isCheck() {
//        return isCheck;
//    }
//
//    public void setCheck(boolean check) {
//        isCheck = check;
//    }
//
//    public String get_id() {
//        return _id;
//    }
//
//    public void set_id(String _id) {
//        this._id = _id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getRealName() {
//        if(TextUtils.isEmpty(realName)){
//            return name;
//        }
//        return realName;
//    }
//
//    public void setRealName(String realName) {
//        this.realName = realName;
//    }
//
//    public String getAvatar() {
//        return avatar;
//    }
//
//    public void setAvatar(String avatar) {
//        this.avatar = avatar;
//    }
//
//    public String getCompanyId() {
//        return companyId;
//    }
//
//    public void setCompanyId(String companyId) {
//        this.companyId = companyId;
//    }
//
//    public String getCompanyName() {
//        return companyName;
//    }
//
//    public void setCompanyName(String companyName) {
//        this.companyName = companyName;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getDept() {
//        return dept;
//    }
//
//    public void setDept(String dept) {
//        this.dept = dept;
//    }
//
//    public String getZhiWei() {
//        return zhiWei;
//    }
//
//    public void setZhiWei(String zhiWei) {
//        this.zhiWei = zhiWei;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public long getLastOnlineTime() {
//        return lastOnlineTime;
//    }
//
//    public void setLastOnlineTime(long lastOnlineTime) {
//        this.lastOnlineTime = lastOnlineTime;
//    }
//
//    public boolean isOwner() {
//        return isOwner;
//    }
//
//    public void setOwner(boolean owner) {
//        isOwner = owner;
//    }
//
//    public boolean isManager() {
//        return isManager;
//    }
//
//    public void setManager(boolean manager) {
//        isManager = manager;
//    }
//
//    public String getPinyin() {
//        return pinyin;
//    }
//
//    public void setPinyin(String pinyin) {
//        this.pinyin = pinyin;
//    }
//
//    @Override
//    public String getFieldIndexBy() {
//        return realName;
//    }
//
//    @Override
//    public void setFieldIndexBy(String indexField) {
//        this.realName = indexField;
//    }
//
//    @Override
//    public void setFieldPinyinIndexBy(String pinyin) {
//        this.pinyin = pinyin;
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//
//        dest.writeByte((byte) (isCheck ? 1 : 0));
//        dest.writeString(pinyin);
//        dest.writeString(_id);
//        dest.writeString(name);
//        dest.writeString(realName);
//        dest.writeString(username);
//        dest.writeString(avatar);
//        dest.writeString(companyId);
//        dest.writeString(companyName);
//        dest.writeString(dept);
//        dest.writeString(zhiWei);
//        dest.writeString(status);
//        dest.writeLong(lastOnlineTime);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        UserEntity entity = (UserEntity) o;
//
//        if (_id != null ? !_id.equals(entity._id) : entity._id != null) return false;
//        return username != null ? username.equals(entity.username) : entity.username == null;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = _id != null ? _id.hashCode() : 0;
//        result = 31 * result + (username != null ? username.hashCode() : 0);
//        return result;
//    }
//}
