package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.models.Labels;
import chat.rocket.core.models.Permission;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.LabelsRepository;
import chat.rocket.core.repositories.PermissionRepository;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmPermission;
import chat.rocket.persistence.realm.models.ddp.RealmSubscription;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Realm;
import io.realm.RealmResults;

public class RealmLabelsRepository extends RealmRepository implements LabelsRepository {

    private final String hostname;

    public RealmLabelsRepository(String hostname) {
        this.hostname = hostname;
    }

    public Flowable<List<Labels>> getAll() {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmLabels.class)
                            .findAll()
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second))
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(labels -> labels != null && labels.isLoaded()
                        && labels.isValid())
                .map(this::toList));
    }
    @Override
    public List<Labels> getByType(String type, String companyId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmLabels> all = realm.where(RealmLabels.class)
                .equalTo(RealmLabels.Columns.TYPE, type)
                .and()
                .equalTo(RealmLabels.Columns.COMPANYID, companyId)
                .findAll();
        close(realm,Looper.myLooper());
        return toList(all);
    }
    public List<Labels> getByTypeNoCompanyId(String type, String companyId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmLabels> all = realm.where(RealmLabels.class)
                .equalTo(RealmLabels.Columns.TYPE, type)
                .and()
                .notEqualTo(RealmLabels.Columns.COMPANYID, companyId)
                .findAll();
        close(realm,Looper.myLooper());
        return toList(all);
    }
    private List<Labels> toList(List<RealmLabels> realmUsers) {
        int total = realmUsers.size();

        final List<Labels> userList = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            userList.add(realmUsers.get(i).asLabels());
        }

        return userList;
    }
}
