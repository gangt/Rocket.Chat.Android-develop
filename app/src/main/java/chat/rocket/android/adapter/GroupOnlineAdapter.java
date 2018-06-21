package chat.rocket.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.core.models.User;
import chat.rocket.persistence.realm.models.ddp.UserEntity;

/**
 * Created by helloworld on 2018/3/6
 */

public class GroupOnlineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<UserEntity> allDataList;
    private Context mContext;

    public GroupOnlineAdapter(Activity activity, List<UserEntity> allDataList) {
        this.allDataList = allDataList;
        this.mContext = activity;
    }

    public void setDatas(List<UserEntity> tempDataList) {
        this.allDataList = tempDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.more_member_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UserEntity entity = allDataList.get(position);
        MyViewHolder vh = (MyViewHolder) holder;
        vh.tv_name.setText(entity.getRealName());
        vh.iv_online.setImageResource(getOnlineStatus(entity.getStatus()));
        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar);
        Glide.with(RocketChatApplication.getInstance())
                .load(entity.getAvatar())
                .apply(options)
                .into(vh.iv_icon);

        boolean manager = entity.isManager();
        boolean owner = entity.isOwner();

        if(manager && owner){
            vh.tv_dept.setText("("+"管理员"+")");
        }else if(owner){
            vh.tv_dept.setText("("+"群主"+")");
        }else if(manager){
            vh.tv_dept.setText("("+"管理员 群主"+")");
        }
        if(entity.isMute()){
            vh.iv_mute.setVisibility(View.VISIBLE);
        }else{
            vh.iv_mute.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allDataList.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_online,iv_icon,iv_mute;
        private TextView tv_name,tv_dept;
        public MyViewHolder(View view) {
            super(view);
            iv_online =  view.findViewById(R.id.iv_online);
            tv_name =  view.findViewById(R.id.tv_name);
            tv_dept =  view.findViewById(R.id.tv_dept);
            iv_icon =  view.findViewById(R.id.iv_icon);
            iv_mute =  itemView.findViewById(R.id.iv_mute);
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

    public interface OnItemClickListener{
        void onClick( int position);
    }

    OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener ){
        this. mOnItemClickListener=onItemClickListener;
    }
}
