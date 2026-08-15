package NoDam.Demo.plan.behaviour.service;

import NoDam.Demo.plan.behaviour.domain.Behaviour;
import NoDam.Demo.plan.behaviour.domain.PreviousBehaviours;
import NoDam.Demo.plan.behaviour.port.BehaviourDBPort;
import NoDam.Demo.plan.domain.DatePlan;
import NoDam.Demo.plan.event.PlacePlanUpdatedEvent;
import NoDam.Demo.plan.repository.DatePlanDBPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.NonNull;

@RequiredArgsConstructor
@Service
public class BehaviourService {

    private final BehaviourDBPort behaviourDBPort;
    private final DatePlanDBPort datePlanDBPort;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;

    record BehaviourRebaseSource(
            DatePlan datePlan,
            PreviousBehaviours previousBehaviours
    ){}

    // while { select -> rebase -> validate -> apply -> insert }
    public void trySave(
            @NonNull Long datePlanId,
            @NonNull Long localVersion,
            @NonNull Behaviour behaviour
    ) {
        boolean isSuccess = false;

        do {
            // select
            BehaviourRebaseSource rebaseSource = transactionTemplate.execute(status -> {
                DatePlan d = datePlanDBPort.latestDatePlan(datePlanId).get();
                PreviousBehaviours p = behaviourDBPort.findPreviousBehaviours(datePlanId, localVersion);
                return new BehaviourRebaseSource(d, p);
            });

            DatePlan datePlan = rebaseSource.datePlan;
            PreviousBehaviours previousBehaviours = rebaseSource.previousBehaviours;

            long dbVersion = previousBehaviours.getLatestVersion();

            // rebase
            behaviour.rebase(datePlan, previousBehaviours);

            // validate
            behaviour.validate(datePlan);

            // apply
            behaviour.apply(datePlan);

            // insert
            isSuccess = transactionTemplate.execute(status -> {
                // insert
                try {
                    behaviourDBPort.saveBehaviourHistory(datePlanId, dbVersion, behaviour);
                    datePlanDBPort.saveDatePlan(datePlan);
                } catch (DataIntegrityViolationException e) {
                    status.setRollbackOnly(); // roll back
                    return false;
                }

                return true;
            });
        } while (!isSuccess);

        eventPublisher.publishEvent(new PlacePlanUpdatedEvent(datePlanId));
    }

}
