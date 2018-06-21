package chat.rocket.android.widget.message;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.zhy.autolayout.AutoLinearLayout;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.helper.FileUtils;
import chat.rocket.android.widget.helper.FrescoHelper;
import chat.rocket.android.widget.helper.GetNetVideoBitmap;
import chat.rocket.android.widget.helper.RxAsyncTask;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.AttachmentAuthor;
import chat.rocket.core.models.AttachmentField;
import chat.rocket.core.models.AttachmentTitle;
import cn.jzvd.JZVideoPlayerStandard;

/**
 */
public class RocketChatMessageAttachmentsLayout extends AutoLinearLayout {
    private LayoutInflater inflater;
    private AbsoluteUrl absoluteUrl;
    private List<Attachment> attachments;
    private OnPhotoClickListener listener;
    private OnAudioClickListener audioListener;
    private boolean isMyself;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RocketChatMessageAttachmentsLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                              int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

    public void setAttachments(List<Attachment> attachments, boolean autoloadImages) {
        if (this.attachments != null && this.attachments.equals(attachments)) {
            return;
        }
        this.attachments = attachments;

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

    ImageView audioImg;

    private void showTitleAttachment(Attachment attachment, View attachmentView) {
        View rl_audio = attachmentView.findViewById(R.id.rl_message_audio);
        final TextView audioView = (TextView) attachmentView.findViewById(R.id.tv_massage_audio);
        View rl_title = attachmentView.findViewById(R.id.rl_title);
        TextView titleView = attachmentView.findViewById(R.id.title);
        audioImg = (ImageView) attachmentView.findViewById(R.id.iv_massage_audio);
        final AttachmentTitle title = attachment.getAttachmentTitle();
        if(title == null || title.getTitle() == null || title.getLink() == null) {
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
                    int min=Integer.parseInt(s) / 1000/60;
                    int sec=Integer.parseInt(s) / 1000%60;
                    audioView.setText((min==0?"":min+"'")+(sec ==0?1:sec)+ "''");
                }
            }.execute();

        }
        else {// 其他格式的文件
            rl_audio.setVisibility(GONE);
            titleView.setVisibility(View.VISIBLE);
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
     * */
    private void showReferenceAttachment(Attachment attachment, View attachmentView) {
//        final View refBox = attachmentView.findViewById(R.id.ref_box);
//        if (attachment.getThumbUrl() == null && attachment.getText() == null) {
//            refBox.setVisibility(GONE);
//            return;
//        }
//
//        Log.i("mydebug", "showReferenceAttachment run");
//        refBox.setVisibility(VISIBLE);
//
//        final SimpleDraweeView thumbImage = refBox.findViewById(R.id.thumb);
//
//        final String thumbUrl = attachment.getThumbUrl();
//        if (TextUtils.isEmpty(thumbUrl)) {
//            thumbImage.setVisibility(GONE);
//        } else {
//            thumbImage.setVisibility(VISIBLE);
//            FrescoHelper.INSTANCE.loadImageWithCustomization(thumbImage, absolutize(thumbUrl));
//        }
//
//        final TextView refText = refBox.findViewById(R.id.text);
//
//        final String refString = attachment.getText();
//        if (TextUtils.isEmpty(refString)) {
//            refText.setVisibility(GONE);
//        } else {
//            refText.setVisibility(VISIBLE);
//            refText.setText(refString);
//        }
        if(attachment.getMessageLink()!=null&&!attachment.getMessageLink().equals("")){
            String msgId=attachment.getMessageLink().split("msg=")[1];
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
        final SimpleDraweeView attachedImage = attachmentView.findViewById(R.id.image);
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

    private void loadImage(final String url, final SimpleDraweeView drawee, boolean autoloadImage) {
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
        FrescoHelper.INSTANCE.loadImageWithCustomization(drawee, absolutize(url));
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
        if(FileUtils.isWord(title)){
            view.setImageResource(R.drawable.send_word);
        }else if(FileUtils.isExcel(title)){
            view.setImageResource(R.drawable.send_excel);
        }else if(FileUtils.isPPT(title)){
            view.setImageResource(R.drawable.send_ppt);
        }else if(FileUtils.isMp3(title)){
            view.setImageResource(R.drawable.send_mp3);
        }else if(FileUtils.isTxt(title)){
            view.setImageResource(R.drawable.send_txt);
        }else if(FileUtils.isZip(title)){
            view.setImageResource(R.drawable.send_zip);
        }else if(FileUtils.isPDF(title)){
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

    public ImageView getAudioImage(){
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
        if (offsetX <= 10 && offsetY <= 10 && intervalTime >= longPressTime) {
            return true;
        }
        return false;
    }
    boolean intercept=false;
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
                intercept=false;
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
            }
        } else {
            //其他模式
        }

        return intercept;
    }

}
