package NoDam.Demo.common.event;

import NoDam.Demo.common.domain.BaseEntity;

public class DeleteEvent <T extends BaseEntity> extends BaseEvent<T> {

    public DeleteEvent(Long userId, Long id) {
        super(userId, id);
    }

}

