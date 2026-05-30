package NoDam.Demo.ai.translate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TranslateRequestDto {
    private List<Input> inputs;

    @Getter
    @AllArgsConstructor
    public static class Input {
        private String text;
        private String language;
        private List<Target> targets;
    }

    @Getter
    @AllArgsConstructor
    public static class Target {
        private String language;
    }
}
