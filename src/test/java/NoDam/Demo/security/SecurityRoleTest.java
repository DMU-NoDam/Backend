package NoDam.Demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.domain.UserRole;
import NoDam.Demo.user.repository.UserRepository;
import NoDam.Demo.user.service.JWTService;
import NoDam.Demo.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "token.SECRET_KEY=testSecretKeyForTestCasesOnly1234567890abcdef",
        "token.ACCESS_EXPIRATION_SECOND=3600",
        "token.REFRESH_EXPIRATION_SECOND=7200"
})
public class SecurityRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 시 유저 권한은 VISITOR여야 한다")
    void registerUserRoleIsVisitor() {
        // given
        String email = "visitor@test.com";
        String password = "password";
        String name = "VisitorName";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User registeredUser = userService.registerWithEmail(email, password, name);

        // then
        assertThat(registeredUser.getRole()).isEqualTo(UserRole.VISITOR);
    }

    @Test
    @DisplayName("VISITOR 권한을 가진 유저는 /domain/visit, /domain/api에 접근 가능해야 한다")
    void visitorAccessTest() throws Exception {
        // given
        User visitorUser = User.builder()
                .email("visitor@test.com")
                .role(UserRole.VISITOR)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(visitorUser));
        String accessToken = jwtService.generateAccessToken(1L); // 아무 ID 값 사용

        // then: /domain/visit 접근 가능 (VISITOR 권한 필요)
        mockMvc.perform(get("/domain/visit")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // then: /domain/api 접근 가능 (VISITOR는 USER 권한도 포함함)
        mockMvc.perform(get("/domain/api")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // then: /domain/admin 접근 불가
        mockMvc.perform(get("/domain/admin")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("USER 권한을 가진 유저는 /domain/api에만 접근 가능해야 한다")
    void userAccessTest() throws Exception {
        // given
        User user = User.builder()
                .email("user@test.com")
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        String accessToken = jwtService.generateAccessToken(1L); // 아무 ID 값 사용

        // then: /domain/visit 접근 불가
        mockMvc.perform(get("/domain/visit")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden());

        // then: /domain/api 접근 가능
        mockMvc.perform(get("/domain/api")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // then: /domain/admin 접근 불가
        mockMvc.perform(get("/domain/admin")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN 권한을 가진 유저는 /domain/admin, /domain/api에 접근 가능해야 한다")
    void adminAccessTest() throws Exception {
        // given
        User adminUser = User.builder()
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(adminUser));
        String accessToken = jwtService.generateAccessToken(1L); // 아무 ID 값 사용

        // then: /domain/visit 접근 불가
        mockMvc.perform(get("/domain/visit")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden());

        // then: /domain/api 접근 가능 (ADMIN은 USER 권한도 포함함)
        mockMvc.perform(get("/domain/api")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // then: /domain/admin 접근 가능
        mockMvc.perform(get("/domain/admin")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증되지 않은 유저는 public 혹은 permitAll 경로 외에는 접근 불가여야 한다")
    void unauthenticatedAccessTest() throws Exception {
        // then: /test 는 permitAll
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk());

        // then: /domain/visit 접근 불가
        mockMvc.perform(get("/domain/visit"))
                .andExpect(status().isForbidden());

        // then: /domain/api 접근 불가
        mockMvc.perform(get("/domain/api"))
                .andExpect(status().isForbidden());

        // then: /domain/admin 접근 불가
        mockMvc.perform(get("/domain/admin"))
                .andExpect(status().isForbidden());
    }
}
