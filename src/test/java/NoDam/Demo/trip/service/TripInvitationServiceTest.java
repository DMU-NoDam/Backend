package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.TripInvitation;
import NoDam.Demo.trip.repository.TripInvitationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    TripInvitationService 단위 test (repository mock)

    여행당 하나의 재사용 가능한 링크(token)를 유지하는지 확인한다
 */
@ExtendWith(MockitoExtension.class)
class TripInvitationServiceTest {

    private static final Long TRIP_ID = 1L;
    private static final Long INVITER_ID = 10L;
    private static final String TOKEN = "11111111-2222-3333-4444-555555555555";

    @Mock
    private TripInvitationRepository tripInvitationRepository;

    @InjectMocks
    private TripInvitationService tripInvitationService;

    private TripInvitation createInvitation() {
        return TripInvitation.builder()
                .tripId(TRIP_ID)
                .inviterUserId(INVITER_ID)
                .token(TOKEN)
                .build();
    }

    @Nested
    @DisplayName("링크 발급")
    class GetOrCreateLink {

        @Test
        @DisplayName("이미 링크가 있으면 새로 만들지 않고 그대로 반환한다")
        void existingLink() {
            TripInvitation invitation = createInvitation();
            when(tripInvitationRepository.findByTripId(TRIP_ID)).thenReturn(Optional.of(invitation));

            TripInvitation result = tripInvitationService.getOrCreateLinkInvitation(TRIP_ID, INVITER_ID);

            assertSame(invitation, result);
            verify(tripInvitationRepository, never()).save(any(TripInvitation.class));
        }

        @Test
        @DisplayName("링크가 없으면 UUID token으로 새로 만들어 저장한다")
        void createLink() {
            when(tripInvitationRepository.findByTripId(TRIP_ID)).thenReturn(Optional.empty());
            when(tripInvitationRepository.save(any(TripInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TripInvitation result = tripInvitationService.getOrCreateLinkInvitation(TRIP_ID, INVITER_ID);

            ArgumentCaptor<TripInvitation> captor = ArgumentCaptor.forClass(TripInvitation.class);
            verify(tripInvitationRepository).save(captor.capture());
            TripInvitation saved = captor.getValue();
            assertSame(saved, result);
            assertEquals(TRIP_ID, saved.getTripId());
            assertEquals(INVITER_ID, saved.getInviterUserId());
            assertNotNull(saved.getToken());
            assertDoesNotThrow(() -> UUID.fromString(saved.getToken()));
        }
    }

    @Nested
    @DisplayName("token 조회")
    class FindByToken {

        @Test
        @DisplayName("있으면 반환한다")
        void findByToken() {
            TripInvitation invitation = createInvitation();
            when(tripInvitationRepository.findByToken(TOKEN)).thenReturn(Optional.of(invitation));

            assertSame(invitation, tripInvitationService.findByToken(TOKEN));
        }

        @Test
        @DisplayName("없으면 NOT_FOUND")
        void findByTokenNotFound() {
            when(tripInvitationRepository.findByToken(TOKEN)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripInvitationService.findByToken(TOKEN));
            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
        }
    }

    @Test
    @DisplayName("여행 삭제 시 : 여행의 모든 초대 링크를 삭제한다")
    void deleteAllByTripId() {
        tripInvitationService.deleteAllByTripId(TRIP_ID);

        verify(tripInvitationRepository).deleteAllByTripId(TRIP_ID);
    }

}
