package chat.rocket.android.recyclertreeview.viewbinder;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hadisatrio.optional.Optional;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.recyclertreeview.bean.Conversation;
import chat.rocket.android.renderer.RocketChatUserStatusProvider;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * Created by user on 2018/1/10.
 */

public class ConversationNodeBinder extends TreeViewBinder<ConversationNodeBinder.ViewHolder> {
    private OnItemClickListener onItemClickListener;

    public ConversationNodeBinder(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getLayoutId() {
        return R.layout.treeitem_conversation;
    }

    @Override
    public ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void bindView(ViewHolder holder, int position, TreeNode node) {
        Conversation content = (Conversation) node.getContent();
        Subscription subscription = content.getSubscription();
        String name = subscription.getName();
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        RealmUserRepository userRepository = new RealmUserRepository(hostname);
        User userByUsername = userRepository.getUserByUsername(name);
        int userStatusDrawableId = R.drawable.userstatus_offline;
        if(userByUsername!=null)
            switch (userByUsername.getStatus()) {
                case User.STATUS_ONLINE:
                    userStatusDrawableId = R.drawable.userstatus_online;
                    break;
                case User.STATUS_AWAY:
                    userStatusDrawableId = R.drawable.userstatus_away;
                    break;
                case User.STATUS_BUSY:
                    userStatusDrawableId = R.drawable.userstatus_busy;
                    break;
            }
        holder.ivCurrentStatus.setImageResource(userStatusDrawableId);

        int i = name.indexOf("&");
        if (i != -1) {
            name = name.substring(0, i);
        }
        holder.tvName.setText(name);
        String unread = subscription.getUnread();
        if (Integer.parseInt(unread) > 0) {
            holder.tvUnRead.setVisibility(View.VISIBLE);
            holder.tvUnRead.setText(unread);
            holder.ivDetele.setVisibility(View.GONE);
        } else if (Integer.parseInt(unread) > 99) {
            holder.tvUnRead.setVisibility(View.VISIBLE);
            holder.tvUnRead.setText("99+");
            holder.ivDetele.setVisibility(View.GONE);
        } else {
            holder.tvUnRead.setVisibility(View.GONE);
            holder.ivDetele.setVisibility(View.VISIBLE);
        }
        holder.ivDetele.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.deteleConversation(holder, position, node);
            }
        });
    }

    public interface OnItemClickListener {
        void deteleConversation(ViewHolder holder, int position, TreeNode node);
    }

    public static class ViewHolder extends TreeViewBinder.ViewHolder {
        private TextView tvName;
        private ImageView ivCurrentStatus;
        private ImageView ivDetele;
        private TextView tvUnRead;

        public ViewHolder(View rootView) {
            super(rootView);
            tvName = rootView.findViewById(R.id.tv_name);
            ivCurrentStatus = rootView.findViewById(R.id.iv_current_user_status);
            ivDetele = rootView.findViewById(R.id.iv_delete);
            tvUnRead = rootView.findViewById(R.id.tv_unread);
        }
    }
}
