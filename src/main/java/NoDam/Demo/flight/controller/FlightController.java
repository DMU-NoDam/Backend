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

    // 편명 + 여행 날짜 범위(startDate ~ endDate) 입력
    // → 해당 범위 내에서 운항하는 항공편의 출발지/도착지/시간 반환
    // 예: GET /flight/public/BX164?startDate=2026-05-18&endDate=2026-05-22
    @GetMapping("/{flightIata}")
    public ResponseEntity<SuccessResponse<FlightInfoResponseDto>> getFlight(
            @PathVariable String flightIata,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok().body(
                new SuccessResponse<>("success", airLabsService.getFlightInfo(flightIata, startDate, endDate))
        );
    }
}
