package chat.rocket.persistence.realm.models.ddp;

import chat.rocket.core.models.Mention;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user on 2018/3/13.
 */

public class RealmMention extends RealmObject {
    @PrimaryKey private String  _id;
    private String username;
    private String name;

    public Mention asMention(){
        return Mention.builder()
                .setId(_id)
                .setUsername(username)
                .setName(name)
                .build();
    }
}
