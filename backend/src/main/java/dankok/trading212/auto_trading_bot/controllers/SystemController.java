package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.dtos.CryptoPriceResponse;
import dankok.trading212.auto_trading_bot.services.CryptoDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SystemController {

    private final CryptoDataService cryptoDataService;

    @Autowired
    public SystemController(CryptoDataService cryptoDataService) {
        this.cryptoDataService = cryptoDataService;
    }

    @GetMapping("/")
    public String home() {
        return "Auto Trading Bot is running!";
    }

    @GetMapping("/api/status")
    public Map<String, Object> getApiStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("usingApiKey", cryptoDataService.isUsingApiKey());
        status.put("apiKeyType", cryptoDataService.getApiKeyType());
        status.put("timestamp", LocalDateTime.now());
        
        try {
            CryptoPriceResponse testResponse = cryptoDataService.fetchPrices("bitcoin");
            status.put("apiConnected", testResponse.isSuccess());
            status.put("lastTestMessage", testResponse.isSuccess() ? "API working normally" : testResponse.getErrorMessage());
        } catch (Exception e) {
            status.put("apiConnected", false);
            status.put("lastTestMessage", "API connection failed: " + e.getMessage());
        }
        
        return status;
    }
}
