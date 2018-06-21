package chat.rocket.android.video.model;

/**
 * Created by Administrator on 2018/4/28/028.
 */

public class RemoteUser {
    private String username ;
    private String avar ;

    public RemoteUser(String username, String avar) {
        this.username = username;
        this.avar = avar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvar() {
        return avar;
    }

    public void setAvar(String avar) {
        this.avar = avar;
    }
}
