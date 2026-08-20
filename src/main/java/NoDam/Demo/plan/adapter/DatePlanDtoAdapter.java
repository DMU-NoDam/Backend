package NoDam.Demo.plan.adapter;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.place.PlaceRepository;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.plan.behaviour.port.BehaviourDBPort;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.domain.TransportPlan;
import NoDam.Demo.plan.dto.response.DatePlanInfo;
import NoDam.Demo.plan.dto.response.PlacePlanInfo;
import NoDam.Demo.plan.repository.DatePlanDtoPort;
import NoDam.Demo.region.domain.Region;
import NoDam.Demo.region.dto.response.RegionResponseDto;
import NoDam.Demo.region.repository.RegionRepository;
import NoDam.Demo.trip.domain.Trip;
import NoDam.Demo.trip.dto.response.TripInfoDto;
import NoDam.Demo.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DatePlanDtoAdapter implements DatePlanDtoPort {

    private final PlaceRepository placeRepository;
    private final RegionRepository regionRepository;
    private final TripRepository tripRepository;
    private final BehaviourDBPort behaviourDBPort;

    @Override
    public DatePlanInfo selectDatePlanInfo(DatePlan datePlan) {
        List<PlacePlan> orderdPlacePlans = datePlan.orderdPlacePlans();

        List<Long> placeIds = new ArrayList<>();
        for (PlacePlan placePlan : orderdPlacePlans)
            placeIds.add(placePlan.getPlaceId());

        Map<Long, Place> placeMap = new HashMap<>();
        for (Place place : placeRepository.findAllById(placeIds))
            placeMap.put(place.getId(), place);

        // PlacePlan에서 출발하는 이동. 끊긴 것은 아직 계산 전이므로 내려보내지 않는다
        Map<Long, TransportPlan> fromTransportMap = new HashMap<>();
        if (datePlan.getTransportPlans() != null)
            for (TransportPlan transportPlan : datePlan.getTransportPlans())
                if (!transportPlan.getDetached())
                    fromTransportMap.put(transportPlan.getFromPlacePlanId(), transportPlan);

        List<PlacePlanInfo> placePlanInfos = new ArrayList<>();
        for (PlacePlan placePlan : orderdPlacePlans)
            placePlanInfos.add(PlacePlanInfo.of(
                    placePlan,
                    placeMap.get(placePlan.getPlaceId()),
                    fromTransportMap.get(placePlan.getId())
            ));

        Region region = regionRepository.findById(datePlan.getRegionId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        Trip trip = tripRepository.findById(datePlan.getTripId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return DatePlanInfo.builder()
                .id(datePlan.getId())
                .date(datePlan.getDate())
                .tripInfo(TripInfoDto.from(trip))
                .datePlanTheme(datePlan.getTripThemeType())
                .region(RegionResponseDto.from(region))
                .placePlanInfos(placePlanInfos)
                .version(behaviourDBPort.selectLatestVersion(datePlan.getId()))
                .build();
    }

}
