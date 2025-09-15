package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.services.CryptoDataService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final CryptoDataService cryptoDataService;

    @Autowired
    public HomeController(CryptoDataService cryptoDataService) {
        this.cryptoDataService = cryptoDataService;
    }

    @GetMapping("/")
    public String home() {
        return "Auto Trading Bot is running!";
    }

    @GetMapping("/all-prices")
    public Map<String, Object> allPrices() {
        return cryptoDataService.fetchAllPrices();
    }
}