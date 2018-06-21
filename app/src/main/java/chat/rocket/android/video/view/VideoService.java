package chat.rocket.android.video.view;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.model.ServiceStop;

/**
 * Created by Administrator on 2018/5/28/028.
 */

public class VideoService extends Service {
    ViewManager manager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!TempFileUtils.getInstance().getTalkingStatus() &&intent !=null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                initManager(extras);
                manager.addView();
                manager.add2Window();
                RocketChatApplication.playSounde(0);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initManager(Bundle bundle) {
        if (manager == null) {
            manager = new ViewManager();
        }
        manager.bindData(bundle);

    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(ServiceStop event) {
         stopSelf();
    }

        @Override
    public void onDestroy() {
        if(manager == null){
            return;
        }
        manager.unRegister();
        manager = null ;
        EventBus.getDefault().unregister(this);
            RocketChatApplication.stopSound();
        super.onDestroy();
    }

}
