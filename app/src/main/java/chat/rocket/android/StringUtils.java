package chat.rocket.android;

import android.text.SpannableString;
import android.text.Spanned;

import chat.rocket.android.log.RCLog;
import chat.rocket.android.video.model.CenteredImageSpan;
import chat.rocket.core.models.Message;

/**
 * Created by user on 2018/4/19.
 */

public class StringUtils {

    public static boolean isChinese(char c) {
        return c >= 0x4E00 &&  c <= 0x9FA5;// 根据字节码判断
    }
    // 判断一个字符串是否含有中文
    public static boolean isChinese(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c)) return true;// 有一个中文字符就返回
        }
        return false;
    }

    public static SpannableString genertateTalkingHistory(Message message, boolean isMyself) {
        CenteredImageSpan imageSpan ;
        if (message.getNType().equals("audio")) {
            imageSpan = new CenteredImageSpan(
                    RocketChatApplication.getInstance().getApplicationContext(),
                    R.drawable.remote_audio);
        }else{
            imageSpan = new CenteredImageSpan(
                    RocketChatApplication.getInstance().getApplicationContext(),
                    isMyself ? R.drawable.chat_img : R.drawable.remote_chat);
        }
        String json = null;
        boolean isMyselff = false;
        isMyselff = message.getUser().getId().equals(RocketChatCache.INSTANCE.getUserId());
        if(message.getFromMsg().contains("undefined") || message.getReceiveMsg().contains("undefined")){
            json = "通话异常";
        }else{
            json = isMyselff ? message.getFromMsg() : message.getReceiveMsg();
        }
        SpannableString spannableString = new SpannableString("  " + json + "  ");
        if (isMyselff) {
            spannableString.setSpan(imageSpan, spannableString.length() - 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }
}
