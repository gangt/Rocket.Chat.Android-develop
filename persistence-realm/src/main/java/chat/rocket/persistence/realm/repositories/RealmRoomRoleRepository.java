package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomRole;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.RoomRoleRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmRoomRole;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Realm;
import io.realm.RealmResults;

public class RealmRoomRoleRepository extends RealmRepository implements RoomRoleRepository {

    private final String hostname;

    public RealmRoomRoleRepository(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public Single<Optional<RoomRole>> getFor(Room room, User user) {
        return Single.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }
                    return pair.first.where(RealmRoomRole.class)
                            .equalTo(RealmRoomRole.Columns.ROOM_ID, room.getId())
                            .equalTo(RealmRoomRole.Columns.USER + "." + RealmUser.ID, user.getId())
                            .findAll()
                            .<RealmResults<RealmRoomRole>>asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(it -> it.isLoaded() && it.isValid())
                .map(it -> {
                    if (it.size() == 0) {
                        return Optional.<RoomRole>absent();
                    }
                    return Optional.of(it.get(0).asRoomRole());
                })
                .first(Optional.absent()));
    }

    public RoomRole getRoomRoleByRid(String rid){
        Realm realm = RealmStore.getRealm(hostname);
        if(realm==null){
            return null;
        }

        RealmRoomRole first = realm.where(RealmRoomRole.class)
                .equalTo(RealmRoomRole.Columns.ROOM_ID, rid)
                .findFirst();
        close(realm,Looper.myLooper());
        if(first == null) return null;
        return first.asRoomRole();
    }

    public List<RoomRole> getRoomRoleAll(String rid){
        Realm realm = RealmStore.getRealm(hostname);
        if(realm==null){
            return null;
        }

        RealmResults<RealmRoomRole> all = realm.where(RealmRoomRole.class)
                .equalTo(RealmRoomRole.Columns.ROOM_ID, rid)
                .findAll();
        close(realm,Looper.myLooper());
        if(all == null) return null;
        return toList(all);
    }

    private List<RoomRole> toList(List<RealmRoomRole> realmUsers) {
        int total = realmUsers.size();

        final List<RoomRole> userList = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            userList.add(realmUsers.get(i).asRoomRole());
        }

        return userList;
    }
}
