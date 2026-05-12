package NoDam.Demo.plan.dto.response;

import NoDam.Demo.trip.dto.response.TripInfoDto;

import java.time.LocalDate;

public class DatePlanInfo {

    private Long id;

    private LocalDate date;
    private TripInfoDto tripInfo;

    private String region;

}
