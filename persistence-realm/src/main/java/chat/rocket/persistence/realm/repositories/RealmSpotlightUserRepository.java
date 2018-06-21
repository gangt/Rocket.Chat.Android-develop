package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.SortDirection;
import chat.rocket.core.models.SpotlightUser;
import chat.rocket.core.repositories.SpotlightUserRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlightUser;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Case;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmSpotlightUserRepository extends RealmRepository implements SpotlightUserRepository {

    private final String hostname;

    public RealmSpotlightUserRepository(String hostname) {
        this.hostname = hostname;
    }

    public Flowable<List<SpotlightUser>> getSuggestionsFor(String name) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }
                    return pair.first.where(RealmSpotlightUser.class)
                            .like(RealmSpotlightUser.Columns.REALNAME, "*" + name + "*", Case.INSENSITIVE)
                            .findAll()
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(it -> it != null && it.isLoaded() && it.isValid())
                .map(realmSpotlightUsers -> toList(realmSpotlightUsers)));
//        Realm realm = RealmStore.getRealm(hostname);
//        if (realm == null) {
//            return null;
//        }
//        RealmResults<RealmSpotlightUser> all = realm.where(RealmSpotlightUser.class)
//                .like(RealmSpotlightUser.Columns.USERNAME, "*" + name + "*", Case.INSENSITIVE)
//                .or()
//                .like(RealmSpotlightUser.Columns.REALNAME, "*" + name + "*", Case.INSENSITIVE)
//                .findAll();
//        close(realm, Looper.myLooper());
//        return toList(all);
    }

    @Override
    public Flowable<List<SpotlightUser>> getSuggestionsFor(String name, SortDirection direction, int limit) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmSpotlightUser.class)
                            .beginGroup()
                            .like(RealmSpotlightUser.Columns.USERNAME, "*" + name + "*", Case.INSENSITIVE)
                            .isNull(RealmSpotlightUser.Columns.NAME)
                            .endGroup()
                            .or()
                            .beginGroup()
                            .like(RealmSpotlightUser.Columns.NAME, "*" + name + "*", Case.INSENSITIVE)
                            .isNotNull(RealmSpotlightUser.Columns.USERNAME)
                            .endGroup()
                            .findAllSorted(RealmSpotlightUser.Columns.USERNAME,
                                    direction.equals(SortDirection.ASC) ? Sort.ASCENDING : Sort.DESCENDING)
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(it -> it != null && it.isLoaded() && it.isValid())
                .map(realmSpotlightUsers -> toList(safeSubList(realmSpotlightUsers, 0, limit))));
    }

    private List<SpotlightUser> toList(List<RealmSpotlightUser> realmSpotlightUsers) {
        int total = realmSpotlightUsers.size();

        final List<SpotlightUser> spotlightUsers = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            spotlightUsers.add(realmSpotlightUsers.get(i).asSpotlightUser());
        }

        return spotlightUsers;
    }
    private List<SpotlightUser> toList(RealmResults<RealmSpotlightUser> realmSpotlightUsers) {
        int total = realmSpotlightUsers.size();

        final List<SpotlightUser> spotlightUsers = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            spotlightUsers.add(realmSpotlightUsers.get(i).asSpotlightUser());
        }

        return spotlightUsers;
    }
}
