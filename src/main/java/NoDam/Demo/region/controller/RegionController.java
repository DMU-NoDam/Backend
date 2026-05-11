package NoDam.Demo.region.controller;

import NoDam.Demo.common.SuccessResponse;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.dto.response.RegionResponseDto;
import NoDam.Demo.region.service.RegionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/region/public")
@Tag(name = "RegionController")
public class RegionController {

    private final RegionQueryService regionQueryService;

    @GetMapping
    @Operation(summary = "전체 지역 목록 조회")
    public ResponseEntity<SuccessResponse> getRegions() {
        List<RegionResponseDto> regions = regionQueryService.findAll().stream()
                .map(RegionResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(new SuccessResponse("success", regions));
    }
}
