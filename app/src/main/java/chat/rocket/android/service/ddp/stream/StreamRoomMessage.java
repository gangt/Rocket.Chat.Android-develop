package chat.rocket.android.service.ddp.stream;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.helper.eventbus.BaseEvent;
import chat.rocket.android.helper.eventbus.EventTags;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.video.helper.ChatStatus;
import chat.rocket.android.video.helper.TempFileUtils;
import chat.rocket.android.video.model.VideoRefreshBusEvent;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import io.realm.RealmObject;

/**
 * stream-room-message subscriber.
 */
public class StreamRoomMessage extends AbstractStreamNotifyEventSubscriber {
    private String roomId;

    public StreamRoomMessage(Context context, String hostname,
                             RealmHelper realmHelper, String roomId) {
        super(context, hostname, realmHelper);
        this.roomId = roomId;
    }

    @Override
    protected String getSubscriptionName() {
        return "stream-room-messages";
    }

    @Override
    protected String getSubscriptionParam() {
        return roomId;
    }

    @Override
    protected Class<? extends RealmObject> getModelClass() {
        return RealmMessage.class;
    }

    @Override
    protected String getPrimaryKeyForModel() {
        return "_id";
    }

    @Override
    protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
//    RCLog.d("->>>>>>>>"+RealmMessage.customizeJson(super.customizeFieldJson(json)).toString());
        try {
            if (json.has("nType") && json.get("nType") != null && json.getString("mediaId") != null ) {
                RCLog.d("->>>>>>>>" + json.getString("mediaId"));
                TempFileUtils.getInstance().saveCurrentMediaId(json.getString("mediaId"));
                EventBus.getDefault().post(new VideoRefreshBusEvent());
            }
            if (json.has("t") && json.getString("t").equals("md")) {
                BaseEvent baseEvent = new BaseEvent();
                baseEvent.setCode(EventTags.DELETE_SUCESS);
                baseEvent.setMsg(json.getString("msgId"));
                EventBus.getDefault().post(baseEvent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return RealmMessage.customizeJson(super.customizeFieldJson(json));
    }
}
