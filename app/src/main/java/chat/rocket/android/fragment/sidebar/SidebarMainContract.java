package chat.rocket.android.fragment.sidebar;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import java.util.List;

import bolts.Continuation;
import chat.rocket.android.shared.BaseContract;
import chat.rocket.core.models.RoomSidebar;
import chat.rocket.core.models.Spotlight;
import chat.rocket.core.models.SpotlightUser;
import chat.rocket.core.models.User;
import io.reactivex.Flowable;
import tellh.com.recyclertreeview_lib.TreeNode;

public interface SidebarMainContract {

  interface View extends BaseContract.View {

    void showScreen();

    void showEmptyScreen();

    void showRoomSidebarList(@NonNull List<RoomSidebar> roomSidebarList);

    void filterRoomSidebarList(CharSequence term);

    void show(User user);

    void onPreparedToLogOut();
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void onRoomSelected(RoomSidebar roomSidebar);

    void onSpotlightSelected(Spotlight spotlight);

    Flowable<List<Spotlight>> searchSpotlight(String term);

    Flowable<List<SpotlightUser>>searchSpotlight(String term, User user);

    void disposeSubscriptions();

    void onUserOnline();

    void onUserAway();

    void onUserBusy();

    void onUserOffline();

    void onLogout(Continuation<Void, Object> continuation);

    void prepareToLogOut();

    void createDirectMessageForMobile(String userName);

    void hideRoomForMobile(User user,String rid);

    void setHint(User user);
  }
}