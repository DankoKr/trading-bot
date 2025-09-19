package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.CryptoPriceResponse;
import dankok.trading212.auto_trading_bot.dtos.HistoricalPriceResponse;
import dankok.trading212.auto_trading_bot.repositories.CryptoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CryptoDataService {

    private final RestClient coinGeckoRestClient;
    private final CryptoRepository cryptoRepository;
    
    @Value("${coingecko.api.key:}")
    private String apiKey;
    
    // Cache to avoid hitting rate limits
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    private static final long MIN_REQUEST_INTERVAL = 1000; // 1 second between requests for free tier

    @Autowired
    public CryptoDataService(RestClient coinGeckoRestClient, CryptoRepository cryptoRepository) {
        this.coinGeckoRestClient = coinGeckoRestClient;
        this.cryptoRepository = cryptoRepository;
    }

    public CryptoPriceResponse fetchPrices(String... coinIds) {
        String ids = String.join(",", coinIds);
        String cacheKey = "prices_" + ids;
        
        if (!canMakeRequest(cacheKey)) {
            return new CryptoPriceResponse(false, Map.of(), "Rate limit exceeded. Please wait before making another request.");
        }
        
        String relativeUrl = String.format("/simple/price?ids=%s&vs_currencies=usd", ids);
        
        try {
            updateLastRequestTime(cacheKey);
            
            Map<?, ?> response = coinGeckoRestClient.get()
                    .uri(relativeUrl)
                    .retrieve()
                    .body(Map.class);

            if (response != null && !response.isEmpty()) {
                Map<String, Double> prices = new HashMap<>();
                
                for (Map.Entry<?, ?> entry : response.entrySet()) {
                    if (entry.getKey() instanceof String coinId && entry.getValue() instanceof Map<?, ?> coinData) {
                        Object usdValue = coinData.get("usd");
                        if (usdValue instanceof Number) {
                            Double price = ((Number) usdValue).doubleValue();
                            prices.put(coinId, price);
                            
                            try {
                                cryptoRepository.savePrice(coinId, price);
                            } catch (Exception e) {
                                System.err.println("Failed to save price for " + coinId + ": " + e.getMessage());
                            }
                        }
                    }
                }
                
                return new CryptoPriceResponse(true, prices, null);
            } else {
                return new CryptoPriceResponse(false, Map.of(), "No price data received from API");
            }
            
        } catch (RestClientException e) {
            String errorMessage = "Error fetching crypto prices: " + e.getMessage();
            
            if (e.getMessage().contains("429") || e.getMessage().contains("rate limit")) {
                errorMessage = "Rate limit exceeded. Please upgrade your CoinGecko API plan or wait before making another request.";
            }
            
            return new CryptoPriceResponse(false, Map.of(), errorMessage);
        } catch (Exception e) {
            String errorMessage = "Unexpected error fetching crypto prices: " + e.getMessage();
            return new CryptoPriceResponse(false, Map.of(), errorMessage);
        }
    }

    public HistoricalPriceResponse fetchHistoricalPricesWithMetadata(String coinId, int days) {
        String cacheKey = "historical_" + coinId + "_" + days;
        
        if (!canMakeRequest(cacheKey)) {
            return new HistoricalPriceResponse(false, coinId, List.of(), days, 0, 
                "Rate limit exceeded. Please wait before making another request.");
        }
        
        String relativeUrl = String.format("/coins/%s/market_chart?vs_currency=usd&days=%d", coinId, days);
        
        try {
            updateLastRequestTime(cacheKey);
            
            Map<?, ?> response = coinGeckoRestClient.get()
                    .uri(relativeUrl)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("prices")) {
                Object pricesObj = response.get("prices");
                if (pricesObj instanceof List<?> pricesData) {
                    List<Double> prices = pricesData.stream()
                            .filter(item -> item instanceof List<?> && ((List<?>) item).size() >= 2)
                            .map(item -> (List<?>) item)
                            .filter(dataPoint -> dataPoint.get(1) instanceof Number)
                            .map(dataPoint -> ((Number) dataPoint.get(1)).doubleValue())
                            .collect(Collectors.toList());
                    
                    return new HistoricalPriceResponse(true, coinId, prices, days, prices.size(), null);
                }
            }
            
            return new HistoricalPriceResponse(false, coinId, List.of(), days, 0, 
                "No historical price data available for " + coinId);
            
        } catch (RestClientException e) {
            String errorMessage = "Error fetching historical crypto prices for " + coinId + ": " + e.getMessage();
            
            if (e.getMessage().contains("429") || e.getMessage().contains("rate limit")) {
                errorMessage = "Rate limit exceeded. Please upgrade your CoinGecko API plan or wait before making another request.";
            }
            
            return new HistoricalPriceResponse(false, coinId, List.of(), days, 0, errorMessage);
        } catch (Exception e) {
            String errorMessage = "Unexpected error fetching historical crypto prices for " + coinId + ": " + e.getMessage();
            return new HistoricalPriceResponse(false, coinId, List.of(), days, 0, errorMessage);
        }
    }

    public List<Double> fetchHistoricalPrices(String coinId, int days) {
        HistoricalPriceResponse response = fetchHistoricalPricesWithMetadata(coinId, days);
        return response.getPrices();
    }

    public Double getCurrentPrice(String coinId) {
        CryptoPriceResponse response = fetchPrices(coinId);
        if (response.isSuccess() && response.getPrices().containsKey(coinId)) {
            return response.getPrices().get(coinId);
        }
        return null;
    }

    public boolean hasSufficientData(String coinId, int requiredDays) {
        HistoricalPriceResponse response = fetchHistoricalPricesWithMetadata(coinId, requiredDays);
        return response.isSuccess() && response.getActualDays() >= requiredDays;
    }
    
    private boolean canMakeRequest(String cacheKey) {
        Long lastTime = lastRequestTime.get(cacheKey);
        if (lastTime == null) {
            return true;
        }
        
        long timeSinceLastRequest = System.currentTimeMillis() - lastTime;
        
        // If using API key (pro), allow more frequent requests
        long minInterval = (apiKey != null && !apiKey.isEmpty()) ? 100 : MIN_REQUEST_INTERVAL;
        
        return timeSinceLastRequest >= minInterval;
    }
    
    private void updateLastRequestTime(String cacheKey) {
        lastRequestTime.put(cacheKey, System.currentTimeMillis());
    }
    
    public boolean isUsingApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    public String getApiKeyType() {
        if (apiKey == null || apiKey.isEmpty()) {
            return "None (Free tier)";
        } else if (apiKey.startsWith("CG-") && apiKey.length() > 20) {
            return "Demo";
        } else {
            return "Pro";
        }
    }
}