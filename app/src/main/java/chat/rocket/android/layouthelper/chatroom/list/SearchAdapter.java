package chat.rocket.android.layouthelper.chatroom.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import chat.rocket.android.R;
import chat.rocket.core.models.SpotlightUser;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;

/**
 * Created by user on 2018/2/2.
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Subscription> mSubscriptionDatas;
    private List<SpotlightUser> mSpotlightUserDatas;
    private static final int TYPE_CHANNEL=100;
    private static final int TYPE_USER=101;
    private LayoutInflater inflater;
    private OnClickItemListener onClickItemListener;
    public SearchAdapter(Context context ,OnClickItemListener onClickItemListener){
        this.inflater = LayoutInflater.from(context);
        this.onClickItemListener=onClickItemListener;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = inflater.inflate(getLayout(viewType), parent, false);
        if (viewType==TYPE_CHANNEL){
            return new SubscriptionViewHolder(inflate);
        }else if (viewType==TYPE_USER) {
            return new SpotlightUserViewHolder(inflate);
        }
        return null;
    }

    private int getLayout(int viewType) {
        if(viewType==TYPE_USER){
            return R.layout.search_item_conversation;
        }else {
            return R.layout.search_item_channel;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof SubscriptionViewHolder){
                Subscription subscription = mSubscriptionDatas.get(position);
                ((SubscriptionViewHolder) holder).bindData(subscription);
                holder.itemView.setOnClickListener(view->{
                    onClickItemListener.joinRoom(subscription);
                });
            }else if(holder instanceof SpotlightUserViewHolder){
                SpotlightUser spotlightUser = mSpotlightUserDatas.get(position - mSubscriptionDatas.size());
                ((SpotlightUserViewHolder) holder).bindData(spotlightUser);
                holder.itemView.setOnClickListener(view->{
                    onClickItemListener.createDirectMessage(spotlightUser);
                });
            }

    }
    public interface OnClickItemListener{
        void joinRoom(Subscription subscription);
        void createDirectMessage(SpotlightUser spotlightUser);
    }
    @Override
    public int getItemViewType(int position) {
        if(position<mSubscriptionDatas.size()){
            return TYPE_CHANNEL;
        }else {
            return TYPE_USER;
        }
    }

    public void setSubscriptionDatas(List<Subscription> datas){
        mSubscriptionDatas=datas;
        notifyDataSetChanged();
    }
    public void setSpotlightUserDatas(List<SpotlightUser> datas){
        mSpotlightUserDatas=datas;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        if(mSubscriptionDatas!=null && mSpotlightUserDatas!=null){
            return mSpotlightUserDatas.size()+mSubscriptionDatas.size();
        }
        return 0;
    }

    public class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        public void bindData(Subscription subscription){
            tvName.setText(subscription.getDisplayName());
        }
        public SubscriptionViewHolder(View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.tv_name);
        }
    }
    public class SpotlightUserViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivStatus;
        private TextView tvName;
        public void bindData(SpotlightUser spotlightUser){
            tvName.setText(spotlightUser.getRealName());
            int userStatusDrawableId= R.drawable.userstatus_offline;
            switch (spotlightUser.getStatus()){
                case User.STATUS_ONLINE:
                    userStatusDrawableId=R.drawable.userstatus_online;
                    break;
                case  User.STATUS_AWAY:
                    userStatusDrawableId = R.drawable.userstatus_away;
                    break;
                case User.STATUS_BUSY:
                    userStatusDrawableId = R.drawable.userstatus_busy;
                    break;
            }
            ivStatus.setImageResource(userStatusDrawableId);
        }
        public SpotlightUserViewHolder(View itemView) {
            super(itemView);
            ivStatus=itemView.findViewById(R.id.iv_current_user_status);
            tvName=itemView.findViewById(R.id.tv_name);
        }
    }
}
