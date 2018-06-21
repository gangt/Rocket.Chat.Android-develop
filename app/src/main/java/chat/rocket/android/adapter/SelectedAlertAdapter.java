package chat.rocket.android.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import indexablerv.IndexableAdapter;

public class SelectedAlertAdapter extends IndexableAdapter<UserEntity>  {
    private LayoutInflater mInflater;
    private onItemClickListener listener;
    public interface  onItemClickListener{
        void onItemClick(View v, UserEntity entity);
    }
    public void setOnItemClickListener(onItemClickListener listener){
        this.listener=listener;
    }
    public SelectedAlertAdapter(Activity activity) {
        super();
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateTitleViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_index_contact, parent, false);
        return new IndexVH(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.selected_alert_item, parent, false);
        return new ContentVH(view);
    }

    @Override
    public void onBindTitleViewHolder(RecyclerView.ViewHolder holder, String indexTitle) {
        IndexVH vh = (IndexVH) holder;
        vh.tv.setText(indexTitle);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, final UserEntity entity) {
        ContentVH vh = (ContentVH) holder;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onItemClick(v,entity);
                }
            }
        });
        vh.tvName.setText(entity.getRealName());
        vh.iv_online.setImageResource(getOnlineStatus(entity.getStatus()));
        vh.tvZhiwei.setText(entity.getZhiWei());
        vh.tvBumen.setText(entity.getDept());
        vh.tvCompany.setText(entity.getCompanyName());
        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar);
        Glide.with(RocketChatApplication.getInstance())
                .load(entity.getAvatar())
                .apply(options)
                .into(vh.iv_icon);
        boolean manager = entity.isManager();
        boolean owner = entity.isOwner();

        if(manager && owner){
            vh.tv_dept.setText("("+"群主 管理员"+")");
        }else if(owner){
            vh.tv_dept.setText("("+"群主"+")");
        }else if(manager){
            vh.tv_dept.setText("("+"管理员"+")");
        }else {
            vh.tv_dept.setText("");
        }

    }

    private int getOnlineStatus(String status) {
        int userStatusDrawableId = R.drawable.userstatus_offline;
        if(TextUtils.isEmpty(status)) return userStatusDrawableId;
        switch (status){
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
        return userStatusDrawableId;
    }

    private class IndexVH extends RecyclerView.ViewHolder {
        TextView tv;

        public IndexVH(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv_index);
        }
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvName,tv_dept,tvZhiwei,tvBumen,tvCompany;
        ImageView iv_online,iv_icon;

        public ContentVH(View itemView) {
            super(itemView);
            tvName =  itemView.findViewById(R.id.tv_name);
            tv_dept =  itemView.findViewById(R.id.tv_dept);
            iv_online = itemView.findViewById(R.id.iv_online);
            iv_icon =  itemView.findViewById(R.id.iv_icon);
            tvBumen=itemView.findViewById(R.id.tv_bumen);
            tvCompany=itemView.findViewById(R.id.tv_company);
            tvZhiwei=itemView.findViewById(R.id.tv_zhiwei);
        }
    }
}
