package NoDam.Demo.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.oauth.OAuthUserInfo;
import NoDam.Demo.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TransactionStatus transactionStatus;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(transactionTemplate, userRepository, eventPublisher);
    }

    @Test
    @DisplayName("OAuth 유저가 이미 있으면 기존 유저를 반환한다")
    void loginWithOAuthReturnsExistingUser() {
        OAuthUserInfo userInfo = oAuthUserInfo();
        User existingUser = User.builder()
                .name("tester")
                .oAuthProvider("google")
                .oAuthId("google-id")
                .build();

        when(userRepository.findByOAuthIdAndProvider("google-id", "google")).thenReturn(Optional.of(existingUser));

        User result = userService.loginWithOAuth(userInfo);

        assertThat(result).isSameAs(existingUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("OAuth 신규 유저는 provider와 oauthId로만 생성한다")
    void loginWithOAuthCreatesUserWithoutEmail() {
        OAuthUserInfo userInfo = oAuthUserInfo();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        stubTransactionTemplate();
        when(userRepository.findByOAuthIdAndProvider("google-id", "google")).thenReturn(Optional.empty());
        when(userRepository.findByOAuthIdAndProviderWithLock("google-id", "google")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.loginWithOAuth(userInfo);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(result).isSameAs(savedUser);
        assertThat(savedUser.getName()).isEqualTo("tester");
        assertThat(savedUser.getOAuthProvider()).isEqualTo("google");
        assertThat(savedUser.getOAuthId()).isEqualTo("google-id");
        assertThat(savedUser.isOAuthUser()).isTrue();
    }

    @Test
    @DisplayName("OAuth 신규 생성 중 중복이 확인되면 예외를 던진다")
    void loginWithOAuthThrowsWhenDuplicateOAuthUserExists() {
        OAuthUserInfo userInfo = oAuthUserInfo();
        User duplicateUser = User.builder()
                .name("duplicate")
                .oAuthProvider("google")
                .oAuthId("google-id")
                .build();

        stubTransactionTemplate();
        when(userRepository.findByOAuthIdAndProvider("google-id", "google")).thenReturn(Optional.empty());
        when(userRepository.findByOAuthIdAndProviderWithLock("google-id", "google")).thenReturn(Optional.of(duplicateUser));

        assertThatThrownBy(() -> userService.loginWithOAuth(userInfo))
                .isInstanceOf(CustomException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    private OAuthUserInfo oAuthUserInfo() {
        return OAuthUserInfo.builder()
                .name("tester")
                .oAuthProvider("google")
                .oAuthId("google-id")
                .build();
    }

    @SuppressWarnings("unchecked")
    private void stubTransactionTemplate() {
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(transactionStatus);
        });
    }
}
