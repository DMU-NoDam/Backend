package NoDam.Demo.plan.domain;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.dto.response.RouteInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
    DatePlan domain 단위 test (DB 없음)

    기본 상태 : A(1) -> B(2) -> C(3), transport는 test마다 필요한 만큼 붙인다
    id는 DB가 만드는 값이라 리플렉션으로 넣는다
 */
class DatePlanTest {

    private static final long GAP = 10_000_000_000_000_000L;

    private DatePlan datePlan;
    private PlacePlan placePlanA;
    private PlacePlan placePlanB;
    private PlacePlan placePlanC;

    @BeforeEach
    void setUp() {
        datePlan = DatePlan.builder()
                .date(LocalDate.of(2026, 1, 1))
                .tripId(1L)
                .regionId(1L)
                .tripThemeType(TripThemeType.FOOD)
                .build();
        setId(datePlan, 1L);
        datePlan.loadTransportPlans(new ArrayList<>());

        placePlanA = addPlacePlan(1L, GAP);
        placePlanB = addPlacePlan(2L, GAP * 2);
        placePlanC = addPlacePlan(3L, GAP * 3);
    }

    @Nested
    @DisplayName("place plan 순서")
    class Order {

        @Test
        @DisplayName("orderIndex 순으로 정렬해 돌려준다")
        void 정렬() {
            addPlacePlan(4L, GAP / 2); // 맨 앞에 끼워 넣는다

            assertEquals(List.of(4L, 1L, 2L, 3L), placePlanIds(datePlan.orderdPlacePlans()));
        }

        @Test
        @DisplayName("기준이 null이면 맨 앞 / 맨 뒤를 돌려준다")
        void 기준이_null() {
            assertEquals(1L, datePlan.findNextPlacePlanId(null));
            assertEquals(3L, datePlan.findPreviousPlacePlanId(null));
        }

        @Test
        @DisplayName("끝에서는 다음 / 이전이 없다")
        void 끝() {
            assertNull(datePlan.findNextPlacePlanId(3L));
            assertNull(datePlan.findPreviousPlacePlanId(1L));
        }

        @Test
        @DisplayName("이 DatePlan의 것이 아니면 null")
        void 없는_id() {
            assertNull(datePlan.findNextPlacePlanId(99L));
            assertNull(datePlan.findPreviousPlacePlanId(99L));
        }

        @Test
        void existPlacePlan() {
            assertTrue(datePlan.existPlacePlan(2L));
            assertFalse(datePlan.existPlacePlan(99L));
        }
    }

    @Nested
    @DisplayName("place plan 변경")
    class Modify {

        @Test
        @DisplayName("사이에 넣으면 orderIndex는 두 값의 중간")
        void 사이_삽입() {
            PlacePlan added = datePlan.addPlacePlan(500L, 1L, 2L);

            assertEquals(GAP + GAP / 2, added.getOrderIndex());
            assertEquals(4, datePlan.getPlacePlans().size());
        }

        @Test
        @DisplayName("맨 앞은 절반, 맨 뒤는 GAP만큼 더한다")
        void 맨앞_맨뒤() {
            assertEquals(GAP / 2, datePlan.addPlacePlan(500L, null, 1L).getOrderIndex());
            assertEquals(GAP * 4, datePlan.addPlacePlan(500L, 3L, null).getOrderIndex());
        }

        @Test
        @DisplayName("간격이 다 차면 CONFLICT")
        void 간격_소진() {
            addPlacePlan(4L, GAP + 1);

            CustomException exception = assertThrows(CustomException.class,
                    () -> datePlan.addPlacePlan(500L, 1L, 4L));
            assertEquals(ErrorCode.CONFLICT, exception.errorCode);
        }

        @Test
        @DisplayName("없는 place plan을 기준으로 삼으면 NOT_FOUND")
        void 없는_기준() {
            CustomException exception = assertThrows(CustomException.class,
                    () -> datePlan.addPlacePlan(500L, 99L, null));
            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
        }

        @Test
        @DisplayName("옮기면 orderIndex만 바뀐다")
        void 이동() {
            datePlan.movePlacePlan(1L, 2L, 3L); // A를 B와 C 사이로

            assertEquals(GAP * 2 + GAP / 2, placePlanA.getOrderIndex());
            assertEquals(List.of(2L, 1L, 3L), placePlanIds(datePlan.orderdPlacePlans()));
        }

        @Test
        void 장소_변경() {
            datePlan.changePlaceId(1L, 999L);

            assertEquals(999L, placePlanA.getPlaceId());
        }

        @Test
        @DisplayName("삭제는 목록에서만 뺀다")
        void 삭제() {
            PlacePlan removed = datePlan.removePlacePlan(2L);

            assertSame(placePlanB, removed);
            assertEquals(List.of(1L, 3L), placePlanIds(datePlan.orderdPlacePlans()));
        }
    }

    @Nested
    @DisplayName("place plan이 바뀌면 transport가 끊긴다")
    class Detach {

        @Test
        @DisplayName("사이에 끼어들면 그 구간만 끊긴다")
        void 삽입() {
            TransportPlan transportAB = addTransportPlan(10L, placePlanA, placePlanB);
            TransportPlan transportBC = addTransportPlan(11L, placePlanB, placePlanC);

            datePlan.addPlacePlan(500L, 1L, 2L);

            assertTrue(transportAB.getDetached());
            assertFalse(transportBC.getDetached());
        }

        @Test
        @DisplayName("옮기면 떠난 자리의 앞뒤와 끼어든 구간이 끊긴다")
        void 이동() {
            PlacePlan placePlanD = addPlacePlan(4L, GAP * 4);

            TransportPlan transportAB = addTransportPlan(10L, placePlanA, placePlanB);
            TransportPlan transportBC = addTransportPlan(11L, placePlanB, placePlanC);
            TransportPlan transportCD = addTransportPlan(12L, placePlanC, placePlanD);

            datePlan.movePlacePlan(2L, 3L, 4L); // B를 C와 D 사이로

            assertTrue(transportAB.getDetached()); // 떠난 자리 : A -> B
            assertTrue(transportBC.getDetached()); // 떠난 자리 : B -> C
            assertTrue(transportCD.getDetached()); // 끼어든 자리 : C -> D
        }

        @Test
        @DisplayName("장소가 바뀌면 앞뒤가 끊긴다")
        void 장소_변경() {
            TransportPlan transportAB = addTransportPlan(10L, placePlanA, placePlanB);
            TransportPlan transportBC = addTransportPlan(11L, placePlanB, placePlanC);

            datePlan.changePlaceId(2L, 999L);

            assertTrue(transportAB.getDetached());
            assertTrue(transportBC.getDetached());
        }

        @Test
        @DisplayName("삭제하면 앞뒤가 끊긴다")
        void 삭제() {
            TransportPlan transportAB = addTransportPlan(10L, placePlanA, placePlanB);
            TransportPlan transportBC = addTransportPlan(11L, placePlanB, placePlanC);

            datePlan.removePlacePlan(2L);

            assertTrue(transportAB.getDetached());
            assertTrue(transportBC.getDetached());
        }

        @Test
        @DisplayName("끊긴 것은 목록에서 빠져 나온다")
        void 끊긴_것_수거() {
            TransportPlan transportAB = addTransportPlan(10L, placePlanA, placePlanB);
            TransportPlan transportBC = addTransportPlan(11L, placePlanB, placePlanC);

            transportAB.detach();

            assertEquals(List.of(transportAB), datePlan.detachedTransportPlans());
            assertEquals(List.of(transportBC), datePlan.getTransportPlans());
            assertTrue(datePlan.detachedTransportPlans().isEmpty()); // 두 번 불러도 남는 게 없다
        }
    }

    @Nested
    @DisplayName("transport 조회와 추가")
    class Transport {

        @Test
        @DisplayName("이동이 없는 구간만 돌려준다")
        void 빈_구간() {
            addTransportPlan(10L, placePlanA, placePlanB);

            List<TransportLeg> legs = datePlan.getDetachedTransportLegs();

            assertEquals(1, legs.size());
            assertEquals(2L, legs.getFirst().from().getId());
            assertEquals(3L, legs.getFirst().to().getId());
        }

        @Test
        @DisplayName("끊긴 이동이 남아 있어도 빈 구간으로 센다")
        void 끊긴_구간() {
            TransportPlan transportAB = addTransportPlan(10L, placePlanA, placePlanB);
            transportAB.detach();

            assertEquals(2, datePlan.getDetachedTransportLegs().size());
        }

        @Test
        @DisplayName("인접한 구간이면 넣는다")
        void 추가() {
            datePlan.addTransportPlan(new TransportLeg(placePlanA, placePlanB, null), routeInfo());

            assertEquals(1, datePlan.getTransportPlans().size());

            TransportPlan added = datePlan.getTransportPlans().getFirst();
            assertEquals(1L, added.getFromPlacePlan().getId());
            assertEquals(2L, added.getToPlacePlan().getId());
        }

        @Test
        @DisplayName("인접하지 않으면 버린다")
        void 인접하지_않음() {
            datePlan.addTransportPlan(new TransportLeg(placePlanA, placePlanC, null), routeInfo());

            assertTrue(datePlan.getTransportPlans().isEmpty());
        }

        @Test
        @DisplayName("이 DatePlan의 place plan이 아니면 NOT_FOUND")
        void 남의_place_plan() {
            TransportLeg leg = new TransportLeg(otherDatePlanPlacePlan(), placePlanB, null);

            CustomException exception = assertThrows(CustomException.class,
                    () -> datePlan.addTransportPlan(leg, routeInfo()));
            assertEquals(ErrorCode.NOT_FOUND, exception.errorCode);
        }

        @Test
        @DisplayName("같은 구간이 이미 있으면 기존 것을 끊고 덮어쓴다")
        void 덮어쓰기() {
            TransportPlan oldTransportAB = addTransportPlan(10L, placePlanA, placePlanB);

            datePlan.addTransportPlan(new TransportLeg(placePlanA, placePlanB, null), routeInfo());

            assertTrue(oldTransportAB.getDetached());
            assertEquals(List.of(oldTransportAB), datePlan.detachedTransportPlans());
            assertEquals(1, datePlan.getTransportPlans().size());
        }

    }

    private PlacePlan addPlacePlan(Long id, long orderIndex) {
        PlacePlan placePlan = PlacePlan.builder()
                .datePlan(datePlan)
                .placeId(id * 100)
                .orderIndex(orderIndex)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        setId(placePlan, id);
        datePlan.getPlacePlans().add(placePlan);

        return placePlan;
    }

    private TransportPlan addTransportPlan(Long id, PlacePlan from, PlacePlan to) {
        TransportPlan transportPlan = newTransportPlan(from, to);

        setId(transportPlan, id);
        datePlan.getTransportPlans().add(transportPlan);

        return transportPlan;
    }

    // 아직 저장되지 않은(id 없는) TransportPlan
    private TransportPlan newTransportPlan(PlacePlan from, PlacePlan to) {
        return TransportPlan.builder()
                .fromPlacePlan(from)
                .toPlacePlan(to)
                .routeInfo(null)
                .build();
    }

    private RouteInfo routeInfo() {
        return new RouteInfo(1000, 600, List.of());
    }

    private PlacePlan otherDatePlanPlacePlan() {
        DatePlan otherDatePlan = DatePlan.builder()
                .date(LocalDate.of(2026, 1, 2))
                .tripId(1L)
                .regionId(1L)
                .tripThemeType(TripThemeType.FOOD)
                .build();
        setId(otherDatePlan, 2L);

        PlacePlan placePlan = PlacePlan.builder()
                .datePlan(otherDatePlan)
                .placeId(9900L)
                .orderIndex(GAP)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();
        setId(placePlan, 99L);

        return placePlan;
    }

    private List<Long> placePlanIds(List<PlacePlan> placePlans) {
        List<Long> ids = new ArrayList<>();
        for (PlacePlan placePlan : placePlans)
            ids.add(placePlan.getId());

        return ids;
    }

    // id는 DB가 채우는 값이라 setter가 없다
    private void setId(Object entity, Long id) {
        try {
            Field field = idField(entity.getClass());
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new IllegalStateException("id 설정 실패", e);
        }
    }

    private Field idField(Class<?> type) throws NoSuchFieldException {
        try {
            return type.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            return type.getSuperclass().getDeclaredField("id");
        }
    }

}
