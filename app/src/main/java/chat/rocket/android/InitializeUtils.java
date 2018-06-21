package chat.rocket.android;


import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import java.util.List;

import bolts.Task;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.api.rest.DefaultServerPolicyApi;
import chat.rocket.android.api.rest.ServerPolicyApi;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.ServerPolicyApiValidationHelper;
import chat.rocket.android.helper.ServerPolicyHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.ChatConnectivityManager;
import chat.rocket.android.service.ConnectivityManagerApi;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmSubscription;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;

/**
 * Created by user on 2018/3/31.
 */

public class InitializeUtils {

    private ConnectivityManagerApi connectivityManager;
    private CompositeDisposable compositeSubscription = new CompositeDisposable();
    private static InitializeUtils singleton;
    private Context context;

    public static InitializeUtils getInstance(){
        if (singleton == null) {
            synchronized (InitializeUtils.class) {
                if (singleton == null) {
                    singleton  = new InitializeUtils();
                }
            }
        }
        return singleton;
    }

    public Task<Void> exit(){
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
        return realmHelper.executeTransaction(realm -> {
            realm.executeTransactionAsync(Realm::deleteAll);
         return null;
    });
    }

    public Task<Void> clearSession(){
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
        return realmHelper.executeTransaction(realm -> {
            realm.delete(RealmSession.class);
            return null;
        });
    }

    public Task<Void> clearSubscription(){
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
        return realmHelper.executeTransaction(realm -> {
            realm.delete(RealmSubscription.class);
            return null;
        });
    }

    public Task<Void> clearUser(){
        String hostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
        RocketChatCache.INSTANCE.setUserId(null);
        RocketChatCache.INSTANCE.setUserName(null);
        RocketChatCache.INSTANCE.setUserUsername(null);
        return realmHelper.executeTransaction(realm ->{
            realm.where(RealmUser.class)
                    .isNotEmpty("emails")
                    .findFirst()
                    .setEmails(null);
            return null;
        });
    }

    public void login(Context context ,String sessionId,String userId,String companyId){
        this.context=context;
        if (connectivityManager==null){
            connectivityManager= ChatConnectivityManager.getInstance(context);
        }
        connectivityManager.keepAliveServer();
        String hostname1 = RocketChatCache.INSTANCE.getSelectedServerHostname();
        MethodCallHelper methodCallHelper = new MethodCallHelper(context, hostname1);
        methodCallHelper.loginUser(context, sessionId)
                .continueWithTask(task -> {
                    if (task.isFaulted()) {
                        RCLog.e("loginUser error: " + task.getError(), true);
                    }else {
                        RealmHelper realmHelper = RealmStore.getOrCreate(RocketChatCache.INSTANCE.getSelectedServerHostname());
                        List<RealmSession> sessions = realmHelper.executeTransactionForReadResults(realm ->
                                realm.where(RealmSession.class)
                                        .isNotNull(RealmSession.TOKEN)
                                        .equalTo(RealmSession.TOKEN_VERIFIED, true)
                                        .isNull(RealmSession.ERROR)
                                        .findAll());
                        if (sessions!=null&&sessions.size()>0){
                            RCLog.d("reSubUser：session!=null");
                            RocketChatCache.INSTANCE.setDownLine(false);
                            RocketChatCache.INSTANCE.setFrameUserId(userId);
                            RocketChatCache.INSTANCE.setCompanyId(companyId);
                        }
                    }
                    return task;
                });
    }

    public void serviceConnection(Context context){
        this.context=context;
        connectivityManager= ChatConnectivityManager.getInstance(context);
        connectToEnforced(ServerPolicyHelper.enforceHostname(context.getString(R.string.fragment_input_hostname_server_hint)),context);
    }

    @SuppressLint("RxLeakedSubscription")
    public  void connectToEnforced(final String hostname, Context context) {
        final ServerPolicyApi serverPolicyApi = new DefaultServerPolicyApi(OkHttpHelper.INSTANCE.getClientForUploadFile(), hostname);
        final ServerPolicyApiValidationHelper validationHelper = new ServerPolicyApiValidationHelper(serverPolicyApi);
        compositeSubscription.clear();
        Disposable subscribe = ServerPolicyHelper.isApiVersionValid(validationHelper)
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        serverValidation -> {
                            if (serverValidation.isValid()) {
                                onServerValid(hostname, serverValidation.usesSecureConnection());
                            } else {
                                Toast.makeText(context, context.getString(R.string.input_hostname_invalid_server_message), Toast.LENGTH_SHORT);
                            }
                        },
                        throwable -> {
                            Logger.INSTANCE.report(throwable);
                            Toast.makeText(context, context.getString(R.string.connection_error_try_later), Toast.LENGTH_SHORT);
                        });
        compositeSubscription.add(subscribe);
    }

    private  void onServerValid(String hostname, boolean usesSecureConnection) {
        RocketChatCache.INSTANCE.setSelectedServerHostname(hostname);
        String server = hostname.replace("/", ".");
        connectivityManager.addOrUpdateServer(server, server, !usesSecureConnection);
        connectivityManager.keepAliveServer();
        RCLog.d("FileUploadingWithUfsObserver-----建立连接-----keepAliveServer");
    }
}
