package chat.rocket.android.recyclertreeview.selectuserbinder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by helloworld on 2018/4/8
 */

public class OrgMember implements Parcelable {

    protected OrgMember(Parcel in) {
        account = in.readString();
        accountNonExpired = in.readString();
        accountNonLocked = in.readString();
        add = in.readString();
        aliasName = in.readString();
        attendNo = in.readString();
        credentialsNonExpired = in.readString();
        email = in.readString();
        enabled = in.readString();
        fromType = in.readString();
        fullname = in.readString();
        hasSyncToWx = in.readString();
        identification = in.readString();
        mobile = in.readString();
        phone = in.readString();
        roles = in.readString();
        sn = in.readString();
        staffNo = in.readString();
        syncToUc = in.readString();
        thisposName = in.readString();
        ucUserid = in.readString();
        userId = in.readString();
        userStatus = in.readString();
        username = in.readString();
    }

    public static final Creator<OrgMember> CREATOR = new Creator<OrgMember>() {
        @Override
        public OrgMember createFromParcel(Parcel in) {
            return new OrgMember(in);
        }

        @Override
        public OrgMember[] newArray(int size) {
            return new OrgMember[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(account);
        dest.writeString(accountNonExpired);
        dest.writeString(accountNonLocked);
        dest.writeString(add);
        dest.writeString(aliasName);
        dest.writeString(attendNo);
        dest.writeString(credentialsNonExpired);
        dest.writeString(email);
        dest.writeString(enabled);
        dest.writeString(fromType);
        dest.writeString(fullname);
        dest.writeString(hasSyncToWx);
        dest.writeString(identification);
        dest.writeString(mobile);
        dest.writeString(phone);
        dest.writeString(roles);
        dest.writeString(sn);
        dest.writeString(staffNo);
        dest.writeString(syncToUc);
        dest.writeString(thisposName);
        dest.writeString(ucUserid);
        dest.writeString(userId);
        dest.writeString(userStatus);
        dest.writeString(username);
    }

    private String account;//": "黎军",
    private String accountNonExpired;//": true,
    private String accountNonLocked;//": true,
    private String add;//": false,
    private String aliasName;//": "黎军",
    private String attendNo;//": 0,
    private String credentialsNonExpired;//": true,
    private String email;//": "13977135555@qq.com",
    private String enabled;//": false,
    private String fromType;//": 0,
    private String fullname;//": "黎军",
    private String hasSyncToWx;//": 0,
    private String identification;//": "0",
    private String mobile;//": "13977135555",
    private String phone;//": "",
    private String roles;//": "bpm_ptyg",
    private String sn;//": 0,
    private String staffNo;//": "0",
    private String syncToUc;//": 0,
    private String thisposName;//": "副总经理",
    private String ucUserid;//": 48600,
    private String userId;//": 48600,
    private String userStatus;//": "正式员工",
    private String username;//": "黎军&48600"

//    private List<UserPosition> userPositions;
//    private List<UserPosition> authorities;


    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(String accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public String getAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(String accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public String getAdd() {
        return add;
    }

    public void setAdd(String add) {
        this.add = add;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getAttendNo() {
        return attendNo;
    }

    public void setAttendNo(String attendNo) {
        this.attendNo = attendNo;
    }

    public String getCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(String credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getFromType() {
        return fromType;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getHasSyncToWx() {
        return hasSyncToWx;
    }

    public void setHasSyncToWx(String hasSyncToWx) {
        this.hasSyncToWx = hasSyncToWx;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getStaffNo() {
        return staffNo;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    public String getSyncToUc() {
        return syncToUc;
    }

    public void setSyncToUc(String syncToUc) {
        this.syncToUc = syncToUc;
    }

    public String getThisposName() {
        return thisposName;
    }

    public void setThisposName(String thisposName) {
        this.thisposName = thisposName;
    }

    public String getUcUserid() {
        return ucUserid;
    }

    public void setUcUserid(String ucUserid) {
        this.ucUserid = ucUserid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
