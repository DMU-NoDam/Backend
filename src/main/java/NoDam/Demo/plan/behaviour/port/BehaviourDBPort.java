package NoDam.Demo.plan.behaviour.port;

import NoDam.Demo.plan.behaviour.domain.Behaviour;
import NoDam.Demo.plan.behaviour.domain.PreviousBehaviours;
import org.springframework.dao.DataIntegrityViolationException;

// BehaviourHistory 저장소 output port
public interface BehaviourDBPort {

    // localVersion 이후 적용된 behaviour들을 읽는다
    // DatePlan 순서와 같은 transaction에서 읽어야 한다 (호출부 책임)
    PreviousBehaviours findPreviousBehaviours(Long datePlanId, long localVersion);

    // localVersion + 1 을 선점한다. (datePlanId, version) unique 충돌이면 CAS 실패
    // transaction은 호출부에서 연다
    void saveBehaviourHistory(
            Long datePlanId,
            long localVersion,
            Behaviour behaviour
    ) throws DataIntegrityViolationException;

    // polling용. 최신 version만 읽는다
    Long selectLatestVersion(Long datePlanId);

}
