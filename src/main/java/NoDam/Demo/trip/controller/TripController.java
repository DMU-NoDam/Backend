package NoDam.Demo.trip.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.service.AutoCreatePlanService;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto;
import NoDam.Demo.trip.dto.request.TripUpdateDto;
import NoDam.Demo.trip.dto.response.TripInfoDto;
import NoDam.Demo.trip.service.TripFacadeService;
import NoDam.Demo.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trip")
@Tag(name = "tripController")
public class TripController {

    private final TripFacadeService tripFacadeService;
    private final AutoCreatePlanService autoCreatePlanService;

    private final Logger logger = LoggerFactory.getLogger("trip Controller :: ");

    @PostMapping("/api")
    @Operation(summary = "trip domain 생성")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "trip": {
                        "name": "도쿄 여행",
                        "uuid": "abc123",
                        "personCount": 2,
                        "scheduleType": "LOOSE",
                        "priceType": "CHEEP",
                        "startDate": "2025-01-01",
                        "endDate": "2025-01-03"
                      },
                      "region": ["jp-tokyo"],
                      "selectedPlace": [],
                      "departFlight": { "airport": "ICN", "time": "2025-01-01 10:00" },
                      "arriveFlight": { "airport": "NRT", "time": "2025-01-03 12:00" },
                      "hotel": null
                    }
                    """))
    )
    public ResponseEntity<SuccessResponse<TripInfoDto>> createTrip(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid TripCreateFacadeRequestDto request
    ) {
        // trip domain 생성까지만 담당 (2~5단계는 각각 별도 async api로 분리)
        Trip trip = tripFacadeService.createTrip(user.getId(), request.getTrip());
        return ResponseEntity.ok().body(new SuccessResponse<>("success", TripInfoDto.from(trip, false)));
    }

    @PostMapping("/api/{tripId}/date-plans")
    @Operation(summary = "3. DatePlan 생성 (비동기)")
    public ResponseEntity<SuccessResponse<Void>> generateDatePlans(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId
    ) {
        autoCreatePlanService.translateGooglePlaceToDbPlace(tripId, user.getId()); // 2번 단계
        autoCreatePlanService.generateAllDatePlans(tripId, user.getId());
        return ResponseEntity.accepted().body(new SuccessResponse<>("accepted", null));
    }

    @PostMapping("/api/{tripId}/place-plans")
    @Operation(summary = "4. 각 DatePlan의 PlacePlan(AI 일정) 생성 (비동기)")
    public ResponseEntity<SuccessResponse<Void>> generatePlacePlans(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId
    ) {
        autoCreatePlanService.autoGenerateAllPlans(tripId, user.getId());
        return ResponseEntity.accepted().body(new SuccessResponse<>("accepted", null));
    }

    @PostMapping("/api/{tripId}/transport-plans")
    @Operation(summary = "5. TransportPlan 생성 (비동기)")
    public ResponseEntity<SuccessResponse<Void>> generateTransportPlans(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId
    ) {
        autoCreatePlanService.autoGenerateAllThemeTransportPlans(tripId, user.getId());
        return ResponseEntity.accepted().body(new SuccessResponse<>("accepted", null));
    }

    @GetMapping("/api")
    @Operation(summary = "여행 리스트 조회")
    public ResponseEntity<SuccessResponse<List<TripInfoDto>>> getTripList(@AuthenticationPrincipal User user) {
        List<TripInfoDto> trips = tripFacadeService.getTripList(user.getId());
        return ResponseEntity.ok().body(new SuccessResponse<>("success", trips));
    }

    @GetMapping("/api/{tripId}")
    @Operation(summary = "여행 상세 조회")
    public ResponseEntity<SuccessResponse<TripInfoDto>> getTrip(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId
    ) {
        TripInfoDto trip = tripFacadeService.getTrip(user.getId(), tripId);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", trip));
    }

    @GetMapping("/api/today")
    @Operation(summary = "오늘의 고정된 여행 조회")
    public ResponseEntity<SuccessResponse<TripInfoDto>> getTodayTrip(@AuthenticationPrincipal User user) {
        Optional<TripInfoDto> trip = tripFacadeService.getTodayTrip(user.getId());
        return ResponseEntity.ok().body(new SuccessResponse<>("success", trip.orElse(null)));
    }

    @PatchMapping("/api/{tripId}/fixed")
    @Operation(summary = "여행 고정 여부 수정")
    public ResponseEntity<SuccessResponse<Void>> updateTripFixed(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId,
            @RequestBody boolean isFixed
    ) {
        Trip trip = tripFacadeService.updateTripFixed(user.getId(), tripId, isFixed);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", null));
    }

    @PatchMapping("/api/{tripId}/theme")
    @Operation(summary = "여행 테마 수정")
    public ResponseEntity<SuccessResponse<Void>> updateTripTheme(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId,
            @RequestBody TripThemeType theme
    ) {
        Trip trip = tripFacadeService.updateTripTheme(user.getId(), tripId, theme);
        return ResponseEntity.ok().body(new SuccessResponse("success", null));
    }

    @PutMapping("/api/{tripId}")
    @Operation(summary = "여행 정보 수정")
    public ResponseEntity<SuccessResponse<TripInfoDto>> updateTrip(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId,
            @RequestBody TripUpdateDto request
    ) {
        Trip trip = tripFacadeService.updateTripInfo(user.getId(), tripId, request);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", TripInfoDto.from(trip, false)));
    }

}
