package chat.rocket.android.service.ddp.base;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.JsonConstants;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import io.realm.RealmObject;

/**
 * "activeUsers" subscriber.
 */
public class ActiveUsersSubscriber extends AbstractBaseSubscriber {
  public ActiveUsersSubscriber(Context context, String hostname, RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
  }

  @Override
  protected String getSubscriptionName() {
    return "activeUsers";
  }

  @Override
  protected String getSubscriptionCallbackName() {
    return "users";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return RealmUser.class;
  }

  @Override
  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
//    RCLog.d("->>>>>>>>"+ json.toString());
    json = super.customizeFieldJson(json);

    // The user object may have some children without a proper primary key (ex.: settings)
    // Here we identify this and add a local key
    // Only happens here when the logged user receives its own data
    if (json.has("settings")) {
      final JSONObject settingsJson = json.getJSONObject("settings");
      settingsJson.put("id", json.getString("_id"));

      if (settingsJson.has("preferences")) {
        final JSONObject preferencesJson = settingsJson.getJSONObject("preferences");
        preferencesJson.put("id", json.getString("_id"));
      }

      try {
        if (json.has("deptRole")&& TextUtils.isEmpty(json.getString("deptRole"))){
          json.remove("deptRole");
          json.put("deptRole",new JSONArray());
        }
      } catch (JSONException e) {
      }
      try {
        if (json.has("allRoles")&& TextUtils.isEmpty(json.getString("allRoles"))){
          json.remove("allRoles");
        }
      } catch (JSONException e) {
      }
//      if(!json.isNull(RealmUser.CREATEDAT)){
//        long createdAt= 0;
//        try {
//          createdAt = json.getJSONObject(RealmUser.CREATEDAT).getLong(JsonConstants.DATE);
//        } catch (JSONException e) {
//          createdAt = json.getLong(RealmUser.CREATEDAT);
//        }
//        json.remove(RealmUser.CREATEDAT);
//        json.put(RealmUser.CREATEDAT,createdAt);
//      }
//      if(!json.isNull(RealmUser.UPDATEDAT)){
//        long updatedAt=json.getJSONObject(RealmUser.UPDATEDAT).getLong(JsonConstants.DATE);
//        json.remove(RealmUser.UPDATEDAT);
//        json.put(RealmUser.UPDATEDAT,updatedAt);
//      }
    }

    return json;
  }
}
