package NoDam.Demo.plan.event;

import NoDam.Demo.plan.thread.TransportAsyncSingleThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlacePlanEventListener {

    private final TransportAsyncSingleThread transportAsyncSingleThread;

    @EventListener
    public void handleTransportPlanGenerate(PlacePlanUpdatedEvent event) {
        transportAsyncSingleThread.start(event.getDatePlanId());
    }

}
