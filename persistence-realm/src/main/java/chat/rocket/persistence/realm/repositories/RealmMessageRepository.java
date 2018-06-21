package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.log.RCLog;
import chat.rocket.android.widget.helper.FileUtils;
import chat.rocket.core.SyncState;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.AttachmentTitle;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.MessageRepository;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.models.ddp.RealmUser1;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmMessageRepository extends RealmRepository implements MessageRepository {

    private final String hostname;

    public RealmMessageRepository(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public Single<Optional<Message>> getById(String messageId) {
        return Single.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmMessage.class)
                            .equalTo(RealmMessage.ID, messageId)
                            .findAll()
                            .<RealmResults<RealmMessage>>asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(it -> it.isLoaded() && it.isValid() && it.size() > 0)
                .map(realmMessages -> Optional.of(realmMessages.get(0).asMessage()))
                .first(Optional.absent()));
    }

    @Override
    public Single<Boolean> save(Message message) {
        return Single.defer(() -> {
            final Realm realm = RealmStore.getRealm(hostname);
            final Looper looper = Looper.myLooper();

            if (realm == null || looper == null) {
                return Single.just(false);
            }

            RealmMessage realmMessage = realm.where(RealmMessage.class)
                    .equalTo(RealmMessage.ID, message.getId())
                    .findFirst();

            if (realmMessage == null) {
                realmMessage = new RealmMessage();
            } else {
                realmMessage = realm.copyFromRealm(realmMessage);
            }

            realmMessage.setId(message.getId());
            realmMessage.setSyncState(message.getSyncState());
            realmMessage.setTimestamp(message.getTimestamp());
            realmMessage.setRoomId(message.getRoomId());
            realmMessage.setMessage(message.getMessage());
            realmMessage.setEditedAt(message.getEditedAt());
            realmMessage.setAttachments(message.getAttachmentsJson());
            realmMessage.setReport(message.getReportJson());
            realmMessage.setCard(message.getCardJson());
            realmMessage.setHidelink(message.getHidelink());
//            realmMessage.setuUserName(message.getUser().getUsername());
            RealmUser1 realmUser = realmMessage.getUser();
            if (realmUser == null) {
                realmUser = realm.where(RealmUser1.class)
                        .equalTo(RealmUser.ID, message.getUser().getId())
                        .findFirst();
            }
            realmMessage.setUser(realmUser);
//            realmMessage.setUser(message.getUserJson());

            final RealmMessage messageToSave = realmMessage;

            return RealmHelper.copyToRealmOrUpdate(realm, messageToSave)
                    .filter(it -> it.isLoaded() && it.isValid())
                    .first(new RealmMessage())
                    .doOnEvent((realmObject, throwable) -> close(realm, looper))
                    .map(realmObject -> true);
        });
    }

    @Override
    public Single<Boolean> delete(Message message) {
        return Single.defer(() -> {
            final Realm realm = RealmStore.getRealm(hostname);
            final Looper looper = Looper.myLooper();

            if (realm == null || looper == null) {
                return Single.just(false);
            }

            realm.beginTransaction();

            return realm.where(RealmMessage.class)
                    .equalTo(RealmMessage.ID, message.getId())
                    .findAll()
                    .<RealmResults<RealmMessage>>asFlowable()
                    .filter(realmObject -> realmObject.isLoaded() && realmObject.isValid())
                    .firstElement()
                    .toSingle()
                    .flatMap(realmMessages -> Single.just(realmMessages.deleteAllFromRealm()))
                    .doOnEvent((success, throwable) -> {
                        if (success) {
                            realm.commitTransaction();
                        } else {
                            realm.cancelTransaction();
                        }
                        close(realm, looper);
                    });
        });
    }

    @Override
    public Boolean delete(String msgId) {
        RealmHelper realmHelper = RealmStore.getOrCreate(hostname);
        realmHelper.executeTransaction(realm -> {
                    realm.where(RealmMessage.class)
                            .equalTo(RealmMessage.ID, msgId)
                            .findAll()
                            .deleteAllFromRealm();
                    close(realm,Looper.myLooper() );
                    return null;
                });

        return false;

    }

    public Flowable<List<Message>> getAllFrom(Room room) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmMessage.class)
                            .notEqualTo(RealmMessage.SYNC_STATE, SyncState.DELETE_NOT_SYNCED)
                            .notEqualTo(RealmMessage.SYNC_STATE, SyncState.DELETING)
                            .equalTo(RealmMessage.ROOM_ID, room.getRoomId())
                            .isNotNull(RealmMessage.USER)
                            .findAllSorted(RealmMessage.TIMESTAMP, Sort.DESCENDING)
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(it -> it.isLoaded() && it.isValid())
                .map(this::toList)
                .distinctUntilChanged());
    }
    @Override
    public Flowable<List<Message>> getAllFrom(Subscription subscription) {
        return Flowable.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmMessage.class)
                            .notEqualTo(RealmMessage.SYNC_STATE, SyncState.DELETE_NOT_SYNCED)
                            .notEqualTo(RealmMessage.SYNC_STATE, SyncState.DELETING)
                            .equalTo(RealmMessage.ROOM_ID, subscription.getRid())
                            .isNotNull(RealmMessage.USER)
                            .findAllSorted(RealmMessage.TIMESTAMP, Sort.DESCENDING)
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .filter(it -> it.isLoaded() && it.isValid())
                .map(this::toList)
                .distinctUntilChanged());
    }

    @Override
    public Single<Integer> unreadCountFor(Subscription subscription, User user) {
        return Single.defer(() -> Flowable.using(
                () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
                pair -> {
                    if (pair.first == null) {
                        return Flowable.empty();
                    }

                    return pair.first.where(RealmMessage.class)
                            .equalTo(RealmMessage.ROOM_ID, subscription.getId())
                            .greaterThanOrEqualTo(RealmMessage.TIMESTAMP, subscription.getLs())
                            .notEqualTo(RealmMessage.USER_ID, user.getId())
                            .findAll()
                            .asFlowable();
                },
                pair -> close(pair.first, pair.second)
        )
                .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
                .map(RealmResults::size)
                .firstElement()
                .toSingle());
    }

    public List<Message> getAllMessageByRoomId(String roomId){
        Realm realm = RealmStore.getRealm(hostname);
        if(realm==null){
            return null;
        }
        RealmResults<RealmMessage> all = realm.where(RealmMessage.class)
                .equalTo(RealmMessage.ROOM_ID, roomId)
                .findAll();
        close(realm,Looper.myLooper());

        return toListRoomNullMessage(all);
    }

    public List<Message> getAllMessageByMessageId(String msgId){
        Realm realm = RealmStore.getRealm(hostname);
        if(realm==null){
            return null;
        }
        RealmResults<RealmMessage> all = realm.where(RealmMessage.class)
                .equalTo(RealmMessage.ID, msgId)
                .findAll();
        close(realm,Looper.myLooper());

        return toList(all);
    }


    /**
     * 获取消息集合，去掉附件为空的数据
     * @param roomId
     * @return
     */
    public List<Message> getAllAttachmentByRoomId(String roomId){
        Realm realm = RealmStore.getRealm(hostname);
        if(realm==null){
            return null;
        }
        RealmResults<RealmMessage> all = realm.where(RealmMessage.class)
                .equalTo(RealmMessage.ROOM_ID, roomId)
                .isNotNull(RealmMessage.ATTACHMENTS)
                .findAll();
        close(realm,Looper.myLooper());

        List<Message> allMessageByRoomId = toList(all);

        if(allMessageByRoomId == null || allMessageByRoomId.size() == 0) return null;
        // 去除没有标题的附件，音频附件
        List<Message> temp = new ArrayList<>();
        for (Message message : allMessageByRoomId){
            List<Attachment> attachments = message.getAttachments();
            if(attachments == null||attachments.size()==0){
                temp.add(message);
                continue;
            }
            AttachmentTitle attachmentTitle = attachments.get(0).getAttachmentTitle();
            if(attachmentTitle == null || attachmentTitle.getTitle() == null){
                temp.add(message);
                continue;
            }
            if(FileUtils.isAudio(attachmentTitle.getTitle())){
                temp.add(message);
            }
        }
        allMessageByRoomId.removeAll(temp);

        return allMessageByRoomId;
    }

    private List<Message> toListRoomNullMessage(RealmResults<RealmMessage> realmMessages) {
        if(realmMessages == null || realmMessages.size() == 0) return null;
        final int total = realmMessages.size();
        final List<Message> messages = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            RealmMessage realmMessage = realmMessages.get(i);
            if(realmMessage != null && !TextUtils.isEmpty(realmMessage.getMessage())) {
                messages.add(realmMessage.asMessage());
            }
        }
        return messages;
    }

    private List<Message> toList(RealmResults<RealmMessage> realmMessages) {
        if(realmMessages == null || realmMessages.size() == 0) return null;
        final int total = realmMessages.size();
        final List<Message> messages = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            messages.add(realmMessages.get(i).asMessage());
        }
        return messages;
    }
}
