package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import java.util.List;

import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Subscription;
import chat.rocket.core.models.User;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface MessageRepository {

  Single<Optional<Message>> getById(String messageId);

  Single<Boolean> save(Message message);

  Single<Boolean> delete(Message message);

  Boolean delete(String msgId);

  Flowable<List<Message>> getAllFrom(Subscription subscription);

  Flowable<List<Message>> getAllFrom(Room room);

  Single<Integer> unreadCountFor(Subscription subscription, User user);
}
