package dankok.trading212.auto_trading_bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${coingecko.api.key:}")
    private String apiKey;

    @Value("${coingecko.api.base-url:https://api.coingecko.com/api/v3}")
    private String baseUrl;

    @Value("${coingecko.api.demo-url:https://api.coingecko.com/api/v3}")
    private String demoUrl;

    @Value("${coingecko.api.pro-url:https://pro-api.coingecko.com/api/v3}")
    private String proUrl;

    @Bean
    public RestClient coinGeckoRestClient() {
        String effectiveBaseUrl;
        String headerName = null;
        
        if (apiKey == null || apiKey.isEmpty()) {
            effectiveBaseUrl = baseUrl;
        } else if (apiKey.startsWith("CG-") && apiKey.length() > 20) {
            effectiveBaseUrl = demoUrl;
            headerName = "x-cg-demo-api-key";
        } else {
            effectiveBaseUrl = proUrl;
            headerName = "x-cg-pro-api-key";
        }
        
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(effectiveBaseUrl);

        if (apiKey != null && !apiKey.isEmpty() && headerName != null) {
            builder.defaultHeader(headerName, apiKey);
        }

        builder.defaultHeader("Accept", "application/json")
               .defaultHeader("User-Agent", "Auto-Trading-Bot/1.0");

        return builder.build();
    }
}