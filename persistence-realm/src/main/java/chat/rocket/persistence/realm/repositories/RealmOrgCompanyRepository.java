package chat.rocket.persistence.realm.repositories;

import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.models.OrgCompany;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmOrgCompany;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.internal.util.Pair;

public class RealmOrgCompanyRepository extends RealmRepository{

    private final String hostname;

    public RealmOrgCompanyRepository(String hostname) {
        this.hostname = hostname;
    }

    public List<OrgCompany> getOrgCompanyListAll() {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmOrgCompany> all = realm.where(RealmOrgCompany.class)
                .findAll();
        close(realm, Looper.myLooper());
        if(all == null || all.size() == 0) {
            return null;
        }
        return toList(all);
    }

    public Flowable<List<OrgCompany>> getOrgCompanyAllFlow(){
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmOrgCompany.class)
                            .findAll()
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second))
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
                        && roomSubscriptions.isValid())
                .map(this::getFirstList));
    }

    public List<OrgCompany> getOrgCompanyListFirst() {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmOrgCompany> all = realm.where(RealmOrgCompany.class)
//                .equalTo(RealmOrgCompany.ORGTYPE, RealmOrgCompany.DEMID)
//                .or()
//                .beginGroup()
//                .equalTo(RealmOrgCompany.ORGTYPE, "2")
//                .and()
//                .equalTo(RealmOrgCompany.ORGSUPID, "1")
//                .endGroup()
//                .or()
//                .beginGroup()
//                .equalTo(RealmOrgCompany.ORGTYPE, "5")
//                .and()
//                .equalTo(RealmOrgCompany.ORGSUPID, "1")
                .findAll();
        close(realm, Looper.myLooper());
        if(all == null || all.size() == 0) {
            return null;
        }
        return getFirstList(all);
    }

    private List<OrgCompany> getFirstList(RealmResults<RealmOrgCompany> realmRooms) {
        int total = realmRooms.size();
        final List<OrgCompany> roomList = new ArrayList<>();

        // 第一层数据集合
        for (int i = 0; i < total; i++) {
            OrgCompany orgCompany = realmRooms.get(i).asOrgCompany();
            String orgSupId = orgCompany.getOrgSupId();
            String orgType=orgCompany.getOrgType();
            if(orgSupId != null && orgSupId.equals(orgCompany.getDemId())&&("1".equals(orgType) || "2".equals(orgType) || "5".equals(orgType))) {
                roomList.add(orgCompany);
            }
        }

        return roomList;
    }

    public List<OrgCompany> getOrgCompanyListToOrgId(String orgId) {
        Realm realm = RealmStore.getRealm(hostname);
        if (realm == null) {
            return null;
        }
        RealmResults<RealmOrgCompany> all = realm.where(RealmOrgCompany.class)
                .equalTo(RealmOrgCompany.ORGSUPID, orgId)
                .and()
                .findAll();
        close(realm, Looper.myLooper());
        if(all == null || all.size() == 0) {
            return null;
        }
        return toList(all);
    }

    private List<OrgCompany> toList(RealmResults<RealmOrgCompany> realmRooms) {
        int total = realmRooms.size();

        final List<OrgCompany> roomList = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            roomList.add(realmRooms.get(i).asOrgCompany());
        }

        return roomList;
    }

}
