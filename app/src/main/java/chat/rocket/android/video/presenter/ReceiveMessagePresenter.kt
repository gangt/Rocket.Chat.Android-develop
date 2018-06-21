package chat.rocket.android.video.presenter

import android.annotation.SuppressLint
import chat.rocket.android.LaunchUtil
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.RocketChatCache
import chat.rocket.android.activity.ChatMainActivity
import chat.rocket.android.video.helper.TempFileUtils
import chat.rocket.android.video.helper.VideoSendAvType
import chat.rocket.android.video.model.VideoBusEvent
import chat.rocket.android.video.model.VideoRequestModel
import chat.rocket.persistence.realm.repositories.RealmUserRepository
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import javax.inject.Inject

/**
 * Created by Administrator on 2018/5/14/014.
 */
class ReceiveMessagePresenter @Inject constructor() {
    private var username: String? = null
    private var tempTimestatmp: String? = null
    private var timestamp: String? = null
    private var remoteUserId: String? = null
    private var callingUserId: String? = null
    @Synchronized
    fun analyseVideoSend(json: JSONObject) {

        //音视频
        try {
            username = json.getJSONObject("calluser").getString("username")
            remoteUserId = json.getString("from")
            val _id = json.getJSONObject("calluser").getString("_id")
            val isvideo = json.getJSONObject("media").getBoolean("video")
            if (username == RocketChatCache.getUserUsername()) {
                return
            }

            if (json.has("mediaId")) {
                timestamp = json.getString("mediaId")
            }

            if (json.has("timestamp")) {
                timestamp = json.getString("timestamp")
            }

            if (timestamp != null && timestamp.equals(tempTimestatmp)) {
                return
            }
            tempTimestatmp = timestamp
            if (TempFileUtils.getInstance().callingUserId == null) {
                TempFileUtils.getInstance().saveCallingUserId(callingUserId)
            } else if (remoteUserId != null && TempFileUtils.getInstance().talkingStatus && !remoteUserId.equals(TempFileUtils.getInstance().callingUserId)) {
                if (json.getInt("av_type").equals(VideoSendAvType.INVITE)) {
                    //notify calling
                    EventBus.getDefault().post(VideoBusEvent(VideoSendAvType.TALKING, json))
                }
                return
            }

            TempFileUtils.getInstance().saveCallingUserId(remoteUserId)

            val type = json.getInt("av_type")

            if (type.equals(VideoSendAvType.CANCEL)) {
                val callingUserId1 = TempFileUtils.getInstance().callingUserId
                if (!callingUserId1.equals(remoteUserId)) {
                    return
                }
            }
            if (type.equals(VideoSendAvType.INVITE)) {
                if (ChatMainActivity.getChatMainActivity().get() != null && !ChatMainActivity.getChatMainActivity().get()!!.isAppOnForeground) {
                    return
                }
                LaunchUtil.showVideoActivity(_id, username, getAvar(), json.getString("room"), false, isvideo)
                return
            }
            EventBus.getDefault().post(VideoBusEvent(json.getInt("av_type"), null))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("RxLeakedSubscription")
    private fun getAvar(): String? {
        try {
            val repository = RealmUserRepository(RocketChatCache.getSelectedServerHostname())
            return repository.getAvatarByUsername(username)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}