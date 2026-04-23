package NoDam.Demo.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import NoDam.Demo.user.service.JWTService;
import NoDam.Demo.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(JWTService.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "token.SECRET_KEY=testSecretKeyForTestCasesOnly1234567890abcdef",
        "token.ACCESS_EXPIRATION_SECOND=3600000",
        "token.REFRESH_EXPIRATION_SECOND=7200000"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("POST /user/public/token-refresh")
    class TokenRefresh {

        @Test
        @DisplayName("성공 - 새 토큰 반환")
        void success() throws Exception {
            String refreshToken = jwtService.generateRefreshToken(1L);

            Map<String, String> body = Map.of("token", refreshToken);

            mockMvc.perform(post("/user/public/token-refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("실패 - 토큰 누락 -> 400")
        void failNoToken() throws Exception {
            mockMvc.perform(post("/user/public/token-refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 refresh 토큰 -> 409")
        void failInvalidToken() throws Exception {
            Map<String, String> body = Map.of("token", "invalid.jwt.token");

            mockMvc.perform(post("/user/public/token-refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }
}
