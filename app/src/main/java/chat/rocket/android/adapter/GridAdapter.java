package chat.rocket.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.widgets.GlideRoundTransform;
import chat.rocket.core.models.User;

/**
 * Created by zhangxiugao on 2018/1/25.
 * 群组成员信息adapter
 */

public class GridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{

    private Context mContext;
    private List<User> datas;//数据
    private boolean hasPermission;

    //自定义监听事件
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    //适配器初始化
    public GridAdapter(Context context,List<User> datas, boolean hasPermission) {
        this.mContext=context;
        this.hasPermission = hasPermission;
        if(datas == null){
            datas = new ArrayList<>();
        }
        this.datas=datas;
    }

//    @Override
//    public int getItemViewType(int position) {
//        //判断item类别，是图还是显示页数（图片有URL）
//        if (position == datas.size() || position == datas.size() + 1) {
//            return 0;
//        } else {
//            return 1;
//        }
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.listitem_group_user, parent,
                false);
        MyViewHolder holder = new MyViewHolder(view);
        //给布局设置点击和长点击监听
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //将数据与item视图进行绑定，如果是MyViewHolder就加载网络图片
        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar_round).transform(new GlideRoundTransform(mContext))
                .error(R.drawable.default_hd_avatar_round);
        if(datas.size() >= 10){
            if (position == 10 && hasPermission) {
                ((MyViewHolder) holder).iv_icon.setImageDrawable(null);
                ((MyViewHolder) holder).tv_name.setText("");
                ((MyViewHolder) holder).iv_icon.setBackgroundResource(R.drawable.group_add);
            } else if (position == 11 && hasPermission) {
                ((MyViewHolder) holder).iv_icon.setImageDrawable(null);
                ((MyViewHolder) holder).tv_name.setText("");
                ((MyViewHolder) holder).iv_icon.setBackgroundResource(R.drawable.group_delete);
            } else {
                User user = datas.get(position);
                ((MyViewHolder) holder).tv_name.setText(user.getRealName());
                Glide.with(RocketChatApplication.getInstance()).load(user.getAvatar()).apply(options).into(((MyViewHolder) holder).iv_icon);
            }
        }else {
            if (position == datas.size()) {
                ((MyViewHolder) holder).iv_icon.setImageDrawable(null);
                ((MyViewHolder) holder).tv_name.setText("");
                ((MyViewHolder) holder).iv_icon.setBackgroundResource(R.drawable.group_add);
            } else if (position == datas.size() + 1) {
                ((MyViewHolder) holder).iv_icon.setImageDrawable(null);
                ((MyViewHolder) holder).tv_name.setText("");
                ((MyViewHolder) holder).iv_icon.setBackgroundResource(R.drawable.group_delete);
            } else {
                User user = datas.get(position);
                ((MyViewHolder) holder).tv_name.setText(user.getRealName());
                Glide.with(RocketChatApplication.getInstance()).load(user.getAvatar()).apply(options).into(((MyViewHolder) holder).iv_icon);
            }
        }
    }

    @Override
    public int getItemCount() {
        // 限制群用户两行显示
        if(hasPermission){
            if(datas.size() >= 10){
                return 12;
            }
            return datas.size() + 2;
        }
        if(datas.size() >= 12){
            return 12;
        }
        return datas.size();//获取数据的个数
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v);
        }
    }

    //自定义ViewHolder，用于加载图片
    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        public MyViewHolder(View view) {
            super(view);
            iv_icon =  view.findViewById(R.id.iv_icon);
            tv_name =  view.findViewById(R.id.tv_name);
        }
    }
}
