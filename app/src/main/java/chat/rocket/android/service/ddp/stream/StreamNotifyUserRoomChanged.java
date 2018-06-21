package chat.rocket.android.service.ddp.stream;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.log.RCLog;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import io.realm.RealmObject;

public class StreamNotifyUserRoomChanged extends AbstractStreamNotifyUserEventSubscriber {
  public StreamNotifyUserRoomChanged(Context context, String hostname,
                                     RealmHelper realmHelper,
                                     String userId) {
    super(context, hostname, realmHelper, userId);
  }

  @Override
  protected String getSubscriptionSubParam() {
    return "rooms-changed";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return RealmRoom.class;
  }

  @Override
  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
//    RCLog.d("rooms-changed->>>>>>>>"+ json.toString());
    return RealmRoom.customizeJson(super.customizeFieldJson(json));
  }

  @Override
  protected String getPrimaryKeyForModel() {
    return "rid";
  }
}
