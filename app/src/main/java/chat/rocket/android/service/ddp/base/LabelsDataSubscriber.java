package chat.rocket.android.service.ddp.base;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.log.RCLog;
import chat.rocket.core.JsonConstants;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.repositories.RealmLabels;
import io.realm.RealmObject;

/**
 * "userData" subscriber.
 */
public class LabelsDataSubscriber extends AbstractBaseSubscriber {
  public LabelsDataSubscriber(Context context, String hostname, RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
  }

  @Override
  protected String getSubscriptionName() {
    return "labelGroups";
  }

  @Override
  protected String getSubscriptionCallbackName() {
    return "labelGroupsList";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return RealmLabels.class;
  }

  @Override
  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
    json = super.customizeFieldJson(json);
//    RCLog.d("->>>>>>>>"+ RealmLabels.customizeJson(json));
    return RealmLabels.customizeJson(json);

  }
}
