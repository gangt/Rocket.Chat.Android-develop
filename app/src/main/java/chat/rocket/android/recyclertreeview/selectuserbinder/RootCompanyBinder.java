package chat.rocket.android.recyclertreeview.selectuserbinder;

import android.view.View;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.core.models.OrgCompany;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * Created by helloworld on 2018/4/4
 */

public class RootCompanyBinder extends TreeViewBinder<RootCompanyBinder.ViewHolder> {

//    private OrgSelectUserActivity orgSelectUserActivity;

    public RootCompanyBinder() {
    }

    @Override
    public RootCompanyBinder.ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void bindView(RootCompanyBinder.ViewHolder viewHolder, int position, TreeNode treeNode) {
        CompanyItemType companyItemType = (CompanyItemType)treeNode.getContent();
        OrgCompany company = companyItemType.getCompany();
        viewHolder.tvName.setText(company.getOrgName());
    }

    @Override
    public int getLayoutId() {
        return R.layout.org_rootgroup;
    }

    public static class ViewHolder extends TreeViewBinder.ViewHolder {
        private TextView tvName;

        public ViewHolder(View rootView) {
            super(rootView);
            tvName = rootView.findViewById(R.id.tv_name);
        }
    }
}
