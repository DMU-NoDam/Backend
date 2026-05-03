package NoDam.Demo.trip.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.request.CreateTripRequest;
import NoDam.Demo.trip.dto.response.TripInfoDto;
import NoDam.Demo.trip.service.TripService;
import NoDam.Demo.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/trip")
@Tag(name = "tripController")
public class TripController {

    private final TripService tripService;

    @PostMapping("/api")
    @Operation(summary = "여행 일정 생성")
    public ResponseEntity createTrip(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid CreateTripRequest request
    ) {
        Trip trip = tripService.createTrip(user.getId(), request);
        return ResponseEntity.ok().body(new SuccessResponse("success", TripInfoDto.from(trip)));
    }

    @GetMapping("/api")
    @Operation(summary = "여행 리스트 조회")
    public ResponseEntity getTripList(@AuthenticationPrincipal User user) {
        List<Trip> trips = tripService.getTripList(user.getId());
        return ResponseEntity.ok().body(new SuccessResponse("success", TripInfoDto.from(trips)));
    }
}
