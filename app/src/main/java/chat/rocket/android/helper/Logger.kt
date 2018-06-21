package chat.rocket.android.helper

import android.util.Log
import chat.rocket.android.BuildConfig
//import com.crashlytics.android.Crashlytics
//import com.google.firebase.crash.FirebaseCrash

object Logger {

    fun report(throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace()
        }
//        FirebaseCrash.report(throwable)
//        Crashlytics.logException(throwable)
        Log.e("Logger", ""+throwable)
    }
}
