package chat.rocket.android.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.zhy.autolayout.AutoLinearLayout;


import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.activity.business.FileViewActivity;
import chat.rocket.android.activity.business.PlayerViewActivity;
import chat.rocket.android.entity.VideoBean;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.DownloadUtil;
import chat.rocket.android.helper.MessageRefHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.layouthelper.chatroom.PairedMessage;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.emotionkeyboard.utils.EmotionUtils;
import chat.rocket.android.widget.emotionkeyboard.utils.SpanStringUtils;
import chat.rocket.android.widget.helper.AudioHelper;
import chat.rocket.android.widget.helper.FileUtils;
import chat.rocket.android.widget.helper.FrescoHelper;
import chat.rocket.android.widget.helper.GetNetVideoBitmap;
import chat.rocket.android.widget.helper.RxAsyncTask;
import chat.rocket.android.widget.message.MessageAttachmentFieldLayout;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.AttachmentAuthor;
import chat.rocket.core.models.AttachmentChild;
import chat.rocket.core.models.AttachmentField;
import chat.rocket.core.models.AttachmentTitle;
import chat.rocket.core.models.Message;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import cn.jzvd.JZVideoPlayerStandard;

/**
 */
public class RocketChatMessageAttachmentsLayout extends AutoLinearLayout {
    private LayoutInflater inflater;
    private AbsoluteUrl absoluteUrl;
    private String msgText;
    private OnPhotoClickListener listener;
    private OnAudioClickListener audioListener;
    private boolean isMyself;
    private RealmMessageRepository messageRepository ;
    private ImageView audioImg;
    private String hostName;
    private Message message;
    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.listener = listener;
    }

    public void setOnAudioClickListener(OnAudioClickListener listener) {
        this.audioListener = listener;
    }

    public RocketChatMessageAttachmentsLayout(Context context) {
        super(context);
        initialize(context, null);
    }

    public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }


    private void initialize(Context context, AttributeSet attrs) {
        inflater = LayoutInflater.from(context);
        setOrientation(VERTICAL);
    }

    public void setAbsoluteUrl(AbsoluteUrl absoluteUrl) {
        this.absoluteUrl = absoluteUrl;
    }

    public void isMySelf(boolean isMyself) {
        this.isMyself = isMyself;
    }

    public void setAttachments(List<Attachment> attachments, Message msg, boolean autoloadImages) {
//        if (this.attachments != null && this.attachments.equals(attachments)) {
//            return;
//        }
//        this.attachments = attachments;
        this.message=msg;
        this.msgText = msg.getMessage();
        this.hostName=RocketChatCache.INSTANCE.getSelectedServerHostname();
        messageRepository = new RealmMessageRepository(hostName);
        for (int i = 0, size = attachments.size(); i < size; i++) {
            appendAttachmentView(attachments.get(i), autoloadImages, false);
        }
    }

    public void appendAttachmentView(Attachment attachment, boolean autoloadImages, boolean showAttachmentStrip) {
        if (attachment == null) {
            return;
        }

        removeAllViews();
        View attachmentView = isMyself ? inflater.inflate(R.layout.message_inline_attachment_right, this, false)
                : inflater.inflate(R.layout.message_inline_attachment, this, false);

//        colorizeAttachmentBar(attachment, attachmentView, showAttachmentStrip);
        showAuthorAttachment(attachment, attachmentView);
        showVideoAttachment(attachment, attachmentView);
        showTitleAttachment(attachment, attachmentView);
        showReferenceAttachment(attachment, attachmentView);
        showImageAttachment(attachment, attachmentView, autoloadImages);
        // audio
        // video
        showFieldsAttachment(attachment, attachmentView);

        addView(attachmentView);
    }

//    private void colorizeAttachmentBar(Attachment attachment, View attachmentView, boolean showAttachmentStrip) {
//        final View attachmentStrip = attachmentView.findViewById(R.id.attachment_strip);
//
//        if (showAttachmentStrip) {
//            final String colorString = attachment.getColor();
//            if (TextUtils.isEmpty(colorString)) {
//                attachmentStrip.setBackgroundResource(R.color.inline_attachment_quote_line);
//                return;
//            }
//
//            try {
//                attachmentStrip.setBackgroundColor(Color.parseColor(colorString));
//            } catch (Exception e) {
//                attachmentStrip.setBackgroundResource(R.color.inline_attachment_quote_line);
//            }
//        } else {
//            attachmentStrip.setVisibility(GONE);
//        }
//    }

    private void showAuthorAttachment(Attachment attachment, View attachmentView) {
        final View authorBox = attachmentView.findViewById(R.id.author_box);
        AttachmentAuthor author = attachment.getAttachmentAuthor();
        if (author == null) {
            authorBox.setVisibility(GONE);
            return;
        }

        Log.i("mydebug", "showAuthorAttachment run");
        authorBox.setVisibility(VISIBLE);

        FrescoHelper.INSTANCE.loadImageWithCustomization((SimpleDraweeView) attachmentView.findViewById(R.id.author_icon), absolutize(author.getIconUrl()));

        final TextView authorName = attachmentView.findViewById(R.id.author_name);
        authorName.setText(author.getName());

        final String link = absolutize(author.getLink());
        authorName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
            }
        });

        // timestamp and link - need to format time
    }

    private void showTitleAttachment(Attachment attachment, View attachmentView) {
        View rl_audio = attachmentView.findViewById(R.id.rl_message_audio);
        final TextView audioView = attachmentView.findViewById(R.id.tv_massage_audio);
        View rl_title = attachmentView.findViewById(R.id.rl_title);
        TextView titleView = attachmentView.findViewById(R.id.title);
        audioImg =  attachmentView.findViewById(R.id.iv_massage_audio);
        final AttachmentTitle title = attachment.getAttachmentTitle();
        if (title == null || title.getTitle() == null || title.getLink() == null) {
            rl_title.setVisibility(View.GONE);
            rl_audio.setVisibility(View.GONE);
            return;
        }

        // 图片和视频另外处理
        if (FileUtils.isPhoto(attachment.getImageUrl()) || FileUtils.isVideo(attachment.getVideoUrl())) {
            rl_title.setVisibility(View.GONE);
            rl_audio.setVisibility(View.GONE);
            return;
        }

        final String link = absolutize(title.getLink());

        if (FileUtils.isAudio(title.getTitle())) {// 音频文件
            rl_audio.setVisibility(VISIBLE);
            rl_title.setVisibility(GONE);

            /**获取语音时长*/
            String currentFileName = DownloadUtil.get().getFileName(attachment.getAttachmentTitle().getTitle(), 0L);
            String appDownloadDir = FileUtils.getAppDownloadDir(RocketChatApplication.getInstance());
            File currentFile = new File(appDownloadDir + currentFileName);
            if (currentFile.exists()) {
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(currentFile.getPath());
                    String s= mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    int min = Integer.parseInt(s) / 1000 / 60;
                    int sec = Integer.parseInt(s) / 1000 % 60;
                    audioView.setText((min == 0 ? "" : min + "'") + (sec == 0 ? 1 : sec) + "''");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
            /**获取语音时长*/
            {
            new RxAsyncTask<String, Integer, String>() {
                @Override
                protected String call(String... strings) {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    if (link != null) {
                        mmr.setDataSource(link, new HashMap<String, String>());
                    }
                    return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                }

                @Override
                protected void onResult(String s) {
                    super.onResult(s);
                    int min = Integer.parseInt(s) / 1000 / 60;
                    int sec = Integer.parseInt(s) / 1000 % 60;
                    audioView.setText((min == 0 ? "" : min + "'") + (sec == 0 ? 1 : sec) + "''");
                }
            }.execute();
                DownloadUtil.get().download(link, appDownloadDir, currentFileName, null);

            }
        } else {// 其他格式的文件
            if (attachment.getImageUrl() == null){
                titleView.setVisibility(View.VISIBLE);
            }
            rl_audio.setVisibility(GONE);
            titleView.setText(title.getTitle());
            showTitleIcon(title.getTitle(), (ImageView) attachmentView.findViewById(R.id.iv_file));
        }
        rl_title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(view);
                }
            }
        });

        rl_audio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                audioListener.onItemClick(view);
            }
        });
    }

    /**
     * 回复引用
     */
    private void showReferenceAttachment(Attachment attachment, View attachmentView) {
        final View refBox = attachmentView.findViewById(R.id.ref_container);
        if (attachment.getThumbUrl() == null && attachment.getText() == null) {
            refBox.setVisibility(GONE);
            return;
        }
        refBox.setVisibility(VISIBLE);
        if (msgText.contains(hostName)&&msgText.contains("?msg=")) {
            String msgId = MessageRefHelper.getInstance().getMsgId(msgText);
            Message msg= MessageRefHelper.getInstance().getMsg(messageRepository,msgId);
            TextView tv_symbol = attachmentView.findViewById(R.id.tv_symbol);
            ImageView iv_symbol = attachmentView.findViewById(R.id.iv_symbol);
            TextView tv_target_user = attachmentView.findViewById(R.id.tv_target_user);
            TextView tv_time = attachmentView.findViewById(R.id.tv_time);
            LinearLayout rl_file = attachmentView.findViewById(R.id.rl_files);
            TextView tv_file_title = attachmentView.findViewById(R.id.tv_file_title);
            ImageView image = attachmentView.findViewById(R.id.iv_file1);
            JZVideoPlayerStandard child_videoplayer=attachmentView.findViewById(R.id.child_videoplayer);
            LinearLayout child_rl_message_audio=attachmentView.findViewById(R.id.child_rl_message_audio);
            TextView child_tv_message_audio=attachmentView.findViewById(R.id.child_tv_massage_audio);
            audioImg=attachmentView.findViewById(R.id.child_iv_massage_audio);
            LinearLayout ll_child_container=attachmentView.findViewById(R.id.child_container);
            TextView text = attachmentView.findViewById(R.id.tv_text);
            TextView tv_replay = attachmentView.findViewById(R.id.tv_replay);
            TextView tv_replay_child = attachmentView.findViewById(R.id.tv_replay_child);
            SimpleDraweeView image_reply = attachmentView.findViewById(R.id.image_reply);
            View line = attachmentView.findViewById(R.id.line);
            line.setVisibility(VISIBLE);
            tv_replay_child.setVisibility(GONE);

            tv_symbol.setText(MessageRefHelper.getInstance().getSymbol(msgText));
            iv_symbol.setImageResource(MessageRefHelper.getInstance().getSymbolRes(msgText));

            if (msg==null){
                tv_target_user.setText(attachment.getAuthorName());
                if(!TextUtils.isEmpty(attachment.getTimestamp()))
                    tv_time.setText(DateTime.fromEpocMs(Long.parseLong(attachment.getTimestamp()),DateTime.Format.DATE1));
                tv_replay.setVisibility(VISIBLE);
                try {
                    if (msgText.contains("@")&&msgText.contains("&")){
                        SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), tv_replay, msgText.split(msgId+"\\) ")[1].split(" +")[1]);
                        tv_replay.setText(spannableString);
                    }
                    else{
                        SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), tv_replay, msgText.split(msgId+"\\) ")[1]);
                        tv_replay.setText(spannableString);
                    }
                } catch (Exception e) {
                    tv_replay.setText("");
                }
                return;
            }
            tv_target_user.setText(MessageRefHelper.getInstance().getTagetUserName(msg));
            tv_time.setText(MessageRefHelper.getInstance().getTime(msg));

            /**回复文字*/
            if (!TextUtils.isEmpty(MessageRefHelper.getInstance().getReplayText(msgText,msg))) {
                tv_replay.setVisibility(VISIBLE);
                SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), tv_replay, MessageRefHelper.getInstance().getReplayText(msgText,msg));
                tv_replay.setText(spannableString);
            }
            if (!TextUtils.isEmpty(msg.getMessage())) {
                /**多层引用*/
                if (msg.getMessage().contains(hostName)&&msg.getMessage().contains("?msg=")){
                    ll_child_container.setVisibility(VISIBLE);
                    text.setVisibility(GONE);
                    setChildView(ll_child_container,msg.getMessage(),attachment.getAttachmentChild());
                    return;
                }else{
                    /**文字*/
                    text.setVisibility(VISIBLE);
//                    text.setText(msg.getMessage());
                    SpannableString spannableString=SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), text, MessageRefHelper.getInstance().getMsgText(msg).toString());
                    text.setText(spannableString);
                    text.setMovementMethod(LinkMovementMethod.getInstance());
                    return;
                }
            }
            /**图片*/
            if (attachment.getAttachmentChild().getImage_url() != null) {
                image_reply.setVisibility(VISIBLE);
//                loadImage(attachment.getAttachmentChild().getImage_url(), image_reply, false);
                if (isCached(Uri.parse(attachment.getAttachmentChild().getImage_url()))) {
                }
                FrescoHelper.INSTANCE.loadImageWithCustomization(image_reply, absolutize(attachment.getAttachmentChild().getImage_url()));
                image_reply.setOnClickListener(view -> initPhotoOrVideoClick(attachment.getAttachmentChild().getTitle_link(),attachment.getAttachmentChild().getTitle()));
                return;
            }
            /**文件*/
            if (!TextUtils.isEmpty(attachment.getAttachmentChild().getTitle_link()) &&
                    TextUtils.isEmpty(attachment.getAttachmentChild().getImage_url())) {
                /**音频*/
                if (FileUtils.isAudio(attachment.getAttachmentChild().getTitle_link())){
                    child_rl_message_audio.setVisibility(VISIBLE);
                    initAudio(attachment.getAttachmentChild().getTitle_link(),attachment.getAttachmentChild().getTitle(),child_tv_message_audio,child_rl_message_audio,msg.getTimestamp());
                    return;

                }/**视频*/
                else if (FileUtils.isVideo(attachment.getAttachmentChild().getTitle_link())){
                    child_videoplayer.setVisibility(VISIBLE);
                    initVideo(child_videoplayer,attachment.getAttachmentChild().getTitle_link(),attachment.getAttachmentChild().getTitle()
                    );
                    child_videoplayer.setOnClickListener(view -> initPhotoOrVideoClick(attachment.getAttachmentChild().getTitle_link(),attachment.getAttachmentChild().getTitle()));
                    return;
                }/**其他类型文件*/
                else {
                rl_file.setVisibility(VISIBLE);
                showTitleIcon(attachment.getAttachmentChild().getTitle(), image);
                tv_file_title.setText(attachment.getAttachmentChild().getTitle());
                rl_file.setOnClickListener(view -> initFileClick(attachment.getAttachmentChild().getTitle_link(),
                        attachment.getAttachmentChild().getTitle(),msg.getTimestamp()));
                    return;
                }
            }
        }
    }
    /**
     * child图片或视频的点击事件
     * */
    private void initPhotoOrVideoClick(String attLink,String attachmentTitle){
        ArrayList<VideoBean> videoPicList = new ArrayList<>();
            if (FileUtils.isPhoto(attachmentTitle)) {
                VideoBean bean = new VideoBean(VideoBean.PICTURE, absoluteUrl.from(attLink));
                videoPicList.add(bean);
            }
            else  if (FileUtils.isVideo(attachmentTitle)){
                VideoBean bean = new VideoBean(VideoBean.VIDEO, absoluteUrl.from(attLink));
                videoPicList.add(bean);
                }

        Intent intent = new Intent(RocketChatApplication.getInstance(), PlayerViewActivity.class);
        intent.putParcelableArrayListExtra("video_url", videoPicList);
        intent.putExtra("selectPosition", videoPicList.size());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        RocketChatApplication.getInstance().startActivity(intent);
    }

    /**
     * 初始引用语音
     * */
    private void initAudio(String attachmentLink,String attachmentTitle, TextView child_tv_message_audio, LinearLayout ll_audio,long time) {
        String link=absolutize(attachmentLink);
        /**获取语音时长*/
        String currentFileName = DownloadUtil.get().getFileName(attachmentTitle, time);
        String appDownloadDir = FileUtils.getAppDownloadDir(RocketChatApplication.getInstance());
        File currentFile = new File(appDownloadDir + currentFileName);
        if (currentFile.exists()) {
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(currentFile.getPath());
                String s= mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int min = Integer.parseInt(s) / 1000 / 60;
                int sec = Integer.parseInt(s) / 1000 % 60;
                child_tv_message_audio.setText((min == 0 ? "" : min + "'") + (sec == 0 ? 1 : sec) + "''");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        new RxAsyncTask<String, Integer, String>() {
            @Override
            protected String call(String... strings) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                if (link != null) {
                    mmr.setDataSource(link, new HashMap<String, String>());
                }
                return mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            }

            @Override
            protected void onResult(String s) {
                super.onResult(s);
                int min = Integer.parseInt(s) / 1000 / 60;
                int sec = Integer.parseInt(s) / 1000 % 60;
                child_tv_message_audio.setText((min == 0 ? "" : min + "'") + (sec == 0 ? 1 : sec) + "''");
            }
        }.execute();
            DownloadUtil.get().download(link, appDownloadDir, currentFileName, null);

        }
        ll_audio.setOnClickListener(view -> {
            if (currentFile.exists()) {
                AudioHelper.getInstance().playSound(currentFile, audioImg, false);
            } else {
                DownloadUtil.get().download(absolutize(attachmentLink), appDownloadDir, currentFileName, null);
            }
        });
    }
    /**
     * 初始引用视频
     * */
    private void initVideo(JZVideoPlayerStandard mJcVideoPlayerStandard,String attachmentLink,String attachmentTitle){
        mJcVideoPlayerStandard.mRetryLayout.setVisibility(View.GONE);
        mJcVideoPlayerStandard.topContainer.setVisibility(View.GONE);
        mJcVideoPlayerStandard.startButton.setOnClickListener(view -> initPhotoOrVideoClick(attachmentLink,attachmentTitle));
        mJcVideoPlayerStandard.thumbImageView.setOnClickListener(view ->initPhotoOrVideoClick(attachmentLink,attachmentTitle) );
        String absFileName = FileUtils.getAppDownloadCacheDir(getContext()) + getFileName(attachmentTitle);
        File file = new File(absFileName);
        if (file.exists()) {
            mJcVideoPlayerStandard.thumbImageView.setImageBitmap(BitmapFactory.decodeFile(absFileName));
        } else {
            File dir = new File(FileUtils.getAppDownloadCacheDir(getContext()));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            new GetNetVideoBitmap(absolutize(attachmentLink),
                    mJcVideoPlayerStandard.thumbImageView, absFileName).execute();
        }
    }

    /**
     * 初始化引用文件点击事件
     * */
    private void initFileClick(String titleLink,String title,long timestamp){
        Intent intent = new Intent(RocketChatApplication.getInstance(), FileViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("attachmentTitle", title);
        intent.putExtra("attachmentLink", absoluteUrl.from(titleLink));
        intent.putExtra("timestamp", timestamp);
        RocketChatApplication.getInstance().startActivity(intent);    }
    /**
     * 嵌套引用
     * **/
    private void setChildView(LinearLayout ll_child_container, String msgText, AttachmentChild attachmentChild) {
        {
//            ll_child_container.setVisibility(VISIBLE);
            View child_inflater=LayoutInflater.from(getContext()).inflate(isMyself?R.layout.include_reply_box_right:R.layout.include_reply_box,ll_child_container,false);
            String msgId = MessageRefHelper.getInstance().getMsgId(msgText);
            Message msg=MessageRefHelper.getInstance().getMsg(messageRepository,msgId);
            TextView tv_symbol = child_inflater.findViewById(R.id.tv_symbol);
            ImageView iv_symbol = child_inflater.findViewById(R.id.iv_symbol);
            TextView tv_target_user = child_inflater.findViewById(R.id.tv_target_user);
            TextView tv_time = child_inflater.findViewById(R.id.tv_time);
            LinearLayout rl_file = child_inflater.findViewById(R.id.rl_files);
            TextView tv_file_title = child_inflater.findViewById(R.id.tv_file_title);
            ImageView image = child_inflater.findViewById(R.id.iv_file1);
            JZVideoPlayerStandard child_videoplayer=child_inflater.findViewById(R.id.child_videoplayer);
            LinearLayout child_rl_message_audio=child_inflater.findViewById(R.id.child_rl_message_audio);
            TextView child_tv_message_audio=child_inflater.findViewById(R.id.child_tv_massage_audio);
            audioImg=child_inflater.findViewById(R.id.child_iv_massage_audio);
            LinearLayout ll_child_container1=child_inflater.findViewById(R.id.child_container);
            TextView text = child_inflater.findViewById(R.id.tv_text);
            TextView tv_replay = child_inflater.findViewById(R.id.tv_replay);
            TextView tv_replay_child = child_inflater.findViewById(R.id.tv_replay_child);
            SimpleDraweeView image_reply = child_inflater.findViewById(R.id.image_reply);
            View line = child_inflater.findViewById(R.id.line);
            line.setVisibility(GONE);
            tv_replay_child.setVisibility(GONE);
            tv_replay.setVisibility(GONE);

            tv_symbol.setText(MessageRefHelper.getInstance().getSymbol(msgText));
            iv_symbol.setImageResource(MessageRefHelper.getInstance().getSymbolRes(msgText));
            if (msg==null){
                tv_target_user.setText(attachmentChild.getAuthorName());
                if(!TextUtils.isEmpty(attachmentChild.getTime()))
                    tv_time.setText(DateTime.fromEpocMs(Long.parseLong(attachmentChild.getTime()),DateTime.Format.DATE1));
                tv_replay_child.setVisibility(VISIBLE);
                try {
                    if (msgText.contains("@")&&msgText.contains("&")){
                        SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), tv_replay, msgText.split(msgId+"\\) ")[1].split(" +")[1]);
                        tv_replay.setText(spannableString);
                    }
                    else{
                        SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), tv_replay, msgText.split(msgId+"\\) ")[1]);
                        tv_replay.setText(spannableString);
                    }
                } catch (Exception e) {
                    tv_replay.setText("");
                }
                return;
            }
            tv_target_user.setText(MessageRefHelper.getInstance().getTagetUserName(msg));
            tv_time.setText(MessageRefHelper.getInstance().getTime(msg));
            if (!TextUtils.isEmpty(MessageRefHelper.getInstance().getReplayText(msgText,msg))){
                tv_replay_child.setVisibility(VISIBLE);
                SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), tv_replay, MessageRefHelper.getInstance().getReplayText(msgText,msg));
                tv_replay_child.setText(spannableString);
            }
            if (!TextUtils.isEmpty(msg.getMessage())) {
                if (msg.getMessage().contains(hostName)&&msg.getMessage().contains("?msg=")){
                    ll_child_container1.setVisibility(VISIBLE);
                    text.setVisibility(GONE);
                    setChildView(ll_child_container1,msg.getMessage(),attachmentChild.getAttachmentChild());
                }else {
                    text.setVisibility(VISIBLE);
                    SpannableString spannableString=SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), text, MessageRefHelper.getInstance().getMsgText(msg).toString());
                    text.setText(spannableString);
                    text.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            List<Attachment> attachments=MessageRefHelper.getInstance().getChildAttachment(msg);
            if (attachments!=null&&attachments.size()>0){
            Attachment attachment=attachments.get(0);
                /**图片*/
                if (!TextUtils.isEmpty(attachment.getImageUrl())) {
                    image_reply.setVisibility(VISIBLE);
                    if (isCached(Uri.parse(attachment.getImageUrl()))) {
                    }
                    FrescoHelper.INSTANCE.loadImageWithCustomization(image_reply, absolutize(attachment.getImageUrl()));
                    image_reply.setOnClickListener(view -> initPhotoOrVideoClick(attachment.getTitleLink(),attachment.getAttachmentTitle().getTitle()));
                }
            if (!TextUtils.isEmpty(attachment.getTitleLink())&&TextUtils.isEmpty(attachment.getImageUrl())) {
                /**音频*/
                if (FileUtils.isAudio(attachment.getTitleLink())){
                    child_rl_message_audio.setVisibility(VISIBLE);
                    initAudio(attachment.getTitleLink(),attachment.getAttachmentTitle().getTitle(),child_tv_message_audio,child_rl_message_audio,msg.getTimestamp());
                }/**视频*/
                else if (FileUtils.isVideo(attachment.getTitleLink())){
                    child_videoplayer.setVisibility(VISIBLE);
                    initVideo(child_videoplayer,attachment.getTitleLink(),attachment.getAttachmentTitle().getTitle());
                    child_videoplayer.setOnClickListener(view -> initPhotoOrVideoClick(attachment.getTitleLink(),attachment.getAttachmentTitle().getTitle()));
                }/**其他类型文件*/
                else {
                    rl_file.setVisibility(VISIBLE);
                    showTitleIcon(attachment.getAttachmentTitle().getTitle(), image);
                    tv_file_title.setText(attachment.getAttachmentTitle().getTitle());
                    rl_file.setOnClickListener(view -> initFileClick(attachment.getTitleLink(),
                            attachment.getAttachmentTitle().getTitle(),msg.getTimestamp()));
                }
            }
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            child_inflater.setLayoutParams(params);
            ll_child_container.addView(child_inflater);
        }
    }

    private void showVideoAttachment(Attachment attachment, View attachmentView) {
        final JZVideoPlayerStandard mJcVideoPlayerStandard = attachmentView.findViewById(R.id.videoplayer);
        final String videoUrl = attachment.getVideoUrl();
        if (!FileUtils.isVideo(videoUrl)) {
            mJcVideoPlayerStandard.setVisibility(GONE);
            return;
        }
        mJcVideoPlayerStandard.setVisibility(VISIBLE);
        mJcVideoPlayerStandard.mRetryLayout.setVisibility(View.GONE);
        mJcVideoPlayerStandard.topContainer.setVisibility(View.GONE);
//        mJcVideoPlayerStandard.setUp(absolutize(videoUrl), JZVideoPlayer.SCREEN_WINDOW_LIST, "");

        mJcVideoPlayerStandard.startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(view);
                }
            }
        });
        mJcVideoPlayerStandard.thumbImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(view);
                }
            }
        });

        String title = attachment.getAttachmentTitle().getTitle();

        String absFileName = FileUtils.getAppDownloadCacheDir(getContext()) + getFileName(title);
        File file = new File(absFileName);
        if (file.exists()) {
            mJcVideoPlayerStandard.thumbImageView.setImageBitmap(BitmapFactory.decodeFile(absFileName));
        } else {
            File dir = new File(FileUtils.getAppDownloadCacheDir(getContext()));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            new GetNetVideoBitmap(absolutize(videoUrl), mJcVideoPlayerStandard.thumbImageView, absFileName).execute();
        }
    }



    private String getFileName(String title) {
        if (!TextUtils.isEmpty(title) && title.contains(".")) {
            return title.split("\\.")[0] + ".jpg";
        } else {
            return title + ".jpg";
        }
    }

    private void showImageAttachment(Attachment attachment, View attachmentView, boolean autoloadImages) {
//        final View imageContainer = attachmentView.findViewById(R.id.image_container);
        final ImageView attachedImage = attachmentView.findViewById(R.id.image);
        if (attachment.getImageUrl() == null) {
            attachedImage.setVisibility(GONE);
            return;
        }

        attachedImage.setVisibility(VISIBLE);

//        final View load = attachmentView.findViewById(R.id.image_load);

        // Fix for https://fabric.io/rocketchat3/android/apps/chat.rocket.android/issues/59982403be077a4dcc4d7dc3/sessions/599F217000CF00015C771EEF2021AA0F_f9320e3f88fd11e7935256847afe9799_0_v2?
        // From: https://github.com/facebook/fresco/issues/1176#issuecomment-216830098
        // android.support.v4.content.ContextCompat creates your vector drawable
//        Drawable placeholderDrawable = ContextCompat.getDrawable(getContext(), R.drawable.image_dummy);

        // Set the placeholder image to the placeholder vector drawable
//        attachedImage.setHierarchy(
//                GenericDraweeHierarchyBuilder.newInstance(getResources())
//                        .setPlaceholderImage(placeholderDrawable)
//                        .build());

        loadImage(attachment.getImageUrl(), attachedImage, autoloadImages);
    }

    private void showFieldsAttachment(Attachment attachment, View attachmentView) {
        List<AttachmentField> fields = attachment.getAttachmentFields();
        if (fields == null || fields.size() == 0) {
            return;
        }

        Log.i("mydebug", "showFieldsAttachment run");
        final ViewGroup attachmentContent = attachmentView.findViewById(R.id.attachment_content);

        for (int i = 0, size = fields.size(); i < size; i++) {
            final AttachmentField attachmentField = fields.get(i);
            if (attachmentField.getTitle() == null
                    || attachmentField.getText() == null) {
                return;
            }
            MessageAttachmentFieldLayout fieldLayout = new MessageAttachmentFieldLayout(getContext());
            fieldLayout.setTitle(attachmentField.getTitle());
            fieldLayout.setValue(attachmentField.getText());

            attachmentContent.addView(fieldLayout);
        }
    }

    private String absolutize(String url) {
        if (absoluteUrl == null) {
            return url;
        }
        return absoluteUrl.from(url);
    }

    private void loadImage(final String url, final ImageView drawee, boolean autoloadImage) {
        drawee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(view);
                }
            }
        });
        if (isCached(Uri.parse(url))) {
            return;
        }
        RequestOptions error = new RequestOptions().placeholder(R.drawable.send_message_default)
                .error(R.drawable.send_message_default);
        Glide.with(getContext()).load(absolutize(url)).apply(error).into(drawee);
//        FrescoHelper.INSTANCE.loadImageWithCustomization(drawee, absolutize(url));
//        if (autoloadImage || isCached(Uri.parse(url))) {
//            load.setVisibility(GONE);
//        }

//        load.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                load.setVisibility(GONE);
//                load.setOnClickListener(null);
//                FrescoHelper.INSTANCE.loadImageWithCustomization(drawee, url);
//            }
//        });
    }

    private void showTitleIcon(String title, ImageView view) {
        if (FileUtils.isWord(title)) {
            view.setImageResource(R.drawable.send_word);
        } else if (FileUtils.isExcel(title)) {
            view.setImageResource(R.drawable.send_excel);
        } else if (FileUtils.isPPT(title)) {
            view.setImageResource(R.drawable.send_ppt);
        } else if (FileUtils.isMp3(title)) {
            view.setImageResource(R.drawable.send_mp3);
        } else if (FileUtils.isTxt(title)) {
            view.setImageResource(R.drawable.send_txt);
        } else if (FileUtils.isZip(title)) {
            view.setImageResource(R.drawable.send_zip);
        } else if (FileUtils.isPDF(title)) {
            view.setImageResource(R.drawable.send_pdf);
        }
    }

    private boolean isCached(Uri loadUri) {
        if (loadUri == null) {
            return false;
        }
        ImageRequest imageRequest = ImageRequest.fromUri(loadUri);
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                .getEncodedCacheKey(imageRequest, null);
        return ImagePipelineFactory.getInstance()
                .getMainFileCache().hasKey(cacheKey);
    }

    public interface OnPhotoClickListener {
        void onItemClick(View v);
    }

    public interface OnAudioClickListener {
        void onItemClick(View v);
    }

    public ImageView getAudioImage() {
        return audioImg;
    }

    float xDown = 0f, yDown = 0f, xUp;
    boolean isLongClickModule = false;
    boolean isLongClicking = false;

    private boolean isLongPressed(float lastX, float lastY,
                                  float thisX, float thisY,
                                  long lastDownTime, long thisEventTime,
                                  long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= 20 && offsetY <= 20 && intervalTime >= longPressTime) {
            return true;
        }
        return false;
    }

    boolean intercept = false;

    /**
     * 重写该方法是为item添加长按事件
     * */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            xDown = event.getX();
            yDown = event.getY();

        } else if (event.getAction() == MotionEvent.ACTION_UP) {// 松开处理
            //获取松开时的x坐标
            if (isLongClickModule) {
                isLongClickModule = false;
                isLongClicking = false;
            }
            xUp = event.getX();

            //按下和松开绝对值差当大于20时滑动，否则不显示
            if ((xUp - xDown) > 20) {
                //添加要处理的内容
            } else if ((xUp - xDown) < -20) {
                //添加要处理的内容
            } else if (0 == (xDown - xUp)) {
                /**处理点击事件*/
                intercept = false;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            //当滑动时背景为选中状态 //检测是否长按,在非长按时检测
            if (!isLongClickModule) {
                isLongClickModule = isLongPressed(xDown, yDown, event.getX(),
                        event.getY(), event.getDownTime(), event.getEventTime(), 300);
            }
            if (isLongClickModule && !isLongClicking) {
                //处理长按事件
                isLongClicking = true;
                intercept = true;
                BaseEvent baseEvent = new BaseEvent();
                baseEvent.setCode(EventTags.SET_LONG_CLICK);
                baseEvent.setTarget(message);
                EventBus.getDefault().post(baseEvent);
            }
        } else {
            //其他模式
        }

        return intercept;
    }

}
