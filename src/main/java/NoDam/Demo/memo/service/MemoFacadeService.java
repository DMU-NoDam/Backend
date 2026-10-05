package NoDam.Demo.memo.service;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.memo.domain.Memo;
import NoDam.Demo.memo.domain.MemoIconType;
import NoDam.Demo.trip.service.TripMemberService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemoFacadeService {

    private final MemoService memoService;
    private final TripMemberService tripMemberService; // trip domain : 생성 시 여행 멤버 여부만 확인

    // 생성 : 이 여행의 멤버여야 함 (없는 여행/남의 여행에 메모가 쌓이지 않도록)
    public Memo createMemo(Long tripId, Long userId, MemoIconType icon, String title, String content) {
        tripMemberService.requireRole(tripId, userId); // 멤버가 아니면 NOT_AUTHOR

        return memoService.createMemo(tripId, userId, icon, title, content);
    }

    // 조회/수정/삭제는 본인이 만든 것만 가능 (같은 여행이라도 다른 사용자와 공유되지 않음)
    private Memo requireOwnership(Long memoId, Long userId) {
        Memo memo = memoService.findById(memoId);
        if (!memo.isOwnedBy(userId))
            throw new CustomException(ErrorCode.NOT_AUTHOR);
        return memo;
    }

    public List<Memo> getMyMemos(Long tripId, Long userId) {
        return memoService.findAllByTripIdAndUserId(tripId, userId);
    }

    public Memo getMemo(Long memoId, Long userId) {
        return requireOwnership(memoId, userId);
    }

    public Memo updateMemo(Long memoId, Long userId, MemoIconType icon, String title, String content) {
        Memo memo = requireOwnership(memoId, userId);
        return memoService.updateMemo(memo, icon, title, content);
    }

    public void deleteMemo(Long memoId, Long userId) {
        Memo memo = requireOwnership(memoId, userId);
        memoService.deleteMemo(memo);
    }

}
