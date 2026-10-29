package NoDam.Demo;

import NoDam.Demo.exchange.config.FreeCurrencyProperties;
import NoDam.Demo.flight.config.AirLabsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
//@EnableAsync
@EnableConfigurationProperties({AirLabsProperties.class, FreeCurrencyProperties.class})
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}