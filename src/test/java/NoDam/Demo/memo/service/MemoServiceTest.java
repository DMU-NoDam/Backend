package NoDam.Demo.memo.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.memo.domain.Memo;
import NoDam.Demo.memo.domain.MemoIconType;
import NoDam.Demo.memo.repository.MemoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
    MemoService 단위 test (repository mock)
 */
@ExtendWith(MockitoExtension.class)
class MemoServiceTest {

    @Mock
    private MemoRepository memoRepository;

    @InjectMocks
    private MemoService memoService;

    private Memo createMemo() {
        return Memo.builder()
                .tripId(1L)
                .userId(10L)
                .icon(MemoIconType.FOOD)
                .title("제목")
                .content("내용")
                .build();
    }

    @Test
    @DisplayName("생성 : 입력값으로 Memo를 만들어 저장한다")
    void createMemo() {
        when(memoRepository.save(any(Memo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        memoService.createMemo(1L, 10L, MemoIconType.HOTEL, "숙소", "체크인");

        ArgumentCaptor<Memo> captor = ArgumentCaptor.forClass(Memo.class);
        verify(memoRepository).save(captor.capture());
        Memo saved = captor.getValue();
        assertEquals(1L, saved.getTripId());
        assertEquals(10L, saved.getUserId());
        assertEquals(MemoIconType.HOTEL, saved.getIcon());
        assertEquals("숙소", saved.getTitle());
        assertEquals("체크인", saved.getContent());
    }

    @Test
    @DisplayName("단건 조회 : 있으면 반환한다")
    void findById() {
        Memo memo = createMemo();
        when(memoRepository.findById(1L)).thenReturn(Optional.of(memo));

        assertSame(memo, memoService.findById(1L));
    }

    @Test
    @DisplayName("단건 조회 : 없으면 NOT_FOUND")
    void findByIdNotFound() {
        when(memoRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> memoService.findById(1L));
        assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
    }

    @Test
    @DisplayName("목록 조회 : 여행, 사용자 기준으로 조회한다")
    void findAllByTripIdAndUserId() {
        List<Memo> memos = List.of(createMemo());
        when(memoRepository.findAllByTripIdAndUserIdOrderByIdDesc(1L, 10L)).thenReturn(memos);

        assertSame(memos, memoService.findAllByTripIdAndUserId(1L, 10L));
    }

    @Test
    @DisplayName("수정 : 값을 변경하고 저장한다")
    void updateMemo() {
        Memo memo = createMemo();
        when(memoRepository.save(memo)).thenReturn(memo);

        Memo result = memoService.updateMemo(memo, MemoIconType.IDEA, "새 제목", null);

        assertEquals(MemoIconType.IDEA, result.getIcon());
        assertEquals("새 제목", result.getTitle());
        assertEquals("내용", result.getContent());
        verify(memoRepository).save(memo);
    }

    @Test
    @DisplayName("삭제 : repository delete를 호출한다")
    void deleteMemo() {
        Memo memo = createMemo();

        memoService.deleteMemo(memo);

        verify(memoRepository).delete(memo);
    }

    @Test
    @DisplayName("여행 삭제 시 : 여행의 모든 메모를 삭제한다")
    void deleteAllByTripId() {
        memoService.deleteAllByTripId(1L);

        verify(memoRepository).deleteAllByTripId(1L);
    }

    @Test
    @DisplayName("여행 나가기 시 : 본인 메모만 삭제한다")
    void deleteAllByTripIdAndUserId() {
        memoService.deleteAllByTripIdAndUserId(1L, 10L);

        verify(memoRepository).deleteAllByTripIdAndUserId(1L, 10L);
    }

}
