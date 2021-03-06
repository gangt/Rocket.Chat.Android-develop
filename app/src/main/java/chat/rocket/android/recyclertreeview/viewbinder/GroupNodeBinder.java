package chat.rocket.android.recyclertreeview.viewbinder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.recyclertreeview.bean.Group;
import chat.rocket.core.models.Labels;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * Created by user on 2018/1/10.
 */

public class GroupNodeBinder extends TreeViewBinder<GroupNodeBinder.ViewHolder> {
    @Override
    public int getLayoutId() {
        return R.layout.treeitem_group;
    }

    @Override
    public ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void bindView(ViewHolder holder, int position, TreeNode node) {
        Group content = (Group) node.getContent();
        Labels label = content.getLabel();
        int unRead = content.getUnRead();
        holder.tvName.setText(label.getName());
        holder.ivArrow.setRotation(node.isExpand() ? 90 : 0);
        if(unRead>0){
            holder.tvUnRead.setVisibility(View.VISIBLE);
            holder.tvUnRead.setText(unRead+"");
        }else if(unRead>99){
            holder.tvUnRead.setVisibility(View.VISIBLE);
            holder.tvUnRead.setText("99+");
        }else {
            holder.tvUnRead.setVisibility(View.GONE);
        }
    }

    public static class ViewHolder extends TreeViewBinder.ViewHolder {
        private TextView tvName;
        private ImageView ivArrow;
        private TextView tvUnRead;
        public ViewHolder(View rootView) {
            super(rootView);
            tvName = rootView.findViewById(R.id.tv_name);
            ivArrow = rootView.findViewById(R.id.fl_arrow);
            tvUnRead =rootView.findViewById(R.id.tv_unread);
        }

        public ImageView getArrow() {
            return ivArrow;
        }
    }
}
