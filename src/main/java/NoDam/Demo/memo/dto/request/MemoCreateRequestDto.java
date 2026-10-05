package NoDam.Demo.memo.dto.request;

import NoDam.Demo.memo.domain.MemoIconType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemoCreateRequestDto {

    private MemoIconType icon; // null이면 기본값(ETC) 사용

    @NotBlank
    @Size(max = 50)
    private String title;

    @Size(max = 2000)
    private String content;

}
