package chat.rocket.android;

import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.evernote.android.job.JobManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.service.ChatConnectivityManager;
import chat.rocket.android.service.ServerConnectivity;
import chat.rocket.android.video.helper.AlarmSound;
import chat.rocket.android.widget.RocketChatWidgets;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.RocketChatPersistenceRealm;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

/**
 * Customized Application-class for Rocket.Chat
 */
public class RocketChatApplication extends MultiDexApplication {

    private static Context instance;
    public static boolean isCacheInvalid;
    public static List<String> expands=new ArrayList<>();
    private RefWatcher mRefWatcher;//leakCanary观察器
    public static Context getInstance() {
        return instance;
    }
    private static boolean IS_PALYING =  false;

    @Override
    public void onCreate() {
        super.onCreate();
        RocketChatCache.INSTANCE.initialize(this);
        JobManager.create(this).addJobCreator(new RocketChatJobCreator());
        DDPClient.initialize(OkHttpHelper.INSTANCE.getClientForWebSocket());
//        Fabric.with(this, new Crashlytics());
        CrashReport.initCrashReport(getApplicationContext(), "1f3855f8a3", true);
        RocketChatPersistenceRealm.init(this);
        List<ServerInfo> serverInfoList = ChatConnectivityManager.getInstance(this).getServerList();
        for (ServerInfo serverInfo : serverInfoList) {
            RealmStore.put(serverInfo.getHostname());
        }

        RocketChatWidgets.initialize(this, OkHttpHelper.INSTANCE.getClientForDownloadFile());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            Logger.INSTANCE.report(e);
        });

        instance = this;
        RocketChatCache.INSTANCE.setConnectStus(ServerConnectivity.STATE_DISCONNECTED+"");
        installLeakCanary();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void onCreate(Context content) {
        RocketChatCache.INSTANCE.initialize(content);
        JobManager.create(content).addJobCreator(new RocketChatJobCreator());
        DDPClient.initialize(OkHttpHelper.INSTANCE.getClientForWebSocket());
//        Fabric.with(content, new Crashlytics());

        RocketChatPersistenceRealm.init(content);

        List<ServerInfo> serverInfoList = ChatConnectivityManager.getInstance(content).getServerList();
        for (ServerInfo serverInfo : serverInfoList) {
            RealmStore.put(serverInfo.getHostname());
        }

        RocketChatWidgets.initialize(content, OkHttpHelper.INSTANCE.getClientForDownloadFile());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            Logger.INSTANCE.report(e);
        });

        instance = content;
        RocketChatCache.INSTANCE.setConnectStus(ServerConnectivity.STATE_DISCONNECTED+"");
    }


    public static AlarmSound sound = null;

    public static void playSounde(int nTime) {
         if(IS_PALYING){
             return;
         }
         IS_PALYING = true ;

        if (sound == null) {
            sound = new AlarmSound(instance.getApplicationContext());
        }
        sound.playBeepSound(nTime, R.raw.beep);
    }

    public static void stopSound() {
        IS_PALYING = false ;
        if (sound != null) {
            sound.release();
            sound = null ;
        }
    }

    /**
     * 安装leakCanary检测内存泄露
     */
    protected void installLeakCanary() {
        this.mRefWatcher = BuildConfig.USE_CANARY ? LeakCanary.install(this) : RefWatcher.DISABLED;
    }

    /**
     * 获得leakCanary观察器
     *
     * @param context
     * @return
     */
    public static RefWatcher getRefWatcher(Context context) {
        RocketChatApplication application = (RocketChatApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }
}