package chat.rocket.android.helper;


import android.content.Intent;
import android.graphics.Color;
import android.text.*;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.activity.business.MyInfoActivity;
import chat.rocket.android.widget.R;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.Mention;
import chat.rocket.core.models.Message;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;

/**
 * Created by jumper on 2018/4/17.
 */

public class MessageRefHelper {
    /**
     * [ ](http://ucp.xt.weilian.cn/working/s%2540irMrmj6hhXKzSkLG51513671625358?msg=qENqykhXx3GTYqA72) @古小宁&48608 回复文档
     */
    private static MessageRefHelper mInstance;

    /**
     * 获取单例引用
     *
     * @return
     */
    public static MessageRefHelper getInstance() {
        if (mInstance == null) {
            synchronized (MessageRefHelper.class) {
                if (mInstance == null) {
                    mInstance = new MessageRefHelper();
                }
            }
        }
        return mInstance;
    }

    /**
     * 回复内容
     */
    public String getReplayText(String msgText, Message msg) {
        String replay_text;
        if (msg!=null&&msg.getUser()!=null&&msg.getUser().getUsername()!=null&&!msgText.equals("") && msgText.split(msg.getId()).length > 1) {
            if (msgText.split(msg.getId())[1].contains("@" + msg.getUser().getUsername()))
                if (msgText.split(msg.getUser().getUsername()).length > 1)
                    replay_text = msgText.split(msg.getUser().getUsername())[1];
                else
                    replay_text = "";
            else
                replay_text = msgText.split(msg.getId())[1].replace(msgText.split(msg.getId())[1].charAt(0) + "", "");
        } else
            replay_text = "";
        return replay_text;
    }

    public String getMsgId(String msgText) {
        return msgText.split("msg=")[1].split("\\)")[0];
    }

    public Message getMsg(RealmMessageRepository messageRepository, String msgId) {
        Message msg;
        if (messageRepository.getAllMessageByMessageId(msgId) != null && messageRepository.getAllMessageByMessageId(msgId).size() != 0)
            msg = messageRepository.getAllMessageByMessageId(msgId).get(0);
        else {
            return null;
        }
        return msg;
    }

    public SpannableString getMsgText(Message msg) {
        String text = msg.getMessage();
        SpannableString spannableString;
        List<Mention> mentions = msg.getMentions();
        if (mentions != null && mentions.size() > 0) {
            for (Mention mention : mentions) {
                int lastIndexOf = mention.getUsername().lastIndexOf("&");
                if (lastIndexOf != -1) {
                    String name = mention.getUsername().substring(0, lastIndexOf);
                    text = text.replace(mention.getUsername(), name);

                }
            }
            spannableString = new SpannableString(text);
            for (Mention mention : mentions) {
                int lastIndexOf = mention.getUsername().lastIndexOf("&");
                String name;
                if (lastIndexOf != -1) {
                    name = mention.getUsername().substring(0, lastIndexOf);
                } else {
                    name = mention.getUsername();
                }

                int indexOf = text.indexOf("@" + name);
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(ds.linkColor);
                        /**Remove the underline */
                        ds.setUnderlineText(false);
                    }

                    @Override
                    public void onClick(View view) {
                        if (mention.getUsername().equals("all") || mention.getUsername().equals("here")) {
                            return;
                        }
                        Intent intent = new Intent(RocketChatApplication.getInstance(), MyInfoActivity.class);
                        intent.putExtra("roomId", msg.getRoomId());
                        intent.putExtra("username", mention.getUsername());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        RocketChatApplication.getInstance().startActivity(intent);
                    }
                }, indexOf, indexOf + name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#06acec")),
                        indexOf, indexOf + name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
            return spannableString;
        } else {
            return new SpannableString(text);
        }

    }

    public String getTime(Message msg) {
        if (msg == null)
            return "";
        else
            return DateTime.fromEpocMs(msg.getTimestamp(), DateTime.Format.DATE1);
    }

    public String getSymbol(String msgText) {
        return msgText.contains("@") ? "@" : "''";
    }

    public int getSymbolRes(String msgText) {
        return msgText.contains("@") ? R.drawable.alt : R.drawable.quotation;
    }

    /**
     * 引用对象的用户名
     */
    public String getTagetUserName(Message msg) {
        if (msg == null || msg.getUser() == null) return "";
        return msg.getUser().getUsername().split("&")[0];
    }

    /**
     * 引用对象的文字内容
     */
    public String getRefText(Message msg) {
        return msg.getMessage();
    }

    /**
     * [{"author_name":"古小宁",
     * "message_link":"http:\/\/ucp.xt.weilian.cn\/working\/s%2540irMrmj6hhXKzSkLG51513671625358?msg=qENqykhXx3GTYqA72",
     * "author_icon":"\/avatar\/gxn%2648608?_dc=0",
     * "text":"",
     * "attachments":[{"title":"移动端接口文档.docx",
     * "title_link_download":true,
     * "title_link":"\/file-upload\/47GcAjr2QxP6GrR5v\/移动端接口文档.docx",
     * "description":"文件描述",
     * "nicktitle":"移动端接口文档.docx"}],
     * "ts":{"$date":1523440149760}}]
     */
    public List<Attachment> getChildAttachment(Message msg) {
        List<Attachment> attachment = msg.getAttachments();
        return attachment;
    }

    public String getAttachmentFileLink(Attachment attachment) {
        return attachment.getAttachmentChild().getTitle_link();
    }

    public String getAttachmentFileImage(Attachment attachment) {
        return attachment.getAttachmentChild().getTitle_link();
    }

}
