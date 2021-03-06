package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hadisatrio.optional.Optional;

import java.util.List;

import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.fragment.chatroom.HomeFragment;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.push.PushManager;
import chat.rocket.android.service.ChatConnectivityManager;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmSubscription;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;

abstract class AbstractAuthedActivity extends AbstractFragmentActivity {
    @State
    protected String hostname;
    @State
    protected String roomId;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean isNotification;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }

        updateHostnameIfNeeded(RocketChatCache.INSTANCE.getSelectedServerHostname());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        if (intent.hasExtra(PushManager.EXTRA_HOSTNAME)) {
            String hostname = intent.getStringExtra(PushManager.EXTRA_HOSTNAME);
            HttpUrl url = HttpUrl.parse(hostname);
            if (url != null) {
                String hostnameFromPush = url.host();
                String loginHostname = RocketChatCache.INSTANCE.getSiteUrlFor(hostnameFromPush);
                RocketChatCache.INSTANCE.setSelectedServerHostname(loginHostname);

                if (intent.hasExtra(PushManager.EXTRA_ROOM_ID)) {
                    RocketChatCache.INSTANCE.setSelectedRoomId(intent.getStringExtra(PushManager.EXTRA_ROOM_ID));
                }
            }
            PushManager.INSTANCE.clearNotificationsByHost(hostname);
        } else {
            updateHostnameIfNeeded(RocketChatCache.INSTANCE.getSelectedServerHostname());
        }

        if (intent.hasExtra(PushManager.EXTRA_NOT_ID) && intent.hasExtra(PushManager.EXTRA_HOSTNAME)) {
            isNotification = true;
            int notificationId = intent.getIntExtra(PushManager.EXTRA_NOT_ID, 0);
            String hostname = intent.getStringExtra(PushManager.EXTRA_HOSTNAME);
            HttpUrl url = HttpUrl.parse(hostname);
            if (url != null) {
                String hostnameFromPush = url.host();
                String loginHostname = RocketChatCache.INSTANCE.getSiteUrlFor(hostnameFromPush);
                PushManager.INSTANCE.clearNotificationsByHostAndNotificationId(loginHostname, notificationId);
            } else {
                PushManager.INSTANCE.clearNotificationsByNotificationId(notificationId);
            }

        }
    }

    private void updateHostnameIfNeeded(String newHostname) {
        if (hostname == null) {
            if (newHostname != null && assertServerRealmStoreExists(newHostname)) {
                updateHostname(newHostname);
                updateRoomIdIfNeeded(RocketChatCache.INSTANCE.getSelectedRoomId());
            } else {
                recoverFromHostnameError();
            }
        } else {
            if (hostname.equals(newHostname)) {
                updateHostname(newHostname);
                updateRoomIdIfNeeded(RocketChatCache.INSTANCE.getSelectedRoomId());
                return;
            }

            if (assertServerRealmStoreExists(newHostname)) {
                Intent intent = new Intent(this, ChatMainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            } else {
                recoverFromHostnameError();
            }
        }
    }

    private boolean assertServerRealmStoreExists(String hostname) {
        return RealmStore.get(hostname) != null;
    }

    private void updateHostname(String hostname) {
        this.hostname = hostname;
        onHostnameUpdated();
    }

    private void recoverFromHostnameError() {
        final List<ServerInfo> serverInfoList =
                ChatConnectivityManager.getInstance(getApplicationContext()).getServerList();
        if (serverInfoList == null || serverInfoList.size() == 0) {
            LaunchUtil.showAddServerActivity(this);
            return;
        }

        // just connect to the first available
        final ServerInfo serverInfo = serverInfoList.get(0);

        RocketChatCache.INSTANCE.setSelectedServerHostname(serverInfo.getHostname());
        RocketChatCache.INSTANCE.setSelectedRoomId(null);
    }

    private void updateRoomIdIfNeeded(String newRoomId) {
        if(newRoomId!=null&&newRoomId.length()==0){
            showFragment(new HomeFragment());
            roomId="";
            return;
        }

        if (roomId == null) {
            if (newRoomId != null && assertRoomSubscriptionExists(newRoomId)) {
                updateRoomId(newRoomId);
            }
        } else {
            if (!roomId.equals(newRoomId) && assertRoomSubscriptionExists(newRoomId)) {
                updateRoomId(newRoomId);
            }
        }
    }

    private boolean assertRoomSubscriptionExists(String roomId) {
        if (!assertServerRealmStoreExists(hostname)) {
            return false;
        }

        RealmSubscription room = RealmStore.get(hostname).executeTransactionForRead(realm ->
                realm.where(RealmSubscription.class).equalTo(RealmSubscription.ROOM_ID, roomId).findFirst());
        if (room == null) {
            RocketChatCache.INSTANCE.setSelectedRoomId(null);
            return false;
        }
        return true;
    }

    private void updateRoomId(String roomId) {
        this.roomId = roomId;
        onRoomIdUpdated();
    }

    protected void onHostnameUpdated() {
    }

    protected void onRoomIdUpdated() {
    }

    @Override
    protected void onResume() {
        super.onResume();

        subscribeToConfigChanges();

        ChatConnectivityManager.getInstance(getApplicationContext()).keepAliveServer();
        if (isNotification) {
            updateHostnameIfNeeded(RocketChatCache.INSTANCE.getSelectedServerHostname());
            updateRoomIdIfNeeded(RocketChatCache.INSTANCE.getSelectedRoomId());
            isNotification = false;
        }
    }

    @Override
    protected void onPause() {
        compositeDisposable.clear();

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void subscribeToConfigChanges() {
        compositeDisposable.add(
                RocketChatCache.INSTANCE.getSelectedServerHostnamePublisher()
                        .map(Optional::get)
                        .distinctUntilChanged()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::updateHostnameIfNeeded,
                                Logger.INSTANCE::report
                        )
        );

        compositeDisposable.add(
                RocketChatCache.INSTANCE.getSelectedRoomIdPublisher()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::updateRoomIdIfNeeded,
                                Logger.INSTANCE::report
                        )
        );
    }
}
