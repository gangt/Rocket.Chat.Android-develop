package chat.rocket.android.recyclertreeview.selectuserbinder;

import chat.rocket.android.R;
import chat.rocket.core.models.OrgCompany;
import tellh.com.recyclertreeview_lib.LayoutItemType;

/**
 * Created by helloworld on 2018/4/8
 */

public class DeptItemType implements LayoutItemType {

    private OrgCompany dept;

    public DeptItemType(OrgCompany dept) {
        this.dept = dept;
    }

    public OrgCompany getDept() {
        return dept;
    }

    @Override
    public int getLayoutId() {
        return R.layout.org_dept_group;
    }
}
