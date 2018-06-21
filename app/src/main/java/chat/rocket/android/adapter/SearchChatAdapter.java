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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.widget.emotionkeyboard.utils.EmotionUtils;
import chat.rocket.android.widget.emotionkeyboard.utils.SpanStringUtils;
import chat.rocket.core.models.Mention;
import chat.rocket.core.models.Message;

/**
 * Created by helloworld on 2018/2/27
 */

public class SearchChatAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private List<Message> allMessageByRoomId;
    private String mark;

    public void setDatas(List<Message> tempDataList, String mark) {
        if (tempDataList == null) {
            tempDataList = new ArrayList<>();
        }
        this.mark = mark;
        allMessageByRoomId = tempDataList;
        notifyDataSetChanged();
    }

    //自定义监听事件
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Message message);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public SearchChatAdapter(Context activity, List<Message> allMessageByRoomId) {
        this.mContext = activity;
        if(allMessageByRoomId == null){
            allMessageByRoomId = new ArrayList<>();
        }
        this.allMessageByRoomId = allMessageByRoomId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_info, parent,
                false);
        return new ContentVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentVH vh = (ContentVH) holder;
        Message message = allMessageByRoomId.get(position);

        //给布局设置点击和长点击监听
        holder.itemView.setOnClickListener(v -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onItemClick(v, message);
            }
        });

        String messageStr = message.getMessage();
        if(messageStr.contains(mark)){
            if (!TextUtils.isEmpty(messageStr)) {
                List<Mention> mentions=message.getMentions();
                if(mentions!=null&&mentions.size()>0){
                    for (Mention mention:mentions){
                        int lastIndexOf=mention.getUsername().lastIndexOf("&");
                        if (lastIndexOf!=-1){
                            String name = mention.getUsername().substring(0, lastIndexOf);
                            messageStr=messageStr.replace(mention.getUsername(),name);
                        }
                    }
                }
            }
            SpannableString sp= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,mContext, vh.tvName,messageStr);
            SpannableString spannableString = new SpannableString(sp);
            int start = messageStr.indexOf(mark);
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ff0000")), start, start+mark.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            vh.tvName.setText(spannableString);
        }else {
            vh.tvName.setText(messageStr);
        }
        vh.tv_time.setText(DateTime.fromEpocMs(message.getTimestamp(), DateTime.Format.DATE_TIME2));
        String realName = message.getUser().getRealName();
        vh.tv_from.setText(realName);
    }

    @Override
    public int getItemCount() {
        return allMessageByRoomId.size();
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvName,tv_time,tv_from;
        public ContentVH(View itemView) {
            super(itemView);
            tvName =  itemView.findViewById(R.id.tv_name);
            tv_time =  itemView.findViewById(R.id.tv_time);
            tv_from =  itemView.findViewById(R.id.tv_from);
        }
    }
}
