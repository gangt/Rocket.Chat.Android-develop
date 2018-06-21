package chat.rocket.android;

import android.content.Context;
import android.content.Intent;

import chat.rocket.android.activity.AddServerActivity;
import chat.rocket.android.activity.LoginActivity;
import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.login.SharedPreferencesUtil;
import chat.rocket.android.video.view.VideoChatActivity;

import static chat.rocket.android.activity.ChatMainActivity.IS_DESTROY;

/**
 * utility class for launching Activity.
 */
public class LaunchUtil {

    /**
     * launch ChatMainActivity with proper flags.
     */
    public static void showMainActivity(Context context) {
        Intent intent = new Intent(context, ChatMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * launch AddServerActivity with proper flags.
     */
    public static void showAddServerActivity(Context context) {
        Intent intent = new Intent(context, AddServerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * launch ServerConfigActivity with proper flags.
     */
    public static void showLoginActivity(Context context, String hostname) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(LoginActivity.KEY_HOSTNAME, hostname);
        context.startActivity(intent);
    }

    /***
     *
     * @param name
     * @param avar
     * @param channelId
     * @param isCall         true: 主叫  false : 被叫
     */
    public static void showVideoActivity(String userId,String name,String avar,String channelId, boolean isCall,boolean isVideo){
        checkChaMainActivity();
        Intent intent = new Intent(RocketChatApplication.getInstance(), VideoChatActivity.class);
        intent.setFlags(Intent. FLAG_ACTIVITY_NEW_TASK );
        intent.putExtra("id",userId);
        intent.putExtra("name",name);
        intent.putExtra("avar",avar);
        intent.putExtra("channel",channelId);
        intent.putExtra("isCall",isCall);
        intent.putExtra("isVideo",isVideo);
        RocketChatApplication.getInstance().startActivity(intent);
    }

    public static void showVideoActivityFromNotification(String userId,String name,String avar,String channelId, boolean isCall,boolean isVideo,String mediaId){
        checkChaMainActivity();
        Intent intent = new Intent(RocketChatApplication.getInstance(), VideoChatActivity.class);
        intent.setFlags(Intent. FLAG_ACTIVITY_NEW_TASK );
        intent.putExtra("id",userId);
        intent.putExtra("name",name);
        intent.putExtra("avar",avar);
        intent.putExtra("channel",channelId);
        intent.putExtra("isCall",isCall);
        intent.putExtra("isVideo",isVideo);
        intent.putExtra("mediaId",mediaId);
        RocketChatApplication.getInstance().startActivity(intent);
    }

    public  static void checkChaMainActivity(){
        if(ChatMainActivity.getChatMainActivity().get() == null){
            showMainActivity( RocketChatApplication.getInstance());
        }
    }

}
