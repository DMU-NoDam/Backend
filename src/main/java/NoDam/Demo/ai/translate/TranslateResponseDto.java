package NoDam.Demo.ai.translate;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TranslateResponseDto {
    private List<Value> value;

    @Getter
    @NoArgsConstructor
    public static class Value {
        private List<Translation> translations;
    }

    @Getter
    @NoArgsConstructor
    public static class Translation {
        private String language;
        private int sourceCharacters;
        private String text;
    }
}
