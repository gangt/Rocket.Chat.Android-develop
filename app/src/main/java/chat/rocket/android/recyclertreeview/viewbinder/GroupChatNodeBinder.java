package chat.rocket.android.recyclertreeview.viewbinder;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.recyclertreeview.bean.GroupChat;
import chat.rocket.core.models.Subscription;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * Created by user on 2018/1/10.
 */

public class GroupChatNodeBinder extends TreeViewBinder<GroupChatNodeBinder.ViewHolder> {
    private OnClickListener onClickListener;

    @Override
    public int getLayoutId() {
        return R.layout.treeitem_group_chat;
    }

    public GroupChatNodeBinder(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @Override
    public void bindView(ViewHolder holder, int position, TreeNode node) {
        GroupChat content = (GroupChat) node.getContent();
        Subscription subscription = content.getSubscription();
        holder.ivArrow.setRotation(node.isExpand() ? 90 : 0);
        holder.tvName.setText(subscription.getDisplayName());
        holder.flArrow.setOnClickListener(view -> {
            onClickListener.onToggle(holder, position, node);
        });
        holder.flArrow.setVisibility(node.getChildList().size() == 0 ? View.INVISIBLE : View.VISIBLE);
//        holder.line.setVisibility(node.isRoot() ? View.VISIBLE : View.GONE);
        String unread = subscription.getUnread();
        if(Integer.parseInt(unread)>0){
            holder.tvUnRead.setVisibility(View.VISIBLE);
            holder.tvUnRead.setText(unread);
        }else if(Integer.parseInt(unread)>99){
            holder.tvUnRead.setVisibility(View.VISIBLE);
            holder.tvUnRead.setText("99+");
        }else {
            holder.tvUnRead.setVisibility(View.GONE);
        }
    }

    public interface OnClickListener {
        void onToggle(ViewHolder holder, int position, TreeNode node);
    }

    public static class ViewHolder extends TreeViewBinder.ViewHolder {
        private TextView tvName;
        private ImageView ivArrow;
//        private View line;
        private FrameLayout flArrow;
        private TextView tvUnRead;

        public ViewHolder(View rootView) {
            super(rootView);
            tvName = rootView.findViewById(R.id.tv_name);
            ivArrow = rootView.findViewById(R.id.iv_arrow);
//            line = rootView.findViewById(R.id.line);
            flArrow = rootView.findViewById(R.id.fl_arrow);
            tvUnRead=rootView.findViewById(R.id.tv_unread);
        }

        public ImageView getArrow() {
            return ivArrow;
        }
    }
}
