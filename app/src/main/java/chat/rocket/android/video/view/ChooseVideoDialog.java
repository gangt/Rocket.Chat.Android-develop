package chat.rocket.android.video.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import chat.rocket.android.R;

/**
 * Created by Administrator on 2018/4/24/024.
 */

public class ChooseVideoDialog {
    private Context context ;
    private Dialog cardDLG ;
    VideoDialogClickListener listener ;

    public ChooseVideoDialog( Context contex) {
        this.context = contex;
    }

    public  void showDialog() {
        cardDLG = new AlertDialog.Builder(context).create();
        cardDLG.show();

        Window window = cardDLG.getWindow();
        LayoutInflater layout = LayoutInflater.from(context);
        View view = layout.inflate(R.layout.dialog_view_user, null);
        window.setContentView(view);
        initlisenter(view);
        cardDLG.getWindow().setBackgroundDrawable(new BitmapDrawable());

        cardDLG.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置弹出的动画效果
        cardDLG.getWindow().setWindowAnimations(R.style.AnimBottom);
        WindowManager.LayoutParams wlp = cardDLG.getWindow().getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        cardDLG.getWindow().setAttributes(wlp);
    }
    @SuppressLint({"RxLeakedSubscription", "RxSubscribeOnError"})
    private void initlisenter(View view) {

        view.findViewById(R.id.vedio_rl).setOnClickListener(v->{
            if(listener != null){
                listener.onClick(0);
            }
            cardDLG.dismiss();
        });

        view.findViewById(R.id.audio_rl).setOnClickListener(v->{
            if(listener != null){
                listener.onClick(1);
            }
            cardDLG.dismiss();
        });

        view.findViewById(R.id.talk_cancel).setOnClickListener(v-> cardDLG.dismiss());
    }

    public interface VideoDialogClickListener{
        void onClick(int position);
    }

    public void setOnVideoDialogClickListener(VideoDialogClickListener listener){
        this.listener = listener ;
    }

}
