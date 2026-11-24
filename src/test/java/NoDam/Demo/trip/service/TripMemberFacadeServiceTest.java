package NoDam.Demo.trip.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.memo.service.MemoService;
import NoDam.Demo.trip.domain.TripMember;
import NoDam.Demo.trip.domain.TripMemberRole;
import NoDam.Demo.trip.dto.response.TripMemberInfo;
import NoDam.Demo.user.domain.User;
import NoDam.Demo.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    TripMemberFacadeService 단위 test (domain service, TransactionTemplate mock)

    OWNER : userId 10, MEMBER : userId 20, 멤버 아님 : userId 99
    User id는 DB가 만드는 값이라 리플렉션으로 넣는다
 */
@ExtendWith(MockitoExtension.class)
class TripMemberFacadeServiceTest {

    private static final Long TRIP_ID = 1L;
    private static final Long OWNER_ID = 10L;
    private static final Long MEMBER_ID = 20L;
    private static final Long OTHER_ID = 99L;

    @Mock
    private TripMemberService tripMemberService;

    @Mock
    private UserService userService;

    @Mock
    private TripFacadeService tripFacadeService;

    @Mock
    private MemoService memoService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private TripMemberFacadeService tripMemberFacadeService;

    private TripMember createMember(Long userId, TripMemberRole role) {
        return TripMember.builder()
                .tripId(TRIP_ID)
                .userId(userId)
                .role(role)
                .build();
    }

    private User createUser(Long id, String name) {
        User user = User.builder()
                .name(name)
                .oAuthProvider("google")
                .oAuthId("oauth-" + id)
                .build();
        setId(user, id);
        return user;
    }

    private void runTransactionCallback() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Nested
    @DisplayName("멤버 목록 조회")
    class GetMembers {

        @Test
        @DisplayName("멤버가 아니면 NOT_AUTHOR, 목록을 조회하지 않는다")
        void getMembersNotMember() {
            when(tripMemberService.requireRole(TRIP_ID, OTHER_ID))
                    .thenThrow(new CustomException(ErrorCode.NOT_AUTHOR));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberFacadeService.getMembers(TRIP_ID, OTHER_ID));
            assertEquals(ErrorCode.NOT_AUTHOR, exception.errorCode);
            verify(tripMemberService, never()).getMembers(anyLong());
        }

        @Test
        @DisplayName("멤버 순서대로 사용자 이름과 역할을 묶어 반환한다")
        void getMembers() {
            when(tripMemberService.requireRole(TRIP_ID, MEMBER_ID)).thenReturn(TripMemberRole.MEMBER);
            when(tripMemberService.getMembers(TRIP_ID)).thenReturn(List.of(
                    createMember(OWNER_ID, TripMemberRole.OWNER),
                    createMember(MEMBER_ID, TripMemberRole.MEMBER)
            ));
            when(userService.getUsers(List.of(OWNER_ID, MEMBER_ID))).thenReturn(List.of(
                    createUser(MEMBER_ID, "멤버"),
                    createUser(OWNER_ID, "방장")
            ));

            List<TripMemberInfo> result = tripMemberFacadeService.getMembers(TRIP_ID, MEMBER_ID);

            assertEquals(2, result.size());
            assertEquals(OWNER_ID, result.get(0).getUserId());
            assertEquals("방장", result.get(0).getUserName());
            assertEquals(TripMemberRole.OWNER, result.get(0).getRole());
            assertEquals(MEMBER_ID, result.get(1).getUserId());
            assertEquals("멤버", result.get(1).getUserName());
            assertEquals(TripMemberRole.MEMBER, result.get(1).getRole());
        }

        @Test
        @DisplayName("조회되지 않은 사용자(삭제된 유저 등)는 userName이 null")
        void getMembersWithUnknownUser() {
            when(tripMemberService.requireRole(TRIP_ID, OWNER_ID)).thenReturn(TripMemberRole.OWNER);
            when(tripMemberService.getMembers(TRIP_ID)).thenReturn(List.of(
                    createMember(OWNER_ID, TripMemberRole.OWNER),
                    createMember(MEMBER_ID, TripMemberRole.MEMBER)
            ));
            when(userService.getUsers(List.of(OWNER_ID, MEMBER_ID))).thenReturn(List.of(
                    createUser(OWNER_ID, "방장")
            ));

            List<TripMemberInfo> result = tripMemberFacadeService.getMembers(TRIP_ID, OWNER_ID);

            assertEquals(2, result.size());
            assertEquals("방장", result.get(0).getUserName());
            assertEquals(MEMBER_ID, result.get(1).getUserId());
            assertNull(result.get(1).getUserName());
        }
    }

    @Nested
    @DisplayName("나가기")
    class Leave {

        @Test
        @DisplayName("멤버가 아니면 NOT_AUTHOR, 아무것도 삭제하지 않는다")
        void leaveNotMember() {
            when(tripMemberService.requireRole(TRIP_ID, OTHER_ID))
                    .thenThrow(new CustomException(ErrorCode.NOT_AUTHOR));

            CustomException exception = assertThrows(CustomException.class,
                    () -> tripMemberFacadeService.leave(TRIP_ID, OTHER_ID, null));
            assertEquals(ErrorCode.NOT_AUTHOR, exception.errorCode);
            verify(tripMemberService, never()).leave(anyLong(), anyLong(), any());
            verify(tripFacadeService, never()).deleteTrip(anyLong(), anyLong());
        }

        @Test
        @DisplayName("혼자인 OWNER는 여행 삭제로 처리한다")
        void leaveAloneOwner() {
            when(tripMemberService.requireRole(TRIP_ID, OWNER_ID)).thenReturn(TripMemberRole.OWNER);
            when(tripMemberService.countMembers(TRIP_ID)).thenReturn(1L);

            tripMemberFacadeService.leave(TRIP_ID, OWNER_ID, null);

            verify(tripFacadeService).deleteTrip(OWNER_ID, TRIP_ID);
            verify(tripMemberService, never()).leave(anyLong(), anyLong(), any());
            verify(memoService, never()).deleteAllByTripIdAndUserId(anyLong(), anyLong());
        }

        @Test
        @DisplayName("다른 멤버가 있는 OWNER는 위임 후 나가고 본인 메모를 삭제한다")
        void leaveOwnerWithMembers() {
            when(tripMemberService.requireRole(TRIP_ID, OWNER_ID)).thenReturn(TripMemberRole.OWNER);
            when(tripMemberService.countMembers(TRIP_ID)).thenReturn(2L);
            runTransactionCallback();

            tripMemberFacadeService.leave(TRIP_ID, OWNER_ID, MEMBER_ID);

            verify(tripMemberService).leave(TRIP_ID, OWNER_ID, MEMBER_ID);
            verify(memoService).deleteAllByTripIdAndUserId(TRIP_ID, OWNER_ID);
            verify(tripFacadeService, never()).deleteTrip(anyLong(), anyLong());
        }

        @Test
        @DisplayName("MEMBER는 바로 나가고 본인 메모를 삭제한다")
        void leaveMember() {
            when(tripMemberService.requireRole(TRIP_ID, MEMBER_ID)).thenReturn(TripMemberRole.MEMBER);
            runTransactionCallback();

            tripMemberFacadeService.leave(TRIP_ID, MEMBER_ID, null);

            verify(tripMemberService).leave(TRIP_ID, MEMBER_ID, null);
            verify(memoService).deleteAllByTripIdAndUserId(TRIP_ID, MEMBER_ID);
            verify(tripMemberService, never()).countMembers(anyLong());
        }
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new IllegalStateException("id 설정 실패", e);
        }
    }

}
