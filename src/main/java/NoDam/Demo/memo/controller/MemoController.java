package NoDam.Demo.memo.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.memo.domain.Memo;
import NoDam.Demo.memo.dto.request.MemoCreateRequestDto;
import NoDam.Demo.memo.dto.request.MemoUpdateRequestDto;
import NoDam.Demo.memo.dto.response.MemoInfo;
import NoDam.Demo.memo.service.MemoFacadeService;
import NoDam.Demo.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 개인용 여행 메모. 같은 여행이라도 다른 사용자와 공유되지 않는다(작성자 본인만 접근 가능).
@RestController
@RequiredArgsConstructor
@RequestMapping("/memo")
@Tag(name = "memoController")
public class MemoController {

    private final MemoFacadeService memoFacadeService;

    @PostMapping("/api/{tripId}")
    @Operation(summary = "메모 생성 (이 여행의 멤버만 가능)")
    public ResponseEntity<SuccessResponse<MemoInfo>> createMemo(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId,
            @RequestBody @Valid MemoCreateRequestDto request
    ) {
        Memo memo = memoFacadeService.createMemo(tripId, user.getId(), request.getIcon(), request.getTitle(), request.getContent());
        return ResponseEntity.ok().body(new SuccessResponse<MemoInfo>("success", MemoInfo.from(memo)));
    }

    @GetMapping("/api/{tripId}")
    @Operation(summary = "내 메모 목록 조회 (본인 것만, 최신순)")
    public ResponseEntity<SuccessResponse<List<MemoInfo>>> getMyMemos(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId
    ) {
        List<MemoInfo> memos = memoFacadeService.getMyMemos(tripId, user.getId()).stream()
                .map(MemoInfo::from)
                .toList();
        return ResponseEntity.ok().body(new SuccessResponse<List<MemoInfo>>("success", memos));
    }

    @GetMapping("/api/detail/{memoId}")
    @Operation(summary = "메모 상세 조회 (본인 것만)")
    public ResponseEntity<SuccessResponse<MemoInfo>> getMemo(
            @AuthenticationPrincipal User user,
            @PathVariable Long memoId
    ) {
        Memo memo = memoFacadeService.getMemo(memoId, user.getId());
        return ResponseEntity.ok().body(new SuccessResponse<MemoInfo>("success", MemoInfo.from(memo)));
    }

    @PutMapping("/api/{memoId}")
    @Operation(summary = "메모 수정 (본인 것만)")
    public ResponseEntity<SuccessResponse<MemoInfo>> updateMemo(
            @AuthenticationPrincipal User user,
            @PathVariable Long memoId,
            @RequestBody @Valid MemoUpdateRequestDto request
    ) {
        Memo memo = memoFacadeService.updateMemo(memoId, user.getId(), request.getIcon(), request.getTitle(), request.getContent());
        return ResponseEntity.ok().body(new SuccessResponse<MemoInfo>("success", MemoInfo.from(memo)));
    }

    @DeleteMapping("/api/{memoId}")
    @Operation(summary = "메모 삭제 (본인 것만)")
    public ResponseEntity<SuccessResponse<Void>> deleteMemo(
            @AuthenticationPrincipal User user,
            @PathVariable Long memoId
    ) {
        memoFacadeService.deleteMemo(memoId, user.getId());
        return ResponseEntity.ok().body(new SuccessResponse<Void>("success", null));
    }

}
