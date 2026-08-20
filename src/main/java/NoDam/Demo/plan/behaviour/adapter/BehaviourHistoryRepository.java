package NoDam.Demo.plan.behaviour.adapter;

import NoDam.Demo.plan.behaviour.domain.BehaviourHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BehaviourHistoryRepository extends JpaRepository<BehaviourHistory, Long> {

    // 마지막으로 적용된 version. 기록이 없으면 null
    @Query("SELECT MAX(bh.version) FROM BehaviourHistory bh WHERE bh.datePlanId = :datePlanId")
    Long findLatestVersion(@Param("datePlanId") Long datePlanId);

    // localVersion 이후에 적용된 기록을 version 순으로 조회한다 (rebase 대상)
    @Query("SELECT bh FROM BehaviourHistory bh " +
           "WHERE bh.datePlanId = :datePlanId AND bh.version > :version " +
           "ORDER BY bh.version ASC")
    List<BehaviourHistory> findAllAfterVersion(@Param("datePlanId") Long datePlanId,
                                               @Param("version") long version);

    // date plan 삭제용. @SQLDelete가 없는 엔티티라 실제로 지워진다
    @Modifying
    @Query("delete from BehaviourHistory bh where bh.datePlanId = :datePlanId")
    void deleteAllByDatePlanId(@Param("datePlanId") Long datePlanId);

}
