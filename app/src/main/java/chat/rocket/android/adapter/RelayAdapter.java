package chat.rocket.android.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jakewharton.rxbinding2.view.RxView;
import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.activity.business.RelayActivity;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Subscription;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by jumper on 2018/3/29
 */

public class RelayAdapter extends RecyclerView.Adapter {

    private RelayActivity mContext;
    private List<Subscription> tempDataList;
    private String mark;
    private HashMap<String, Boolean> selectListStr;

    public void setDatas(List<Subscription> tempDataList, String mark) {
        if (tempDataList == null) {
            tempDataList = new ArrayList<>();
        }
        this.mark = mark;
        this.tempDataList = tempDataList;
        notifyDataSetChanged();
    }

    public HashMap<String, Boolean> getIsSelected() {
        return selectListStr;
    }



    public List<Subscription> getTempDataList() {
        return tempDataList;
    }


    //自定义监听事件
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Room data);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }
    RealmUserRepository userRepository;
    List<String> stringList;
    public RelayAdapter(RelayActivity activity, List<Subscription> tempDataList) {
        this.mContext = activity;
        if (tempDataList == null) {
            tempDataList = new ArrayList<>();
        }
        userRepository = new RealmUserRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
       stringList=new ArrayList<>();
        for (Subscription subscription:tempDataList){
            try {
                stringList.add(userRepository.getAvatarByUsername(subscription.getName()));
            } catch (Exception e) {
                stringList.add("");
            }
        }
        this.tempDataList = tempDataList;
        selectListStr = new HashMap<>();
//        initDate();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_relay, parent,
                false);
        AutoUtils.auto(view);
        return new ContentVH(view);
    }

    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentVH vh = (ContentVH) holder;
        Subscription data = tempDataList.get(position);
        notifyCheck(vh.checkBox, position);
        //给布局设置点击和长点击监听
        RxView.clicks(holder.itemView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    if (selectListStr.size() < 6) {
                        if (!vh.checkBox.isChecked()) {
                            mContext.setRightCount(selectListStr.size()+1);
                            selectListStr.put(data.getRid(), true);
                            vh.checkBox.setChecked(true);
                        } else {
                            mContext.setRightCount(selectListStr.size()-1);
                            selectListStr.remove(data.getRid());
                            vh.checkBox.setChecked(false);
                        }
                    } else {
                        if (vh.checkBox.isChecked()) {
                            mContext.setRightCount(selectListStr.size()-1);
                            selectListStr.remove(data.getRid());
                            vh.checkBox.setChecked(false);
                        }else {
                            mContext.showUnderSexHint();
                        }
                    }

                });
//        holder.itemView.setOnClickListener(v -> {
////            if(mOnItemClickListener!=null){
////                mOnItemClickListener.onItemClick(v, data);
////            }
//        });
        vh.tvTitle.setText(TextUtils.isEmpty(data.getDisplayName())?data.getName().split("&")[0]:data.getDisplayName());
        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar);
        Glide.with(RocketChatApplication.getInstance())
                .load(stringList.get(position))
                .apply(options)
                .into(vh.image);
    }

    private void notifyCheck(CheckBox checkbox, int position) {
        checkbox.setChecked(selectListStr.keySet().contains(tempDataList.get(position).getRid()) ? true : false);
    }

    @Override
    public int getItemCount() {
        return tempDataList.size();
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        CheckBox checkBox;
        ImageView image;

        public ContentVH(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            checkBox = itemView.findViewById(R.id.cb_check);
            image = itemView.findViewById(R.id.image);
        }
    }


}
