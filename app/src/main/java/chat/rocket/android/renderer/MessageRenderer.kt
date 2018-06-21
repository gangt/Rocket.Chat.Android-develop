package chat.rocket.android.renderer

import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.*
import chat.rocket.android.activity.business.MyInfoActivity
import chat.rocket.android.fragment.chatroom.RoomFragment
import chat.rocket.android.helper.DateTime
import chat.rocket.android.helper.TextUtils
import chat.rocket.android.layouthelper.chatroom.MessageListAdapter
import chat.rocket.android.layouthelper.chatroom.PairedMessage
import chat.rocket.android.widget.AbsoluteUrl
import chat.rocket.android.widget.emotionkeyboard.utils.EmotionUtils
import chat.rocket.android.widget.emotionkeyboard.utils.SpanStringUtils
import chat.rocket.android.widgets.RocketChatMessageAttachmentsLayout
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout
import chat.rocket.android.widgets.RocketChatMessageReportLayout
import chat.rocket.core.SyncState
import chat.rocket.core.models.Message
import chat.rocket.persistence.realm.repositories.RealmUserRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class MessageRenderer(val message: Message, val autoLoadImage: Boolean)  {
    private var onAttachItemLongClickListener: MessageListAdapter.OnAttachItemLongClickListener<PairedMessage>? = null
    val userRepository = RealmUserRepository(RocketChatCache.getSelectedServerHostname())
    /**
     * Show user's avatar image in RocketChatAvatar widget.
     */
    fun showAvatar(rocketChatAvatarWidget: ImageView, hostname: String) {
        val avatar = userRepository.getAvatarByUsername(message.user?.username)
        val options = RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar)
        Glide.with(RocketChatApplication.getInstance())
                .load(avatar)
                .apply(options)
                .into(rocketChatAvatarWidget)
    }

    /**
     * Show username in textView.
     */
    fun showUsername(usernameTextView: TextView, subUsernameTextView: TextView?) {
        val username: String? = message.user?.realName
        if (username != null) {
            if (message.alias == null) {
                usernameTextView.text = username
            } else {
                usernameTextView.text = message.alias
                if (subUsernameTextView != null) {
                    subUsernameTextView.text = subUsernameTextView.context.getString(R.string.sub_username, username)
                    subUsernameTextView.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Show timestamp or message state in textView.
     */
    fun showTimestampOrMessageState(textView: TextView) {
        when (message.syncState) {
            SyncState.SYNCING -> textView.text = textView.context.getText(R.string.sending)
            SyncState.NOT_SYNCED -> textView.text = textView.context.getText(R.string.not_synced)
            SyncState.FAILED -> textView.text = textView.context.getText(R.string.failed_to_sync)
            else -> textView.text = DateTime.fromEpocMs(message.timestamp, DateTime.Format.TIME)
        }
    }

    /**
     * Show body in RocketChatMessageLayout widget.
     */
    fun showBody(rocketChatMessageLayout: TextView,isMyself :Boolean) {
        val mentions = message.mentions
        var str = message.message
        var attachments=message.attachments
        var spannableString :SpannableString
        if(mentions!=null&&mentions.size>0){
            for(mention in mentions){
                val lastIndexOf = mention.username.lastIndexOf("&")
                if(lastIndexOf!=-1) {
                    val name = mention.username.substring(0, lastIndexOf)
                    str = str.replace(mention.username, name)
                }
            }
            spannableString= SpannableString(str)
            for(mention in mentions){
                val lastIndexOf = mention.username.lastIndexOf("&")
                val name:String
                if(lastIndexOf!=-1){
                 name = mention.username.substring(0, lastIndexOf)
                }else{
                    name=mention.username
                }
                val indexOf = str.indexOf("@" + name);
                spannableString.setSpan(object : LongClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        /**set textColor */
                        ds.color = ds.linkColor
                        /**Remove the underline */
                        ds.isUnderlineText = false
                    }

                    override fun onLongClick(widget: View?) {
                        ToastUtils.showToast("长按spannablestring");
                    }

                    override fun onClick(widget: View) {
                        if(mention.username.equals("all")||mention.username.equals("here")){
                            return
                        }
                        val intent = Intent(RocketChatApplication.getInstance(), MyInfoActivity::class.java)
                        intent.putExtra("roomId", message.roomId)
                        intent.putExtra("username",mention.username)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        RocketChatApplication.getInstance().startActivity(intent)
                    }
                },indexOf,indexOf+name.length+1,Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#06acec")),
                        indexOf,indexOf+name.length+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if(TextUtils.isEmpty(str)||(str.contains(RocketChatCache.getSelectedServerHostname()+"")&& str.contains("?msg="))||"true".equals(message.getHidelink())){
                rocketChatMessageLayout.visibility=View.GONE
            }else{
                rocketChatMessageLayout.visibility=View.VISIBLE
            }
            rocketChatMessageLayout.setText(spannableString)
            rocketChatMessageLayout.movementMethod = MyLinkMovementMethod.getInstance()

        }
        else{
            if( TextUtils.isEmpty(str)||(str.contains(RocketChatCache.getSelectedServerHostname()+"") && str.contains("?msg="))||"true".equals(message.getHidelink())){
                if(!TextUtils.isEmpty(message.nType) ){
                    rocketChatMessageLayout.visibility=View.VISIBLE
                    rocketChatMessageLayout.text = StringUtils.genertateTalkingHistory(message,isMyself)
                    return
                }
                rocketChatMessageLayout.visibility=View.GONE
            }else{
                rocketChatMessageLayout.visibility=View.VISIBLE
                rocketChatMessageLayout.text = str
                val mContent = SpanStringUtils.getEmotionContent(EmotionUtils.EMOTION_CLASSIC_TYPE,RocketChatApplication.getInstance(), rocketChatMessageLayout, str)
                rocketChatMessageLayout.text=mContent
            }
        }
    }



    /**
     * Show urls in RocketChatMessageUrlsLayout widget.
     */
    fun showUrl(rocketChatMessageUrlsLayout: RocketChatMessageUrlsLayout) {
        val webContents = message.webContents
        if (webContents == null || webContents.isEmpty()) {
            rocketChatMessageUrlsLayout.visibility = View.GONE
        } else {
            rocketChatMessageUrlsLayout.setUrls(webContents, autoLoadImage)
            rocketChatMessageUrlsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * show attachments in RocketChatMessageAttachmentsLayout widget.
     */
    fun showAttachment(rocketChatMessageAttachmentsLayout: RocketChatMessageAttachmentsLayout, absoluteUrl: AbsoluteUrl?) {
        val attachments = message.attachments

        if (attachments == null || attachments.isEmpty()) {
            rocketChatMessageAttachmentsLayout.visibility = View.GONE
        } else {
            rocketChatMessageAttachmentsLayout.setAbsoluteUrl(absoluteUrl)
            rocketChatMessageAttachmentsLayout.isMySelf(message.user!!.username .equals(RocketChatCache.getUserUsername()) )
            rocketChatMessageAttachmentsLayout.setAttachments(attachments,message, autoLoadImage)
            rocketChatMessageAttachmentsLayout.visibility = View.VISIBLE
        }
    }

    fun showReport(rocketChatMessageReportLayout: RocketChatMessageReportLayout?){
        val report=message.report
        val card=message.card
        if(rocketChatMessageReportLayout==null)
            return
        if (report == null&&card==null){
            rocketChatMessageReportLayout!!.visibility=View.GONE
        }else{
            rocketChatMessageReportLayout!!.visibility = View.VISIBLE
            rocketChatMessageReportLayout!!.setAttachments(card,report)
        }
    }

//    fun setOnAttachItemLongClickListener(onAttachItemLongClickListener: OnAttachItemLongClickListener<PairedMessage>) {
//        this.onAttachItemLongClickListener = onAttachItemLongClickListener
//    }
//
//    interface OnAttachItemLongClickListener<PairedMessage> {
//        fun onAttachItemLongClick(pairedMessage: PairedMessage)
//    }

}