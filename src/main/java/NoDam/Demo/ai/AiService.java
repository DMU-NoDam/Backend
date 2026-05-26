package NoDam.Demo.ai;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import NoDam.Demo.ai.translate.TranslateRequestDto;
import NoDam.Demo.ai.translate.TranslateResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AiService {

    private final ObjectMapper objectMapper;
    private final String provider;
    private final WebClient cliClient;
    private final WebClient geminiClient;
    private final String geminiModel;
    private final String geminiApiKey;

    @Value("${external.translate.base-url}")
    private String translateBaseUrl;

    @Value("${external.translate.mock}")
    private boolean translateMock;

    private final Logger logger = LoggerFactory.getLogger(AiService.class);

    // CLI
    private record CliRequest(String prompt) {}
    private record CliResponse(String response) {}

    // Gemini
    private record GeminiRequest(List<Content> contents) {
        record Content(List<Part> parts) {}
        record Part(String text) {}
    }
    private record GeminiResponse(List<Candidate> candidates) {
        record Candidate(Content content) {}
        record Content(List<Part> parts) {}
        record Part(String text) {}
    }

    public AiService(
            ObjectMapper objectMapper,
            @Value("${external.ai.provider}") String provider,
            @Value("${external.ai.cli.base-url}") String cliBaseUrl,
            @Value("${external.ai.gemini.base-url}") String geminiBaseUrl,
            @Value("${external.ai.gemini.model}") String geminiModel,
            @Value("${external.ai.gemini.api-key}") String geminiApiKey,
            @Value("${external.ai.connect-timeout-seconds}") int connectTimeoutSeconds,
            @Value("${external.ai.read-timeout-seconds}") int readTimeoutSeconds,
            @Value("${external.ai.response-timeout-seconds}") int responseTimeoutSeconds,
            @Value("${external.ai.max-connections}") int maxConnections
    ) {
        this.objectMapper = objectMapper;
        this.provider = provider;
        this.geminiModel = geminiModel;
        this.geminiApiKey = geminiApiKey;

        ConnectionProvider connectionProvider = ConnectionProvider.builder("ai-pool")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ZERO)
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutSeconds * 1000)
                .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutSeconds, TimeUnit.SECONDS)));

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        this.cliClient = WebClient.builder()
                .baseUrl(cliBaseUrl)
                .clientConnector(connector)
                .build();

        this.geminiClient = WebClient.builder()
                .baseUrl(geminiBaseUrl)
                .clientConnector(connector)
                .build();
    }

    public List<String> translate(List<String> texts, String sourceLang, String targetLang) {
        if (texts == null || texts.isEmpty())
            throw new RuntimeException("translate texts is null or empty");
        if (translateMock)
            return texts;

        String body;
        try {
            body = WebClient.create()
                    .post()
                    .uri(translateBaseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new TranslateRequestDto(texts, sourceLang, targetLang))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        logger.error("Translate API Error: Status={}, Body={}", clientResponse.statusCode(), errorBody);
                                        return Mono.error(new CustomException(ErrorCode.API_FAIL));
                                    })
                    )
                    .bodyToMono(String.class)
                    .block();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Translate API connection failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.API_FAIL);
        }

        try {
            TranslateResponseDto response = objectMapper.readValue(body, TranslateResponseDto.class);
            return response.getTranslatedText();
        } catch (Exception e) {
            logger.error("Translate API response parse failed: body={}", body);
            throw new CustomException(ErrorCode.API_FAIL);
        }
    }

    public <T> T call(Prompt prompt, Class<T> responseType, Object... args) {
        String[] serializedArgs = Arrays.stream(args)
                .map(this::serialize)
                .toArray(String[]::new);

        String responseFormat = generateResponseFormat(responseType);

        String[] allArgs = Arrays.copyOf(serializedArgs, serializedArgs.length + 1);
        allArgs[serializedArgs.length] = responseFormat;

        String fullPrompt = prompt.toPrompt(allArgs);
        String rawResponse = callLlm(fullPrompt);
        return deserialize(rawResponse, responseType);
    }

    private String callLlm(String prompt) {
        logger.info("AiService.callLlm :: provider={} prompt={}", provider, prompt);
        return "gemini".equals(provider) ? callGemini(prompt) : callCli(prompt);
    }

    private String callCli(String prompt) {
        CliResponse response = post(cliClient, "/generate", new CliRequest(prompt), CliResponse.class);
        if (response == null) return null;
        logger.info("AiService.callCli :: response={}", response.response());
        return response.response();
    }

    private String callGemini(String prompt) {
        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt)))));
        String uri = String.format("/%s:generateContent?key=%s", geminiModel, geminiApiKey);

        GeminiResponse response = post(geminiClient, uri, request, GeminiResponse.class);
        if (response == null) return null;

        String text = extractGeminiText(response);
        logger.info("AiService.callGemini :: response={}", text);
        return text;
    }

    private String extractGeminiText(GeminiResponse response) {
        if (response.candidates() == null || response.candidates().isEmpty()) return null;
        GeminiResponse.Candidate candidate = response.candidates().get(0);
        if (candidate.content() == null || candidate.content().parts() == null || candidate.content().parts().isEmpty()) return null;
        return candidate.content().parts().get(0).text();
    }

    private <T> T post(WebClient client, String uri, Object body, Class<T> responseType) {
        try {
            String raw = client.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("[Empty Body]")
                                    .flatMap(errorBody -> {
                                        logger.error("AiService.post :: Error Status={}, Body={}",
                                                response.statusCode(), errorBody);
                                        // 기존 retry 구조를 유지하기 위해 WebClientResponseException을 생성해 던집니다.
                                        return Mono.error(WebClientResponseException.create(
                                                response.statusCode().value(),
                                                response.statusCode().toString(),
                                                response.headers().asHttpHeaders(),
                                                errorBody.getBytes(),
                                                null
                                        ));
                                    })
                    )
                    .bodyToMono(String.class)
                    // 5xx 서버 에러일 때만 2번 재시도 (5초 대기 후 재시도)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(5))
                            .filter(e -> e instanceof WebClientResponseException &&
                                    ((WebClientResponseException) e).getStatusCode().is5xxServerError()))
                    .block();
            logger.info("AiService.post :: raw={}", raw);
            if (raw == null) return null;
            return objectMapper.readValue(raw, responseType);
        } catch (WebClientResponseException e) {
            logger.error("AiService.post :: HTTP {} {} - body={}",
                    e.getStatusCode().value(), e.getStatusText(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.API_FAIL);
        } catch (Exception e) {
            logger.error("AiService.post :: failed - {}", e.getMessage());
            throw new CustomException(ErrorCode.API_FAIL);
        }
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AiService :: failed to serialize request body", e);
        }
    }

    private <T> T deserialize(String response, Class<T> responseType) {
        if (response == null) return null;
        try {
            return objectMapper.readValue(extractJson(response), responseType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AiService :: failed to deserialize response", e);
        }
    }

    // LLM이 ```json ... ``` 마크다운 코드블록으로 감쌀 경우 내부 JSON만 추출
    private String extractJson(String response) {
        String trimmed = response.strip();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('\n');
            int end = trimmed.lastIndexOf("```");
            if (start != -1 && end > start) {
                return trimmed.substring(start + 1, end).strip();
            }
        }
        return trimmed;
    }

    private String generateResponseFormat(Class<?> responseType) {
        try {
            Object instance = responseType.getDeclaredConstructor().newInstance();
            if (instance instanceof AiExample ex) return ex.toJsonStr();
            logger.warn("AiService :: {} does not implement AiExample", responseType.getSimpleName());
            return "{}";
        } catch (Exception e) {
            logger.warn("AiService :: failed to generate response format for {}", responseType.getSimpleName());
            return "{}";
        }
    }

}
