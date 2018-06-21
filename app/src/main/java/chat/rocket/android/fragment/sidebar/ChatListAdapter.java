package chat.rocket.android.fragment.sidebar;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.DateTime;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;


/**
 * Created by lyq on 2018/5/12.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private List<Subscription> mData;
    private OnItemClickListener listener;
    public ChatListAdapter(OnItemClickListener listener){
        this.listener=listener;
    }
    interface OnItemClickListener{
       public void onItemClick(Subscription subscription);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(inflate);
    }

    public void setData(List<Subscription> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Subscription subscription = mData.get(position);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(subscription));
        String unread = subscription.getUnread();
        if(Integer.parseInt(unread)>0){
            holder.count.setVisibility(View.VISIBLE);
            holder.count.setText(unread);
            if(Integer.parseInt(unread)>99){
                holder.count.setText("99+");
            }
        }else {
            holder.count.setVisibility(View.GONE);
        }
       String sub_name = subscription.getName();
       String roomType = subscription.getT();
        if (!roomType.equals("d")) {
            holder.name.setText(subscription.getDisplayName());
        } else {
            String name = sub_name;
            int i = name.indexOf("&");
            if (i != -1) {
                name = name.substring(0, i);
            }
            holder.name.setText(name);
        }
        if(("w").equals(roomType)){
            holder.head.setImageResource(R.drawable.icon_w);
            holder.status.setVisibility(View.GONE);
        }else if(("m").equals(roomType)){
            holder.head.setImageResource(R.drawable.icon_m);
            holder.status.setVisibility(View.GONE);
        }else if(("p").equals(roomType)){
            holder.head.setImageResource(R.drawable.icon_p);
            holder.status.setVisibility(View.GONE);
        }else if (("d").equals(roomType)){
            String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
            RealmUserRepository userRepository = new RealmUserRepository(hostname);
            User userByUsername = userRepository.getUserByUsername(sub_name);
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
            holder.status.setImageResource(userStatusDrawableId);
            holder.status.setVisibility(View.VISIBLE);
            RequestOptions error = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                    .error(R.drawable.default_hd_avatar);
            if(userByUsername!=null)
            Glide.with(RocketChatApplication.getInstance())
                    .load(userByUsername.getAvatar()).apply(error)
                    .into(holder.head);
        }
        holder.time.setText(DateTime.fromEpocMs(subscription.getUpdatedAt(),DateTime.Format.DATE3));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_user_head)
        ImageView head;
        @BindView(R.id.tv_name)
        TextView name;
        @BindView(R.id.tv_time)
        TextView time;
        @BindView(R.id.tv_count)
        TextView count;
        @BindView(R.id.iv_current_user_status)
        ImageView status;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
