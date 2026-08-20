package NoDam.Demo.plan.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.place.dto.PlaceSwitchRequestDto;
import NoDam.Demo.plan.dto.request.AddPlacePlanRequestDto;
import NoDam.Demo.plan.dto.request.ChangePlacePlanRequestDto;
import NoDam.Demo.plan.dto.request.FixPlacePlanRequestDto;
import NoDam.Demo.plan.dto.request.MovePlacePlanRequestDto;
import NoDam.Demo.plan.dto.request.RemovePlacePlanRequestDto;
import NoDam.Demo.plan.dto.response.DatePlanInfo;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.dto.response.PlanStatusResponse;
import NoDam.Demo.plan.dto.response.TransportPlanInfo;
import NoDam.Demo.plan.service.PlanFacadeService;
import NoDam.Demo.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plan")
public class PlanController {

    private final PlanFacadeService planFacadeService;

    @GetMapping("/api/{tripId}/status")
    public ResponseEntity<SuccessResponse<PlanStatusResponse>> getPlanStatus(
            @PathVariable Long tripId,
            @AuthenticationPrincipal User user
    ) {
        PlanStatusResponse response = planFacadeService.getPlanStatus(tripId, user.getId());
        return ResponseEntity.ok(new SuccessResponse<PlanStatusResponse>("success", response));
    }

    // todo : /api/{tripId}의 반환값이 변경 되면서 front에서 처리 가능 여부를 확인해야함 (불가능 하다면 back 처리 필요)
//    @GetMapping("/api/{tripId}/summary")
//    public ResponseEntity<SuccessResponse<Map<TripThemeType, >>> selectPlansSummary() {
//
//    }

    // 확정 테마의 DatePlanInfo 목록. 테마 미확정이면 빈 목록
    @GetMapping("/api/{tripId}")
    public ResponseEntity<SuccessResponse<List<DatePlanInfo>>> selectPlans(
            @PathVariable Long tripId,
            @AuthenticationPrincipal User user
    ) {
        List<DatePlanInfo> response = planFacadeService.getPlans(tripId, user.getId());
        return ResponseEntity.ok(new SuccessResponse<List<DatePlanInfo>>("success", response));
    }

    // polling용. 최신 version만 읽는다. 기록이 없으면 null
    @GetMapping("/api/{datePlanId}/version")
    public ResponseEntity<SuccessResponse<Long>> getLatestVersion(
            @PathVariable Long datePlanId
    ) {
        Long response = planFacadeService.getLatestVersion(datePlanId);
        return ResponseEntity.ok(new SuccessResponse<Long>("success", response));
    }

    @GetMapping("/api/transport-plan/{transportPlanId}")
    public ResponseEntity<SuccessResponse<TransportPlanInfo>> getTransportPlanDetail(
            @PathVariable Long transportPlanId
    ) {
        TransportPlanInfo response = planFacadeService.getTransportPlanDetail(transportPlanId);
        return ResponseEntity.ok(new SuccessResponse<TransportPlanInfo>("success", response));
    }

    @PostMapping("/api/{datePlanId}/add-place")
    public ResponseEntity<SuccessResponse<DatePlanInfo>> addPlacePlan(
            @PathVariable Long datePlanId,
            @RequestParam Long version,
            @RequestBody AddPlacePlanRequestDto dto,
            @AuthenticationPrincipal User user
    ) {
        DatePlanInfo response = planFacadeService.addPlacePlan(datePlanId, dto.getPlaceId(),
                dto.getPreviousPlacePlanId(), dto.getNextPlacePlanId(), version, user.getId());
        return ResponseEntity.ok(new SuccessResponse<DatePlanInfo>("success", response));
    }

    @PostMapping("/api/{datePlanId}/change-place")
    public ResponseEntity<SuccessResponse<DatePlanInfo>> changePlacePlan(
            @PathVariable Long datePlanId,
            @RequestParam Long version,
            @RequestBody ChangePlacePlanRequestDto dto,
            @AuthenticationPrincipal User user
    ) {
        DatePlanInfo response = planFacadeService.changePlacePlan(
                datePlanId, dto.getPlacePlanId(), dto.getPlaceId(), version, user.getId());
        return ResponseEntity.ok(new SuccessResponse<DatePlanInfo>("success", response));
    }

    @PostMapping("/api/{datePlanId}/move-place")
    public ResponseEntity<SuccessResponse<DatePlanInfo>> movePlacePlan(
            @PathVariable Long datePlanId,
            @RequestParam Long version,
            @RequestBody MovePlacePlanRequestDto dto,
            @AuthenticationPrincipal User user
    ) {
        DatePlanInfo response = planFacadeService.movePlacePlan(datePlanId, dto.getPlacePlanId(),
                dto.getPreviousPlacePlanId(), dto.getNextPlacePlanId(), version, user.getId());
        return ResponseEntity.ok(new SuccessResponse<DatePlanInfo>("success", response));
    }

    @PostMapping("/api/{datePlanId}/remove-place")
    public ResponseEntity<SuccessResponse<DatePlanInfo>> removePlacePlan(
            @PathVariable Long datePlanId,
            @RequestParam Long version,
            @RequestBody RemovePlacePlanRequestDto dto,
            @AuthenticationPrincipal User user
    ) {
        DatePlanInfo response = planFacadeService.deletePlacePlan(
                datePlanId, dto.getPlacePlanId(), version, user.getId());
        return ResponseEntity.ok(new SuccessResponse<DatePlanInfo>("success", response));
    }

    @PostMapping("/api/{datePlanId}/fix-place")
    public ResponseEntity<SuccessResponse<DatePlanInfo>> fixPlacePlan(
            @PathVariable Long datePlanId,
            @RequestParam Long version,
            @RequestBody FixPlacePlanRequestDto dto,
            @AuthenticationPrincipal User user
    ) {
        DatePlanInfo response = planFacadeService.fixPlacePlan(
                datePlanId, dto.getPlacePlanId(), dto.getIsFixed(), version, user.getId());
        // return ResponseEntity.ok(new SuccessResponse<DatePlanInfo>("success", response));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new SuccessResponse<DatePlanInfo>("success", response)); // todo : 기능 구현 전, version은 증가하지만 fix는 적용 안됨
    }

    @GetMapping("/api/{datePlanId}/reload/{transportPlanId}")
    public ResponseEntity reloadTransport(
            @PathVariable(value = "datePlanId") Long datePlanId,
            @PathVariable(value = "transportPlanId") Long transportPlanId,
            @AuthenticationPrincipal User user
    ) {
        // todo : transport plan이 잘못 생성된다면 구현 예정
        // todo : front랑 협의 필요 (디자인 확인 필요)
        return null;
    }

}
