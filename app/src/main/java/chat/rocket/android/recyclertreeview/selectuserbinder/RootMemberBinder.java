package chat.rocket.android.recyclertreeview.selectuserbinder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.activity.business.OrgSelectUserActivity;
import chat.rocket.core.models.OrgCompany;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * Created by helloworld on 2018/4/4
 */

public class RootMemberBinder extends TreeViewBinder<RootMemberBinder.ViewHolder> {

    private List<UserEntity> selectUserEntityList;
    private OrgSelectUserActivity activity;
//    private RealmUserRepository userRepository;
    private boolean isSingle=false;
    public RootMemberBinder(OrgSelectUserActivity activity, RealmUserRepository userRepository,boolean isSingle) {
        this.activity = activity;
        this.isSingle=isSingle;
//        this.userRepository = userRepository;
//        if(this.selectUserEntityList == null) {
//            this.selectUserEntityList = new ArrayList<>();
//        }
    }

    @Override
    public RootMemberBinder.ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void bindView(RootMemberBinder.ViewHolder viewHolder, int position, TreeNode treeNode) {
        MemberItemType itemType = (MemberItemType)treeNode.getContent();
        UserEntity member = itemType.getMember();
        viewHolder.tvName.setText(member.getRealName());
        viewHolder.tv_zhiwei.setText(member.getZhiWei());

        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar);
        Glide.with(RocketChatApplication.getInstance())
                .load(member.getAvatar())
                .apply(options)
                .into(viewHolder.iv_icon);

        selectUserEntityList = activity.getAllSelectUserEntityList();
        if (isSingle)
            viewHolder.cb_check.setVisibility(View.GONE);
        if(selectUserEntityList.contains(member)){
            viewHolder.cb_check.setChecked(true);
        }else{
            viewHolder.cb_check.setChecked(false);
        }

        List childList = treeNode.getParent().getChildList();
        if (isSingle)
        viewHolder.itemView.setOnClickListener(view -> {
            activity.addSelectUserEntityToList(member);
            activity.onClickOk();
        });
        viewHolder.ll_check.setOnClickListener(v -> {
            if (!isSingle) {
                selectUserEntityList = activity.getAllSelectUserEntityList();

                if (selectUserEntityList.contains(member)) {
                    activity.removeSelectUserEntityToList(member);
                    viewHolder.cb_check.setChecked(false);
                } else {
                    activity.addSelectUserEntityToList(member);
                    viewHolder.cb_check.setChecked(true);
                }

                // 全选之后设置部门勾选
                ArrayList<UserEntity> temp = new ArrayList<>();
                for (Object child : childList) {
                    TreeNode node = (TreeNode) child;
                    MemberItemType item = (MemberItemType) node.getContent();
                    UserEntity m = item.getMember();
                    temp.add(m);
                }
                //搜索所有的已经选择的成员列表，查询是否所有的成员是否都已经勾选，是的话，当前部门需要勾选
                List<OrgCompany> selectOrgDeptList = activity.getSelectOrgDeptList();
                DeptItemType item = (DeptItemType) treeNode.getParent().getContent();
                if (selectUserEntityList.containsAll(temp)) {
                    selectOrgDeptList.add(item.getDept());
                } else {
                    selectOrgDeptList.remove(item.getDept());
                }
                activity.setSelectDeptList(selectOrgDeptList);
            }else {
                activity.addSelectUserEntityToList(member);
                activity.onClickOk();
            }});
    }

    public List<UserEntity> getSelectUserEntityList() {
        return selectUserEntityList;
    }

//    public void setSelectUserEntityList(ArrayList<UserEntity> selectUserEntityList) {
//        this.selectUserEntityList = selectUserEntityList;
//    }

//    public void setMemberCheck(boolean isGouXuan, OrgCompany company) {
//        this.isGouXuan = isGouXuan;
//        this.orderCompany = company;
//    }

    @Override
    public int getLayoutId() {
        return R.layout.org_member_group;
    }

    public static class ViewHolder extends TreeViewBinder.ViewHolder {
        private TextView tv_zhiwei;
        private TextView tvName;
        private CheckBox cb_check;
        private ImageView iv_icon;
        private LinearLayout ll_check;

        public ViewHolder(View rootView) {
            super(rootView);
            tv_zhiwei = rootView.findViewById(R.id.tv_zhiwei);
            tvName = rootView.findViewById(R.id.tv_name);
            ll_check = rootView.findViewById(R.id.ll_check);
            cb_check = rootView.findViewById(R.id.cb_check);
            iv_icon = rootView.findViewById(R.id.iv_icon);
        }
    }
}
