package chat.rocket.android.login;

import java.util.List;

public class UserInfo {

    private String _id;
    private Object createdAt;
    private String         type;
    private String status;
    private boolean         active;
    private String name;
    private Object         _updatedAt;
    private List<String> roles;
    private String         realName;
    private boolean isDeleted;
    private String         username;
    private String avatar;
    private boolean         PushStatus;
    private String statusDefault;
    private String         sixCount;
    private String userId;
    private String mobile;
    private String         phone;
    private String         companyId;
    private String companyName;
    private String         companyType;
    private String companyCode;
    private Object         allRoles;
    private int utcOffset;
    private Object         lastLogin;

    private List<Emails> emails;
    private List<DeptRole> deptRole;
    private List<Companies> companies;

    public class Emails{
        private String address;//	String	邮箱地址
        private String verified;//	Boolean	是否已验证

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getVerified() {
            return verified;
        }

        public void setVerified(String verified) {
            this.verified = verified;
        }
    }
    public class Companies{
        private String companyId;
        private String         companyName;
        private String companyType;
        private String         companyCode;

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

        public String getCompanyType() {
            return companyType;
        }

        public void setCompanyType(String companyType) {
            this.companyType = companyType;
        }

        public String getCompanyCode() {
            return companyCode;
        }

        public void setCompanyCode(String companyCode) {
            this.companyCode = companyCode;
        }
    }
    public class DeptRole{
        private String org_path_name;//	String	所在部门
        private String pos_desc	    ;//  String	职位描述
        private String code	        ;//  String	部门编码
        private String pos_name     ;//	String	任职名称
        private String pos_code     ;// 	String	职位编码
        private String org_desc     ;//	String	部门描述
        private String org_name     ;// 	String	部门名称
        private String org_code    	;//String	部门编码

        public String getOrg_path_name() {
            return org_path_name;
        }

        public void setOrg_path_name(String org_path_name) {
            this.org_path_name = org_path_name;
        }

        public String getPos_desc() {
            return pos_desc;
        }

        public void setPos_desc(String pos_desc) {
            this.pos_desc = pos_desc;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getPos_name() {
            return pos_name;
        }

        public void setPos_name(String pos_name) {
            this.pos_name = pos_name;
        }

        public String getPos_code() {
            return pos_code;
        }

        public void setPos_code(String pos_code) {
            this.pos_code = pos_code;
        }

        public String getOrg_desc() {
            return org_desc;
        }

        public void setOrg_desc(String org_desc) {
            this.org_desc = org_desc;
        }

        public String getOrg_name() {
            return org_name;
        }

        public void setOrg_name(String org_name) {
            this.org_name = org_name;
        }

        public String getOrg_code() {
            return org_code;
        }

        public void setOrg_code(String org_code) {
            this.org_code = org_code;
        }
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatusDefault() {
        return statusDefault;
    }

    public void setStatusDefault(String statusDefault) {
        this.statusDefault = statusDefault;
    }

    public String getSixCount() {
        return sixCount;
    }

    public void setSixCount(String sixCount) {
        this.sixCount = sixCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public Object getAllRoles() {
        return allRoles;
    }

    public void setAllRoles(Object allRoles) {
        this.allRoles = allRoles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isPushStatus() {
        return PushStatus;
    }

    public void setPushStatus(boolean pushStatus) {
        PushStatus = pushStatus;
    }

    public int getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(int utcOffset) {
        this.utcOffset = utcOffset;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Object createdAt) {
        this.createdAt = createdAt;
    }

    public Object get_updatedAt() {
        return _updatedAt;
    }

    public void set_updatedAt(Object _updatedAt) {
        this._updatedAt = _updatedAt;
    }

    public Object getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Object lastLogin) {
        this.lastLogin = lastLogin;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Emails> getEmails() {
        return emails;
    }

    public void setEmails(List<Emails> emails) {
        this.emails = emails;
    }

    public List<DeptRole> getDeptRole() {
        return deptRole;
    }

    public void setDeptRole(List<DeptRole> deptRole) {
        this.deptRole = deptRole;
    }

    public List<Companies> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Companies> companies) {
        this.companies = companies;
    }
}
