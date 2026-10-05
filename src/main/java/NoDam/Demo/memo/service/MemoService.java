package NoDam.Demo.memo.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.memo.domain.Memo;
import NoDam.Demo.memo.domain.MemoIconType;
import NoDam.Demo.memo.repository.MemoRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;

    public Memo createMemo(Long tripId, Long userId, MemoIconType icon, String title, String content) {
        Memo memo = Memo.builder()
                .tripId(tripId)
                .userId(userId)
                .icon(icon)
                .title(title)
                .content(content)
                .build();
        return memoRepository.save(memo);
    }

    public Memo findById(Long memoId) {
        return memoRepository.findById(memoId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    public List<Memo> findAllByTripIdAndUserId(Long tripId, Long userId) {
        return memoRepository.findAllByTripIdAndUserIdOrderByIdDesc(tripId, userId);
    }

    public Memo updateMemo(Memo memo, MemoIconType icon, String title, String content) {
        memo.update(icon, title, content);
        return memoRepository.save(memo);
    }

    public void deleteMemo(Memo memo) {
        memoRepository.delete(memo);
    }

    // 여행 삭제 시 호출(트랜잭션 내부) : 이 여행에 속한 모든 사용자의 메모를 정리한다
    public void deleteAllByTripId(Long tripId) {
        memoRepository.deleteAllByTripId(tripId);
    }

    // 여행 나가기 시 호출(트랜잭션 내부) : 나가는 사용자의 메모만 정리한다
    public void deleteAllByTripIdAndUserId(Long tripId, Long userId) {
        memoRepository.deleteAllByTripIdAndUserId(tripId, userId);
    }

}
