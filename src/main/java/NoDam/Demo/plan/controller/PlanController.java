package NoDam.Demo.plan.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.service.PlanFacadeService;
import NoDam.Demo.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<DatePlan> planList = planFacadeService.getPlans(tripId, user.getId());

        Map<TripThemeType, List<PlacePlanInfo>> response = planList.stream()
                .collect(Collectors.groupingBy(DatePlan::getTripThemeType))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap(dp -> dp.getPlacePlans().stream())
                                .sorted(Comparator.comparing(pp -> pp.getDatePlan().getDate(),
                                        Comparator.naturalOrder()))
                                .map(PlacePlanInfo::of)
                                .collect(Collectors.toList())
                ));

        return ResponseEntity.ok(new SuccessResponse<>("success", response));
    }

}
