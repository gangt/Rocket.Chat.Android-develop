package chat.rocket.android.fragment.chatroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.widget.RoomToolbar;

/**
 * Created by jumper_C on 2018/5/4.
 */

public class HomeFragment extends AbstractChatRoomFragment {
    @Override
    protected int getLayout() {
        return R.layout.fragment_home;
    }

    @Override
    protected void onSetupView() {
        setToolbarTitle(getString(R.string.app_name_chat));
        if(RocketChatCache.INSTANCE.getHomePageVisiable())
            rootView.findViewById(R.id.image).setVisibility(View.VISIBLE);
        else
            rootView.findViewById(R.id.image).setVisibility(View.GONE);

        RoomToolbar toolbar = getActivity().findViewById(R.id.activity_main_toolbar);
        toolbar.getMenu().clear();
    }

}
