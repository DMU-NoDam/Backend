package NoDam.Demo.common.event;

import NoDam.Demo.common.domain.BaseEntity;

public class CreateEvent <T extends BaseEntity> extends BaseEvent<T> {

    public CreateEvent(Long userId, Long id) {
        super(userId, id);
    }

}
