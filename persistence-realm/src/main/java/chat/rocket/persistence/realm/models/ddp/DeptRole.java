package chat.rocket.persistence.realm.models.ddp;

/**
 * Created by jumper_C on 2018/6/11.
 */

public class DeptRole {

    /**
     * org_code : WEINING
     * org_name : 总工办
     * org_id : 10000001740326
     * pos_id : 10000002700131
     * pos_name : 总工办_副主任
     * job_name : 副主任
     * job_id : 10000002700031
     */

    private String org_code;
    private String org_name;
    private long org_id;
    private long pos_id;
    private String pos_name;
    private String job_name;
    private long job_id;

    public String getOrg_code() {
        return org_code;
    }

    public void setOrg_code(String org_code) {
        this.org_code = org_code;
    }

    public String getOrg_name() {
        return org_name;
    }

    public void setOrg_name(String org_name) {
        this.org_name = org_name;
    }

    public long getOrg_id() {
        return org_id;
    }

    public void setOrg_id(long org_id) {
        this.org_id = org_id;
    }

    public long getPos_id() {
        return pos_id;
    }

    public void setPos_id(long pos_id) {
        this.pos_id = pos_id;
    }

    public String getPos_name() {
        return pos_name;
    }

    public void setPos_name(String pos_name) {
        this.pos_name = pos_name;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public long getJob_id() {
        return job_id;
    }

    public void setJob_id(long job_id) {
        this.job_id = job_id;
    }
}
