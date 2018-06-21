package chat.rocket.android.video.model;

/**
 * Created by Administrator on 2018/5/7/007.
 */

public class VideoChatBusEvent {
    private int chat ;

    public VideoChatBusEvent(int chat) {
        this.chat = chat;
    }

    public int getChat() {
        return chat;
    }
}
