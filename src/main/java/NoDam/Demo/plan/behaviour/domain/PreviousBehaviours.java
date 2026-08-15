package NoDam.Demo.plan.behaviour.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class PreviousBehaviours {

    private long latestVersion;
    private List<BehaviourHistory> behaviourHistories; // version 오름차순

    public PreviousBehaviours(
            List<BehaviourHistory> previousBehaviours, // local version (client version) ~ db version
            long localVersion // db에서 읽기 이전 client로 받은 version
    ) {
        if(previousBehaviours.isEmpty()) {
            latestVersion = localVersion;
            behaviourHistories = List.of();
        } else {
            this.behaviourHistories = new ArrayList<>(previousBehaviours);
            this.behaviourHistories.sort(Comparator.comparingLong(BehaviourHistory::getVersion));
            this.latestVersion = this.behaviourHistories.getLast().getVersion();
        }

        // validate :: local version ~ db version 빠진 값은 없는지
        if(!isEmpty() && behaviourHistories.stream().count() != latestVersion - localVersion)
            throw new RuntimeException(String.format(
                    "BehaviourHistory 누락: datePlanId=%d, version 범위 (%d, %d] 에는 %d개가 있어야 하지만 %d개만 조회됨",
                    behaviourHistories.getFirst().getDatePlanId(),
                    localVersion,
                    latestVersion,
                    latestVersion - localVersion,
                    behaviourHistories.size()));
    }

    // 적용된 순서(version 오름차순)대로 돌려준다
    public List<Behaviour> behaviours() {
        return behaviourHistories.stream().map(BehaviourHistory::getBehaviour).toList();
    }

    public long getLatestVersion() {
        return latestVersion;
    }

    public boolean isEmpty() {
        return behaviourHistories.isEmpty();
    }

}
