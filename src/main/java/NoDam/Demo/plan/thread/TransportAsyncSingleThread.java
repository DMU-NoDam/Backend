package NoDam.Demo.plan.thread;

import NoDam.Demo.adapter.route.RoutePort;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.place.domain.Place;
import NoDam.Demo.place.service.PlaceSelectService;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.domain.TransportLeg;
import NoDam.Demo.plan.dto.response.RouteInfo;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class TransportAsyncSingleThread {
    
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final DatePlanDBPort datePlanDBPort;
    private final PlaceSelectService placeSelectService;
    private final RoutePort routePort;

    private final Set<Long> runningSet = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger(TransportAsyncSingleThread.class);

    public void start(Long datePlanId) {
        synchronized (runningSet) {
            if (!runningSet.add(datePlanId)) return;
        }

        executor.execute(() -> run(datePlanId));
    }

    public boolean isEnd(Long datePlanId) {
        synchronized (runningSet) {
            return !runningSet.contains(datePlanId);
        }
    }

    // 진행 중인 작업은 끊는다 (다음 event에서 빈 구간을 다시 채운다)
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    private void run(Long datePlanId) {
        try {
            while (task(datePlanId));
        } catch (Exception e) {
            logger.error("transport async single thread failed datePlanId={}", datePlanId, e);
        } finally {
            synchronized (runningSet) {
                runningSet.remove(datePlanId);
            }
        }
    }

    // 이동이 없는 구간을 찾아 route를 계산하고 저장한다.
    // todo : transaction 범위 설정해보기
    private boolean task(Long datePlanId) {
        DatePlan datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        List<TransportLeg> legList = datePlan.getDetachedTransportLegs();
        if (legList.isEmpty()) return true;

        TransportLeg target = legList.getFirst();

        Place from = placeSelectService.findById(target.from().getPlaceId());
        Place to = placeSelectService.findById(target.to().getPlaceId());

        RouteInfo routeInfo = routePort.computeRoutesFromPlace(from, to, target.from().getEndTime());
        // route info null이어도 일단 add 처리 --> 건너 뛰기 기능이 없음

        // 계산하는 사이 순서가 바뀌었을 수 있으므로 다시 읽는다.
        datePlan = datePlanDBPort.latestDatePlan(datePlanId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 저장 가능한지 여부는 domain 내부 판단
        datePlan.addTransportPlan(target, routeInfo);

        datePlanDBPort.saveDatePlan(datePlan);

        return true;
    }

}
