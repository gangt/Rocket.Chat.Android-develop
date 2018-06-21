package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import io.reactivex.annotations.Nullable;

/**
 * Created by helloworld on 2018/4/10
 */
@AutoValue
public abstract class OrgCompany {

    @Nullable
    public abstract String getCode();

    @Nullable
    public abstract String getCompany();

    @Nullable
    public abstract String getCompanyId();


    @Nullable
    public abstract String getCreatetime();

    @Nullable
    public abstract String getCreatorId();

    @Nullable
    public abstract String getDemId();

    @Nullable
    public abstract String getDepth();

    @Nullable
    public abstract String getFromType();


    @Nullable
    public abstract String getIconPath();

    @Nullable
    public abstract String getIsDelete();

    @Nullable
    public abstract String getIsParent();

    @Nullable
    public abstract String getIsRoot();


    @Nullable
    public abstract String getOrgCode();

    @Nullable
    public abstract String getOrgDesc();

    public abstract String getOrgId();

    @Nullable
    public abstract String getOrgName();

    @Nullable
    public abstract String getOrgPathname();

    @Nullable
    public abstract String getOrgStaff();


    @Nullable
    public abstract String getOrgSupId();

    @Nullable
    public abstract String getOpen();

    @Nullable
    public abstract String getOrgType();

    @Nullable
    public abstract String getPath();

    @Nullable
    public abstract String getSn();

    @Nullable
    public abstract String getTopOrgId();

    @Nullable
    public abstract String getUpdatetime();

    @Nullable
    public abstract String getUpdateId();

    @Nullable
    public abstract String getOnlineNum();


    public static Builder builder() {
        return new AutoValue_OrgCompany.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setOpen(String open);

        public abstract Builder setOrgCode(String orgCode);

        public abstract Builder setOrgName(String orgName);

        public abstract Builder setOrgDesc(String orgDesc);

        public abstract Builder setTopOrgId(String topOrgId);

        public abstract Builder setSn(String sn);

        public abstract Builder setPath(String path);

        public abstract Builder setOrgType(String orgType);

        public abstract Builder setOrgSupId(String orgSupId);

        public abstract Builder setUpdateId(String updateId);

        public abstract Builder setOrgPathname(String orgPathname);

        public abstract Builder setUpdatetime(String updatetime);

        public abstract Builder setOrgId(String orgId);

        public abstract Builder setCompanyId(String companyId);

        public abstract Builder setCode(String code);

        public abstract Builder setCreatorId(String creatorId);

        public abstract Builder setDepth(String depth);

        public abstract Builder setFromType(String fromType);

        public abstract Builder setIsDelete(String isDelete);

        public abstract Builder setIsParent(String isParent);

        public abstract Builder setIconPath(String iconPath);


        public abstract Builder setDemId(String demId);

        public abstract Builder setCreatetime(String createtime);


        public abstract Builder setCompany(String company);

        public abstract Builder setIsRoot(String isRoot);

        public abstract Builder setOrgStaff(String orgStaff);
        public abstract Builder setOnlineNum(String onlineNum);

        public abstract OrgCompany build();
    }
}
