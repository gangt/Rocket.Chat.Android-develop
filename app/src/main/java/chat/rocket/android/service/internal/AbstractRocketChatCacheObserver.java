package chat.rocket.android.service.internal;

import com.hadisatrio.optional.Optional;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.Registrable;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import chat.rocket.persistence.realm.models.ddp.RealmSubscription;
import io.reactivex.disposables.CompositeDisposable;

public abstract class AbstractRocketChatCacheObserver implements Registrable {
  private final RealmHelper realmHelper;
  private String roomId;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  protected AbstractRocketChatCacheObserver(RealmHelper realmHelper) {
    this.realmHelper = realmHelper;
  }

  private void updateRoomIdWith(String roomId) {
    if (!TextUtils.isEmpty(roomId)) {
      RealmSubscription room = realmHelper.executeTransactionForRead(realm ->
          realm.where(RealmSubscription.class).equalTo("rid", roomId).findFirst());
      if (room != null) {
        if (this.roomId == null || !this.roomId.equals(roomId)) {
          this.roomId = roomId;
          onRoomIdUpdated(roomId);
        }
        return;
      }
    }

    if (this.roomId != null) {
      this.roomId = null;
      onRoomIdUpdated(null);
    }
  }

  protected abstract void onRoomIdUpdated(String roomId);

  @Override
  public final void register() {
    compositeDisposable.add(
        RocketChatCache.INSTANCE
            .getSelectedRoomIdPublisher()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .subscribe(this::updateRoomIdWith, RCLog::e)
    );
  }

  @Override
  public final void unregister() {
    compositeDisposable.clear();
  }
}
