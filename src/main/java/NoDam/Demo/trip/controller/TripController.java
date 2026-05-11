package NoDam.Demo.trip.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.TripCreateFacadeRequestDto;
import NoDam.Demo.trip.dto.response.TripInfoDto;
import NoDam.Demo.trip.service.TripFacadeService;
import NoDam.Demo.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trip")
@Tag(name = "tripController")
public class TripController {

    private final TripFacadeService tripFacadeService;

    @PostMapping("/api")
    @Operation(summary = "여행 일정 생성")
    public ResponseEntity createTrip(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid TripCreateFacadeRequestDto request
    ) {
        Trip trip = tripFacadeService.createTrip(user.getId(), request);
        return ResponseEntity.ok().body(new SuccessResponse("success", TripInfoDto.from(trip)));
    }

    @GetMapping("/api")
    @Operation(summary = "여행 리스트 조회")
    public ResponseEntity getTripList(@AuthenticationPrincipal User user) {
        List<Trip> trips = tripFacadeService.getTripList(user.getId());
        return ResponseEntity.ok().body(new SuccessResponse("success", TripInfoDto.from(trips)));
    }

    @GetMapping("/api/today")
    @Operation(summary = "오늘의 고정된 여행 조회")
    public ResponseEntity getTodayTrip(@AuthenticationPrincipal User user) {
        Optional<Trip> trip = tripFacadeService.getTodayTrip(user.getId());
        TripInfoDto response = (trip.isPresent()) ? TripInfoDto.from(trip.get()) : null;
        return ResponseEntity.ok().body(new SuccessResponse("success", response));
    }

    @PatchMapping("/api/{tripId}/fixed")
    @Operation(summary = "여행 고정 여부 수정")
    public ResponseEntity updateTripFixed(
            @AuthenticationPrincipal User user,
            @PathVariable Long tripId,
            @RequestBody boolean isFixed
    ) {
        Trip trip = tripFacadeService.updateTripFixed(user.getId(), tripId, isFixed);
        return ResponseEntity.ok().body(new SuccessResponse("success", TripInfoDto.from(trip)));
    }
}
