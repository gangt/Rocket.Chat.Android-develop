package chat.rocket.persistence.realm.models.ddp;

import chat.rocket.core.models.OrgCompany;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by helloworld on 2018/4/10
 */

public class RealmOrgCompany extends RealmObject {

    public static final String ORGTYPE = "orgType";
    public static final String ORGSUPID = "orgSupId";
    public static final String DEMID = "demId";

    @PrimaryKey
    private String orgId     ;//           ": 10000001740018,
    private String code      ;//   ": "weining_nnwntzjtyxzrgs",
    private String company   ;//   ": "",
    private String companyId ;//       ": 0,
    private String createtime;//       ": 1511234797000,
    private String creatorId ;//       ": 1,
    private String demId     ;//      ": 1,
    private String depth     ;//       ": 0,
    private String fromType  ;//       ": 0,
    private String iconPath  ;//       ": "",
    private String isDelete  ;//           ": 0,
    private String isParent  ;//           ": "true",
    private String isRoot   ;//           : 0,
    private String onlineNum ;//               ": 0,
    private String open      ;//       ": "true",
    private String orgCode   ;//           ": "WEINING",
    private String orgDesc   ;//           ": "南宁威宁投资集团有限责任公司",
    private String orgName   ;//           ": "南宁威宁投资集团有限责任公司",
    private String orgPathname;//              ": "/南宁威宁投资集团有限责任公司",
    private String orgStaff  ;//           ": 1000,
    private String orgSupId  ;//       ": 1,
    private String orgType   ;//           ": 1,
    private String path      ;//   ": "1.10000001740018.",
    private String sn        ;//                   ": 2,
    private String topOrgId  ;//               ": 0,
    private String updateId  ;//           ": 10000001740114,
    private String updatetime;//               ": 1521106964000

//    public static JSONArray customizeJson(JSONObject jsonObject) throws JSONException {
////        if(!jsonObject.isNull(TOKENEXPIRES)){
////            long tokenExpires=jsonObject.getJSONObject(TOKENEXPIRES).getLong(JsonConstants.DATE);
////            jsonObject.remove(TOKENEXPIRES);
////            jsonObject.put(TOKENEXPIRES,tokenExpires);
////        }
//        return  jsonObject.get("res");
//    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getDemId() {
        return demId;
    }

    public void setDemId(String demId) {
        this.demId = demId;
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }

    public String getFromType() {
        return fromType;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

    public String getIsParent() {
        return isParent;
    }

    public void setIsParent(String isParent) {
        this.isParent = isParent;
    }

    public String getIsRoot() {
        return isRoot;
    }

    public void setIsRoot(String isRoot) {
        this.isRoot = isRoot;
    }

    public String getOnlineNum() {
        return onlineNum;
    }

    public void setOnlineNum(String onlineNum) {
        this.onlineNum = onlineNum;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgDesc() {
        return orgDesc;
    }

    public void setOrgDesc(String orgDesc) {
        this.orgDesc = orgDesc;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgPathname() {
        return orgPathname;
    }

    public void setOrgPathname(String orgPathname) {
        this.orgPathname = orgPathname;
    }

    public String getOrgStaff() {
        return orgStaff;
    }

    public void setOrgStaff(String orgStaff) {
        this.orgStaff = orgStaff;
    }

    public String getOrgSupId() {
        return orgSupId;
    }

    public void setOrgSupId(String orgSupId) {
        this.orgSupId = orgSupId;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getTopOrgId() {
        return topOrgId;
    }

    public void setTopOrgId(String topOrgId) {
        this.topOrgId = topOrgId;
    }

    public String getUpdateId() {
        return updateId;
    }

    public void setUpdateId(String updateId) {
        this.updateId = updateId;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public OrgCompany asOrgCompany() {

        return OrgCompany.builder()
                .setOpen(open)
                .setOrgId(orgId)
                .setOrgName(orgName)
                .setOrgType(orgType)
                .setCode(code)
                .setCompany(company)
                .setCompanyId(companyId)
                .setCreatetime(createtime)
                .setCreatorId(creatorId)
                .setOrgCode(orgCode)
                .setOrgSupId(orgSupId)
                .setOrgDesc(orgDesc)
                .setPath(path)
                .setUpdatetime(updatetime)
                .setIsParent(isParent)
                .setUpdateId(updateId)
                .setDemId(demId)
                .build();
    }

}
