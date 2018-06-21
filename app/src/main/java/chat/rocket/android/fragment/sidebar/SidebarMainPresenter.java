package chat.rocket.android.fragment.sidebar;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.webkit.CookieManager;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.login.SharedPreferencesUtil;
import chat.rocket.android.login.UserInfo;
import chat.rocket.android.service.ChatConnectivityManager;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserRoomChanged;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomSidebar;
import chat.rocket.core.models.Spotlight;
import chat.rocket.core.models.SpotlightUser;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.SpotlightRepository;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.core.utils.Pair;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.repositories.RealmSpotlightRepository;
import chat.rocket.persistence.realm.repositories.RealmSpotlightUserRepository;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.realm.Realm;
import tellh.com.recyclertreeview_lib.TreeNode;

public class SidebarMainPresenter extends BasePresenter<SidebarMainContract.View> implements SidebarMainContract.Presenter {
    private final String hostname;
    private final RoomInteractor roomInteractor;
    private final UserRepository userRepository;
    private final AbsoluteUrlHelper absoluteUrlHelper;
    private final MethodCallHelper methodCallHelper;
    private SpotlightRepository realmSpotlightRepository;
    private RealmSpotlightUserRepository realmSpotlightUserRepository;
    private List<RoomSidebar> roomSidebarList;

    public SidebarMainPresenter(String hostname,
                                RoomInteractor roomInteractor,
                                UserRepository userRepository,
                                AbsoluteUrlHelper absoluteUrlHelper,
                                MethodCallHelper methodCallHelper,
                                RealmSpotlightRepository realmSpotlightRepository
            , RealmSpotlightUserRepository realmSpotlightUserRepository) {
        this.hostname = hostname;
        this.roomInteractor = roomInteractor;
        this.userRepository = userRepository;
        this.absoluteUrlHelper = absoluteUrlHelper;
        this.methodCallHelper = methodCallHelper;
        this.realmSpotlightRepository = realmSpotlightRepository;
        this.realmSpotlightUserRepository = realmSpotlightUserRepository;
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void bindView(@NonNull SidebarMainContract.View view) {
        super.bindView(view);
//        methodCallHelper.labelsForMobile();
        if (TextUtils.isEmpty(hostname)) {
            view.showEmptyScreen();
            return;
        }

        view.showScreen();
        subscribeToRooms();
//        userRepository.getCurrent().distinctUntilChanged().subscribe(userOptional -> {
//            User user = userOptional.get();
//            view.show(user);
//        }, RCLog::e);
        final Disposable subscription = Flowable.combineLatest(
                userRepository.getCurrent().distinctUntilChanged(),
                absoluteUrlHelper.getRocketChatAbsoluteUrl().toFlowable(),
                Pair::new
        )
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    view.show(pair.first.orNull());
                }, Logger.INSTANCE::report);
        addSubscription(subscription);
    }

    public void resume() {
        UserInfo user = SharedPreferencesUtil.getObject(RocketChatApplication.getInstance(), UserInfo.class);
        methodCallHelper.roomsTypesForMobile();
        methodCallHelper.roomsForMobile(user == null ? "" : user.get_id());

    }

    @Override
    public void onRoomSelected(RoomSidebar roomSidebar) {
        RocketChatCache.INSTANCE.setSelectedRoomId(roomSidebar.getRoomId());
    }

    @Override
    public Flowable<List<Spotlight>> searchSpotlight(String term) {

        return realmSpotlightRepository.getSuggestionsFor(term, 10);
    }

    @Override
    public Flowable<List<SpotlightUser>> searchSpotlight(String term, User user) {
        methodCallHelper.searchSpotlight(term, user.getId());
        return realmSpotlightUserRepository.getSuggestionsFor(term);
    }

    @Override
    public void onSpotlightSelected(Spotlight spotlight) {
        if (spotlight.getType().equals(Room.TYPE_DIRECT_MESSAGE)) {
            String username = spotlight.getName();
            methodCallHelper.createDirectMessage(username)
                    .continueWithTask(task -> {
                        if (task.isCompleted()) {
                            RocketChatCache.INSTANCE.setSelectedRoomId(task.getResult());
                        }
                        return null;
                    });
        } else {
            methodCallHelper.joinRoom(spotlight.getId())
                    .continueWithTask(task -> {
                        if (task.isCompleted()) {
                            RocketChatCache.INSTANCE.setSelectedRoomId(spotlight.getId());
                        }
                        return null;
                    });
        }
    }

    @Override
    public void onUserOnline() {
        updateCurrentUserStatus(User.STATUS_ONLINE);
    }

    @Override
    public void onUserAway() {
        updateCurrentUserStatus(User.STATUS_AWAY);
    }

    @Override
    public void onUserBusy() {
        updateCurrentUserStatus(User.STATUS_BUSY);
    }

    @Override
    public void onUserOffline() {
        updateCurrentUserStatus(User.STATUS_OFFLINE);
    }

    @Override
    public void onLogout(Continuation<Void, Object> continuation) {
        methodCallHelper.logout().continueWith(task -> {
            if (task.isFaulted()) {
                Logger.INSTANCE.report(task.getError());
                return Task.forError(task.getError());
            }
            return task.onSuccess(continuation);
        });
    }

    @Override
    public void prepareToLogOut() {
        onLogout(task -> {
            if (task.isFaulted()) {
                return Task.forError(task.getError());
            }

            clearSubscriptions();
            String currentHostname = RocketChatCache.INSTANCE.getSelectedServerHostname();
            RealmHelper realmHelper = RealmStore.getOrCreate(currentHostname);
            return realmHelper.executeTransaction(realm -> {
                RocketChatCache.INSTANCE.removeHostname(currentHostname);
                RocketChatCache.INSTANCE.removeSelectedRoomId(currentHostname);
                RocketChatCache.INSTANCE.setSelectedServerHostname(RocketChatCache.INSTANCE.getFirstLoggedHostnameIfAny());
                realm.executeTransactionAsync(Realm::deleteAll);
                view.onPreparedToLogOut();
                ChatConnectivityManager.getInstance(RocketChatApplication.getInstance())
                        .removeServer(hostname);
                CookieManager.getInstance().removeAllCookie();
                return null;
            });
        });
    }

    @Override
    public void createDirectMessageForMobile(String userName) {
        methodCallHelper.createDirectMessageForMobile(userName);
    }

    @Override
    public void hideRoomForMobile(User user,String rid) {
        methodCallHelper.hideRoomForMobile(user.getId(),rid);
    }

    @Override
    public void setHint(User user) {
        if (user==null) {
            Disposable subscribe = userRepository.getCurrent().distinctUntilChanged()
                    .subscribe(userOptional -> {
                        User user1 = userOptional.get();
                        if (user1 != null) {
                            view.show(user1);
                        }
                    }, RCLog::e);
            addSubscription(subscribe);
        }
    }

    @Override
    public void disposeSubscriptions() {
        clearSubscriptions();
    }

    private void subscribeToRooms() {
        final Disposable subscription = roomInteractor.getOpenRooms()
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processRooms, Logger.INSTANCE::report);
        addSubscription(subscription);
    }

    private void processRooms(List<Room> roomList) {
        roomSidebarList = new ArrayList<>();
        List<String> userToObserverList = new ArrayList<>();

        for (Room room : roomList) {
            String roomName = room.getName();
            String roomType = room.getType();

            RoomSidebar roomSidebar = new RoomSidebar();

            roomSidebar.setId(room.getId());
//            roomSidebar.setRoomId(room.getRoomId());
            roomSidebar.setRoomName(roomName);
            roomSidebar.setType(roomType);
            roomSidebar.setAlert(room.isAlert());
            roomSidebar.setFavorite(room.isFavorite());
            roomSidebar.setUnread(room.getUnread());
            roomSidebar.setUpdateAt(room.getUpdatedAt());
            roomSidebar.setLastSeen(room.getLastSeen());

            if (roomType.equals(Room.TYPE_DIRECT_MESSAGE)) {
                userToObserverList.add(roomName);
            }

            roomSidebarList.add(roomSidebar);
        }
        if (userToObserverList.isEmpty()) {
            view.showRoomSidebarList(roomSidebarList);
        } else {
            getUsersStatus();
        }
    }

    private void getUsersStatus() {
        // TODO Filter when Android Studion uses the java8 features (removeIf).
        // .filter(userList -> userList.removeIf(user -> !userToObserverList.contains(user.getUsername())))
        final Disposable subscription = userRepository.getAll()
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processUsers, Logger.INSTANCE::report);
        addSubscription(subscription);
    }

    private void processUsers(List<User> userList) {
        for (User user : userList) {
            for (RoomSidebar roomSidebar : roomSidebarList) {
                if (roomSidebar.getRoomName().equals(user.getUsername())) {
                    roomSidebar.setUserStatus(user.getStatus());
                }
            }
        }
        view.showRoomSidebarList(roomSidebarList);
    }

    private void updateCurrentUserStatus(String status) {
//        methodCallHelper.setUserStatus(status).continueWith(new LogIfError());
//        methodCallHelper.setUserStatusForMobile(RocketChatCache.INSTANCE.getUserId(),status,
//                (status.equals(User.STATUS_ONLINE)||status.equals(User.STATUS_BUSY))?false:true);
        methodCallHelper.setUserStatusForMobile(RocketChatCache.INSTANCE.getUserId(),status,false);
    }
}