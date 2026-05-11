package NoDam.Demo.stay.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.stay.dto.XoteloSearchResponseDto;
import NoDam.Demo.stay.service.XoteloSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stay/public")
@Tag(name = "StayController")
public class StayController {

    private final XoteloSearchService xoteloSearchService;

    @GetMapping("/search")
    @Operation(summary = "stay 지역 코드로 검색")
    public ResponseEntity<SuccessResponse<List<XoteloSearchResponseDto>>> searchStays(@RequestParam String regionCode) {
        List<XoteloSearchResponseDto> stays = xoteloSearchService.searchStays(regionCode);
        return ResponseEntity.ok().body(new SuccessResponse<>("success", stays));
    }
}
