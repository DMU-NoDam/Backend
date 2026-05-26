package NoDam.Demo.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockAiConfig {

    @Value("${external.ai.provider}")
    private String aiProvider;

    @Bean
    public boolean isMockAi() {
        return aiProvider.equals("mock");
    }

}
