package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.domain.TripInvitation;
import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.trip.dto.response.TripInvitationPreviewInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    TripInvitationFacadeService 단위 test (domain service mock)

    getOrCreateLink : OWNER 확인 후 링크 발급을 위임하는지
    previewByToken : token -> trip 조회 -> 미리보기 정보 변환
    joinByToken : MEMBER로 추가, ALREADY_JOINED_TRIP은 멱등 성공 처리, 그 외 에러는 그대로 전파
 */
@ExtendWith(MockitoExtension.class)
class TripInvitationFacadeServiceTest {

    private static final Long TRIP_ID = 1L;
    private static final Long OWNER_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final String TOKEN = "invite-token";

    @Mock
    private TripInvitationService tripInvitationService;

    @Mock
    private TripMemberService tripMemberService;

    @Mock
    private TripSelectService tripSelectService;

    @InjectMocks
    private TripInvitationFacadeService tripInvitationFacadeService;

    private TripInvitation createInvitation() {
        return TripInvitation.builder()
                .tripId(TRIP_ID)
                .inviterUserId(OWNER_ID)
                .token(TOKEN)
                .build();
    }

    @Nested
    @DisplayName("초대 링크 발급")
    class GetOrCreateLink {

        @Test
        @DisplayName("OWNER면 domain service의 링크를 그대로 반환한다")
        void getOrCreateLink() {
            TripInvitation invitation = createInvitation();
            when(tripInvitationService.getOrCreateLinkInvitation(TRIP_ID, OWNER_ID)).thenReturn(invitation);

            TripInvitation result = tripInvitationFacadeService.getOrCreateLink(TRIP_ID, OWNER_ID);

            assertSame(invitation, result);
            verify(tripMemberService).requireOwner(TRIP_ID, OWNER_ID);
        }

        @Test
        @DisplayName("OWNER가 아니면 NOT_AUTHOR, 링크를 발급하지 않는다")
        void getOrCreateLinkNotOwner() {
            doThrow(new CustomException(ErrorCode.NOT_AUTHOR))
                    .when(tripMemberService).requireOwner(TRIP_ID, USER_ID);

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripInvitationFacadeService.getOrCreateLink(TRIP_ID, USER_ID));

            assertEquals(ErrorCode.NOT_AUTHOR, exception.errorCode);
            verify(tripInvitationService, never()).getOrCreateLinkInvitation(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("초대 링크 미리보기")
    class PreviewByToken {

        @Test
        @DisplayName("token의 여행 정보를 미리보기로 변환한다")
        void previewByToken() {
            Trip trip = Trip.builder()
                    .name("도쿄 여행")
                    .userId(OWNER_ID)
                    .uuid("trip-uuid")
                    .personCount(2)
                    .startDate(LocalDate.of(2026, 12, 1))
                    .endDate(LocalDate.of(2026, 12, 3))
                    .isFixed(false)
                    .build();
            when(tripInvitationService.findByToken(TOKEN)).thenReturn(createInvitation());
            when(tripSelectService.findByIdPublic(TRIP_ID)).thenReturn(trip);

            TripInvitationPreviewInfo result = tripInvitationFacadeService.previewByToken(TOKEN);

            assertEquals("도쿄 여행", result.getTripName());
            assertEquals("2026-12-01", result.getStartDate());
            assertEquals("2026-12-03", result.getEndDate());
        }

        @Test
        @DisplayName("token이 없으면 NOT_FOUND, 여행을 조회하지 않는다")
        void previewByTokenNotFound() {
            when(tripInvitationService.findByToken(TOKEN)).thenThrow(new CustomException(ErrorCode.NOT_FOUND));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripInvitationFacadeService.previewByToken(TOKEN));

            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
            verify(tripSelectService, never()).findByIdPublic(anyLong());
        }
    }

    @Nested
    @DisplayName("초대 링크 참여")
    class JoinByToken {

        @Test
        @DisplayName("token의 여행에 MEMBER로 추가한다")
        void joinByToken() {
            when(tripInvitationService.findByToken(TOKEN)).thenReturn(createInvitation());
            when(tripMemberService.addMember(TRIP_ID, USER_ID, TripMemberRole.MEMBER))
                    .thenReturn(TripMember.builder().tripId(TRIP_ID).userId(USER_ID).role(TripMemberRole.MEMBER).build());

            tripInvitationFacadeService.joinByToken(TOKEN, USER_ID);

            verify(tripMemberService).addMember(TRIP_ID, USER_ID, TripMemberRole.MEMBER);
        }

        @Test
        @DisplayName("이미 참여 중(ALREADY_JOINED_TRIP)이면 에러 없이 성공 처리한다")
        void joinByTokenAlreadyJoined() {
            when(tripInvitationService.findByToken(TOKEN)).thenReturn(createInvitation());
            when(tripMemberService.addMember(TRIP_ID, USER_ID, TripMemberRole.MEMBER))
                    .thenThrow(new CustomException(ErrorCode.ALREADY_JOINED_TRIP));

            assertDoesNotThrow(() -> tripInvitationFacadeService.joinByToken(TOKEN, USER_ID));
        }

        @Test
        @DisplayName("ALREADY_JOINED_TRIP 외의 에러는 그대로 전파한다")
        void joinByTokenOtherError() {
            when(tripInvitationService.findByToken(TOKEN)).thenReturn(createInvitation());
            when(tripMemberService.addMember(TRIP_ID, USER_ID, TripMemberRole.MEMBER))
                    .thenThrow(new CustomException(ErrorCode.CONFLICT));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripInvitationFacadeService.joinByToken(TOKEN, USER_ID));

            assertEquals(ErrorCode.CONFLICT, exception.errorCode);
        }

        @Test
        @DisplayName("token이 없으면 NOT_FOUND, 멤버를 추가하지 않는다")
        void joinByTokenNotFound() {
            when(tripInvitationService.findByToken(TOKEN)).thenThrow(new CustomException(ErrorCode.NOT_FOUND));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripInvitationFacadeService.joinByToken(TOKEN, USER_ID));

            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
            verify(tripMemberService, never()).addMember(anyLong(), anyLong(), any());
        }
    }
}
