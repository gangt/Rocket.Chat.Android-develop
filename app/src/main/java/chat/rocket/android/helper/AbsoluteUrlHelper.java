package chat.rocket.android.helper;

import com.hadisatrio.optional.Optional;

import chat.rocket.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.core.models.Session;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.ServerInfoRepository;
import chat.rocket.core.repositories.UserRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class AbsoluteUrlHelper {

    private final String hostname;
    private final ServerInfoRepository serverInfoRepository;
    private final UserRepository userRepository;
    private final SessionInteractor sessionInteractor;

    public AbsoluteUrlHelper(String hostname,
                             ServerInfoRepository serverInfoRepository,
                             UserRepository userRepository,
                             SessionInteractor sessionInteractor) {
        this.hostname = hostname;
        this.serverInfoRepository = serverInfoRepository;
        this.userRepository = userRepository;
        this.sessionInteractor = sessionInteractor;
    }

    public Single<Optional<RocketChatAbsoluteUrl>> getRocketChatAbsoluteUrl() {
        return Flowable.zip(
                serverInfoRepository.getByHostname(hostname)
                        .filter(serverInfoOptional -> {
                                    boolean present = serverInfoOptional.isPresent();
                                    return present;
                                }
                        )
                        .map(serverInfoOptional1 -> {
                                    ServerInfo serverInfo = serverInfoOptional1.get();
                                    return serverInfo;
                                }
                        ),
                userRepository.getCurrent()
                        .filter(userOptional -> {
                                    boolean present = userOptional.isPresent();
                                    return present;
                                }
                        )
                        .map(userOptional1 -> {
                                    User user = userOptional1.get();
                                    return user;
                                }
                        ),
                sessionInteractor.getDefault()
                        .filter(sessionOptional -> {
                                    boolean present = sessionOptional.isPresent();
                                    return present;
                                }
                        )
                        .map(sessionOptional1 -> {
                                    Session session = sessionOptional1.get();
                                    return session;
                                }
                        ),
                (info, user, session) -> Optional.of(new RocketChatAbsoluteUrl(
                        info, user, session
                ))
        )
                .first(Optional.absent());
    }
}
