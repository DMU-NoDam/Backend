package NoDam.Demo.flight.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.flight.dto.FlightInfoResponseDto;
import NoDam.Demo.flight.service.AirLabsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flight/public")
@RequiredArgsConstructor
public class FlightController {

    private final AirLabsService airLabsService;

    @GetMapping("/{flightIata}")
    public ResponseEntity<SuccessResponse<FlightInfoResponseDto>> getFlight(@PathVariable String flightIata) {
        return ResponseEntity.ok().body(new SuccessResponse<>("success", airLabsService.getFlightInfo(flightIata)));
    }
}
