package NoDam.Demo.memo.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.memo.domain.Memo;
import NoDam.Demo.memo.domain.MemoIconType;
import NoDam.Demo.trip.service.TripMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    MemoFacadeService 단위 test (domain service mock)

    작성자 : userId 10, 다른 사용자 : userId 99
 */
@ExtendWith(MockitoExtension.class)
class MemoFacadeServiceTest {

    private static final Long TRIP_ID = 1L;
    private static final Long MEMO_ID = 100L;
    private static final Long OWNER_ID = 10L;
    private static final Long OTHER_ID = 99L;

    @Mock
    private MemoService memoService;

    @Mock
    private TripMemberService tripMemberService;

    @InjectMocks
    private MemoFacadeService memoFacadeService;

    private Memo createMemo() {
        return Memo.builder()
                .tripId(TRIP_ID)
                .userId(OWNER_ID)
                .icon(MemoIconType.FOOD)
                .title("제목")
                .content("내용")
                .build();
    }

    @Nested
    @DisplayName("생성")
    class Create {

        @Test
        @DisplayName("여행 멤버면 생성된다")
        void member() {
            Memo memo = createMemo();
            when(memoService.createMemo(TRIP_ID, OWNER_ID, MemoIconType.FOOD, "제목", "내용")).thenReturn(memo);

            Memo result = memoFacadeService.createMemo(TRIP_ID, OWNER_ID, MemoIconType.FOOD, "제목", "내용");

            assertSame(memo, result);
            verify(tripMemberService).requireRole(TRIP_ID, OWNER_ID);
        }

        @Test
        @DisplayName("여행 멤버가 아니면 NOT_AUTHOR, 생성하지 않는다")
        void notMember() {
            when(tripMemberService.requireRole(TRIP_ID, OTHER_ID)).thenThrow(new CustomException(ErrorCode.NOT_AUTHOR));

            CustomException exception = assertThrows(CustomException.class,
                    () -> memoFacadeService.createMemo(TRIP_ID, OTHER_ID, MemoIconType.FOOD, "제목", "내용"));

            assertEquals(ErrorCode.NOT_AUTHOR, exception.errorCode);
            verify(memoService, never()).createMemo(anyLong(), anyLong(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("조회")
    class Read {

        @Test
        @DisplayName("내 메모 목록을 반환한다")
        void getMyMemos() {
            List<Memo> memos = List.of(createMemo());
            when(memoService.findAllByTripIdAndUserId(TRIP_ID, OWNER_ID)).thenReturn(memos);

            assertSame(memos, memoFacadeService.getMyMemos(TRIP_ID, OWNER_ID));
        }

        @Test
        @DisplayName("본인 메모는 조회된다")
        void getMemoOwner() {
            Memo memo = createMemo();
            when(memoService.findById(MEMO_ID)).thenReturn(memo);

            assertSame(memo, memoFacadeService.getMemo(MEMO_ID, OWNER_ID));
        }

        @Test
        @DisplayName("다른 사용자의 메모는 NOT_AUTHOR")
        void getMemoOther() {
            when(memoService.findById(MEMO_ID)).thenReturn(createMemo());

            CustomException exception = assertThrows(CustomException.class,
                    () -> memoFacadeService.getMemo(MEMO_ID, OTHER_ID));

            assertEquals(ErrorCode.NOT_AUTHOR, exception.errorCode);
        }
    }

    @Nested
    @DisplayName("수정")
    class Update {

        @Test
        @DisplayName("본인 메모는 수정된다")
        void owner() {
            Memo memo = createMemo();
            when(memoService.findById(MEMO_ID)).thenReturn(memo);
            when(memoService.updateMemo(memo, MemoIconType.HOTEL, "새 제목", null)).thenReturn(memo);

            memoFacadeService.updateMemo(MEMO_ID, OWNER_ID, MemoIconType.HOTEL, "새 제목", null);

            verify(memoService).updateMemo(memo, MemoIconType.HOTEL, "새 제목", null);
        }

        @Test
        @DisplayName("다른 사용자의 메모는 NOT_AUTHOR, 수정하지 않는다")
        void other() {
            when(memoService.findById(MEMO_ID)).thenReturn(createMemo());

            assertThrows(CustomException.class,
                    () -> memoFacadeService.updateMemo(MEMO_ID, OTHER_ID, MemoIconType.HOTEL, "새 제목", null));

            verify(memoService, never()).updateMemo(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("삭제")
    class Delete {

        @Test
        @DisplayName("본인 메모는 삭제된다")
        void owner() {
            Memo memo = createMemo();
            when(memoService.findById(MEMO_ID)).thenReturn(memo);

            memoFacadeService.deleteMemo(MEMO_ID, OWNER_ID);

            verify(memoService).deleteMemo(memo);
        }

        @Test
        @DisplayName("다른 사용자의 메모는 NOT_AUTHOR, 삭제하지 않는다")
        void other() {
            when(memoService.findById(MEMO_ID)).thenReturn(createMemo());

            assertThrows(CustomException.class,
                    () -> memoFacadeService.deleteMemo(MEMO_ID, OTHER_ID));

            verify(memoService, never()).deleteMemo(any());
        }
    }

}
