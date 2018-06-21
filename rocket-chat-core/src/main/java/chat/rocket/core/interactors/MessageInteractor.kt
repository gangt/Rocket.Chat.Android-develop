package chat.rocket.core.interactors

import chat.rocket.core.SyncState
import chat.rocket.core.models.*
import chat.rocket.core.repositories.MessageRepository
import chat.rocket.core.repositories.SubscriptionRepository
import com.hadisatrio.optional.Optional
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

class MessageInteractor(private val messageRepository: MessageRepository,
                        private val subscriptionRepository: SubscriptionRepository) {

    fun loadMessages(subscription: Subscription): Single<Boolean> {
        var unreadint = 20
        if (Integer.parseInt( subscription.unread) > 20) {
            unreadint = Integer.parseInt( subscription.unread)
        }
        val roomHistoryState = RoomHistoryState.builder()
                .setRoomId(subscription.rid)
                .setSyncState(SyncState.NOT_SYNCED)
                .setCount(unreadint)
                .setReset(true)
                .setComplete(false)
                .setTimestamp(0)
                .build()

        return subscriptionRepository.setHistoryState(roomHistoryState)
    }

    fun loadMoreMessages(subscription: Subscription): Single<Boolean> {
        return subscriptionRepository.getHistoryStateByRoomId(subscription.rid)
                .filter { it.isPresent }
                .map { it.get() }
                .filter { roomHistoryState ->
                    val syncState = roomHistoryState.syncState
                    !roomHistoryState.isComplete && (syncState == SyncState.SYNCED || syncState == SyncState.FAILED)
                }
                .map { Optional.of(it) }
                .first(Optional.absent())
                .flatMap { historyStateOptional ->
                    if (!historyStateOptional.isPresent) {
                        return@flatMap Single.just(false)
                    }
                    subscriptionRepository
                            .setHistoryState(historyStateOptional.get().withCount(20).withSyncState(SyncState.NOT_SYNCED))
                }
    }

    fun send(subscription: Subscription, sender: User, messageText: String): Single<Boolean> {
        val message = Message.builder()
                .setId(UUID.randomUUID().toString().replace("-",""))
                .setSyncState(SyncState.NOT_SYNCED)
                .setTimestamp(System.currentTimeMillis())
                .setRoomId(subscription.rid)
                .setMessage(messageText)
                .setGroupable(false)
                .setUser(sender)
                .setEditedAt(0)
                .build()

        return messageRepository.save(message)
    }


    fun resend(message: Message, sender: User): Single<Boolean> {
        return messageRepository.save(
                message.withSyncState(SyncState.NOT_SYNCED).withUser(sender))
    }

    fun update(message: Message, sender: User, content: String): Single<Boolean> {
        return messageRepository.save(
                message.withSyncState(SyncState.NOT_SYNCED)
                        .withUser(sender)
                        .withMessage(content)
                        .withEditedAt(message.editedAt + 1))
    }

    fun delete(message: Message): Single<Boolean> {
        return messageRepository.save(message.withSyncState(SyncState.DELETE_NOT_SYNCED))
    }

    fun delete(msgId: String): Boolean {
        return messageRepository.delete(msgId)
    }
    /**
     * Resets the message syncstate to SYNCED after a user has accepted a failed delete
     */
    fun acceptDeleteFailure(message: Message): Single<Boolean> {
        return messageRepository.save(message.withSyncState(SyncState.SYNCED))
    }

    fun unreadCountFor(subscription: Subscription, user: User): Single<Int> {
        return messageRepository.unreadCountFor(subscription, user)
    }

    fun getAllFrom(subscription: Subscription): Flowable<List<Message>> {
        return messageRepository.getAllFrom(subscription)
    }
}
