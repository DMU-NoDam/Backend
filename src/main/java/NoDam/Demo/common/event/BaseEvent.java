package NoDam.Demo.common.event;

import NoDam.Demo.common.domain.BaseEntity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEvent <T extends BaseEntity>  {

    // 누가
    private Long userId;

    // 무엇을
    private Long id;

    // 언제
    private LocalDateTime time;

    public BaseEvent(Long userId, Long id) {
        this.userId = userId;
        this.id = id;
        this.time = LocalDateTime.now();
    }

}
