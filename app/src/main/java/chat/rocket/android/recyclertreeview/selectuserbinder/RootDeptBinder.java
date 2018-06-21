package chat.rocket.android.recyclertreeview.selectuserbinder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.activity.business.OrgSelectUserActivity;
import chat.rocket.core.models.OrgCompany;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * Created by helloworld on 2018/4/4
 */

public class RootDeptBinder extends TreeViewBinder<RootDeptBinder.ViewHolder> {

    private OrgSelectUserActivity activity;
    private MyOnItemClickListener onItemClickListener;
    private List<OrgCompany> selectOrgCompanyList;
    private boolean isSingle;

    public RootDeptBinder(MyOnItemClickListener onItemClickListener,OrgSelectUserActivity orgSelectUserActivity,boolean isSingle) {
        this.onItemClickListener = onItemClickListener;
        this.activity = orgSelectUserActivity;
        this.isSingle=isSingle;
    }

    @Override
    public RootDeptBinder.ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void bindView(RootDeptBinder.ViewHolder viewHolder, int position, TreeNode treeNode) {
        DeptItemType itemType = (DeptItemType)treeNode.getContent();
        OrgCompany dept = itemType.getDept();
        viewHolder.tvName.setText(dept.getOrgName());

        int childSize = treeNode.getChildList().size();
        if(childSize == 0){
            viewHolder.tv_count.setVisibility(View.GONE);
        }else {
            viewHolder.tv_count.setVisibility(View.VISIBLE);
            String count = RocketChatApplication.getInstance().getResources().getString(R.string.count);
            viewHolder.tv_count.setText(String.format(count, childSize + ""));
        }
        viewHolder.ivIcon.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(viewHolder,position,treeNode,viewHolder.ivIcon);
            }
        });
        if(treeNode.isExpand()){
            viewHolder.ivIcon.setImageDrawable(activity.getResources().getDrawable(R.drawable.org_delete));
        }else{
            viewHolder.ivIcon.setImageDrawable(activity.getResources().getDrawable(R.drawable.org_add));
        }

//        if(selectOrgCompanyList != null && selectOrgCompanyList.size() > 0 && selectOrgCompanyList.contains(dept)) {
//            viewHolder.cb_check.setChecked(true);
//        }

        List childList = treeNode.getChildList();
        ArrayList<UserEntity> temp = new ArrayList<>();
        for(Object child : childList){
            TreeNode node = (TreeNode) child;
            MemberItemType item = (MemberItemType)node.getContent();
            UserEntity m = item.getMember();
            temp.add(m);
        }
        if (isSingle)
            viewHolder.cb_check.setVisibility(View.GONE);
        if(temp.size() > 0 && activity.getAllSelectUserEntityList().containsAll(temp)){
            viewHolder.cb_check.setChecked(true);
        }else {
            viewHolder.cb_check.setChecked(false);
        }

    }

    public void setSelectDeptList(List<OrgCompany> list){
        this.selectOrgCompanyList = list;
    }

    public List<OrgCompany> getSelectOrgCompanyList() {
        if(selectOrgCompanyList == null){
            selectOrgCompanyList = new ArrayList<>();
        }
        return selectOrgCompanyList;
    }

    @Override
    public int getLayoutId() {
        return R.layout.org_dept_group;
    }

    public static class ViewHolder extends TreeViewBinder.ViewHolder {
        private ImageView ivIcon;
        private TextView tvName,tv_count;
        private LinearLayout ll_check;
        private CheckBox cb_check;

        public ViewHolder(View rootView) {
            super(rootView);
            ivIcon = rootView.findViewById(R.id.iv_icon);
            tvName = rootView.findViewById(R.id.tv_name);
            ll_check = rootView.findViewById(R.id.ll_check);
            tvName = rootView.findViewById(R.id.tv_name);
            cb_check = rootView.findViewById(R.id.cb_check);
            tv_count = rootView.findViewById(R.id.tv_count);
        }
    }

    public interface MyOnItemClickListener {
        void onItemClickListener(ViewHolder holder, int position, TreeNode node, ImageView ivIcon);
    }
}
