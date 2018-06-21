package chat.rocket.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;

/**
 * Created by jumper_C on 2018/5/12.
 */

public class RelayImagAdapter extends RecyclerView.Adapter {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_relay_image, parent,
                false);
        AutoUtils.auto(view);
        return new ContentVH(view);
    }
    List<String> imgs;
    private Context mContext;
    public RelayImagAdapter(Context mContext,List<String> imgs){
        this.mContext=mContext;
        this.imgs=imgs;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentVH vh = (ContentVH) holder;
//        vh.imageView.
        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar);
        Glide.with(RocketChatApplication.getInstance())
                .load(imgs.get(position))
                .apply(options)
                .into(vh.imageView);
    }

    @Override
    public int getItemCount() {
        return imgs.size();
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ContentVH(View itemView) {
            super(itemView);
            imageView =  itemView.findViewById(R.id.image);
        }
    }
}
