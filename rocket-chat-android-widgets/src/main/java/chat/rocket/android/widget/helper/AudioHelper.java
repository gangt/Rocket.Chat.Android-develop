package chat.rocket.android.widget.helper;

import android.media.MediaPlayer;
import android.os.Message;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;



/**
 * Created by jumper on 2018/3/24.
 */

public class AudioHelper {

    private static AudioHelper mInstance;
    private MediaPlayer mediaPlayer;

    /**
     * 获取单例引用
     *
     * @return
     */
    public static AudioHelper getInstance() {
        if (mInstance == null) {
            synchronized (AudioHelper.class) {
                if (mInstance == null) {
                    mInstance = new AudioHelper();
                }
            }
        }
        return mInstance;
    }


//    public AudioHelper(MediaPlayer mediaPlayer) {
//        this.mediaPlayer = mediaPlayer;
//    }

    /**
     * 播放语音
     *
     * @param path
     */
    public void playSound(String path, final ImageView audioImg, final boolean isRightSide) {
        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            else if (mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                stopTimer();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    playAudioAnimation(audioImg, isRightSide);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * 播放语音
     *
     * @param path
     */
    public void playSound(File path, final ImageView audioImg, final boolean isRightSide) {
        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            else if (mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                stopTimer();
            }else {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path.getPath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    playAudioAnimation(audioImg, isRightSide);
                }
            });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 释放资源
     */
    public void relese() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()){
                    stopTimer();
                }
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mediaPlayer = null;
            }
        }
    }

    /**
     * 播放语音图标动画
     */
    //语音动画控制器
    Timer mTimer = null;
    //语音动画控制任务
    TimerTask mTimerTask = null;
    //记录语音动画图片
    int index = 1;
    AudioAnimationHandler audioAnimationHandler = null;

    public void playAudioAnimation(final ImageView imageView, boolean isRightSide) {
        //定时器检查播放状态
        stopTimer();
        Timer mTimer = new Timer();
        //将要关闭的语音图片归位
        if (audioAnimationHandler != null) {
            Message msg = new Message();
            msg.what = 3;
            audioAnimationHandler.sendMessage(msg);
        }

        audioAnimationHandler = new AudioAnimationHandler(imageView, isRightSide);
        mTimerTask = new TimerTask() {
            public boolean hasPlayed = false;

            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    hasPlayed = true;
                    index = (index + 1) % 3;
                    Message msg = new Message();
                    msg.what = index;
                    audioAnimationHandler.sendMessage(msg);
                } else {
                    //当播放完时
                    Message msg = new Message();
                    msg.what = 3;
                    audioAnimationHandler.sendMessage(msg);
                    //播放完毕时需要关闭Timer等
                    if (hasPlayed) {
                        stopTimer();
                    }
                }
            }
        };
        //调用频率为500毫秒一次
        mTimer.schedule(mTimerTask, 0, 500);
    }

    /**
     * 停止
     */
    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }


//    public static String getDuring(String mUri) {
//        String duration=null;
//        final MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//        try {
//            if (mUri != null) {
//                mmr.setDataSource(mUri, new HashMap<String, String>());
////                mmr.setDataSource(mUri);
//            }
//            duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            } catch (Exception ex) {
//        } finally {
//            mmr.release();
//        }
//        Log.e("ryan", "duration " + duration);
//        return duration;
//    }

}
