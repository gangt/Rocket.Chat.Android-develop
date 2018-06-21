package chat.rocket.android.video.model;

/**
 * Created by Administrator on 2018/5/29/029.
 */


/**
 *   isVideo = getIntent().getBooleanExtra("isVideo", true);
 userName = getIntent().getStringExtra("name");
 avar = getIntent().getStringExtra("avar");
 if ("".equals(avar)) {
 RealmUserRepository repository = new RealmUserRepository(RocketChatCache.INSTANCE.getSelectedServerHostname());
 avar = repository.getAvatarByUsername(userName);
 }
 if (userName.indexOf("&") != -1) {
 userName = userName.substring(0, userName.indexOf("&"));
 }
 channel = getIntent().getStringExtra("channel");
 isCall = getIntent().getBooleanExtra("isCall", true);
 id = getIntent().getStringExtra("id");
 mediaId = getIntent().getStringExtra("mediaId");
 */

public interface FloatingViewCommon {

    String IS_VIDEO = "isVideo";
    String  IS_CALL = "isCall";
}
