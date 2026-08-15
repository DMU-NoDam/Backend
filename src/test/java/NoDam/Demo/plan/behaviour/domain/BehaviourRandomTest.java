package NoDam.Demo.plan.behaviour.domain;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.type.TripThemeType;
import NoDam.Demo.plan.behaviour.adapter.BehaviourHistoryRepository;
import NoDam.Demo.plan.behaviour.service.BehaviourService;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.PlacePlan;
import NoDam.Demo.plan.repository.DatePlanRepository;
import NoDam.Demo.plan.repository.PlacePlanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
    랜덤 behaviour를 실제 DB에 동시에 던지고 결과를 확인한다

    1. 동시 실행 : 모두 version 0에서 출발, (datePlanId, version) unique로 경쟁 -> 진 쪽은 rebase 후 재시도
    2. 재생 1   : history에 남은 behaviour(rebase가 끝난 형태)를 version 순으로 메모리 DatePlan에 다시 적용
    3. 재생 2   : 처음 만든 event들을 history에 남은 순서대로 rebase 하며 메모리 date plan에 적용한다
    4. 비교     : 3개 결과의 place 순서가 같아야 한다
 */
@SpringBootTest
class BehaviourRandomTest {

    private static final long GAP = 10_000_000_000_000_000L;

    private static final int PLACE_PLAN_COUNT = 5;  // A B C D E
    private static final int BEHAVIOUR_COUNT = 8;
    private static final int THREAD_COUNT = 8;

    @Autowired private BehaviourService behaviourService;
    @Autowired private BehaviourHistoryRepository behaviourHistoryRepository;
    @Autowired private DatePlanRepository datePlanRepository;
    @Autowired private PlacePlanRepository placePlanRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    @PersistenceContext private EntityManager entityManager;

    private DatePlan datePlan;
    private List<Long> placePlanIds;
    private List<Long> placeIds;

    @BeforeEach
    void setUp() {
        datePlan = datePlanRepository.save(DatePlan.builder()
                .date(LocalDate.now())
                .tripId(-1L)
                .regionId(-1L)
                .tripThemeType(TripThemeType.FOOD)
                .build());

        placePlanIds = new ArrayList<>();
        placeIds = new ArrayList<>();

        for (int i = 1; i <= PLACE_PLAN_COUNT; i++) {
            PlacePlan placePlan = placePlanRepository.save(PlacePlan.builder()
                    .datePlan(datePlan)
                    .placeId((long) i)
                    .orderIndex(GAP * i)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .build());

            placePlanIds.add(placePlan.getId());
            placeIds.add(placePlan.getPlaceId());
        }
    }

    // repository.delete()는 @SQLDelete라 soft delete가 쌓인다. 테스트 데이터는 native로 지운다
    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            hardDelete("delete from behaviour_history where date_plan_id = :datePlanId");
            hardDelete("delete from place_plan where date_plan_id = :datePlanId");
            hardDelete("delete from date_plan where id = :datePlanId");
        });
    }

    private void hardDelete(String sql) {
        entityManager.createNativeQuery(sql)
                .setParameter("datePlanId", datePlan.getId())
                .executeUpdate();
    }

    @Test
    @DisplayName("동시 실행, 적용본 재생, 원본 재생의 결과가 모두 같다")
    void 동시_실행과_재생_결과가_같다() throws Exception {
        long seed = 1000L;
        List<Behaviour> behaviours = randomBehaviours(new Random(seed));

        // 적용본(rebase 완료) -> 그 원본 (rebase 전 상태)
        Map<Behaviour, Behaviour> originalByApplied = runConcurrently(behaviours);

        List<BehaviourHistory> histories = behaviourHistoryRepository.findAllAfterVersion(datePlan.getId(), 0);

        // 성공한 것 + 버려진 것 = 요청 수
        assertEquals(BEHAVIOUR_COUNT, histories.size() + (BEHAVIOUR_COUNT - originalByApplied.size()), "seed=" + seed);
        assertEquals(histories.size(), originalByApplied.size(), "성공 수와 기록 수가 다름 seed=" + seed);

        // version은 1부터 빈 번호 없이 이어져야 한다
        for (int i = 0; i < histories.size(); i++)
            assertEquals(i + 1, histories.get(i).getVersion(), "version 불연속 seed=" + seed);

        assertEquals(currentPlaceIds(), replayApplied(histories), "적용본 재생 불일치 seed=" + seed);
        assertEquals(currentPlaceIds(), replayOriginal(histories, originalByApplied), "원본 재생 불일치 seed=" + seed);
    }

    // 모두 version 0에서 출발시켜 경쟁을 만든다. 성공한 것만 적용본 -> 원본으로 담아 반환한다
    private Map<Behaviour, Behaviour> runConcurrently(List<Behaviour> behaviours) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(behaviours.size());

        Map<Behaviour, Behaviour> originalByApplied = new ConcurrentHashMap<>();

        for (Behaviour behaviour : behaviours) {
            Behaviour original = copy(behaviour); // trySave가 behaviour를 rebase로 바꾸므로 원본을 남긴다

            executor.execute(() -> {
                try {
                    startLatch.await();
                    behaviourService.trySave(datePlan.getId(), 0L, behaviour);
                    originalByApplied.put(behaviour, original); // 예외 없이 끝났으면 저장된 것
                } catch (CustomException e) {
                    // CONFLICT, 버려진 behaviour
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(endLatch.await(30, TimeUnit.SECONDS), "동시 실행이 끝나지 않음");
        executor.shutdownNow();

        return originalByApplied;
    }

    // 재생 1 : 적용본을 version 순서대로 rebase 없이 적용한다
    private List<Long> replayApplied(List<BehaviourHistory> histories) {
        DatePlan replayDatePlan = initialDatePlan();

        for (BehaviourHistory history : histories)
            history.getBehaviour().apply(replayDatePlan);

        return placeIdsOf(replayDatePlan);
    }

    // 재생 2 : 원본을 version 순서대로 앞선 것들 기준으로 rebase 하며 적용한다
    private List<Long> replayOriginal(List<BehaviourHistory> histories, Map<Behaviour, Behaviour> originalByApplied) {
        DatePlan replayDatePlan = initialDatePlan();
        List<BehaviourHistory> appliedHistories = new ArrayList<>();

        for (BehaviourHistory history : histories) {
            Behaviour rebased = copy(findOriginal(originalByApplied, history.getBehaviour()));

            if (!appliedHistories.isEmpty()) // 빈 목록으로는 PreviousBehaviours를 만들 수 없다
                rebased.rebase(replayDatePlan, new PreviousBehaviours(appliedHistories));

            rebased.apply(replayDatePlan);
            appliedHistories.add(BehaviourHistory.builder()
                    .datePlanId(datePlan.getId())
                    .version(appliedHistories.size() + 1)
                    .behaviour(rebased)
                    .build());
        }

        return placeIdsOf(replayDatePlan);
    }

    // history에 남은 적용본과 같은 대상을 가진 원본을 찾는다
    private Behaviour findOriginal(Map<Behaviour, Behaviour> originalByApplied, Behaviour saved) {
        for (Map.Entry<Behaviour, Behaviour> entry : originalByApplied.entrySet())
            if (entry.getKey().getClass().equals(saved.getClass())
                    && Objects.equals(targetKey(entry.getKey()), targetKey(saved)))
                return entry.getValue();

        throw new IllegalStateException("적용본의 원본을 찾지 못함");
    }

    // 성공한 behaviour들 사이에서는 (type, 이 key)가 유일하다
    // 같은 type + 같은 대상은 공통 규칙으로 버려지고, ADD의 placeId는 요청마다 다르게 만든다
    private Long targetKey(Behaviour behaviour) {
        if (behaviour instanceof AddPlaceBehaviour addPlaceBehaviour) return addPlaceBehaviour.getPlaceId();
        if (behaviour instanceof ChangePlaceBehaviour changePlaceBehaviour) return changePlaceBehaviour.getPlacePlanId();
        if (behaviour instanceof RemovePlaceBehaviour removePlaceBehaviour) return removePlaceBehaviour.getPlacePlanId();

        return ((MovePlaceBehaviour) behaviour).getPlacePlanId();
    }

    // 실행 전 상태와 같은 메모리 DatePlan
    private DatePlan initialDatePlan() {
        DatePlan replayDatePlan = DatePlan.builder()
                .date(datePlan.getDate())
                .tripId(datePlan.getTripId())
                .regionId(datePlan.getRegionId())
                .tripThemeType(datePlan.getTripThemeType())
                .build();
        replayDatePlan.loadTransportPlans(new ArrayList<>()); // @Transient라 직접 채운다

        for (int i = 0; i < PLACE_PLAN_COUNT; i++) {
            PlacePlan placePlan = PlacePlan.builder()
                    .datePlan(replayDatePlan)
                    .placeId(placeIds.get(i))
                    .orderIndex(GAP * (i + 1))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 0))
                    .build();

            setId(placePlan, placePlanIds.get(i));
            replayDatePlan.getPlacePlans().add(placePlan);
        }

        return replayDatePlan;
    }

    private List<Long> placeIdsOf(DatePlan target) {
        List<Long> placeIdList = new ArrayList<>();
        for (PlacePlan placePlan : target.orderdPlacePlans())
            placeIdList.add(placePlan.getPlaceId());

        return placeIdList;
    }

    private List<Long> currentPlaceIds() {
        List<Long> currentPlaceIds = new ArrayList<>();
        for (PlacePlan placePlan : placePlanRepository.findByDatePlanIdOrderByOrderIndexAsc(datePlan.getId()))
            currentPlaceIds.add(placePlan.getPlaceId());

        return currentPlaceIds;
    }

    private List<Behaviour> randomBehaviours(Random random) {
        List<Behaviour> behaviours = new ArrayList<>();
        for (int i = 0; i < BEHAVIOUR_COUNT; i++)
            behaviours.add(randomBehaviour(random, 100L + i)); // placeId는 요청마다 다르게 (원본 찾기에 쓴다)

        return behaviours;
    }

    private Behaviour randomBehaviour(Random random, Long placeId) {
        int type = random.nextInt(4); // FIX는 아직 적용 대상이 없어 제외한다

        if (type == 0) { // ADD : 인접한 쌍 사이
            int index = random.nextInt(placePlanIds.size() + 1);
            return new AddPlaceBehaviour(previousId(index), nextId(index), placeId);
        }

        if (type == 1)
            return new ChangePlaceBehaviour(randomPlacePlanId(random), placeId);

        if (type == 2)
            return new RemovePlaceBehaviour(randomPlacePlanId(random));

        int index = random.nextInt(placePlanIds.size() + 1);
        return new MovePlaceBehaviour(randomPlacePlanId(random), previousId(index), nextId(index));
    }

    private Behaviour copy(Behaviour behaviour) {
        if (behaviour instanceof AddPlaceBehaviour addPlaceBehaviour)
            return new AddPlaceBehaviour(addPlaceBehaviour.getPreviousPlacePlanId(),
                    addPlaceBehaviour.getNextPlacePlanId(), addPlaceBehaviour.getPlaceId());

        if (behaviour instanceof ChangePlaceBehaviour changePlaceBehaviour)
            return new ChangePlaceBehaviour(changePlaceBehaviour.getPlacePlanId(), changePlaceBehaviour.getPlaceId());

        if (behaviour instanceof RemovePlaceBehaviour removePlaceBehaviour)
            return new RemovePlaceBehaviour(removePlaceBehaviour.getPlacePlanId());

        MovePlaceBehaviour movePlaceBehaviour = (MovePlaceBehaviour) behaviour;
        return new MovePlaceBehaviour(movePlaceBehaviour.getPlacePlanId(),
                movePlaceBehaviour.getPreviousPlacePlanId(), movePlaceBehaviour.getNextPlacePlanId());
    }

    private Long previousId(int index) {
        return index == 0 ? null : placePlanIds.get(index - 1);
    }

    private Long nextId(int index) {
        return index == placePlanIds.size() ? null : placePlanIds.get(index);
    }

    private Long randomPlacePlanId(Random random) {
        return placePlanIds.get(random.nextInt(placePlanIds.size()));
    }

    // PlacePlan.id는 setter가 없어 리플렉션으로 넣는다 (재생용 사본)
    private void setId(PlacePlan placePlan, Long id) {
        try {
            Field field = PlacePlan.class.getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(placePlan, id);
        } catch (Exception e) {
            throw new IllegalStateException("id 설정 실패", e);
        }
    }

}
