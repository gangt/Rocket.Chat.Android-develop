package chat.rocket.android.video.helper

import chat.rocket.android.R
import chat.rocket.android.RocketChatApplication

/**
 * Created by Tidom on 2018/5/19/019.
 */
object  MediaPlayerEngine  {

    var sound: AlarmSound? = null

    fun playSounde(nTime: Int) {
        if (sound == null) {
            sound = AlarmSound(RocketChatApplication.getInstance().applicationContext)
        }
        sound!!.playBeepSound(nTime, R.raw.beep)
    }

    fun stopSound() {
        if (sound != null) {
            sound!!.release()
            sound = null
        }
    }

}