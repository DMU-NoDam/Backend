package NoDam.Demo.plan.behaviour.adapter;

import NoDam.Demo.plan.behaviour.domain.Behaviour;
import NoDam.Demo.plan.behaviour.domain.BehaviourHistory;
import NoDam.Demo.plan.behaviour.domain.PreviousBehaviours;
import NoDam.Demo.plan.behaviour.port.BehaviourDBPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BehaviourDBAdapter implements BehaviourDBPort {

    private final BehaviourHistoryRepository behaviourHistoryRepository;

    // query 한개라 자체 transaction은 필요 없다
    // 단, DatePlan 순서와 같은 snapshot이어야 하므로 호출부의 transaction 안에서 읽는다
    // 따로 읽으면 version은 최신인데 순서는 과거인 조합이 나올 수 있고, 그 경우 CAS가 잡아내지 못한다
    @Override
    public PreviousBehaviours findPreviousBehaviours(Long datePlanId, long localVersion) {
        return new PreviousBehaviours(
                behaviourHistoryRepository.findAllAfterVersion(datePlanId, localVersion),
                localVersion
        );
    }

    // localVersion + 1 을 선점한다. 그 사이 다른 요청이 같은 version을 가져갔으면 예외
    // transaction은 호출부에서 연다 (선점과 적용이 한 transaction이어야 한다)
    @Override
    public void saveBehaviourHistory(
            Long datePlanId,
            long localVersion,
            Behaviour behaviour
    ) throws DataIntegrityViolationException {
        behaviourHistoryRepository.save(BehaviourHistory.builder()
                .datePlanId(datePlanId)
                .version(localVersion + 1)
                .behaviour(behaviour)
                .build());
    }

    // 편집 기록이 없으면(생성 직후) 0
    @Override
    public Long selectLatestVersion(Long datePlanId) {
        Long latestVersion = behaviourHistoryRepository.findLatestVersion(datePlanId);

        return latestVersion == null ? 0L : latestVersion;
    }

}
