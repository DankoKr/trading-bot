package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.dtos.CryptoPriceResponse;
import dankok.trading212.auto_trading_bot.dtos.HistoricalPriceResponse;
import dankok.trading212.auto_trading_bot.services.CryptoDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/prices")
public class PriceController {

    private final CryptoDataService cryptoDataService;

    @Autowired
    public PriceController(CryptoDataService cryptoDataService) {
        this.cryptoDataService = cryptoDataService;
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    @GetMapping
    public CryptoPriceResponse getCurrentPrices(@RequestParam(defaultValue = "bitcoin") String coins, HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new CryptoPriceResponse(false, new HashMap<>(), "Authentication required");
        }
        
        String[] coinIds = coins.split(",");
        return cryptoDataService.fetchPrices(coinIds);
    }

    @GetMapping("/{coinId}")
    public Double getCurrentPrice(@PathVariable String coinId, HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return 0.0;
        }
        
        return cryptoDataService.getCurrentPrice(coinId);
    }

    @GetMapping("/historical/{coinId}")
    public HistoricalPriceResponse getHistoricalPrices(
            @PathVariable String coinId,
            @RequestParam(defaultValue = "30") int days,
            HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new HistoricalPriceResponse(false, coinId, new ArrayList<>(), days, 0, "Authentication required");
        }
        
        return cryptoDataService.fetchHistoricalPricesWithMetadata(coinId, days);
    }
}
