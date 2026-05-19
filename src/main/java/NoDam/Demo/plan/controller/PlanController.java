package NoDam.Demo.plan.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.dto.request.ChangePlacePlanRequestDto;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.service.PlanFacadeService;
import NoDam.Demo.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plan")
public class PlanController {

    private final PlanFacadeService planFacadeService;

    @GetMapping("/api/{tripId}")
    public ResponseEntity<SuccessResponse<Map<TripThemeType, List<PlacePlanInfo>>>> selectPlans(
            @PathVariable Long tripId,
            @AuthenticationPrincipal User user
    ) {
        Map<TripThemeType, List<PlacePlanInfo>> response = planFacadeService.getPlans(tripId, user.getId());
        return ResponseEntity.ok(new SuccessResponse<>("success", response));
    }

    @DeleteMapping("/api/place-plan/{placePlanId}")
    public ResponseEntity<SuccessResponse<Void>> deletePlacePlan(
            @PathVariable Long placePlanId,
            @AuthenticationPrincipal User user
    ) {
        planFacadeService.deletePlacePlan(placePlanId, user.getId());
        return ResponseEntity.ok(new SuccessResponse<Void>("success", null));
    }

    @PutMapping("/api/place-plan")
    public ResponseEntity<SuccessResponse<PlacePlanInfo>> changePlacePlan(
            @RequestBody ChangePlacePlanRequestDto dto,
            @AuthenticationPrincipal User user
    ) {
        PlacePlanInfo result = planFacadeService.changePlacePlan(dto.getOldPlacePlanId(), dto.getNewPlaceId(), user.getId());
        return ResponseEntity.ok(new SuccessResponse<PlacePlanInfo>("success", result));
    }

}
