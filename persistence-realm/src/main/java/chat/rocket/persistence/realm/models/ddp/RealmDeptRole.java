package chat.rocket.persistence.realm.models.ddp;

import chat.rocket.core.models.DeptRole;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmDeptRole extends RealmObject {

  private String code	        ;//  String	部门编码

  private String org_path_name;//	String	所在部门
  private String pos_desc	    ;//  String	职位描述
  private String pos_name     ;//	String	任职名称
  private String pos_code     ;// 	String	职位编码
  private String org_desc     ;//	String	部门描述
  private String org_name     ;// 	String	部门名称
  private String org_code    	;//String	部门编码

  private String pos_id    	;
  private String org_id    	;

  public DeptRole asDeptRole() {
    return DeptRole.builder()
            .setCode(code)
            .setOrg_code(org_code)
            .setOrg_desc(org_desc)
            .setOrg_name(org_name)
            .setOrg_path_name(org_path_name)
            .setPos_code(pos_code)
            .setPos_desc(pos_desc)
            .setPos_name(pos_name)
            .setPos_id(pos_id)
            .setOrg_id(org_id)
            .build();
  }

  @SuppressWarnings({"PMD.ShortVariable"})
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RealmDeptRole email = (RealmDeptRole) o;

    if (org_code != email.org_code) {
      return false;
    }
    return code != null ? code.equals(email.code) : email.code == null;

  }

  @Override
  public int hashCode() {
    int result = code != null ? code.hashCode() : 0;
    result = 31 * result;
    return result;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

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

  public String getPos_id() {
    return pos_id;
  }

  public void setPos_id(String pos_id) {
    this.pos_id = pos_id;
  }

  public String getOrg_id() {
    return org_id;
  }

  public void setOrg_id(String org_id) {
    this.org_id = org_id;
  }
}
