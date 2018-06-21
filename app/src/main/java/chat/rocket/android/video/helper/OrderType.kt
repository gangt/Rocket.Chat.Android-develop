package chat.rocket.android.video.helper

/**
 * Created by Administrator on 2018/5/14/014.
 */
enum class OrderType(var str : String) {
    //音频邀请（3），视频邀请（4）,拒绝 (0) , 取消（1）  ,接听 （2） 挂断（5）

    AUDIO_INVITE("3")  ,VIDEO_INVITE("4")  ,REJECT("0"),
    CANCEL("1")  ,      ACCEPT("2")        ,HANG_UP("5");

    fun getType()  = str
}