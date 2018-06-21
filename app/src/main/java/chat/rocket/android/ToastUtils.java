package chat.rocket.android;

import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by helloworld on 2018/2/6
 */

public class ToastUtils {
    private static Toast toast = null;

    public static void showToast(String text) {
        if(!TextUtils.isEmpty(text) && text.contains("DDPClientCallback$Closed")){
            text = "连接服务器异常";
        }
        if (toast == null) {
            toast = Toast.makeText(RocketChatApplication.getInstance(), text, Toast.LENGTH_SHORT);
        } else {
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public static void showToastLong(String text) {
        if (toast == null) {
            toast = Toast.makeText(RocketChatApplication.getInstance(), text, Toast.LENGTH_LONG);
        } else {
            toast.setText(text);
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.show();
    }
}
