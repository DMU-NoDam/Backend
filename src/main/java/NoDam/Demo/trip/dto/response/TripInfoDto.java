package NoDam.Demo.trip.dto.response;

import NoDam.Demo.common.type.PersonType;
import NoDam.Demo.common.type.ScheduleType;
import NoDam.Demo.common.type.TransportType;
import NoDam.Demo.common.util.DateUtil;
import NoDam.Demo.trip.domain.Trip;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripInfoDto {
    private Long id;
    private String name;
    private int personCount;
    private String site;
    private ScheduleType scheduleType;
    private TransportType transportType;
    private PersonType personType;
    private String startDate;
    private String endDate;
    private Long price;

    public static TripInfoDto from(Trip trip) {
        return TripInfoDto.builder()
                .id(trip.getId())
                .name(trip.getName())
                .personCount(trip.getPersonCount())
                .site("일본")
                .scheduleType(trip.getScheduleType())
                .transportType(trip.getTransportType())
                .personType(trip.getPersonType())
                .startDate(DateUtil.fromLocalDate(trip.getStartDate()))
                .endDate(DateUtil.fromLocalDate(trip.getEndDate()))
                .price(trip.getPrice())
                .build();
    }

    public static List<TripInfoDto> from(List<Trip> trips) {
        return trips.stream()
                .map(TripInfoDto::from)
                .collect(Collectors.toList());
    }
}
