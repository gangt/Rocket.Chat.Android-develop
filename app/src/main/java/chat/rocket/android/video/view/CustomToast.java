package chat.rocket.android.video.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;

/**
 * Created by Administrator on 2018/4/28/030.
 */

public class CustomToast {

    public static void showToastInfo(int textId){
        Context context = RocketChatApplication.getInstance();
        Toast toast =  new Toast(context);
        View view = View.inflate(context, R.layout.toast_view,null);
        TextView textView = view.findViewById(R.id.showview);
        textView.setText(textId);
        toast.setGravity(Gravity.CENTER,0,280);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showToastLongInfo(int textId){
        Context context = RocketChatApplication.getInstance();
        Toast toast =  new Toast(context);
        View view = View.inflate(context, R.layout.toast_view,null);
        TextView textView = view.findViewById(R.id.showview);
        textView.setText(textId);
        toast.setGravity(Gravity.CENTER,0,280);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

}
