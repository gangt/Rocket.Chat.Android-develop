package chat.rocket.android.layouthelper.chatroom;

import android.content.Intent;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.ToastUtils;
import chat.rocket.android.activity.business.FileViewActivity;
import chat.rocket.android.activity.business.PlayerViewActivity;
import chat.rocket.android.entity.VideoBean;
import chat.rocket.android.helper.DownloadUtil;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.helper.AudioHelper;
import chat.rocket.android.widget.helper.FileUtils;
import chat.rocket.android.widgets.RocketChatMessageAttachmentsLayout;
import chat.rocket.android.widgets.RocketChatMessageReportLayout;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.AttachmentTitle;
import chat.rocket.core.models.Message;

/**
 * ViewData holder of NORMAL chat message.
 */
public class MessageNormalViewHolder extends AbstractMessageViewHolder {
    private final TextView body;
    //  private final RocketChatMessageUrlsLayout urls;
    private final RocketChatMessageAttachmentsLayout attachments;
    private final RocketChatMessageReportLayout reportLayout;
    //  private Context mContext;
    private MessageListAdapter messageListAdapter;
    private boolean isMySelf;

    /**
     * constructor WITH hostname.
     */
    public MessageNormalViewHolder(View itemView, AbsoluteUrl absoluteUrl, String hostname, MessageListAdapter messageListAdapter, boolean isMySelf) {
        super(itemView, absoluteUrl, hostname);
        this.messageListAdapter = messageListAdapter;
        this.isMySelf = isMySelf;
        body = itemView.findViewById(R.id.message_body);
//    urls = itemView.findViewById(R.id.message_urls);
        attachments = itemView.findViewById(R.id.message_attachments);
        reportLayout = itemView.findViewById(R.id.message_reports);
    }

    @Override
    protected void bindMessage(PairedMessage pairedMessage, boolean autoloadImages) {
        MessageRenderer messageRenderer = new MessageRenderer(pairedMessage.target, autoloadImages);
        messageRenderer.showAvatar(avatar, hostname);
        messageRenderer.showUsername(username, subUsername);
        messageRenderer.showTimestampOrMessageState(timestamp);
        messageRenderer.showBody(body,isMySelf);
        body.setOnLongClickListener(view -> {
            BaseEvent baseEvent = new BaseEvent();
            baseEvent.setCode(EventTags.SET_LONG_CLICK);
            baseEvent.setTarget(pairedMessage.target);
            EventBus.getDefault().post(baseEvent);
            return true;
        });
        attachments.setOnLongClickListener(view -> {
                    BaseEvent baseEvent = new BaseEvent();
                    baseEvent.setCode(EventTags.SET_LONG_CLICK);
                    baseEvent.setTarget(pairedMessage.target);
                    EventBus.getDefault().post(baseEvent);
                    return true;
                }
        );
//    messageRenderer.showUrl(urls);
        messageRenderer.showAttachment(attachments, absoluteUrl);
        messageRenderer.showReport(reportLayout);
        setClickListener(pairedMessage.target);
    }


    /**
     * 所有文件的点击事件，包括图片，视频
     *
     * @param message
     */
    private void setClickListener(Message message) {
        List<Attachment> attachmentList = message.getAttachments();
        if (attachmentList == null || attachmentList.size() == 0) return;

        Attachment attachment = attachmentList.get(0);

        AttachmentTitle attachmentTitle = attachment.getAttachmentTitle();
        if (attachmentTitle == null) return;
        String title = attachmentTitle.getTitle();
        if (title == null) return;

        if (FileUtils.isPhoto(title) || FileUtils.isVideo(title)) {
            onClickPhotoVideo(attachmentTitle.getLink());
        } else if (FileUtils.isAudio(title)) {
            onAudioClick(attachments.getAudioImage(), attachmentTitle, message);
        } else {
            onClickFile(attachmentTitle, message.getTimestamp());
        }
    }

    private String absolutize(String url) {
        if (absoluteUrl == null) {
            return url;
        }
        return absoluteUrl.from(url);
    }

    private void onClickFile(AttachmentTitle attachmentTitle, long timestamp) {
        attachments.setOnPhotoClickListener(v -> {
            Intent intent = new Intent(RocketChatApplication.getInstance(), FileViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("attachmentTitle", attachmentTitle.getTitle());
            intent.putExtra("attachmentLink", absoluteUrl.from(attachmentTitle.getLink()));
            intent.putExtra("timestamp", timestamp);
            RocketChatApplication.getInstance().startActivity(intent);
        });
    }

    /**
     * 点击播放音频
     */
    private void onAudioClick(ImageView audioImg,AttachmentTitle attachmentTitle, Message message) {
        attachments.setOnAudioClickListener(v -> {
            String currentFileName = DownloadUtil.get().getFileName(attachmentTitle.getTitle(), message.getTimestamp());
            String appDownloadDir = FileUtils.getAppDownloadDir(RocketChatApplication.getInstance());
            File currentFile = new File(appDownloadDir + currentFileName);
            if (currentFile.exists()) {
                RCLog.d(currentFileName + "已经存在，准备播放音频");
                AudioHelper.getInstance().playSound(currentFile, audioImg, isMySelf);
            } else {
                DownloadUtil.get().download(absolutize(attachmentTitle.getLink()), appDownloadDir, currentFileName, new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess() {
                        RCLog.d(currentFileName + "音频下载成功");
                        AudioHelper.getInstance().playSound(currentFile, audioImg, isMySelf);
                    }

                    @Override
                    public void onDownloading(int progress) {
                        //                progressBar.setProgress(progress);
                    }

                    @Override
                    public void onDownloadFailed() {
                        RCLog.d(currentFileName + "音频下载失败");
                        AudioHelper.getInstance().playSound(attachmentTitle.getDownloadLink(), audioImg, isMySelf);
                    }
                });
            }
        });
    }

    /**
     * @param link 点击的图片或者视频的下载连接
     */
    private void onClickPhotoVideo(String link) {
        attachments.setOnPhotoClickListener(v -> {
            List<PairedMessage> itemData = messageListAdapter.getItemData();
            ArrayList<VideoBean> videoPicList = new ArrayList<>();

            int selectPosition = 0;//点击图片或视频 整个图片视频集合列表中的位置
            boolean isSelectOk = false;
            for (int i = 0; i < itemData.size(); i++) {
                List<Attachment> attachmentList = itemData.get(i).target.getAttachments();
                if (attachmentList == null || attachmentList.size() == 0) continue;
                Attachment att = attachmentList.get(0);
                if (att.getAttachmentTitle() == null) continue;
                String attLink = att.getAttachmentTitle().getLink();

                if (TextUtils.isEmpty(attLink)) continue;
                if (FileUtils.isPhoto(attLink)) {
                    VideoBean bean = new VideoBean(VideoBean.PICTURE, absoluteUrl.from(attLink));
                    videoPicList.add(bean);
                    if (attLink.equals(link) && !isSelectOk) {
                        selectPosition = videoPicList.size() - 1;
                        isSelectOk = true;
                    }
                } else if (FileUtils.isVideo(attLink)) {
                    VideoBean bean = new VideoBean(VideoBean.VIDEO, absoluteUrl.from(attLink));
                    videoPicList.add(bean);
                    if (attLink.equals(link) && !isSelectOk) {
                        selectPosition = videoPicList.size() - 1;
                        isSelectOk = true;
                    }
                }
            }
            Collections.reverse(videoPicList);
            Intent intent = new Intent(RocketChatApplication.getInstance(), PlayerViewActivity.class);
            intent.putParcelableArrayListExtra("video_url", videoPicList);
            intent.putExtra("selectPosition", videoPicList.size() - selectPosition - 1);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            RocketChatApplication.getInstance().startActivity(intent);
        });
    }

}
