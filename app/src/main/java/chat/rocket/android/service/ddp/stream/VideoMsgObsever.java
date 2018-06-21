package chat.rocket.android.service.ddp.stream;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.login.SharedPreferencesUtil;
import chat.rocket.android.video.presenter.ReceiveMessagePresenter;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import io.realm.RealmObject;

import static chat.rocket.android.activity.ChatMainActivity.IS_DESTROY;

public class VideoMsgObsever extends AbstractStreamNotifyUserEventSubscriber {

    private Context contex;
    static ReceiveMessagePresenter presenter;

    static {
        presenter = new ReceiveMessagePresenter();

    }

    public VideoMsgObsever(Context context, String hostname,
                           RealmHelper realmHelper,
                           String userId) {
        super(context, hostname, realmHelper, userId);
        this.contex = context;
    }

    @Override
    protected String getSubscriptionSubParam() {
        return "webrtc";
    }

    @Override
    protected Class<? extends RealmObject> getModelClass() {
        return RealmRoom.class;
    }

    @Override
    protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
        if(!SharedPreferencesUtil.getBooleanData(context, ChatMainActivity.IS_DESTROY,false)){
            presenter.analyseVideoSend(json);
        }
        return RealmRoom.customizeJson(super.customizeFieldJson(json));
    }

    @Override
    protected String getPrimaryKeyForModel() {
        return "rid";
    }
}
