package NoDam.Demo.memo.dto.request;

import NoDam.Demo.memo.domain.MemoIconType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemoUpdateRequestDto {

    private MemoIconType icon;

    @Size(max = 50)
    private String title;

    @Size(max = 2000)
    private String content;

}
