package NoDam.Demo.user.service;

import NoDam.Demo.common.event.DeleteEvent;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.dto.request.UpdateUserInfoDto;
import NoDam.Demo.user.oauth.OAuthUserInfo;
import NoDam.Demo.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final TransactionTemplate transactionTemplate;

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public User loginWithOAuth(OAuthUserInfo userInfo) {
        Optional<User> userOpt = userRepository.findByOAuthIdAndProvider(userInfo.getOAuthId(), userInfo.getOAuthProvider());

        if(userOpt.isPresent())
            return userOpt.get();
        else {
            // try register
            User newUser = transactionTemplate.execute(status -> {
                User user = User.builder()
                        .name(userInfo.getName())
                        .oAuthProvider(userInfo.getOAuthProvider())
                        .oAuthId(userInfo.getOAuthId())
                        .build();

                // validate duplicate (oAuthId, provider) with write lock
                userRepository.findByOAuthIdAndProviderWithLock(user.getOAuthId(), user.getOAuthProvider()).ifPresent(
                        (u)-> {throw new CustomException(ErrorCode.CONFLICT);}
                );

                return userRepository.save(user);
            });
            return newUser;
        }
    }

    public User updateUserInfo(
            User user,
            UpdateUserInfoDto updateDto
    ) {
        user.update(updateDto.getName());

        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
        eventPublisher.publishEvent(new DeleteEvent<User>(user.getId(), user.getId()));
    }

}
