package chat.rocket.android.fragment.chatroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.widget.RoomToolbar;
import chat.rocket.core.models.User;

public abstract class AbstractChatRoomFragment extends AbstractFragment {
  private RoomToolbar roomToolbar;
  private TextView mTvTitle;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    roomToolbar = getActivity().findViewById(R.id.activity_main_toolbar);
    mTvTitle=getActivity().findViewById(R.id.tv_title);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  protected void setToolbarTitle(CharSequence title) {
    if(title.toString().length()==0)
    roomToolbar.getMenu().clear();
    roomToolbar.hideChannelIcons();
    roomToolbar.setTitle(title);
    mTvTitle.setText(title);
  }

  protected void showToolbarPrivateChannelIcon() {
    roomToolbar.showPrivateChannelIcon();
  }

  protected void showToolbarPublicChannelIcon() {
    roomToolbar.showPublicChannelIcon();
  }

  protected void showToolbarLivechatChannelIcon() {
    roomToolbar.showLivechatChannelIcon();
  }

  protected void showToolbarUserStatuslIcon(@Nullable String status) {
    if (status == null) {
      roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_OFFLINE);
    } else {
      switch (status) {
        case User.STATUS_ONLINE:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_ONLINE);
          break;
        case User.STATUS_BUSY:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_BUSY);
          break;
        case User.STATUS_AWAY:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_AWAY);
          break;
        default:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_OFFLINE);
          break;
      }
    }
  }
}