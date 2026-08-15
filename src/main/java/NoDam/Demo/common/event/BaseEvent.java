package NoDam.Demo.common.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseEvent {

    private LocalDateTime subscribeAt;

    private Long subscriberUserId;

}
