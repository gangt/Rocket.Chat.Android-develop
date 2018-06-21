package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import java.util.List;

import chat.rocket.core.SortDirection;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomHistoryState;
import chat.rocket.core.models.Subscription;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface SubscriptionRepository {

  Flowable<List<Subscription>> getAll();

  Flowable<Optional<Subscription>> getById(String roomId);

  Flowable<Optional<RoomHistoryState>> getHistoryStateByRoomId(String roomId);

  Single<Boolean> setHistoryState(RoomHistoryState roomHistoryState);

  Flowable<List<Subscription>> getSortedLikeName(String name, SortDirection direction, int limit);

  Flowable<List<Subscription>> getLatestSeen(int limit);
}
