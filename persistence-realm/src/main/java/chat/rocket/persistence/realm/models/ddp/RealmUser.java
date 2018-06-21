package chat.rocket.persistence.realm.models.ddp;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.models.Auth;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.Email;
import chat.rocket.core.models.User;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;

/**
 * RealmUser.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
        "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class RealmUser extends RealmObject {

    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String USERNAME = "username";
    public static final String STATUS = "status";
    public static final String UTC_OFFSET = "utcOffset";
    public static final String EMAILS = "emails";
    public static final String SETTINGS = "settings";

    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_BUSY = "busy";
    public static final String STATUS_AWAY = "away";
    public static final String STATUS_OFFLINE = "offline";
    public static final String CREATEDAT="createdAt";
    public static final String UPDATEDAT="_updatedAt";

    @PrimaryKey
    private String _id;
    private String name;
    private String username;
    private String status;
    private double utcOffset;
    private RealmList<RealmEmail> emails;
    private RealmSettings settings;
    private RealmList<String> roles;
    private String PushStatus;
    private String active;
    private String avatar;
    private String companyId;
    private String companyName;
    private String companyCode;
    private String companyType;
    private String mobile;
    private String phone;
    private String sixCount;
    private String statusConnection;
    private String statusDefault;
    private String userId;
    private String realName;

    private long _updatedAt;
    private long createdAt;
    private String isMaster;
    private RealmList<RealmDeptRole> deptRole;
    private String job_name;
    private String org_name;
    private String pos_name;
    private String org_code;

    public String getPushStatus() {
        return PushStatus;
    }

    public void setPushStatus(String pushStatus) {
        PushStatus = pushStatus;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    //
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

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
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

    public String getSixCount() {
        return sixCount;
    }

    public void setSixCount(String sixCount) {
        this.sixCount = sixCount;
    }

    public String getStatusConnection() {
        return statusConnection;
    }

    public void setStatusConnection(String statusConnection) {
        this.statusConnection = statusConnection;
    }

    public String getStatusDefault() {
        return statusDefault;
    }

    public void setStatusDefault(String statusDefault) {
        this.statusDefault = statusDefault;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public RealmList<String> getRoles() {
        return roles;
    }

    public void setRoles(RealmList<String> roles) {
        this.roles = roles;
    }

    public static RealmQuery<RealmUser> queryCurrentUser(Realm realm) {
        RealmAuth realmAuth = realm.where(RealmAuth.class).findFirst();
        if(realmAuth==null){
            return realm.where(RealmUser.class)
                   .isNotEmpty(EMAILS);
        }
        Auth auth = realmAuth.asAuth();
        return realm.where(RealmUser.class).
                equalTo(RealmUser.ID,auth.getId())
                .and().isNotEmpty(EMAILS);
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(double utcOffset) {
        this.utcOffset = utcOffset;
    }

    public RealmList<RealmEmail> getEmails() {
        return emails;
    }

    public void setEmails(RealmList<RealmEmail> emails) {
        this.emails = emails;
    }

    public RealmSettings getSettings() {
        return settings;
    }

    public long get_updatedAt() {
        return _updatedAt;
    }

    public void set_updatedAt(long _updatedAt) {
        this._updatedAt = _updatedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public RealmList<RealmDeptRole> getDeptRole() {
        return deptRole;
    }

    public void setDeptRole(RealmList<RealmDeptRole> deptRole) {
        this.deptRole = deptRole;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getOrg_name() {
        return org_name;
    }

    public void setOrg_name(String org_name) {
        this.org_name = org_name;
    }

    public String getPos_name() {
        return pos_name;
    }

    public void setPos_name(String pos_name) {
        this.pos_name = pos_name;
    }

    public String getOrg_code() {
        return org_code;
    }

    public void setOrg_code(String org_code) {
        this.org_code = org_code;
    }

    public String isMaster() {
        return isMaster;
    }

    public void setMaster(String master) {
        isMaster = master;
    }

    public User asUser() {
        // convert email list
        final int total = emails != null ? emails.size() : 0;
        final int total2 = roles != null ? roles.size() : 0;
        final int total3 = deptRole != null ? deptRole.size() : 0;
        final List<Email> coreEmails = new ArrayList<>(total);
        final List<String> coreRoles = new ArrayList<>(total2);
        final List<DeptRole> coreDeptRoles = new ArrayList<>(total3);
        for (int i = 0; i < total; i++) {
            coreEmails.add(emails.get(i).asEmail());
        }
        for (int i = 0; i < total2; i++){
            coreRoles.add(roles.get(i));
        }
        for (int i = 0; i < total3; i++){
            coreDeptRoles.add(deptRole.get(i).asDeptRole());
        }

            return User.builder()
                    .setId(_id)
                    .setName(name)
                    .setUsername(username)
                    .setStatus(status)
                    .setUtcOffset(utcOffset)
                    .setEmails(coreEmails)
                    .setSettings(settings != null ? settings.asSettings() : null)
                    .setActive(active)
                    .setAvatar(avatar)
                    .setCompanyId(companyId)
                    .setCompanyType(companyType)
                    .setCompanyName(companyName)
                    .setCompanyCode(companyCode)
                    .setMobile(mobile)
                    .setPhone(phone)
                    .setSixCount(sixCount)
                    .setPushStatus(PushStatus)
                    .setRoles(coreRoles)
                    .setStatusConnection(statusConnection)
                    .setStatusDefault(statusDefault)
                    .setUserId(userId)
                    .setRealName(realName)
                    .setDeptRole(coreDeptRoles)
                    .setOrgCode(org_code)
                    .setOrgName(org_name)
                    .setPosName(pos_name)
                    .setJobName(job_name)
                    .set_updatedAt(_updatedAt)
                    .setCreatedAt(createdAt)
                    .setMaster(isMaster)
                    .build();
    }

    @Override
    public String toString() {
        return "RealmUser{" +
                "_id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", utcOffset=" + utcOffset +
                ", emails=" + emails +
                ", settings=" + settings +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RealmUser user = (RealmUser) o;

        if (Double.compare(user.utcOffset, utcOffset) != 0) {
            return false;
        }
        if (_id != null ? !_id.equals(user._id) : user._id != null) {
            return false;
        }
        if (name != null ? !name.equals(user.name) : user.name != null) {
            return false;
        }
        if (username != null ? !username.equals(user.username) : user.username != null) {
            return false;
        }
        if (status != null ? !status.equals(user.status) : user.status != null) {
            return false;
        }
        if (emails != null ? !emails.equals(user.emails) : user.emails != null) {
            return false;
        }
        return settings != null ? settings.equals(user.settings) : user.settings == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        temp = Double.doubleToLongBits(utcOffset);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (emails != null ? emails.hashCode() : 0);
        result = 31 * result + (settings != null ? settings.hashCode() : 0);
        return result;
    }
}
