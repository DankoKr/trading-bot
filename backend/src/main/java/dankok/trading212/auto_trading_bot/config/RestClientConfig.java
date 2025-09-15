package dankok.trading212.auto_trading_bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient coinGeckoRestClient() {
        return RestClient.builder()
            .baseUrl("https://api.coingecko.com/api/v3")
            .build();
    }
}