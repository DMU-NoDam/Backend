package NoDam.Demo.ai.translate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class TranslateResponseDto {
    private List<String> translatedText;
}
