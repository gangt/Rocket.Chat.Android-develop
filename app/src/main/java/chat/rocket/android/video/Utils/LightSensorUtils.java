package chat.rocket.android.video.Utils;

/**
 * Created by Tidom on 2018/6/14/014.
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

public class LightSensorUtils implements SensorEventListener {
    private static final Object mLock = new Object();
    private static LightSensorUtils instance;
    private static Context mContext;

    private SensorManager mSensorManager;
    private List<Sensor> mList;
    private boolean mIsContains = false;
    private final Float criticalValue = 40.0f;


    private LightSensorUtils() {
    }

    public void init(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : mList) {
            if (Sensor.TYPE_LIGHT == sensor.getType()) {
                mIsContains = true;
                return;
            }
        }
    }
    public static LightSensorUtils getInstance() {
        if (instance == null) {
            synchronized (mLock) {
                if (instance == null) {
                    instance = new LightSensorUtils();
                }
            }
        }
        return instance;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
          boolean isBright = event.values[0] > criticalValue ? true : false;
            if(!isBright){
                ScreenLockUtils.getInstance().releaseCpuLock();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}