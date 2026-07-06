package NoDam.Demo.adapter.ai;

import java.util.List;

public interface AiPort {

    List<String> translate(List<String> texts, String sourceLang, String targetLang);

    <T> T call(Prompt prompt, Class<T> responseType, Object... args);

}
