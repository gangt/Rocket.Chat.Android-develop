package chat.rocket.android.recyclertreeview.selectuserbinder;

import chat.rocket.android.R;
import chat.rocket.core.models.OrgCompany;
import tellh.com.recyclertreeview_lib.LayoutItemType;

/**
 * Created by helloworld on 2018/4/8
 */

public class CompanyItemType implements LayoutItemType {

    private OrgCompany company;

    public CompanyItemType(OrgCompany company) {
        this.company = company;
    }

    @Override
    public int getLayoutId() {
        return R.layout.org_rootgroup;
    }

    public OrgCompany getCompany() {
        return company;
    }

}
