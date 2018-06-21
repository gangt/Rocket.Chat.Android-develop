package chat.rocket.android.entity;

/**
 * Created by zhangxiugao on 2018/1/15
 */

public class LabelsForMobile {
    private String name;//	String	体系名称
    private String _id;//	String	体系id
    private Object _updatedAt;//	Date	修改时间
    private String level;//	Int	排序号
    private String type;//	String	频道类型	sd:关系空间 w:工作巢 p: 组织巢
    private String group;//	String	所属组	null:关系空间 work:工作巢 og:组织巢
    private Company company;//	Object	所属企业

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Object get_updatedAt() {
        return _updatedAt;
    }

    public void set_updatedAt(Object _updatedAt) {
        this._updatedAt = _updatedAt;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public class Company{
        private String companyId;
        private String         companyName;
        private String _id;

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

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }
    }
}
