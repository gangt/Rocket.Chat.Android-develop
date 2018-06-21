package chat.rocket.android.widget.helper;

import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import chat.rocket.android.widget.R;

/**
 * Created by jumper on 2018/3/24.
 */

class AudioAnimationHandler extends Handler {
    ImageView imageView;
    //判断是左对话框还是右对话框
    boolean isleft;

    public AudioAnimationHandler(ImageView imageView,boolean isRightSide) {
        this.imageView = imageView;
        //判断是左对话框还是右对话框 我这里是在前面设置ScaleType来表示的
        isleft = !isRightSide;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        //根据msg.what来替换图片，达到动画效果
        switch (msg.what) {
            case 0:
                imageView.setImageResource(isleft ? R.drawable.chatfrom_voice_playing_l1 : R.drawable.chatfrom_voice_playing_f1);
                break;
            case 1:
                imageView.setImageResource(isleft ? R.drawable.chatfrom_voice_playing_l2 : R.drawable.chatfrom_voice_playing_f2);
                break;
            case 2:
                imageView.setImageResource(isleft ? R.drawable.chatfrom_voice_playing_l3 : R.drawable.chatfrom_voice_playing_f3);
                break;
            default:
                imageView.setImageResource(isleft ? R.drawable.chatfrom_voice_playing_l3 : R.drawable.chatfrom_voice_playing_f3);
                break;
        }
    }

}
