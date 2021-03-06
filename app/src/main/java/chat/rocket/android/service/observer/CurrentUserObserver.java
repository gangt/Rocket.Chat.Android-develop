package chat.rocket.android.service.observer;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserRoomChanged;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.android.service.ddp.stream.VideoMsgObsever;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * observe the user with emails.
 */
public class CurrentUserObserver extends AbstractModelObserver<RealmUser> {
    private final MethodCallHelper methodCall;
    private boolean currentUserExists;
    private ArrayList<Registrable> listeners;
    VideoMsgObsever videoMsgObsever ;

    public CurrentUserObserver(Context context, String hostname,
                               RealmHelper realmHelper) {
        super(context, hostname, realmHelper);
        methodCall = new MethodCallHelper(realmHelper);
        currentUserExists = false;
    }

    @Override
    public RealmResults<RealmUser> queryItems(Realm realm) {
        return RealmUser.queryCurrentUser(realm).findAll();
    }

    @Override
    public void onUpdateResults(List<RealmUser> results) {
        boolean exists = !results.isEmpty();

        if (currentUserExists != exists) {
            if (exists) {
                onLogin(results.get(0));
            } else {
                onLogout();
            }
            currentUserExists = exists;
        }
    }

    @DebugLog
    private void onLogin(RealmUser user) {
        if (listeners != null) {
            onLogout();
        }
        listeners = new ArrayList<>();

        String userId = user.getId();
        String username = user.getUsername();
        String name = user.getName();


        RocketChatCache.INSTANCE.setUserId(userId);
        RocketChatCache.INSTANCE.setUserUsername(username);
        RocketChatCache.INSTANCE.setUserName(name);

        // get and observe Room subscriptions.
        methodCall.getRoomSubscriptions().onSuccess(task -> {
            if (listeners != null) {
                Registrable listener = new StreamNotifyUserSubscriptionsChanged(
                        context, hostname, realmHelper, userId);
                listener.register();
                listeners.add(listener);
            }
            return null;
        }).continueWith(new LogIfError());
        methodCall.getRooms().onSuccess(task -> {
            if(listeners!=null){
                Registrable listener = new StreamNotifyUserRoomChanged(context, hostname, realmHelper, userId);
                listener.register();
                listeners.add(listener);
            }
            return null;
        }).continueWith(new LogIfError());

        videoMsgObsever = new VideoMsgObsever(context, hostname, realmHelper, userId);
        videoMsgObsever.register();
        listeners.add(videoMsgObsever);

    }

    @DebugLog
    private void onLogout() {
        if (listeners != null) {
            for (Registrable listener : listeners) {
                listener.unregister();
            }
        }
        if(videoMsgObsever !=null){
            videoMsgObsever.unregister();
            videoMsgObsever = null ;
            listeners = null;
        }
    }
}
