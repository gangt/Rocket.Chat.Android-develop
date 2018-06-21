package chat.rocket.android.layouthelper.chatroom

import android.support.v4.view.ViewPager
import android.text.TextUtils
import chat.rocket.android.RocketChatCache
import chat.rocket.android.helper.MessageRefHelper
import chat.rocket.android.widget.AbsoluteUrl
import chat.rocket.android.widget.message.ImageKeyboardEditText
import chat.rocket.android.widget.message.MessageFormLayout
import chat.rocket.core.models.Message
import chat.rocket.persistence.realm.repositories.RealmMessageRepository

class MessageFormManager(private val messageFormLayout: MessageFormLayout/*, val callback: MessageFormLayout.ExtraActionSelectionClickListener*/) {
    private var sendMessageCallback: SendMessageCallback? = null
    private var showBiaoqingCallback: ShowBiaoqingCallback? = null
    private var showFileCallback: ShowFileCallback? = null
    private var showAudioCallback: ShowAudioCallback? = null
    private var showVideoCallback: ShowVideoCallback? = null
    private var showCameraCallback: ShowCameraCallback? = null
    private var showGalleryCallback: ShowGalleryCallback? = null
    private var replyMarkDown: String = ""

    init {
//        messageFormLayout.setExtraActionSelectionClickListener(callback)
        messageFormLayout.setSubmitTextListener(this::sendMessage)
        messageFormLayout.setShowSoftwindowListener(this::showBiaoqingwindow)
        messageFormLayout.setShowFileWindowListener (this::showFileWindow)
        messageFormLayout.setShowAudioWindowListener(this::showAudioWindow)
        messageFormLayout.setShowVideoWindowListener (this::showVideoWindow)
        messageFormLayout.setShowCameraWindowListener (this::showCameraWindow)
        messageFormLayout.setShowGalleryWindowListener (this::showCameraWindow)
    }

    fun setSendMessageCallback(sendMessageCallback: SendMessageCallback) {
        this.sendMessageCallback = sendMessageCallback
    }

    fun setShowBiaoqingCallback(showBiaoqingCallback: ShowBiaoqingCallback) {
        this.showBiaoqingCallback = showBiaoqingCallback
    }
    fun setShowFileCallback(showFileCallback: ShowFileCallback) {
        this.showFileCallback = showFileCallback
    }
    fun setShowVideoCallback(showVideoCallback: ShowVideoCallback) {
        this.showVideoCallback = showVideoCallback
    }
    fun setShowCameraCallback(showCameraCallback: ShowCameraCallback) {
        this.showCameraCallback = showCameraCallback
    }
    fun setShowGalleryCallback(showGalleryCallback: ShowGalleryCallback) {
        this.showGalleryCallback = showGalleryCallback
    }
    fun setShowAudioCallback(showAudioCallback: ShowAudioCallback) {
        this.showAudioCallback = showAudioCallback
    }

    fun onMessageSend() {
        clearComposingText()
    }

    fun setEditMessage(message: String) {
        clearComposingText()
        messageFormLayout.setText(message)
    }

    fun clearComposingText() {
        messageFormLayout.setText("")
    }

    fun enableComposingText(enable: Boolean) {
        messageFormLayout.isEnabled = enable
    }

    fun setVisibility (isShow: Boolean,isMeetingFinishOrPause :Int) {
        messageFormLayout.setVisibility(isShow,isMeetingFinishOrPause)
    }

    fun setReply(absoluteUrl: AbsoluteUrl, replyMarkDown: String, message: Message) {
        this.replyMarkDown = replyMarkDown
        var refText:String?=""
         if (!TextUtils.isEmpty(message.message) && message.message.contains(RocketChatCache.getSelectedServerHostname()+"") && message.getMessage().contains("?msg=")) {
             val messageRepository = RealmMessageRepository(RocketChatCache.getSelectedServerHostname())
            val msgId = MessageRefHelper.getInstance().getMsgId(message.getMessage())
            val msg1 = MessageRefHelper.getInstance().getMsg(messageRepository, msgId)
             if (msg1==null)
                 if (message.message.contains("@") && message.message.contains("&"))
                     refText = message.message.split((msgId + "\\) ").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1].split(" +".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]//@的方法
                 else
                     refText = message.message.split((msgId + "\\) ").toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]//@的方法
            else
            refText= MessageRefHelper.getInstance().getReplayText(message.getMessage(), msg1)
        }
        messageFormLayout.setReplyContent(absoluteUrl, message,refText)
        messageFormLayout.setReplyCancelListener({
            this.replyMarkDown = ""
            messageFormLayout.clearReplyContent()
            messageFormLayout.hideKeyboard()
        })
    }

    private fun sendMessage(message: String) {
        val finalMessage = if (replyMarkDown.trim().isNotEmpty()) "$replyMarkDown $message" else message
        replyMarkDown = ""
        if(finalMessage.trim().isNotEmpty())
        sendMessageCallback?.onSubmitText(finalMessage)
    }

    private fun showBiaoqingwindow(viewpager: ViewPager, editText: ImageKeyboardEditText){
        showBiaoqingCallback?.onShowBiaoqing(viewpager, editText)
    }
    private fun showFileWindow(position: Int){
        showFileCallback?.onShowFile(position)
    }
    private fun showAudioWindow(position: Int){
        showAudioCallback?.onShowAudio(position)
    }
    private fun showVideoWindow(position: Int){
        showVideoCallback?.onShowVideo(position)
    }
    private fun showCameraWindow(position: Int){
        showCameraCallback?.onShowCamera(position)
    }

    interface SendMessageCallback {
        fun onSubmitText(messageText: String)
    }

    interface ShowBiaoqingCallback {
        fun onShowBiaoqing(viewpager: ViewPager, editText: ImageKeyboardEditText)
    }
    interface ShowFileCallback {
        fun onShowFile(position: Int)
    }
    interface ShowAudioCallback {
        fun onShowAudio(position: Int)
    }
    interface ShowVideoCallback {
        fun onShowVideo(position: Int)
    }
    interface ShowCameraCallback {
        fun onShowCamera(position: Int)
    }
    interface ShowGalleryCallback {
        fun onShowGallery(position: Int)
    }
}