package chat.rocket.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.widget.helper.FileUtils;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.Message;

/**
 * Created by helloworld on 2018/2/27
 */

public class FuJianAdapter extends RecyclerView.Adapter{

    private Context mContext;
    private List<Message> allMessageByRoomId;

    public void setDatas(List<Message> tempDataList) {
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

    public FuJianAdapter(Context activity, List<Message> allMessageByRoomId) {
        this.mContext = activity;
        if(allMessageByRoomId == null){
            allMessageByRoomId = new ArrayList<>();
        }
        this.allMessageByRoomId = allMessageByRoomId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cundang_item, parent,
                false);
        ContentVH holder = new ContentVH(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        [{"title":"Balance(magazine)-05-2.3.001-bigpicture_05_11.jpg","description":null,"title_link":"\/file-upload\/uf8NkcHnWhRxNEXcf\/Balance(magazine)-05-2.3.001-bigpicture_05_11.jpg","title_link_download":true,"nicktitle":"","image_url":"\/file-upload\/uf8NkcHnWhRxNEXcf\/Balance(magazine)-05-2.3.001-bigpicture_05_11.jpg","image_type":"image\/jpeg","image_size":447111}]
//        [{"title":"TS0cKrTQ4verMLpIm3ed.m4a","description":null,"title_link":"\/file-upload\/MZ5WgZJy7DE7xRYNn\/TS0cKrTQ4verMLpIm3ed.m4a","title_link_download":true,"nicktitle":"TS0cKrTQ4verMLpIm3ed.m4a","audio_url":"\/file-upload\/MZ5WgZJy7DE7xRYNn\/TS0cKrTQ4verMLpIm3ed.m4a","audio_type":"audio\/x-m4a","audio_size":null}]
        ContentVH vh = (ContentVH) holder;
        Message message = allMessageByRoomId.get(position);

        //给布局设置点击和长点击监听
        holder.itemView.setOnClickListener(v -> {
            if(mOnItemClickListener!=null){
                mOnItemClickListener.onItemClick(v, message);
            }
        });

        Attachment attachment = message.getAttachments().get(0);
        if(attachment == null || attachment.getAttachmentTitle() == null) return;
        String title = attachment.getAttachmentTitle().getTitle();
        vh.tvName.setText(title);
        vh.tv_time.setText(DateTime.fromEpocMs(message.getTimestamp(), DateTime.Format.DATE_TIME2));
        String realName = message.getUser().getRealName();
//        String name = message.getUser().getName();
        vh.tv_from.setText(realName);
        showTitleIcon(title

                , vh.imageView);
    }

    @Override
    public int getItemCount() {
        return allMessageByRoomId.size();
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvName,tv_time,tv_from;
        ImageView imageView;
        public ContentVH(View itemView) {
            super(itemView);
            tvName =  itemView.findViewById(R.id.tv_name);
            tv_time =  itemView.findViewById(R.id.tv_time);
            tv_from =  itemView.findViewById(R.id.tv_from);
            imageView = itemView.findViewById(R.id.image_file_type);
        }
    }
    private void showTitleIcon(String title, ImageView view) {
        if (FileUtils.isWord(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_word);
        } else if (FileUtils.isExcel(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_excel);
        } else if (FileUtils.isPPT(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_ppt);
        } else if (FileUtils.isMp3(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_mp3);
        } else if (FileUtils.isTxt(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_txt);
        } else if (FileUtils.isZip(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_zip);
        } else if (FileUtils.isPDF(title)) {
            view.setImageResource(chat.rocket.android.widget.R.drawable.send_pdf);
        }else {
            view.setImageResource(R.drawable.icon_file_defaule);
        }
    }
}
