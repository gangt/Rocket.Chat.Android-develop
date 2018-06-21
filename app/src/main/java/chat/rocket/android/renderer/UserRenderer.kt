package chat.rocket.android.renderer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.widgets.GlideRoundTransform
import chat.rocket.core.models.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class UserRenderer(val user: User) {

    /**
     * Show user's avatar image in RocketChatAvatar widget.
     */
    fun showAvatar(rocketChatAvatarWidget: ImageView, hostname: String) {
        val avatar = user?.avatar
        val options = RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar)
        Glide.with(RocketChatApplication.getInstance())
                .load(avatar).apply(options)
                .into(rocketChatAvatarWidget)
    }

    /**
     * Show username in textView.
     */
    fun showUsername(textView: TextView) {
        val realName : String ? = user?.realName

        if (realName != null) {
            textView.text = realName
        }
    }

    /**
     * Show user's status color in imageView.
     */
    fun showStatusColor(imageView: ImageView) {
        val userStatus: String? = user.status
        if (userStatus != null) {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(RocketChatUserStatusProvider.getStatusResId(userStatus))
        } else {
            imageView.visibility = View.GONE
        }
    }
    fun showStatusInfo(textView: TextView){
        val userStatus: String? = user.status
        if (userStatus != null) {
            textView.setText(RocketChatUserStatusProvider.getStatusInfo(userStatus));
        }
    }

    fun showStatus(textView: TextView){
        val userStatus: String? = user.status
        if (userStatus != null) {
            textView.setText(RocketChatUserStatusProvider.getStatus(userStatus));
        }
    }
}