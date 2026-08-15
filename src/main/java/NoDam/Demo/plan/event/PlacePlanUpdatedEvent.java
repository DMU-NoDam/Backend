package NoDam.Demo.plan.event;

import NoDam.Demo.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlacePlanUpdatedEvent extends BaseEvent {

    private Long datePlanId;

}
