package chat.rocket.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import hugo.weaving.DebugLog;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Background service for Rocket.Chat.Application class.
 */
public class RocketChatService extends Service implements ConnectivityServiceInterface {

    private ConnectivityManagerInternal connectivityManager;
    private static volatile Semaphore webSocketThreadLock = new Semaphore(1);
    private static volatile RocketChatWebSocketThread currentWebSocketThread;

    public class LocalBinder extends Binder {
        ConnectivityServiceInterface getServiceInterface() {
            return RocketChatService.this;
        }
    }

    private final LocalBinder localBinder = new LocalBinder();

    /**
     * ensure RocketChatService alive.
     */
    /*package*/
    static void keepAlive(Context context) {
        RCLog.d("service-startService");
        context.startService(new Intent(context, RocketChatService.class));
    }

    public static void bind(Context context, ServiceConnection serviceConnection) {
        RCLog.d("service-bindService");
        context.bindService(
                new Intent(context, RocketChatService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbind(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        connectivityManager = ChatConnectivityManager.getInstanceForInternal(getApplicationContext());
        connectivityManager.resetConnectivityStateList();
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (RocketChatCache.INSTANCE.getConnectStus().equals(ServerConnectivity.STATE_DISCONNECTED+"")){
        RocketChatCache.INSTANCE.setConnectStus(ServerConnectivity.STATE_CONNECTING+"");
        connectivityManager.ensureConnections();
        }
        return START_STICKY;
    }

    @Override
    public Single<Boolean> ensureConnectionToServer(String hostname) { //called via binder.
        forceInvalidateTokens();
        return getOrCreateWebSocketThread(hostname)
                .flatMap(RocketChatWebSocketThread::keepAlive);
    }

    private void forceInvalidateTokens() {
        RCLog.d("RealmBasedConnectivityManager+清除tokens------------");
        RealmStore.getOrCreate(RocketChatCache.INSTANCE.getSelectedServerHostname()).executeTransaction(realm -> {
            RealmSession session = RealmSession.queryDefaultSession(realm).findFirst();
            if (session != null
                    && !TextUtils.isEmpty(session.getToken())
                    && (session.isTokenVerified() || !TextUtils.isEmpty(session.getError()))) {
                session.setTokenVerified(false);
                session.setError(null);
            }
            return null;
        }).continueWith(new LogIfError());
    }

    @Override
    public Single<Boolean> disconnectFromServer(String hostname) { //called via binder.
        return Single.defer(() -> {
            if (!existsThreadForHostname(hostname)) {
                return Single.just(true);
            }

            if (currentWebSocketThread != null) {
                return currentWebSocketThread.terminate(false)
                        // after disconnection from server
                        .doAfterTerminate(() -> {
                            currentWebSocketThread = null;
                            // remove RealmConfiguration key from HashMap
                            RealmStore.sStore.remove(hostname);
                        });
            } else {
                return Observable.timer(1, TimeUnit.SECONDS).singleOrError()
                        .flatMap(_val -> disconnectFromServer(hostname));
            }
        });
    }

    @DebugLog
    private Single<RocketChatWebSocketThread> getOrCreateWebSocketThread(String hostname) {
        return Single.defer(() -> {
            webSocketThreadLock.acquire();
            RCLog.d("RealmBasedConnectivityManager+开启重连------------");
            int connectivityState = ChatConnectivityManager.getInstance(getApplicationContext()).getConnectivityState(hostname);
            boolean isDisconnected = connectivityState < ServerConnectivity.STATE_CONNECTED;
            if (currentWebSocketThread != null && existsThreadForHostname(hostname) && !isDisconnected) {
                webSocketThreadLock.release();
                return Single.just(currentWebSocketThread);
            }

            if (currentWebSocketThread != null) {
                boolean hasFailed = existsThreadForHostname(hostname);
                return currentWebSocketThread.terminate(hasFailed)
                        .doAfterTerminate(() -> currentWebSocketThread = null)
                        .flatMap(terminated ->
                                RocketChatWebSocketThread.getStarted(getApplicationContext(), hostname)
                                        .doOnSuccess(thread -> {
                                            currentWebSocketThread = thread;
                                            webSocketThreadLock.release();
                                        })
                                        .doOnError(throwable -> {
                                            currentWebSocketThread = null;
                                            RCLog.e(throwable);
                                            Logger.INSTANCE.report(throwable);
                                            webSocketThreadLock.release();
                                        })
                        );
            }

            return RocketChatWebSocketThread.getStarted(getApplicationContext(), hostname)
                    .doOnSuccess(thread -> {
                        currentWebSocketThread = thread;
                        webSocketThreadLock.release();
                    })
                    .doOnError(throwable -> {
                        currentWebSocketThread = null;
                        RCLog.e(throwable);
                        Logger.INSTANCE.report(throwable);
                        webSocketThreadLock.release();
                    });
        });
    }

    private boolean existsThreadForHostname(String hostname) {
        if (hostname == null || currentWebSocketThread == null) {
            return false;
        }
        return currentWebSocketThread.getName().equals("RC_thread_" + hostname);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }
}
