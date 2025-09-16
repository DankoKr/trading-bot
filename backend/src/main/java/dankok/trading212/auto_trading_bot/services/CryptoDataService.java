package dankok.trading212.auto_trading_bot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CryptoDataService {

    private final RestClient coinGeckoRestClient;

    @Autowired
    public CryptoDataService(RestClient coinGeckoRestClient) {
        this.coinGeckoRestClient = coinGeckoRestClient;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchPrices(String... coinIds) {
        String ids = String.join(",", coinIds);
        String relativeUrl = String.format("/simple/price?ids=%s&vs_currencies=usd", ids);
        try {
            return (Map<String, Object>) coinGeckoRestClient.get()
                    .uri(relativeUrl)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            System.err.println("Error fetching crypto prices: " + e.getMessage());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Double> fetchHistoricalPrices(String coinId, int days) {
        String relativeUrl = String.format("/coins/%s/market_chart?vs_currency=usd&days=%d", coinId, days);
        try {
            Map<String, Object> response = coinGeckoRestClient.get()
                    .uri(relativeUrl)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("prices")) {
                List<List<Number>> pricesData = (List<List<Number>>) response.get("prices");
                return pricesData.stream()
                        .map(dataPoint -> dataPoint.get(1).doubleValue())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error fetching historical crypto prices for " + coinId + ": " + e.getMessage());
        }
        return List.of();
    }
}