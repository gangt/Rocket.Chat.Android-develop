package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.SortDirection;
import chat.rocket.core.models.RoomHistoryState;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.repositories.SubscriptionRepository;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlightUser;
import chat.rocket.persistence.realm.models.ddp.RealmSubscription;
import chat.rocket.persistence.realm.models.internal.LoadMessageProcedure;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmSubscriptionRepository extends RealmRepository implements SubscriptionRepository {

    private final String hostname;

    public RealmSubscriptionRepository(String hostname) {
        this.hostname = hostname;
    }

    public List<Subscription> searchHistorySetting(String companyId,String str){
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.TYPE, RealmSubscription.TYPE_SETTING)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "false")
                .and()
                .notEqualTo(RealmSubscription.Columns.COMPANYID, companyId)
                .and()
                .like(RealmSpotlightUser.Columns.USERNAME, "*" + str + "*", Case.INSENSITIVE)
                .findAll();
        close(realm,Looper.myLooper());
        return toList(all);
    }

    public Flowable<List<Subscription>> getHistorySetting(String companyId){
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmSubscription.class)
                            .equalTo(RealmSubscription.TYPE, RealmSubscription.TYPE_SETTING)
                            .and()
                            .equalTo(RealmSubscription.Columns.OPEN, "false")
                            .and()
                            .equalTo(RealmSubscription.Columns.COMPANYID, companyId)
                            .findAll()
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second))
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(this::toList));
    }

    public Flowable<List<Subscription>> getByUserName(String name,String userId){
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmSubscription.class)
                            .equalTo(RealmSubscription.Columns.NAME, name)
                            .and()
                            .equalTo(RealmSubscription.Columns.OPEN, "true")
                            .and()
                            .equalTo(RealmSubscription.Columns.UID, userId)
                            .findAll()
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second))
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(this::toList));
    }
    @Override
    public Flowable<List<Subscription>> getAll() {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmSubscription.class)
                            .findAll()
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second))
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(this::toList));
    }
    @Override
    public Flowable<Optional<RoomHistoryState>> getHistoryStateByRoomId(String roomId) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    LoadMessageProcedure messageProcedure = pair.first.where(LoadMessageProcedure.class)
                            .equalTo(LoadMessageProcedure.ID, roomId)
                            .findFirst();

                    if (messageProcedure == null) {
                        return Flowable.just(Optional.<LoadMessageProcedure>absent());
                    }

                    return messageProcedure.<LoadMessageProcedure>asFlowable()
                            .filter(loadMessageProcedure -> loadMessageProcedure.isLoaded()
                                    && loadMessageProcedure.isValid())
                            .map(Optional::of);
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .map(optional -> {
                    if (optional.isPresent()) {
                        return Optional.of(optional.get().asRoomHistoryState());
                    }
                    return Optional.absent();
                }));
    }
    public Subscription getByIdSub(String roomId){
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmSubscription all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.ROOM_ID, roomId)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN,"true")
                .findFirst();
        close(realm,Looper.myLooper());
        if(all==null){
            return null;
        }
        return all.asSubscription();
    }
    @Override
    public Flowable<Optional<Subscription>> getById(String roomId) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    RealmSubscription realmSubscription = pair.first.where(RealmSubscription.class)
                            .equalTo(RealmSubscription.ROOM_ID, roomId)
                            .findFirst();

                    if (realmSubscription == null) {
                        return Flowable.just(Optional.<RealmSubscription>absent());
                    }

                    return realmSubscription.<RealmSubscription>asFlowable()
                            .filter(
                                    roomSubscription -> roomSubscription.isLoaded()
                                            && roomSubscription.isValid())
                            .map(Optional::of);
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .map(optional -> {
                    if (optional.isPresent()) {
                        return Optional.of(optional.get().asSubscription());
                    }

                    return Optional.absent();
                }));
    }
    @Override
    public Single<Boolean> setHistoryState(RoomHistoryState roomHistoryState) {
        return Single.defer(() -> {
            final Realm realm = RealmStore.getRealm(hostname);
            final Looper looper = Looper.myLooper();

            if (realm == null || looper == null) {
                return Single.just(false);
            }

            LoadMessageProcedure loadMessage = new LoadMessageProcedure();
            loadMessage.setRoomId(roomHistoryState.getRoomId());
            loadMessage.setSyncState(roomHistoryState.getSyncState());
            loadMessage.setCount(roomHistoryState.getCount());
            loadMessage.setReset(roomHistoryState.isReset());
            loadMessage.setHasNext(!roomHistoryState.isComplete());
            loadMessage.setTimestamp(roomHistoryState.getTimestamp());

            return RealmHelper.copyToRealmOrUpdate(realm, loadMessage)
                    .filter(realmObject -> realmObject.isLoaded() && realmObject.isValid())
                    .firstElement()
                    .doOnEvent((realmObject, throwable) -> close(realm, looper))
                    .toSingle()
                    .map(realmObject -> true);
        });
    }

    @Override
    public Flowable<List<Subscription>> getSortedLikeName(String name, SortDirection direction, int limit) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }
                    return pair.first.where(RealmSubscription.class)
                            .like(RealmSubscription.NAME, "*" + name + "*", Case.INSENSITIVE)
                            .beginGroup()
                            .equalTo(RealmSubscription.TYPE, RealmSubscription.TYPE_CHANNEL)
                            .or()
                            .equalTo(RealmSubscription.TYPE, RealmSubscription.TYPE_PRIVATE)
                            .endGroup()
                            .findAllSorted(RealmSubscription.NAME,
                                    direction.equals(SortDirection.ASC) ? Sort.ASCENDING : Sort.DESCENDING)
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(realmRooms -> toList(safeSubList(realmRooms, 0, limit))));
    }
    @Override
    public Flowable<List<Subscription>> getLatestSeen(int limit) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }
                    return pair.first.where(RealmSubscription.class)
                            .beginGroup()
                            .equalTo(RealmSubscription.TYPE, RealmSubscription.TYPE_CHANNEL)
                            .or()
                            .equalTo(RealmSubscription.TYPE, RealmSubscription.TYPE_PRIVATE)
                            .endGroup()
                            .findAllSorted(RealmSubscription.LAST_SEEN, Sort.ASCENDING)
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(realmRooms -> toList(safeSubList(realmRooms, 0, limit))));
    }
    public List<Subscription> getByTypeNotCompanyIdAll(String type, String companyId,String uId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.Columns.TYPE, type)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .and()
                .notEqualTo(RealmSubscription.Columns.COMPANYID, companyId)
                .and()
                .equalTo(RealmSubscription.U_ID,uId)
                .findAll();
        close(realm,Looper.myLooper());
        return toList(all);
    }

    public int getByTypeCount(String type, String uId) {
        return getByTypeAll(type, uId).size();
    }
    public List<Subscription> getByTypeAll(String type,String companyId,String uId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.Columns.TYPE, type)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .and()
                .equalTo(RealmSubscription.Columns.UID,uId)
                .and()
                .equalTo(RealmSubscription.Columns.COMPANYID, companyId)
                .findAllSorted(RealmSubscription.LAST_SEEN,Sort.DESCENDING);
        close(realm,Looper.myLooper());
        return toList(all);
    }
    public List<Subscription> getByTypeAll(String type,String uId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.Columns.TYPE, type)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .and()
                .equalTo(RealmSubscription.Columns.UID,uId)
                .findAllSorted(RealmSubscription.UPDATED_AT,Sort.DESCENDING);
        close(realm,Looper.myLooper());
        return toList(all);
    }

    public Flowable<List<Subscription>> getAllSub(String uid) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmSubscription.class)
                            .equalTo(RealmSubscription.Columns.OPEN, "true")
                            .and()
                            .equalTo(RealmSubscription.Columns.UID,uid)
                            .findAllSorted(RealmSubscription.UPDATED_AT,Sort.DESCENDING)
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second))
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(this::toList));
    }
    public Subscription getByName(String name,String uid){
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmSubscription realmSubscription = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.Columns.NAME, name)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .and()
                .equalTo(RealmSubscription.Columns.UID, uid)
                .findFirst();
        close(realm,Looper.myLooper());
        if(realmSubscription==null){
            return null;
        }
        return realmSubscription.asSubscription();
    }
    public List<Subscription> getByTypeNoLeafAll(String type,String uId,String userCompanyId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.U_ID,uId)
                .and()
                .equalTo(RealmSubscription.Columns.TYPE, type)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.LABELID)
                .or()
                .isNull(RealmSubscription.Columns.LABELID)
                .endGroup()
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.LABELNAME)
                .or()
                .isNull(RealmSubscription.Columns.LABELNAME)
                .endGroup()
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.GID)
                .or()
                .isNull(RealmSubscription.Columns.GID)
                .endGroup()
                .and()
                .equalTo(RealmSubscription.Columns.GNAME, "SE_UCP")
                .and()
                .equalTo(RealmSubscription.Columns.COMPANYID,userCompanyId)
                .findAllSorted(RealmSubscription.UPDATED_AT, Sort.DESCENDING);
        close(realm,Looper.myLooper());
        return toList(all);
    }
    public List<Subscription> getByTypeNoLeafAndNoCompanyIdAll(String type,String uId,String userCompanyId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.U_ID,uId)
                .and()
                .equalTo(RealmSubscription.Columns.TYPE, type)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.LABELID)
                .or()
                .isNull(RealmSubscription.Columns.LABELID)
                .endGroup()
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.LABELNAME)
                .or()
                .isNull(RealmSubscription.Columns.LABELNAME)
                .endGroup()
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.GID)
                .or()
                .isNull(RealmSubscription.Columns.GID)
                .endGroup()
                .and()
                .equalTo(RealmSubscription.Columns.GNAME, "SE_UCP")
                .and()
                .notEqualTo(RealmSubscription.Columns.COMPANYID,userCompanyId)
                .findAll();
        close(realm,Looper.myLooper());
        return toList(all);
    }
    public List<Subscription> getByTypeNoLeafAll(String type,String uId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.U_ID,uId)
                .and()
                .equalTo(RealmSubscription.Columns.TYPE, type)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.LABELID)
                .or()
                .isNull(RealmSubscription.Columns.LABELID)
                .endGroup()
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.LABELNAME)
                .or()
                .isNull(RealmSubscription.Columns.LABELNAME)
                .endGroup()
                .and()
                .beginGroup()
                .isEmpty(RealmSubscription.Columns.GID)
                .or()
                .isNull(RealmSubscription.Columns.GID)
                .endGroup()
                .and()
                .equalTo(RealmSubscription.Columns.GNAME, "SE_UCP")
//                .and()
//                .equalTo(RealmSubscription.Columns.COMPANYID,userCompanyId)
                .findAll();
        close(realm,Looper.myLooper());
        return toList(all);
    }

    public List<Subscription> getByGIdAll(String gId, String gName,String uId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.U_ID,uId)
                .and()
                .equalTo(RealmSubscription.Columns.GID, gId)
                .and()
                .equalTo(RealmSubscription.Columns.GNAME, gName)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .findAll();
        close(realm,Looper.myLooper());
        return toList(all);
    }

    public List<Subscription> getByLabelAll(String labelId, String labelName,String uId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.U_ID,uId)
                .and()
                .equalTo(RealmSubscription.Columns.LABELID, labelId)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .and()
                .isEmpty(RealmSubscription.Columns.GID)
                .and()
                .equalTo(RealmSubscription.Columns.GNAME, "SE_UCP")
                .findAllSorted(RealmSubscription.UPDATED_AT, Sort.DESCENDING);
        close(realm, Looper.myLooper());
        return toList(all);
    }

    public List<Subscription> getSuggestionsFor(String name,String userid) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmSubscription> all = realm.where(RealmSubscription.class)
                .beginGroup()
//                .like(RealmSubscription.Columns.NAME, "*" + name + "*", Case.INSENSITIVE)
//                .or()
                .like(RealmSubscription.Columns.DISPLAYNAME, "*" + name + "*", Case.INSENSITIVE)
                .endGroup()

                .and()
                .notEqualTo(RealmSubscription.TYPE,"d")
                .and()
                .equalTo(RealmSubscription.U_ID,userid)
                .findAll();
        close(realm, Looper.myLooper());
        return toList(all);
    }

    public  Flowable<Optional<Subscription>> getSubscription(String rid){
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                   RealmSubscription subscription = pair.first.where(RealmSubscription.class)
                            .equalTo(RealmSubscription.ROOM_ID, rid)
                            .findFirst();

                    if (subscription == null) {
                        return Flowable.just(Optional.<RealmSubscription>absent());
                    }

                    return subscription.<RealmSubscription>asFlowable()
                            .filter(loadMessageProcedure -> loadMessageProcedure.isLoaded()
                                    && loadMessageProcedure.isValid())
                            .map(Optional::of);
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .map(optional -> {
                    if (optional.isPresent()) {
                        return Optional.of(optional.get().asSubscription());
                    }
                    return Optional.absent();
                }));
    }

    public Subscription getSubscriptionRid(String rid){
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmSubscription realmSubscription = realm.where(RealmSubscription.class)
                .equalTo(RealmSubscription.ROOM_ID, rid)
                .and()
                .equalTo(RealmSubscription.Columns.OPEN, "true")
                .findFirst();
        close(realm,Looper.myLooper());
        if(realmSubscription==null){
            return null;
        }
        return realmSubscription.asSubscription();
    }

    private List<Subscription> toList(List<RealmSubscription> realmUsers) {
        int total = realmUsers.size();

        final List<Subscription> list = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            list.add(realmUsers.get(i).asSubscription());
        }

        return list;
    }
    private List<Subscription> toList(RealmResults<RealmSubscription> realmUsers) {
        int total = realmUsers.size();

        final List<Subscription> list = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            list.add(realmUsers.get(i).asSubscription());
        }

        return list;
    }
}
