package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.trip.repository.TripMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    TripMemberService 단위 test (repository, TransactionTemplate mock)

    TransactionTemplate은 callback을 그대로 실행하도록 stub 한다
    OWNER : userId 10, MEMBER : userId 20, 멤버 아님 : userId 99
 */
@ExtendWith(MockitoExtension.class)
class TripMemberServiceTest {

    private static final Long TRIP_ID = 1L;
    private static final Long OWNER_ID = 10L;
    private static final Long MEMBER_ID = 20L;
    private static final Long OTHER_ID = 99L;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private TripMemberService tripMemberService;

    private TripMember createMember(Long userId, TripMemberRole role) {
        return TripMember.builder()
                .tripId(TRIP_ID)
                .userId(userId)
                .role(role)
                .build();
    }

    private void runTransactionCallback() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Nested
    @DisplayName("권한 확인")
    class Role {

        @Test
        @DisplayName("requireRole : 멤버면 역할을 반환한다")
        void requireRole() {
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, MEMBER_ID))
                    .thenReturn(Optional.of(createMember(MEMBER_ID, TripMemberRole.MEMBER)));

            assertEquals(TripMemberRole.MEMBER, tripMemberService.requireRole(TRIP_ID, MEMBER_ID));
        }

        @Test
        @DisplayName("requireRole : 멤버가 아니면 NOT_AUTHOR")
        void requireRoleNotMember() {
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, OTHER_ID)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberService.requireRole(TRIP_ID, OTHER_ID));
            assertEquals(ErrorCode.NOT_AUTHOR, exception.errorCode);
        }

        @Test
        @DisplayName("requireOwner : OWNER면 통과한다")
        void requireOwner() {
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, OWNER_ID))
                    .thenReturn(Optional.of(createMember(OWNER_ID, TripMemberRole.OWNER)));

            tripMemberService.requireOwner(TRIP_ID, OWNER_ID);
        }

        @Test
        @DisplayName("requireOwner : MEMBER면 NOT_AUTHOR")
        void requireOwnerByMember() {
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, MEMBER_ID))
                    .thenReturn(Optional.of(createMember(MEMBER_ID, TripMemberRole.MEMBER)));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberService.requireOwner(TRIP_ID, MEMBER_ID));
            assertEquals(ErrorCode.NOT_AUTHOR, exception.errorCode);
        }

        @Test
        @DisplayName("isMember : repository exists 결과를 그대로 반환한다")
        void isMember() {
            when(tripMemberRepository.existsByTripIdAndUserId(TRIP_ID, MEMBER_ID)).thenReturn(true);

            assertTrue(tripMemberService.isMember(TRIP_ID, MEMBER_ID));
        }
    }

    @Nested
    @DisplayName("멤버 추가")
    class AddMember {

        @Test
        @DisplayName("참여 중이 아니면 입력값으로 TripMember를 만들어 저장한다")
        void addMember() {
            when(tripMemberRepository.existsByTripIdAndUserId(TRIP_ID, MEMBER_ID)).thenReturn(false);
            when(tripMemberRepository.save(any(TripMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
            runTransactionCallback();

            TripMember result = tripMemberService.addMember(TRIP_ID, MEMBER_ID, TripMemberRole.MEMBER);

            ArgumentCaptor<TripMember> captor = ArgumentCaptor.forClass(TripMember.class);
            verify(tripMemberRepository).save(captor.capture());
            TripMember saved = captor.getValue();
            assertSame(saved, result);
            assertEquals(TRIP_ID, saved.getTripId());
            assertEquals(MEMBER_ID, saved.getUserId());
            assertEquals(TripMemberRole.MEMBER, saved.getRole());
        }

        @Test
        @DisplayName("이미 참여 중이면 ALREADY_JOINED_TRIP, 저장하지 않는다")
        void addMemberAlreadyJoined() {
            when(tripMemberRepository.existsByTripIdAndUserId(TRIP_ID, MEMBER_ID)).thenReturn(true);

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberService.addMember(TRIP_ID, MEMBER_ID, TripMemberRole.MEMBER));
            assertEquals(ErrorCode.ALREADY_JOINED_TRIP, exception.errorCode);
            verify(tripMemberRepository, never()).save(any(TripMember.class));
        }

        @Test
        @DisplayName("동시 요청으로 unique 제약에 걸리면 ALREADY_JOINED_TRIP")
        void addMemberUniqueViolation() {
            when(tripMemberRepository.existsByTripIdAndUserId(TRIP_ID, MEMBER_ID)).thenReturn(false);
            when(tripMemberRepository.save(any(TripMember.class)))
                    .thenThrow(new DataIntegrityViolationException("uk_trip_member_trip_user"));
            runTransactionCallback();

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberService.addMember(TRIP_ID, MEMBER_ID, TripMemberRole.MEMBER));
            assertEquals(ErrorCode.ALREADY_JOINED_TRIP, exception.errorCode);
        }
    }

    @Nested
    @DisplayName("나가기")
    class Leave {

        @Test
        @DisplayName("멤버가 아니면 NOT_AUTHOR")
        void leaveNotMember() {
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, OTHER_ID)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberService.leave(TRIP_ID, OTHER_ID, null));
            assertEquals(ErrorCode.NOT_AUTHOR, exception.errorCode);
        }

        @Test
        @DisplayName("MEMBER는 바로 삭제된다")
        void leaveMember() {
            TripMember member = createMember(MEMBER_ID, TripMemberRole.MEMBER);
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, MEMBER_ID)).thenReturn(Optional.of(member));

            tripMemberService.leave(TRIP_ID, MEMBER_ID, null);

            verify(tripMemberRepository).delete(member);
        }

        @Test
        @DisplayName("OWNER가 위임 대상 없이 나가면 BAD_REQUEST")
        void leaveOwnerWithoutNewOwner() {
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, OWNER_ID))
                    .thenReturn(Optional.of(createMember(OWNER_ID, TripMemberRole.OWNER)));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberService.leave(TRIP_ID, OWNER_ID, null));
            assertEquals(ErrorCode.BAD_REQUEST, exception.errorCode);
        }

        @Test
        @DisplayName("OWNER가 본인을 위임 대상으로 지정하면 BAD_REQUEST")
        void leaveOwnerToSelf() {
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, OWNER_ID))
                    .thenReturn(Optional.of(createMember(OWNER_ID, TripMemberRole.OWNER)));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberService.leave(TRIP_ID, OWNER_ID, OWNER_ID));
            assertEquals(ErrorCode.BAD_REQUEST, exception.errorCode);
        }

        @Test
        @DisplayName("위임 대상이 여행 멤버가 아니면 NOT_FOUND")
        void leaveOwnerToNotMember() {
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, OWNER_ID))
                    .thenReturn(Optional.of(createMember(OWNER_ID, TripMemberRole.OWNER)));
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, OTHER_ID)).thenReturn(Optional.empty());

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberService.leave(TRIP_ID, OWNER_ID, OTHER_ID));
            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
        }

        @Test
        @DisplayName("OWNER는 위임 대상을 OWNER로 올리고 본인 멤버십을 삭제한다")
        void leaveOwnerWithDelegation() {
            TripMember owner = createMember(OWNER_ID, TripMemberRole.OWNER);
            TripMember newOwner = createMember(MEMBER_ID, TripMemberRole.MEMBER);
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, OWNER_ID)).thenReturn(Optional.of(owner));
            when(tripMemberRepository.findByTripIdAndUserId(TRIP_ID, MEMBER_ID)).thenReturn(Optional.of(newOwner));
            runTransactionCallback();

            tripMemberService.leave(TRIP_ID, OWNER_ID, MEMBER_ID);

            assertEquals(TripMemberRole.OWNER, newOwner.getRole());
            verify(tripMemberRepository).save(newOwner);
            verify(tripMemberRepository).delete(owner);
        }
    }

    @Test
    @DisplayName("여행 삭제 시 : 여행의 모든 멤버십을 삭제한다")
    void deleteAllByTripId() {
        tripMemberService.deleteAllByTripId(TRIP_ID);

        verify(tripMemberRepository).deleteAllByTripId(TRIP_ID);
    }

}
