package chat.rocket.android.video.helper

/**
 * Created by Administrator on 2018/5/14/014.
 */
enum class ChatHistoryOrder(var str : String) {
    TALKING_TIME("talking_time") ,TALKING_CANCEL("talking_cancel"), TALKING_REJECT("talking_reject");
    fun  getType() = str

}