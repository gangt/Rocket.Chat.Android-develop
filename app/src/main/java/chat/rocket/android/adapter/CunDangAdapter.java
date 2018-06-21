package chat.rocket.android.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhy.autolayout.utils.AutoUtils;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.activity.business.FileViewActivity;
import chat.rocket.android.activity.business.FuJianActivity;
import chat.rocket.android.activity.business.PlayerViewActivity;
import chat.rocket.android.entity.GuidangBean;
import chat.rocket.android.entity.GuidangBean1;
import chat.rocket.android.entity.VideoBean;
import chat.rocket.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.widget.helper.FileUtils;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Attachment;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by helloworld on 2018/2/27
 */

public class CunDangAdapter extends RecyclerView.Adapter {
    //    GuidangBean result;
    Context mContext;
    String url;
    List<GuidangBean1.DataBeanX.DataBean.VarListBean> data;
    private ArrayList<VideoBean> videoPicList;

    @SuppressLint("RxLeakedSubscription")
    public CunDangAdapter(Context mContext, List<GuidangBean1.DataBeanX.DataBean.VarListBean> data) {
//        this.result=result;
        this.mContext = mContext;
        this.data = data;

    }

    public void setData(List<GuidangBean1.DataBeanX.DataBean.VarListBean> data, boolean isloadMore,String url) {
        if (isloadMore) {
            this.data.addAll(data);
        } else {
            this.data.clear();
            this.data.addAll(data);
        }
        this.url=url;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cundang_item_new, parent,
                false);
        ContentVH holder = new ContentVH(view);
        AutoUtils.auto(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentVH vh = (ContentVH) holder;

        vh.tvName.setText(data.get(position).getName());
//        vh.tv_time.setText(data.get(position).getName());
        vh.tv_from.setText(data.get(position).getUperName());
        showTitleIcon(data.get(position).getName(), vh.imageView);
        vh.itemView.setOnClickListener(view -> {

            videoPicList = new ArrayList<>();
            String title = data.get(position).getName();
            int selectPosition = 0;//点击图片或视频 整个图片视频集合列表中的位置
            boolean isSelectOk = false;
            for (int i = 0; i < data.size(); i++) {
                GuidangBean1.DataBeanX.DataBean.VarListBean att = data.get(i);
                long id = att.getId();
                String path = att.getPath();
                String name = att.getName();
                if (TextUtils.isEmpty(name)) continue;
                if (FileUtils.isPhoto(name)) {
                    VideoBean bean = new VideoBean(VideoBean.PICTURE, fromUrl(id + "", path));
                    videoPicList.add(bean);
                    if (name.equals(title)&&!isSelectOk) {
                        selectPosition = videoPicList.size() - 1;
                        isSelectOk = true;
                    }
                } else if (FileUtils.isVideo(name)) {
                    VideoBean bean = new VideoBean(VideoBean.VIDEO, fromUrl(id + "", path));
                    videoPicList.add(bean);
                    if (name.equals(title)&&!isSelectOk) {
                        selectPosition = videoPicList.size() - 1;
                        isSelectOk = true;
                    }
                }
            }

            if (FileUtils.isVideo(title) || FileUtils.isPhoto(title)) {
                Intent intent = new Intent(mContext, PlayerViewActivity.class);
                ArrayList<VideoBean> currentVideoPicList = new ArrayList<>();
                currentVideoPicList.add(videoPicList.get(selectPosition));
                intent.putParcelableArrayListExtra("video_url", currentVideoPicList);
                intent.putExtra("selectPosition", selectPosition);
                mContext.startActivity(intent);
                return;
            }
            Intent intent = new Intent(RocketChatApplication.getInstance(), FileViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("attachmentTitle", data.get(position).getName());

            intent.putExtra("attachmentLink", fromUrl(data.get(position).getId() + "", data.get(position).getPath()));
//            intent.putExtra("attachmentLink", absoluteUrl.from(titleLink));
//            intent.putExtra("timestamp", timestamp);
            RocketChatApplication.getInstance().startActivity(intent);
        });
    }

    public String fromUrl(String id, String path) {
        String oaLink = url;
        return oaLink + "?" + "sessionId=" + RocketChatCache.INSTANCE.getSessionId()
                + "&id=" + id + "&documentIPath=" + path;
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvName, tv_time, tv_from;
        ImageView imageView;

        public ContentVH(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_file_type);
            tvName = itemView.findViewById(R.id.tv_name);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_from = itemView.findViewById(R.id.tv_from);
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
        } else {
            view.setImageResource(R.drawable.icon_file_defaule);
        }
    }
}
