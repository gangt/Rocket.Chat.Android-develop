package chat.rocket.persistence.realm.repositories;

import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.log.RCLog;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmUserEntity;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.internal.util.Pair;

public class RealmUserEntityRepository extends RealmRepository  {

    private final String hostname;

    public RealmUserEntityRepository(String hostname) {
        this.hostname = hostname;
    }

    public UserEntity getUserEntityForUsername(String type, String companyId, String username){
        Realm realm = RealmStore.getRealm(hostname);
        if(realm==null){
            return null;
        }

        RealmUserEntity first = realm.where(RealmUserEntity.class)
                .equalTo(RealmUserEntity.COMPANY_ID, companyId)
                .equalTo(RealmUserEntity.ROOM_TYPE, type)
                .equalTo(RealmUserEntity.USERNAME, username)
                .findFirst();
        close(realm,Looper.myLooper());
        if(first == null) return null;
        return first.asUserEntity();
    }

    public List<UserEntity> getUserEntityAll(String type, String companyId){
        Realm realm = RealmStore.getRealm(hostname);
        if(realm==null){
            return null;
        }

        try {
            RealmResults<RealmUserEntity> all = realm.where(RealmUserEntity.class)
                    .equalTo(RealmUserEntity.COMPANY_ID, companyId)
                    .equalTo(RealmUserEntity.ROOM_TYPE, type)
                    .findAll();
            close(realm,Looper.myLooper());
            return toList(all);
        }catch (Exception e){
            RCLog.e("getRealmUserEntityAll="+e.toString());
        }
        return null;
    }

    public Flowable<List<UserEntity>> getUserEntityAllFlow(String type,String company){
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmUserEntity.class)
                            .equalTo(RealmUserEntity.COMPANY_ID, company)
                            .and()
                            .equalTo(RealmUserEntity.ROOM_TYPE, type)
                            .findAll()
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second))
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(this::toList));
    }

    private List<UserEntity> toList(RealmResults<RealmUserEntity> list) {
        int total = list.size();

        final List<UserEntity> spotlightUsers = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            spotlightUsers.add(list.get(i).asUserEntity());
        }

        return spotlightUsers;
    }

}
