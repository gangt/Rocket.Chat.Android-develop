package chat.rocket.persistence.realm.models.ddp;

import chat.rocket.core.models.SpotlightUser;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmSpotlightUser extends RealmObject {

    public interface Columns {
        String ID = "_id";
        String USERNAME = "username";
        String NAME = "name";
        String STATUS = "status";
        String REALNAME="realName";
    }

    @PrimaryKey
    private String _id;
    private String username;
    private String name;
    private String status;
    private boolean isDeleted;
    private String realName;

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SpotlightUser asSpotlightUser() {
        return SpotlightUser.builder()
                .setId(_id)
                .setUsername(username)
                .setName(name)
                .setStatus(status)
                .setRealName(realName)
                .setDeleted(isDeleted)
                .build();
    }
}
