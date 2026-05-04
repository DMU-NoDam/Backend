package NoDam.Demo.security;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.domain.UserRole;
import NoDam.Demo.user.repository.UserRepository;
import NoDam.Demo.user.service.JWTService;
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
    private JWTService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("USER 권한을 가진 유저는 /domain/api에만 접근 가능해야 한다")
    void userAccessTest() throws Exception {
        User user = User.builder()
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        String accessToken = jwtService.generateAccessToken(1L);

        mockMvc.perform(get("/domain/api")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/domain/admin")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN 권한을 가진 유저는 /domain/admin, /domain/api에 접근 가능해야 한다")
    void adminAccessTest() throws Exception {
        User adminUser = User.builder()
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(adminUser));
        String accessToken = jwtService.generateAccessToken(1L);

        mockMvc.perform(get("/domain/api")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/domain/admin")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증되지 않은 유저는 public 혹은 permitAll 경로 외에는 접근 불가여야 한다")
    void unauthenticatedAccessTest() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/domain/api"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/domain/admin"))
                .andExpect(status().isForbidden());
    }
}
