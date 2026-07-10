package NoDam.Demo.trip.domain;

import NoDam.Demo.common.converter.LongListConverter;
import NoDam.Demo.common.converter.StringListConverter;
import NoDam.Demo.flight.service.AirportCode;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * trip 생성 요청 스냅샷
 * trip domain에 담기지 않는 입력값 + google -> place 변환 결과를 보관한다.
 * 생성 : trip domain 생성 직후 (원자적으로 함께 저장)
 * 사용 : 2번(google -> db 변환), 3번(List<DatePlan> 생성) 작업
 * 삭제 : 3번 작업 이후 (List<DatePlan> 이 모두 생성된 이후)
 */
@Entity
@Table(name = "trip_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TripRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false, unique = true)
    private Long tripId;

    // === 입력값 (trip domain에 없는 값) ===

    @Convert(converter = StringListConverter.class)
    @Column(nullable = true)
    private List<String> regionCodes; // 사용자 선택 region code 목록

    @Convert(converter = StringListConverter.class)
    @Column(nullable = true)
    private List<String> selectedPlaceGoogleIds; // 사용자 입력 필수 장소 google id 목록

    @Column(nullable = true)
    private String hotelGoogleId; // 사용자 입력 숙소 google id (단일)

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private AirportCode departAirportCode; // 출발(한국) 공항

    @Column(nullable = true)
    private LocalDateTime departTime; // 한국 출발 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private AirportCode arriveAirportCode; // 도착(목적지) 공항

    @Column(nullable = true)
    private LocalDateTime arriveTime; // 목적지 도착 시간

    // === google -> place 변환 결과 (2번 작업에서 채움) ===

    @Convert(converter = LongListConverter.class)
    @Column(nullable = true)
    private List<Long> selectedPlaceIds; // 변환된 필수 장소 place id 목록

    @Column(nullable = true)
    private Long hotelPlaceId; // 변환된 숙소 place id

    @Column(nullable = true)
    private Long departAirportPlaceId; // code -> db 확인 후 변환된 출발 공항 place id

    @Column(nullable = true)
    private Long arriveAirportPlaceId; // code -> db 확인 후 변환된 도착 공항 place id

    @Builder
    public TripRequest(Long tripId, List<String> regionCodes, List<String> selectedPlaceGoogleIds,
                       String hotelGoogleId, AirportCode departAirportCode, LocalDateTime departTime,
                       AirportCode arriveAirportCode, LocalDateTime arriveTime) {
        this.tripId = tripId;
        this.regionCodes = regionCodes != null ? regionCodes : List.of();
        this.selectedPlaceGoogleIds = selectedPlaceGoogleIds != null ? selectedPlaceGoogleIds : List.of();
        this.hotelGoogleId = hotelGoogleId;
        this.departAirportCode = departAirportCode;
        this.departTime = departTime;
        this.arriveAirportCode = arriveAirportCode;
        this.arriveTime = arriveTime;
        this.selectedPlaceIds = List.of();
    }

    // 2번 작업 : google/code -> place 변환 결과 반영
    public void updateConvertedPlaces(List<Long> selectedPlaceIds, Long hotelPlaceId,
                                      Long departAirportPlaceId, Long arriveAirportPlaceId) {
        this.selectedPlaceIds = selectedPlaceIds != null ? selectedPlaceIds : List.of();
        this.hotelPlaceId = hotelPlaceId;
        this.departAirportPlaceId = departAirportPlaceId;
        this.arriveAirportPlaceId = arriveAirportPlaceId;
    }

}
