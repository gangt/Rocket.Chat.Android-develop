package chat.rocket.android.video.helper

/**
 * Created by Administrator on 2018/5/14/014.
 */
enum class SendType (var nType : String){

    AUDIO("audio") , VIDEO("video");

    fun  getType() = nType

}