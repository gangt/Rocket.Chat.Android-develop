package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Subscription;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmRoomRole;
import chat.rocket.persistence.realm.models.ddp.RealmSubscription;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.models.internal.RealmSession;

/**
 * message type.
 */
public enum MessageType {
  ROOM_NAME_CHANGED("r") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_room_name_changed,
          MessageType.getTarget(message), getUsername(message));
    }
  },
  USER_ADDED("au") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_user_added_by,
              getUsername(message),message.getMessage());
    }
  },
  USER_REMOVED("ru") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_user_removed_by,
          message.getMessage(), getUsername(message));
    }
  },
  USER_JOINED("uj") {
    @Override
    public String getString(Context context, Message message) {
      return  String.format(context.getString(R.string.message_user_joined_channel),getUsername(message));
    }
  },
  USER_LEFT("ul") {
    @Override
    public String getString(Context context, Message message) {
      return  String.format(context.getString(R.string.message_user_left),getUsername(message));
    }
  },
  WELCOME("wm") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_welcome, getUsername(message));
    }
  },
  MESSAGE_REMOVED("rm") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_removed);
    }
  },
  MESSAGE_DELETE("md") {
    @Override
    public String getString(Context context, Message message) {
      return String.format(context.getString(R.string.message_delete),getUsername(message).equals(RocketChatCache.INSTANCE.getUserName())?"你":getUsername(message));
    }
  },
    MESSAGE_CHANGED_DESCRIPTION("room_changed_description") {
    @Override
    public String getString(Context context, Message message) {
      return String.format(context.getString(R.string.room_changed_description),getUsername(message),MessageType.getTarget(message));
    }
  },
  MESSAGE_USER_MUTED("user-muted") {
    @Override
    public String getString(Context context, Message message) {
      return String.format(context.getString(R.string.message_user_muted),message.getMessage(),getUsername(message));
    }
  },
  MESSAGE_USER_UNMUTED("user-unmuted") {
    @Override
    public String getString(Context context, Message message) {
      return String.format(context.getString(R.string.message_user_unmuted),message.getMessage(),getUsername(message));
    }
  },
  MESSAGE_ROLE_ADDED("subscription-role-added") {
    @Override
    public String getString(Context context, Message message) {
      return String.format(context.getString(R.string.message_role_added),getUsername(message),message.getMessage(),message.getAlias().equals("moderator")?"管理员":"群主");
    }
  },
  MESSAGE_ROLE_REMOVED("subscription-role-removed") {
    @Override
    public String getString(Context context, Message message) {
      return String.format(context.getString(R.string.message_role_removed),getUsername(message),message.getMessage(),message.getAlias().equals("moderator")?"管理员":"群主");
    }
  },
  MESSAGE_CHANGED_TOPIC("room_changed_topic") {
    @Override
    public String getString(Context context, Message message) {
      return String.format(context.getString(R.string.message_update_topic),getUsername(message),MessageType.getTarget(message));
    }
  },
  ROOM_CHANGED_HOST("room_changed_host") {
    @Override
    public String getString(Context context, Message message) {
      return String.format(context.getString(R.string.room_changed_host),getUsername(message),MessageType.getTarget(message));
    }
  },
  MESSAGE_PINNED("message_pinned") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_pinned);
    }
  },
  UNSPECIFIED("");
  //------------

  private final String value;

  MessageType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static MessageType parse(String value) {
    for (MessageType type : MessageType.values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return UNSPECIFIED;
  }

  public String getString(Context context, Message message) {
    return "";
  }

  private static String getUsername(Message message) {
    if (message != null && message.getUser() != null) {
      return message.getUser().getRealName();
    } else {
      return "";
    }
  }

  private static String getTarget(Message message) {
    if (message != null && message.getUser() != null) {
      return message.getMessage().split("@")[0];
    } else {
      return "";
    }
  }
  /**
   * 管理员"moderator",
   * 群主"owner"
   * */
  private static String getRoleAdd(String role){
    RealmHelper realmHelper= RealmStore.getOrCreate(RocketChatCache.INSTANCE.getSelectedServerHostname());
    List<RealmRoomRole> roomRoles = realmHelper.executeTransactionForReadResults(realm ->
            realm.where(RealmRoomRole.class)
                    .equalTo(RealmRoomRole.Columns.ID, role)
                    .findAll());
    return roomRoles.get(0).getRoles().first().asRole().getName();
  }

}
