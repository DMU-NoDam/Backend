package NoDam.Demo.trip.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import NoDam.Demo.conf.security.AuthorityMapper;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.CreateTripRequest;
import NoDam.Demo.trip.service.TripService;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.domain.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TripController.class)
@AutoConfigureMockMvc(addFilters = false)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripService tripService;

    private User user;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("tester")
                .oAuthId("id")
                .oAuthProvider("kakao")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        auth = new UsernamePasswordAuthenticationToken(user, null, user.getRole().getAuthorities(AuthorityMapper::toSpringAuthority));
        
        // SecurityContext에 직접 주입
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("여행 생성 API 성공")
    void createTripApiSuccess() throws Exception {
        // given
        CreateTripRequest request = new CreateTripRequest();
        request.setName("Tokyo Trip");
        request.setUuid("uuid-1234");
        request.setPersonCount(2);
        request.setSite("일본");
        request.setTripThemeType(TripThemeType.HEALING);
        request.setStartDate("2026-05-01");
        request.setEndDate("2026-05-05");

        Trip trip = Trip.builder()
                .name("Tokyo Trip")
                .uuid("uuid-1234")
                .userId(1L)
                .siteId(1L)
                .tripThemeType(TripThemeType.HEALING)
                .startDate(LocalDate.of(2026, 5, 1))
                .endDate(LocalDate.of(2026, 5, 5))
                .build();

        when(tripService.createTrip(any(), any(CreateTripRequest.class))).thenReturn(trip);

        // when & then
        mockMvc.perform(post("/trip/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.body.name").value("Tokyo Trip"))
                .andExpect(jsonPath("$.body.tripThemeType").value("HEALING"))
                .andExpect(jsonPath("$.body.site").value("일본"));
    }

    @Test
    @DisplayName("여행 리스트 조회 API 성공")
    void getTripListApiSuccess() throws Exception {
        // given
        Trip trip = Trip.builder()
                .name("Tokyo Trip")
                .uuid("uuid-1234")
                .siteId(1L)
                .startDate(LocalDate.of(2026, 5, 1))
                .endDate(LocalDate.of(2026, 5, 5))
                .build();

        when(tripService.getTripList(anyLong())).thenReturn(List.of(trip));

        // when & then
        mockMvc.perform(get("/trip/api"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.body[0].name").value("Tokyo Trip"));
    }
}
