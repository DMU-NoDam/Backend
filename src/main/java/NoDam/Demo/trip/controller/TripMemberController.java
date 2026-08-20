package NoDam.Demo.trip.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.trip.domain.TripInvitation;
import NoDam.Demo.trip.dto.request.LeaveTripRequestDto;
import NoDam.Demo.trip.dto.response.TripInvitationPreviewInfo;
import NoDam.Demo.trip.dto.response.TripMemberInfo;
import NoDam.Demo.trip.service.TripInvitationFacadeService;
import NoDam.Demo.trip.service.TripMemberFacadeService;
import NoDam.Demo.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trip")
@Tag(name = "tripMemberController")
public class TripMemberController {

    private final TripMemberFacadeService tripMemberFacadeService;
    private final TripInvitationFacadeService tripInvitationFacadeService;

    @GetMapping("/api/{tripId}/members")
    @Operation(summary = "여행 멤버 목록 조회")
    public ResponseEntity<SuccessResponse<List<TripMemberInfo>>> getMembers(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId
    ) {
        List<TripMemberInfo> members = tripMemberFacadeService.getMembers(tripId, user.getId());
        return ResponseEntity.ok().body(new SuccessResponse<>("success", members));
    }

    @PostMapping("/api/{tripId}/leave")
    @Operation(summary = "여행 나가기 (MEMBER는 바로 나가기, OWNER는 newOwnerUserId로 위임 후 나가기. 혼자인 OWNER는 여행 삭제로 처리)")
    public ResponseEntity<SuccessResponse<Void>> leave(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId,
            @RequestBody(required = false) LeaveTripRequestDto request
    ) {
        Long newOwnerUserId = request != null ? request.getNewOwnerUserId() : null;
        tripMemberFacadeService.leave(tripId, user.getId(), newOwnerUserId);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", null));
    }

    @PostMapping("/api/{tripId}/invitations/link")
    @Operation(summary = "초대 링크 발급/재조회 (여행당 하나, 반복 참여 가능, OWNER 전용)")
    public ResponseEntity<SuccessResponse<Map<String, String>>> getOrCreateInvitationLink(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId
    ) {
        TripInvitation invitation = tripInvitationFacadeService.getOrCreateLink(tripId, user.getId());
        return ResponseEntity.ok().body(new SuccessResponse<>("success", Map.of("token", invitation.getToken())));
    }

    @GetMapping("/public/invitations/{token}")
    @Operation(summary = "초대 링크 미리보기 (비로그인 가능)")
    public ResponseEntity<SuccessResponse<TripInvitationPreviewInfo>> previewInvitationLink(
            @PathVariable String token
    ) {
        TripInvitationPreviewInfo preview = tripInvitationFacadeService.previewByToken(token);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", preview));
    }

    @PostMapping("/api/invitations/{token}/join")
    @Operation(summary = "초대 링크로 참여 (로그인 필요, 수락 절차 없이 즉시 멤버 등록, 이미 참여 중이면 멱등하게 성공 처리)")
    public ResponseEntity<SuccessResponse<Void>> joinByInvitationLink(
            @AuthenticationPrincipal User user,
            @PathVariable String token
    ) {
        tripInvitationFacadeService.joinByToken(token, user.getId());
        return ResponseEntity.ok().body(new SuccessResponse<>("success", null));
    }

}
