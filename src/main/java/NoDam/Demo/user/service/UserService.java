package NoDam.Demo.user.service;

import NoDam.Demo.common.domain.DomainResult;
import NoDam.Demo.common.event.CreateEvent;
import NoDam.Demo.common.event.DeleteEvent;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.dto.request.UpdateUserInfoDto;
import NoDam.Demo.user.oauth.OAuthUserInfo;
import NoDam.Demo.user.repository.UserRepository;
import jakarta.validation.constraints.NotEmpty;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
public class UserService {

    private final TransactionTemplate transactionTemplate;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Validated
    public User registerWithEmail(
            @NotEmpty String email,
            @NotEmpty String password,
            String name
    ) {
        String encodePassword = passwordEncoder.encode(password);

        User createdUser = transactionTemplate.execute(status -> {
            if(userRepository.findByEmail(email).isPresent())
                throw new CustomException(ErrorCode.CONFLICT);

            return userRepository.save(
                    User.emailUser(email, name, encodePassword)
            );
        });

        eventPublisher.publishEvent(new CreateEvent<User>(createdUser.getId(), createdUser.getId()));
        return createdUser;
    }

    public User loginWithOAuth(OAuthUserInfo userInfo) {
        Optional<User> userOpt = userRepository.findByOAuthIdAndProvider(userInfo.getOAuthId(), userInfo.getOAuthProvider());

        if(userOpt.isPresent())
            return userOpt.get();
        else {
            // try register
            User newUser = transactionTemplate.execute(status -> {
                User user = User.oAuthUser(userInfo.getName(), userInfo.getEmail(), userInfo.getOAuthProvider(), userInfo.getOAuthId());

                // validate duplicate (oAuthId,provider), email (with write lock)
                Optional<User> userOAuthOpt = userRepository.findByOAuthIdAndProviderWithLock(user.getOAuthId(), user.getOAuthProvider());
                Optional<User> userEmailOpt = userRepository.findByEmail(user.getEmail());

                if(userOAuthOpt.isPresent() || userEmailOpt.isPresent())
                    throw new CustomException(ErrorCode.CONFLICT);

                return userRepository.save(user);
            });
            return newUser;
        }
    }

    @Validated
    public DomainResult<User> loginWithEmail(
            @NotEmpty String email,
            @NotEmpty String password
    ) {
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new CustomException(ErrorCode.NOT_FOUND)
        );

        if(passwordEncoder.matches(password, user.getPassword()))
            return DomainResult.success(user);

        return DomainResult.fail();
    }

    public User updateUserInfo(
            User user,
            UpdateUserInfoDto updateDto
    ) {
        // update password
        if(
            updateDto.getNewPassword() != null &&  // new Password not null
            !updateDto.getNewPassword().isEmpty() && // new Password not empty
            updateDto.getOldPassword() != null // old Password not null -> throws at password encoder
        ) {
            if (passwordEncoder.matches(updateDto.getOldPassword(), user.getPassword()))
                user.updatePassword(passwordEncoder.encode(updateDto.getNewPassword()));
            else
                throw new CustomException(ErrorCode.CONFLICT);
        }

        // update user info
        user.update(updateDto.getName());

        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
        eventPublisher.publishEvent(new DeleteEvent<User>(user.getId(), user.getId()));
    }

}
