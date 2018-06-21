package chat.rocket.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.RocketChatConstants;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.helper.DateTime;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Subscription;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmSubscriptionRepository;

/**
 * Created by jumper on 2018/3/29
 */

public class MeettingHistoryAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private List<Room> tempDataList;
    private String mark;

    public ReStartListener reStartListener;
    public void setDatas(List<Room> tempDataList, String mark) {
        if (tempDataList == null) {
            tempDataList = new ArrayList<>();
        }
        this.mark = mark;
        this.tempDataList = tempDataList;
        notifyDataSetChanged();
    }

    //自定义监听事件
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Room data);
    }

    public interface ReStartListener {
        void restart(View view,int position);
    }

    public void setRestartListener(ReStartListener reStartListener){
      this.reStartListener=reStartListener;
    }
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public MeettingHistoryAdapter(Context activity, List<Room> tempDataList) {
        this.mContext = activity;
        if(tempDataList == null){
            tempDataList = new ArrayList<>();
        }
        this.tempDataList = tempDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_meetting_history, parent,
                false);
        AutoUtils.auto(view);
        return new ContentVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentVH vh = (ContentVH) holder;
        Room data = tempDataList.get(position);

        //给布局设置点击和长点击监听
        holder.itemView.setOnClickListener(v -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onItemClick(v, data);
            }
        });
        vh.tvTitle.setText(data.getDisplayName());
//        RealmRoomRepository roomRepository = new RealmRoomRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
//        Room room=roomRepository.getByRoomId(data.getRid());
        String startTime = DateTime.fromEpocMs(data.getStartTime(), DateTime.Format.DATE_TIME2);
        String endTime = DateTime.fromEpocMs(data.getEndTime(), DateTime.Format.DATE_TIME2);
        vh.tv_time.setText(startTime+"~"+endTime);
        vh.tv_subtitle.setText(data.getUUserName().split("&")[0]+"("+data.getCompanyName()+")");
        RealmRoomRepository repository = new RealmRoomRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
//        Subscription subscriptionRid = subscriptionRepository.getSubscriptionRid(data.getId());
//        List<String> roles = subscriptionRid.getRoles();
//        (data.getHost().equals(RocketChatCache.INSTANCE.getUserUsername()))||(roles != null && roles.size() > 0
//        && (roles.contains(RocketChatConstants.OWNER) || roles.contains(RocketChatConstants.MODERATOR)))
        vh.btn_restart.setVisibility(data.isPause()&&repository.getByRoomId(data.getId()).getUUserName().equals(RocketChatCache.INSTANCE.getUserUsername())?View.VISIBLE:View.GONE);
        vh.btn_restart.setOnClickListener(view -> {
//            ToastUtils.showToast("重新开始");
            reStartListener.restart(view,position);
        });
    }

    @Override
    public int getItemCount() {
        return tempDataList.size();
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvTitle,tv_time,tv_subtitle;
        Button  btn_restart;

        public ContentVH(View itemView) {
            super(itemView);
            tvTitle =  itemView.findViewById(R.id.tv_meetting_history_title);
            tv_time =  itemView.findViewById(R.id.tv_meetting_history_time);
            tv_subtitle =  itemView.findViewById(R.id.tv_meetting_history_create);
            btn_restart=itemView.findViewById(R.id.btn_meetting_history);
        }
    }
}
