package NoDam.Demo.ai.translate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@AllArgsConstructor
public class TranslateRequestDto {
    private List<String> q;
    private String source;
    private String target;
}
