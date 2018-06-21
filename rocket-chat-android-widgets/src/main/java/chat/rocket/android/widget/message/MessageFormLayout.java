package chat.rocket.android.widget.message;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zhy.autolayout.AutoLinearLayout;

import java.util.List;

import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.AudioRecorderButton;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.emotionkeyboard.utils.EmotionUtils;
import chat.rocket.android.widget.emotionkeyboard.utils.SpanStringUtils;
import chat.rocket.android.widget.helper.DebouncingOnClickListener;
import chat.rocket.android.widget.helper.FrescoHelper;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.AttachmentTitle;
import chat.rocket.core.models.Mention;
import chat.rocket.core.models.Message;

    public class MessageFormLayout extends AutoLinearLayout {

    protected ViewGroup composer;

    //  private ImageButton attachButton;
    private ImageButton sendButton;
    private ImageView iv_biaoqing, iv_file,iv_gallery,iv_camera, iv_audio, iv_video, iv_alert;
    private ViewPager viewPager;

    private RelativeLayout replyBar;
    private ImageView replyCancelButton;
    private SimpleDraweeView replyThumb;
    private TextView replyMessageText;
    private TextView replyUsernameText;
    private ToggleButton voiceSwitch;
    private Button recordButton;

    //  private ExtraActionSelectionClickListener extraActionSelectionClickListener;
    private SubmitTextListener submitTextListener;
    private AudioChange audioChange;
    private ShowSoftWindow showSoftwindow;
    private ShowFileWindow showFileWindow;
    private ShowAudioWindow showAudioWindow;
    private ShowVideoWindow showVideoWindow;
    private ShowCameraWindow showCameraWindow;
    private ShowGalleryWindow showGalleryWindow;
    private ShowSelectActivity showSelectActivity;
    private ShowAlertSelect showAlertSelect;
    private AudioFinish audioFinish;
    private ImageKeyboardEditText editText;
    private View view;
    private ImageKeyboardEditText.OnCommitContentListener listener;
    private final int TYPE_TEXT_INPUT = 1;//显示文本
    private final int TYPE_VOICE_INPUT = 2;//显示语音
    private AudioRecorderButton mAudioRecorderButton;

    private LinearLayout ll_opreation,keyboard_container,ll_edit;
    private RelativeLayout rl_composer_muted;
    private TextView composer_muted;

    public MessageFormLayout(Context context) {
        super(context);
        init();
    }

    public MessageFormLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

//  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//  public MessageFormLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//    super(context, attrs, defStyleAttr, defStyleRes);
//    init();
//  }

    private void init() {
        composer = (ViewGroup) LayoutInflater.from(getContext())
                .inflate(R.layout.message_composer, this, false);

//    attachButton = composer.findViewById(R.id.button_attach);
        voiceSwitch = composer.findViewById(R.id.image_voice);
        recordButton = composer.findViewById(R.id.btn_speak);
        replyCancelButton = composer.findViewById(R.id.reply_cancel);
        replyMessageText = composer.findViewById(R.id.reply_message);
        replyUsernameText = composer.findViewById(R.id.reply_username);
        replyThumb = composer.findViewById(R.id.reply_thumb);
        replyBar = composer.findViewById(R.id.reply_bar);
        sendButton = composer.findViewById(R.id.button_send);
        iv_biaoqing = composer.findViewById(R.id.iv_biaoqing);
        iv_file = composer.findViewById(R.id.iv_file);
        iv_gallery = composer.findViewById(R.id.iv_gallery);
        iv_camera= composer.findViewById(R.id.iv_camera);
        iv_audio = composer.findViewById(R.id.iv_audio);
        iv_video = composer.findViewById(R.id.iv_video);
        iv_alert = composer.findViewById(R.id.iv_alert);
        viewPager = composer.findViewById(R.id.emoticonPanel);
        viewPager.requestDisallowInterceptTouchEvent(true);
        editText = composer.findViewById(R.id.editor);
        view = composer.findViewById(R.id.view);
        mAudioRecorderButton = composer.findViewById(R.id.btn_speak);
        ll_opreation=composer.findViewById(R.id.ll_opreation);
        keyboard_container=composer.findViewById(R.id.keyboard_container);
        rl_composer_muted=composer.findViewById(R.id.rl_composer_muted);
        composer_muted=composer.findViewById(R.id.composer_muted);
        ll_edit=composer.findViewById(R.id.ll_edit);

        mAudioRecorderButton.setAudioFinishRecorderListener(new AudioRecorderButton.AudioFinishRecorderListener() {
            @Override
            public void onFinish(float seconds, String filePath) {
                if (audioFinish != null) {
                    audioFinish.onAudioFinish(filePath);
                }
            }
        });

        iv_biaoqing.setVisibility(VISIBLE);// TODO 表情功能注释
//    attachButton.setOnClickListener(new DebouncingOnClickListener() {
//      @Override
//      public void doClick(View view) {
//        onExtraActionSelectionClick();
//      }
//    });
        voiceSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioChange!=null){
                    audioChange.onAudioChange();
                }
            }
        });
        voiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            // checked为true 文本输入 为false语音输入
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean checked) {

            }
        });

        sendButton.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                String messageText = getText();
                if (messageText.length() > 0 && submitTextListener != null) {
                    submitTextListener.onSubmitText(messageText);
                    clearReplyContent();
                }
            }
        });
        iv_biaoqing.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View view) {
                hideKeyboard();
                if (showSoftwindow != null) {
                    if(viewPager.isShown()){
                        hideEmotionLayout(true);
                    }else {
                        editText.clearFocus();
                        viewPager.setVisibility(View.VISIBLE);
                    }
                    showSoftwindow.onShowSoftwindow(viewPager, editText);
                }
            }
        });
        iv_file.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View view) {
                hideKeyboard();
                if (showSoftwindow != null) {
                    showFileWindow.onShowFileWindow(1);
                }
            }
        });
        iv_audio.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View view) {
                hideKeyboard();
                if (showSoftwindow != null) {
                    showAudioWindow.onShowAudioWindow(2);
                }
            }
        });
        iv_video.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View view) {
                hideKeyboard();
                if (showSoftwindow != null) {
                    showVideoWindow.onShowVideoWindow(3);
                }
            }
        });
        iv_camera.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View view) {
                hideKeyboard();
                if (showSoftwindow != null) {
                    showCameraWindow.onShowCameraWindow(4);
                }
            }
        });
        iv_gallery.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View view) {
                hideKeyboard();
                if (showSoftwindow != null) {
                    showGalleryWindow.onShowGalleryWindow(5);
                }
            }
        });
        iv_alert.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                hideKeyboard();
                if (showAlertSelect != null) {
                    showAlertSelect.onShowAlertSelect();
                }
            }
        });
//    sendButton.setScaleX(0);
//    sendButton.setScaleY(0);
//    sendButton.setVisibility(GONE);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if ((count == 1 && s.toString().endsWith("@") && s.length() == 1)
                        || (count == 1 && s.toString().endsWith("@") && s.toString().substring(s.length() - 2, s.length() - 1).matches("[^0-9a-zA-Z]"))) {
                    if (showSelectActivity != null) {
                        showSelectActivity.onShowSelectActivity();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setVisibility(GONE);
            }
        });
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    viewPager.setVisibility(GONE);
                }
            }
        });

        editText.setContentListener(new ImageKeyboardEditText.OnCommitContentListener() {
            @Override
            public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags,
                                           Bundle opts, String[] supportedMimeTypes) {
                if (listener != null) {
                    return listener.onCommitContent(inputContentInfo, flags, opts, supportedMimeTypes);
                }
                return false;
            }
        });

        addView(composer);
    }

        /**
         * 隐藏表情布局
         * @param showSoftInput 是否显示软件盘
         */
        public void hideEmotionLayout(boolean showSoftInput) {
            if (viewPager.isShown()) {
                viewPager.setVisibility(View.GONE);
            }
        }
        public void setToggle(){
            voiceSwitch.setChecked(!voiceSwitch.isChecked());
        }
    public void changeRecordStatus() {
        if (!voiceSwitch.isChecked()) {// 语音
            switchBottomPanel(TYPE_VOICE_INPUT);
        } else {
            switchBottomPanel(TYPE_TEXT_INPUT);
        }

    }

    private void switchBottomPanel(int type) {
        if (type == TYPE_TEXT_INPUT) {
            requestFocusAndShowKeyboard();
            recordButton.setVisibility(View.GONE);
            ll_edit.setVisibility(View.VISIBLE);

        } else if (type == TYPE_VOICE_INPUT) {
            hideKeyboard();
            hideEmotionLayout(true);
            recordButton.setVisibility(View.VISIBLE);
            ll_edit.setVisibility(View.GONE);
        }
    }

    public void clearReplyContent() {
        replyBar.setVisibility(View.GONE);
        replyThumb.setVisibility(View.GONE);
        replyMessageText.setText("");
        replyUsernameText.setText("");
    }

    public void showReplyThumb() {
        replyThumb.setVisibility(View.VISIBLE);
    }

    public void setReplyCancelListener(OnClickListener onClickListener) {
        replyCancelButton.setOnClickListener(onClickListener);
    }

    public boolean getAudioRecorderButtonDown() {
        return mAudioRecorderButton.isDown;
    }

    public void setPrepareAudio() {
        mAudioRecorderButton.setPrepareAudio();
    }

    public EditText getEditText() {
        return (EditText) composer.findViewById(R.id.editor);
    }

//  public void setExtraActionSelectionClickListener(
//      ExtraActionSelectionClickListener extraActionSelectionClickListener) {
//    this.extraActionSelectionClickListener = extraActionSelectionClickListener;
//  }

    /**
     * 禁言时，显示遮罩盖住输入框，不可输入
     * @param isShow
     */
    public void setVisibility(boolean isShow,int isMeetingFinishOrPause) {
        if(isShow){
            ll_opreation.setVisibility(View.GONE);
            keyboard_container.setVisibility(View.GONE);
            rl_composer_muted.setVisibility(View.VISIBLE);
//            view.setVisibility(View.VISIBLE);
            if(isMeetingFinishOrPause==3)
                composer_muted.setText(R.string.set_mute_status);
            else if (isMeetingFinishOrPause==2)
                composer_muted.setText(R.string.set_finish_status);
            else if (isMeetingFinishOrPause==1)
                composer_muted.setText(R.string.set_pause_status);
            else if (isMeetingFinishOrPause==4)
                composer_muted.setText(R.string.set_you_muted);
            else
                composer_muted.setText(R.string.set_lock_status);
//            editText.setGravity(Gravity.CENTER_HORIZONTAL);
//            editText.clearFocus();
        }else{
            ll_opreation.setVisibility(View.VISIBLE);
            keyboard_container.setVisibility(View.VISIBLE);
            rl_composer_muted.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
            editText.setHint("");
            editText.setGravity(Gravity.CENTER_VERTICAL);
        }
    }



    public void setSubmitTextListener(SubmitTextListener submitTextListener) {
        this.submitTextListener = submitTextListener;
    }

    public void setShowSoftwindowListener(ShowSoftWindow showSoftwindow) {
        this.showSoftwindow = showSoftwindow;
    }

    public void setShowFileWindowListener(ShowFileWindow showFileWindow) {
        this.showFileWindow = showFileWindow;
    }

    public void setShowAudioWindowListener(ShowAudioWindow showAudioWindow) {
        this.showAudioWindow = showAudioWindow;
    }

    public void setShowVideoWindowListener(ShowVideoWindow showVideoWindow) {
        this.showVideoWindow = showVideoWindow;
    }

    public void setShowCameraWindowListener(ShowCameraWindow showCameraWindow) {
        this.showCameraWindow = showCameraWindow;
    }

    public void setShowGalleryWindowListener(ShowGalleryWindow showGalleryWindow) {
        this.showGalleryWindow = showGalleryWindow;
    }

    public void setShowSelectedActivityListener(ShowSelectActivity showSelectActivity) {
        this.showSelectActivity = showSelectActivity;
    }

    public void setShowAlertSelectListener(ShowAlertSelect showAlertSelect) {
        this.showAlertSelect = showAlertSelect;
    }

    public void setAudioFinishListener(AudioFinish audioFinish) {
        this.audioFinish = audioFinish;
    }

    public void setAudioListener(AudioChange audioChange) {
        this.audioChange = audioChange;
    }
//  private void onExtraActionSelectionClick() {
//    if (extraActionSelectionClickListener != null) {
//      extraActionSelectionClickListener.onClick();
//    }
//  }

    private EditText getEditor() {
        return (EditText) composer.findViewById(R.id.editor);
    }

    public final String getText() {
        return getEditor().getText().toString();
    }

    public final void setTextUserName(final CharSequence text) {
        final EditText editor = getEditor();
        final String s = editor.getText().toString();
        editor.post(new Runnable() {
            @Override
            public void run() {
                editor.setText(s + text);
                if (text.length() > 0) {
                    editor.setSelection(s.length() + text.length());

                    requestFocusAndShowKeyboard();
                }
            }
        });
    }

    public final void setText(final CharSequence text) {
        final EditText editor = getEditor();
        editor.post(new Runnable() {
            @Override
            public void run() {
                editor.setText(text);
                if (text.length() > 0) {
                    editor.setSelection(text.length());

                    requestFocusAndShowKeyboard();
                }
            }
        });
    }

    public void setEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
//    attachButton.setEnabled(enabled);
    }
    public void setFocusable(){
        if(editText!=null){
            editText.clearFocus();
//            editText.setFocusable(false);
        }
    }
    public void setPermissionToVerifyListener(AudioRecorderButton.PermissionToVerifyListener listener) {
        mAudioRecorderButton.setPermissionToVerifyListener(listener);
    }


    public void setEditTextCommitContentListener(
            ImageKeyboardEditText.OnCommitContentListener listener) {
        this.listener = listener;
    }

    public void setReplyContent(@NonNull AbsoluteUrl absoluteUrl, @NonNull Message message, String refText) {
        String text = message.getMessage();
        replyUsernameText.setText(message.getUser().getRealName());
        if (!TextUtils.isEmpty(text)) {
            List<Mention> mentions=message.getMentions();
            if (refText!=null&&!refText.equals("")){
                text=refText;
            }else if(mentions!=null&&mentions.size()>0){
                for (Mention mention:mentions){
                    int lastIndexOf=mention.getUsername().lastIndexOf("&");
                    if (lastIndexOf!=-1){
                        String name = mention.getUsername().substring(0, lastIndexOf);
                        text=text.replace(mention.getUsername(),name);
                    }
                }
            }
            SpannableString spannableString= SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,getContext(), replyMessageText, text);
            replyMessageText.setText(spannableString);
        } else {
            if (message.getAttachments() != null && message.getAttachments().size() > 0) {
                Attachment attachment = message.getAttachments().get(0);
                AttachmentTitle attachmentTitle = attachment.getAttachmentTitle();
                String imageUrl = null;
                if (attachment.getImageUrl() != null) {
                    imageUrl = absoluteUrl.from(attachment.getImageUrl());
                }
                if (attachmentTitle != null) {
                    text = attachmentTitle.getTitle();
                }
                if (TextUtils.isEmpty(text)) {
                    text = "Unknown";
                }
                if (imageUrl != null) {
                    FrescoHelper.INSTANCE.loadImageWithCustomization(replyThumb, imageUrl);
                    showReplyThumb();
                }
                replyMessageText.setText(text);
            }
        }
        replyBar.setVisibility(View.VISIBLE);
        requestFocusAndShowKeyboard();
    }

    public void hideKeyboard() {
        final EditText editor = getEditor();
        editor.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) editor.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(editor.getWindowToken(), 0);
            }
        });
    }

    private void requestFocusAndShowKeyboard() {
        final EditText editor = getEditor();
        editor.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) editor.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                editor.requestFocus();
                inputMethodManager.showSoftInput(editor, 0);
            }
        });
    }

    private void animateHide(final View view) {
        view.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(GONE);
            }
        });
    }

    private void animateShow(final View view) {
        view.animate().scaleX(1).scaleY(1).setDuration(150).withStartAction(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(VISIBLE);
            }
        });
    }

//  public interface ExtraActionSelectionClickListener {
//    void onClick();
//  }

    public interface SubmitTextListener {
        void onSubmitText(String message);
    }

    public interface ShowSoftWindow {
        void onShowSoftwindow(ViewPager message, ImageKeyboardEditText editText);
    }

    public interface ShowFileWindow {
        void onShowFileWindow(int position);
    }

    public interface ShowAudioWindow {
        void onShowAudioWindow(int position);
    }

    public interface ShowVideoWindow {
        void onShowVideoWindow(int position);
    }

    public interface ShowCameraWindow {
        void onShowCameraWindow(int position);
    }

    public interface ShowGalleryWindow {
        void onShowGalleryWindow(int position);
    }

    public interface ShowSelectActivity {
        void onShowSelectActivity();
    }

    public interface ShowAlertSelect {
        void onShowAlertSelect();
    }

    public interface AudioFinish {
        void onAudioFinish(String filePath);
    }

    public interface AudioChange {
        void  onAudioChange();
    }
}
