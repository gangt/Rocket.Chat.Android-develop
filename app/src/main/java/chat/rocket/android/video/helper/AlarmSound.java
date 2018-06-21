package chat.rocket.android.video.helper;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

import java.io.IOException;

/**
 * Created by Administrator on 2018/5/10/010.
 */

public class AlarmSound {
    public AlarmSound(Context context) {
        mContext = context;
    }

    private boolean playBeep = true;
    private boolean vibrate;
    private MediaPlayer mediaPlayer = null;
    private static final long VIBRATE_DURATION = 1000;
    private Vibrator vibrator = null;
    private Context mContext;
    private AlarmThead stopAlarmThread = null;

    public AlarmSound sound = null;


    public void playBeepSound(int time, int raw) {

        if (time != 0) {
            if (stopAlarmThread == null) {
                stopAlarmThread = new AlarmThead(time);
                stopAlarmThread.start();
            } else {
                if (stopAlarmThread.isRunning()) {
                    stopAlarmThread.reSetTime();
                } else {
                    stopAlarmThread = new AlarmThead(time);
                    stopAlarmThread.start();
                }
            }
        } else {
            if (stopAlarmThread != null && stopAlarmThread.isRunning()) {
                stopAlarmThread.stopTread();
            }
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        initBeepSound(raw);

        if (playBeep && mediaPlayer != null) {
            try {
                mediaPlayer.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }


    }

    public void playVibrate(int time) {
        cancelBeep();
        vibrate = true;
        if (vibrate) {
            vibrator = (Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION * (time <= 0 ? 1 : time));
        }
    }

    public void cancelBeep() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    public void stopAlarm() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            if(stopAlarmThread !=null){
                stopAlarmThread.stopTread();
                stopAlarmThread = null;
            }
        }

    }

    public void initBeepSound(int raw) {

        if (playBeep && mediaPlayer == null) {

            mediaPlayer = MediaPlayer.create(mContext, raw);
            mediaPlayer.setOnCompletionListener(arg0 -> mediaPlayer.stop());
        }
    }

    class AlarmThead extends Thread {
        private boolean bRunning = false;
        private int time = 0;
        private int t = 0;

        public AlarmThead(int time) {
            bRunning = true;
            this.time = time;
        }

        public void stopTread() {
            bRunning = false;
        }

        public void reSetTime() {
            t = 0;
        }

        public boolean isRunning() {
            return this.bRunning;
        }

        @Override
        public void run() {
            while (bRunning) {
                t++;
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (t >= time) {
                    stopAlarm();
                    stopTread();
                }
            }
        }
    }
}
