package dankok.trading212.auto_trading_bot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class CryptoDataService {

    private final RestClient coinGeckoRestClient;

    @Autowired
    public CryptoDataService(RestClient coinGeckoRestClient) {
        this.coinGeckoRestClient = coinGeckoRestClient;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchAllPrices() {
        String relativeUrl = "/simple/price?ids=bitcoin,ethereum,solana,dogecoin&vs_currencies=usd";
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

    public double fetchBitcoinPrice() {
        String relativeUrl = "/simple/price?ids=bitcoin&vs_currencies=usd";

        try {
            Map<String, Map<String, Double>> response = (Map<String, Map<String, Double>>) coinGeckoRestClient.get()
                    .uri(relativeUrl)
                    .retrieve()
                    .body(Map.class);

            return response.get("bitcoin").get("usd");
        } catch (Exception e) {
            System.err.println("Error fetching crypto price: " + e.getMessage());
            return 0.0;
        }
    }
}