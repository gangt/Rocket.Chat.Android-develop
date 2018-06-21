package chat.rocket.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.activity.business.OrgSelectUserActivity;
import chat.rocket.android.helper.DateTime;
import chat.rocket.core.models.Mention;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.SpotlightUser;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import indexablerv.IndexableAdapter;

public class OrgSearchAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<UserEntity> spotlightUsers;
    private String mark;

    public void setDatas(List<UserEntity> tempDataList, String mark) {
        if (tempDataList == null) {
            tempDataList = new ArrayList<>();
        }
        this.mark = mark;
        spotlightUsers = tempDataList;
        notifyDataSetChanged();
    }

    //自定义监听事件
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, UserEntity spotlightUsers);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public OrgSearchAdapter(Context activity, List<UserEntity> spotlightUsers) {
        this.mContext = activity;
        if (spotlightUsers == null) {
            spotlightUsers = new ArrayList<>();
        }
        this.spotlightUsers = spotlightUsers;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_orgselect_search, parent,
                false);
        return new ContentVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentVH vh = (ContentVH) holder;
        UserEntity spotlightUser = spotlightUsers.get(position);

        //给布局设置点击和长点击监听
        holder.itemView.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, spotlightUser);
            }
        });

        vh.tvName.setText(spotlightUser.getRealName());
    }

    @Override
    public int getItemCount() {
        return spotlightUsers.size();
    }

    private class ContentVH extends RecyclerView.ViewHolder {
       TextView tvName;
       ImageView ivStatus;
        public ContentVH(View itemView) {
            super(itemView);
            ivStatus=itemView.findViewById(R.id.iv_current_user_status);
            tvName=itemView.findViewById(R.id.tv_name);
        }
    }
}
