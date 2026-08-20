package NoDam.Demo.plan.domain;

import NoDam.Demo.common.converter.LongListConverter;
import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.dto.response.RouteInfo;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "date_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE date_plan SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class DatePlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(nullable = false)
    private Long regionId; // cross-module: region 참조

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TripThemeType tripThemeType;

    @Convert(converter = LongListConverter.class)
    @Column(nullable = true)
    private List<Long> necessaryPlaces; // 필수 장소 place id 목록

    @Column(nullable = true)
    private Long hotelPlaceId; // 해당 날짜 숙소 place id

    @Column(nullable = true)
    private Long airportPlaceId; // 해당 날짜 공항 place id (첫날 = 목적지 도착 공항, 마지막날 = 목적지 출발 공항)

    @Column(nullable = true)
    private LocalTime airportTime; // 첫날 = 한국 출발 시간 ceiling, 마지막날 = 목적지 출발 시간 ceiling

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus planStatus;

    @OneToMany(mappedBy = "datePlan")
    private List<PlacePlan> placePlans = new ArrayList<>();

    @Transient
    private List<TransportPlan> transportPlans;

    @Builder
    public DatePlan(LocalDate date, Long tripId, Long regionId, List<Long> necessaryPlaces, TripThemeType tripThemeType,
                    Long hotelPlaceId, Long airportPlaceId, LocalTime airportTime) {
        this.date = date;
        this.tripId = tripId;
        this.regionId = regionId;
        this.necessaryPlaces = necessaryPlaces != null ? necessaryPlaces : List.of();
        this.tripThemeType = tripThemeType;
        this.hotelPlaceId = hotelPlaceId;
        this.airportPlaceId = airportPlaceId;
        this.airportTime = airportTime;
        this.planStatus = PlanStatus.CREATED;
    }

    public void updatePlanStatus(PlanStatus planStatus) {
        this.planStatus = planStatus;
    }

    // orderIndex 초기 간격. 사이 삽입은 중간값으로 쪼갠다
    private static final long ORDER_INDEX_GAP = 10_000_000_000_000_000L;

    public List<PlacePlan> orderdPlacePlans() {
        List<PlacePlan> orderdPlacePlans = new ArrayList<>(placePlans);
        orderdPlacePlans.sort(Comparator.comparing(PlacePlan::getOrderIndex));

        return orderdPlacePlans;
    }

    public boolean existPlacePlan(Long placePlanId) {
        for (PlacePlan placePlan : placePlans)
            if (Objects.equals(placePlan.getId(), placePlanId)) return true;

        return false;
    }

    // 기준 바로 다음 PlacePlan. 기준이 null이면 맨 앞, 다음이 없으면 null
    public PlacePlan findNextPlacePlan(Long placePlanId) {
        List<PlacePlan> orderdPlacePlans = orderdPlacePlans();

        if (placePlanId == null)
            return orderdPlacePlans.isEmpty() ? null : orderdPlacePlans.getFirst();

        for (int i = 0; i < orderdPlacePlans.size() - 1; i++)
            if (Objects.equals(orderdPlacePlans.get(i).getId(), placePlanId))
                return orderdPlacePlans.get(i + 1);

        return null;
    }

    // 기준 바로 이전 PlacePlan. 기준이 null이면 맨 뒤, 이전이 없으면 null
    public PlacePlan findPreviousPlacePlan(Long placePlanId) {
        List<PlacePlan> orderdPlacePlans = orderdPlacePlans();

        if (placePlanId == null)
            return orderdPlacePlans.isEmpty() ? null : orderdPlacePlans.getLast();

        for (int i = 1; i < orderdPlacePlans.size(); i++)
            if (Objects.equals(orderdPlacePlans.get(i).getId(), placePlanId))
                return orderdPlacePlans.get(i - 1);

        return null;
    }

    // previous, next 사이에 새 PlacePlan을 넣는다
    public PlacePlan addPlacePlan(Long placeId, Long previousPlacePlanId, Long nextPlacePlanId) {
        PlacePlan placePlan = PlacePlan.builder()
                .datePlan(this)
                .placeId(placeId)
                .orderIndex(calcOrderIndex(previousPlacePlanId, nextPlacePlanId))
                .startTime(null)
                .endTime(null)
                .build();

        placePlans.add(placePlan);
        detachTransportPlan(previousPlacePlanId, nextPlacePlanId); // 사이에 끼어들었으므로 끊긴다

        return placePlan;
    }

    // 처음 List<place plan> 를 만들 때 사용
    // todo : request 를 받아서 처리하고 List<PlacePlan>을 반환하는 형식으로 수정 필요, 나중에 auto create 수정할 떄 같이 수정
    public void addAll(List<PlacePlan> newPlacePlans) {
        placePlans.addAll(newPlacePlans);

        List<PlacePlan> sortedPlacePlans = new ArrayList<>(placePlans);
        sortedPlacePlans.sort(Comparator.comparing(
                PlacePlan::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));

        long orderIndex = ORDER_INDEX_GAP;
        for (PlacePlan placePlan : sortedPlacePlans) {
            placePlan.updateOrderIndex(orderIndex);
            orderIndex += ORDER_INDEX_GAP;
        }
    }

    // previous, next 사이로 옮긴다
    public PlacePlan movePlacePlan(Long placePlanId, Long previousPlacePlanId, Long nextPlacePlanId) {
        PlacePlan placePlan = findPlacePlan(placePlanId);
        placePlan.updateOrderIndex(calcOrderIndex(previousPlacePlanId, nextPlacePlanId));

        detachTransportPlansOf(placePlanId);                        // 떠난 자리의 앞뒤 구간이 끊긴다
        detachTransportPlan(previousPlacePlanId, nextPlacePlanId);  // 끼어든 자리의 구간이 끊긴다

        return placePlan;
    }

    public PlacePlan changePlaceId(Long placePlanId, Long placeId) {
        PlacePlan placePlan = findPlacePlan(placePlanId);
        placePlan.updatePlaceId(placeId);

        detachTransportPlansOf(placePlanId); // 장소가 바뀌었으므로 앞뒤 경로를 버린다

        return placePlan;
    }

    // 목록에서만 제외한다. 실제 삭제는 조회한 쪽의 책임
    public PlacePlan removePlacePlan(Long placePlanId) {
        PlacePlan placePlan = findPlacePlan(placePlanId);
        placePlans.remove(placePlan);

        detachTransportPlansOf(placePlanId);

        return placePlan;
    }

    // @Transient라 조회한 쪽이 채워준다
    public void loadTransportPlans(List<TransportPlan> transportPlans) {
        this.transportPlans = new ArrayList<>(transportPlans);
    }

    // 고아가 된 TransportPlan을 목록에서 빼고 돌려준다. 실제 삭제는 조회한 쪽의 책임
    public List<TransportPlan> detachedTransportPlans() {
        List<TransportPlan> detachedTransportPlans = new ArrayList<>();

        for (TransportPlan transportPlan : transportPlans)
            if (transportPlan.getDetached()) detachedTransportPlans.add(transportPlan);

        transportPlans.removeAll(detachedTransportPlans);

        return detachedTransportPlans;
    }

    // empty transport legs :: place plan <> place plan 중에 없는 transport 정보 vo 들
    // 조회해야할 transport plans들
    // 끊긴 구간은 이미 detach 되었으므로, 살아있는 from은 곧 현재 순서의 from이다
    public List<TransportLeg> getDetachedTransportLegs() {
        Set<Long> connectedPlacePlanIds = new HashSet<>(); // 나가는 이동이 살아있는 PlacePlan
        for (TransportPlan transportPlan : transportPlans)
            if (!transportPlan.getDetached())
                connectedPlacePlanIds.add(transportPlan.getFromPlacePlanId());

        List<PlacePlan> orderdPlacePlans = orderdPlacePlans();

        List<TransportLeg> emptyTransportLegs = new ArrayList<>();
        for (int i = 0; i < orderdPlacePlans.size() - 1; i++) {
            PlacePlan from = orderdPlacePlans.get(i);
            if (connectedPlacePlanIds.contains(from.getId())) continue;

            emptyTransportLegs.add(new TransportLeg(from, orderdPlacePlans.get(i + 1), null));
        }

        return emptyTransportLegs;
    }

    // from, to가 이 DatePlan의 것인지 확인하고 넣는다. 같은 구간이 이미 있으면 덮어쓴다
    public void addTransportPlan(TransportLeg leg, RouteInfo info) {

        PlacePlan from = findPlacePlan(leg.from().getId());
        PlacePlan to = findPlacePlan(leg.to().getId());

        PlacePlan next = findNextPlacePlan(from.getId());
        if (next == null || !Objects.equals(next.getId(), to.getId()))
            return;

        detachTransportPlan(from.getId(), to.getId()); // 기존 구간은 버린다

        transportPlans.add(TransportPlan.builder()
                .fromPlacePlan(from)
                .toPlacePlan(to)
                .routeInfo(info)
                .build());
    }

    // before -> target -> after 2개의 transport plan 을 detach한다
    private void detachTransportPlansOf(Long placePlanId) {
        for (TransportPlan transportPlan : transportPlans) {
            if (transportPlan.getDetached()) continue;

            // target -> after
            if (Objects.equals(transportPlan.getFromPlacePlanId(), placePlanId)) {
                transportPlan.detach();
                continue;
            }

            // before -> target
            if (Objects.equals(transportPlan.getToPlacePlanId(), placePlanId))
                transportPlan.detach();
        }
    }

    // A -> B의 transport 를 detach
    private void detachTransportPlan(Long betweenPlacePlanA, Long betweenPlacePlanB) {
        for (TransportPlan transportPlan : transportPlans) {
            if (transportPlan.getDetached()) continue;
            if (!Objects.equals(transportPlan.getFromPlacePlanId(), betweenPlacePlanA)) continue;
            if (!Objects.equals(transportPlan.getToPlacePlanId(), betweenPlacePlanB)) continue;

            transportPlan.detach(); // 여러개면 모두 detach 한다
        }
    }

    // previous, next 사이의 중간값. 한쪽이 없으면 맨 앞(절반) 또는 맨 뒤(+GAP)
    private Long calcOrderIndex(Long previousPlacePlanId, Long nextPlacePlanId) {
        Long previousOrderIndex = previousPlacePlanId == null ? null : findPlacePlan(previousPlacePlanId).getOrderIndex();
        Long nextOrderIndex = nextPlacePlanId == null ? null : findPlacePlan(nextPlacePlanId).getOrderIndex();

        if (previousOrderIndex == null && nextOrderIndex == null)
            throw new RuntimeException("DatePlan.calcOrderIndex :: previous, next index are null");
        if (previousOrderIndex == null) return nextOrderIndex / 2;
        if (nextOrderIndex == null) return previousOrderIndex + ORDER_INDEX_GAP;

        // 간격을 다 쓰면 renumbering이 필요하다 (아직 없으므로 실패시킨다)
        if (nextOrderIndex - previousOrderIndex <= 1)
            throw new CustomException(ErrorCode.CONFLICT);

        return previousOrderIndex + (nextOrderIndex - previousOrderIndex) / 2;
    }

    public PlacePlan findPlacePlan(Long placePlanId) {
        for (PlacePlan placePlan : placePlans)
            if (Objects.equals(placePlan.getId(), placePlanId)) return placePlan;

        throw new CustomException(ErrorCode.NOT_FOUND);
    }
}
