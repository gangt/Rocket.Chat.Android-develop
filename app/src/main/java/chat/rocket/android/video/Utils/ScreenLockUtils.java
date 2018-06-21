package chat.rocket.android.video.Utils;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Tidom on 2018/6/14/014.
 */

public class ScreenLockUtils {
    private static ScreenLockUtils instance;
    private static final String TAG = "AlertWakeLock";
    private  static PowerManager.WakeLock sCpuWakeLock;
    PowerManager pm ;

    private ScreenLockUtils() {
    }

    public static ScreenLockUtils getInstance() {
        if (instance == null) {
            instance = new ScreenLockUtils();
        }

        return instance;
    }

    PowerManager.WakeLock createPartialWakeLock(Context context) {
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (null == pm) {
            return null;
        }
        return pm.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
    }

    public  void acquireCpuWakeLock(Context context) {
        if (sCpuWakeLock == null) {
            sCpuWakeLock = createPartialWakeLock(context);
        }
        sCpuWakeLock.acquire();
    }

    public void releaseCpuLock() {
        sCpuWakeLock.setReferenceCounted(false);
        sCpuWakeLock.release();
    }
}
