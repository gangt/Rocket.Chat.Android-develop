package chat.rocket.android.video.model

import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.widgets.GlideRoundTransform
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/**
 * Created by Administrator on 2018/4/28/028.
 */
class RemoteUserRender(val user: RemoteUser) {

    /**
     * Show user's avatar image
     */
    fun showAvatar(img: ImageView) {
        val avatar = user.avar
        val options = RequestOptions().placeholder(R.drawable.default_hd_avatar_round)
                .error(R.drawable.default_hd_avatar).transform(GlideRoundTransform(RocketChatApplication.getInstance()))
        Glide.with(RocketChatApplication.getInstance())
                .load(avatar).apply(options)
                .into(img)
    }

    /**
     * Show username in textView.
     */
    fun showUsername(textView: TextView) {
        val realName : String ? = user.username
        if (realName != null) {
            textView.text = realName
        }
    }

}