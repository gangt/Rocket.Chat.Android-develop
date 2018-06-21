package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class User {

    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_BUSY = "busy";
    public static final String STATUS_AWAY = "away";
    public static final String STATUS_OFFLINE = "offline";

    public abstract String getId();

    @Nullable
    public abstract String getName();

    @Nullable
    public abstract String getUsername();

    @Nullable
    public abstract String getStatus();

    public abstract double getUtcOffset();

    @Nullable
    public abstract List<Email> getEmails();

    @Nullable
    public abstract Settings getSettings();

    @Nullable
    public abstract List<String> getRoles();

    @Nullable
    public abstract String getPushStatus();

    @Nullable
    public abstract String getActive();

    @Nullable
    public abstract String getAvatar();

    @Nullable
    public abstract String getCompanyId();

    @Nullable
    public abstract String getCompanyName();

    @Nullable
    public abstract String getCompanyCode();

    @Nullable
    public abstract String getCompanyType();

    @Nullable
    public abstract String getMobile();

    @Nullable
    public abstract String getPhone();

    @Nullable
    public abstract String getSixCount();

    @Nullable
    public abstract String getStatusConnection();

    @Nullable
    public abstract String getStatusDefault();

    @Nullable
    public abstract String getUserId();

    @Nullable
    public abstract String getRealName();

    public abstract long getCreatedAt();
    public abstract long get_updatedAt();
    @Nullable
    public abstract String getMaster();
    @Nullable
    public abstract List<DeptRole> getDeptRole();

    @Nullable
    public abstract String getOrgCode();

    @Nullable
    public abstract String getOrgName();

    @Nullable
    public abstract String getPosName();

    @Nullable
    public abstract String getJobName();

    public static Builder builder() {
        return new AutoValue_User.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setId(String id);

        public abstract Builder setName(String name);

        public abstract Builder setUsername(String username);

        public abstract Builder setStatus(String status);

        public abstract Builder setUtcOffset(double utcOffset);

        public abstract Builder setEmails(List<Email> emails);

        public abstract Builder setSettings(Settings settings);

        public abstract Builder setRoles(List<String> roles);

        public abstract Builder setPushStatus(String pushStatus);

        public abstract Builder setActive(String active);

        public abstract Builder setAvatar(String avatar);

        public abstract Builder setCompanyId(String companyId);

        public abstract Builder setCompanyName(String companyName);

        public abstract Builder setCompanyCode(String companyName);

        public abstract Builder setCompanyType(String companyType);

        public abstract Builder setMobile(String mobile);

        public abstract Builder setPhone(String phone);

        public abstract Builder setSixCount(String sixCount);

        public abstract Builder setStatusConnection(String statusConnection);

        public abstract Builder setStatusDefault(String statusDefault);

        public abstract Builder setUserId(String userId);

        public abstract Builder setRealName(String realName);

        public abstract Builder setCreatedAt(long id);
        public abstract Builder set_updatedAt(long id);
        public abstract Builder setMaster(String master);
        public abstract Builder setDeptRole(List<DeptRole> id);
        public abstract Builder setOrgCode(String orgCode);
        public abstract Builder setOrgName(String orgName);
        public abstract Builder setPosName(String posName);
        public abstract Builder setJobName(String jobName);

        public abstract User build();
    }
}
