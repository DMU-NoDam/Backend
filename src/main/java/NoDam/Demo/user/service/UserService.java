package NoDam.Demo.user.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.repository.UserRepository;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
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

    @Validated
    public User registerWithEmail(
            @NotEmpty String email,
            @NotEmpty String password,
            String name
    ) {
        String encodePassword = passwordEncoder.encode(password);

        return transactionTemplate.execute(status -> {
            if(userRepository.findByEmail(email).isPresent())
                throw new CustomException(ErrorCode.CONFLICT);

            return userRepository.save(User
                    .builder()
                    .email(email)
                    .name(name)
                    .password(encodePassword)
                    .build()
            );
        });
    }

    @Validated
    public User login(
            @NotEmpty String email,
            @NotEmpty String password
    ) {
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new CustomException(ErrorCode.NOT_FOUND)
        );

        if(user.login(passwordEncoder, password))
            return user;

        throw new CustomException(ErrorCode.CONFLICT);
    }

}
