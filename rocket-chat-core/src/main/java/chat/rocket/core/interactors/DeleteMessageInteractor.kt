package chat.rocket.core.interactors

import chat.rocket.core.PermissionsConstants
import chat.rocket.core.PublicSettingsConstants
import chat.rocket.core.models.Message
import chat.rocket.core.models.PublicSetting
import chat.rocket.core.models.Room
import chat.rocket.core.models.User
import chat.rocket.core.repositories.MessageRepository
import chat.rocket.core.repositories.PublicSettingRepository
import chat.rocket.core.repositories.RoomRepository
import chat.rocket.core.repositories.UserRepository
import chat.rocket.core.utils.Pair
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import io.reactivex.functions.Function4
import java.util.*

class DeleteMessageInteractor(private val permissionInteractor: PermissionInteractor,
                            private val userRepository: UserRepository,
                            private val messageRepository: MessageRepository,
                            private val roomRepository: RoomRepository,
                            private val publicSettingRepository: PublicSettingRepository) {

    fun isAllowed(message: Message): Single<Boolean> {
        return Single.zip<Optional<User>, Optional<Room>, Optional<PublicSetting>, Optional<PublicSetting>, Pair<Optional<Room>, Boolean>>(
                userRepository.getCurrent().first(Optional.absent()),
                roomRepository.getById(message.roomId).first(Optional.absent()),
                publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_DELETING),
                publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_DELETING_BLOCK_TIMEOUT),
                Function4 { user, room, allowDelete, deleteTimeout ->
                    val deleteAllowed = allowDelete.isPresent && allowDelete.get().valueAsBoolean

                    val deleteTimeLimitInMinutes = deleteTimeout.longValue()

                    val deleteAllowedInTime = if (deleteTimeLimitInMinutes > 0) {
                        (Calendar.getInstance().timeInMillis-message.timestamp).millisToMinutes() < deleteTimeLimitInMinutes
                    } else {
                        true
                    }




                    val deleteOwn = user.isPresent && user.get().id == message.user?.id

                    Pair.create(room, deleteAllowed && deleteAllowedInTime && deleteOwn)
                }
        )
                .flatMap { (room, deleteAllowed) ->
                    if (!room.isPresent) {
                        return@flatMap Single.just(false)
                    }

                    permissionInteractor.isAllowed(PermissionsConstants.DELETE_MESSAGE, room.get())
                            .map { it || deleteAllowed }
                }
    }
}

fun Optional<PublicSetting>.longValue(defaultValue: Long = 0) = if (this.isPresent) {
    this.get().valueAsLong
} else {
    defaultValue
}

fun Long.millisToMinutes() = this / 60_000