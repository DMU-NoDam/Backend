package NoDam.Demo.flight.controller;

import NoDam.Demo.flight.dto.FlightInfoResponseDto;
import NoDam.Demo.flight.service.AirLabsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final AirLabsService airLabsService;

    @GetMapping("/{flightIata}")
    public FlightInfoResponseDto getFlight(@PathVariable String flightIata) {
        return airLabsService.getFlightInfo(flightIata);
    }
}