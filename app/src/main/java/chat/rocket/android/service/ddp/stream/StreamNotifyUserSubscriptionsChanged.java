package chat.rocket.android.service.ddp.stream;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.log.RCLog;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmSubscription;
import io.realm.RealmObject;

public class StreamNotifyUserSubscriptionsChanged extends AbstractStreamNotifyUserEventSubscriber {
  public StreamNotifyUserSubscriptionsChanged(Context context, String hostname,
                                              RealmHelper realmHelper,
                                              String userId) {
    super(context, hostname, realmHelper, userId);
  }

  @Override
  protected String getSubscriptionSubParam() {
    return "subscriptions-changed";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return RealmSubscription.class;
  }

  @Override
  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
//    RCLog.d("subscriptions-changed->>>>>>>>"+ json.toString());
    return RealmSubscription.customizeJson(super.customizeFieldJson(json));
  }

  @Override
  protected String getPrimaryKeyForModel() {
    return "rid";
  }
}
