package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.services.TradingBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class HomeController {

    private final TradingBotService tradingBotService;

    @Autowired
    public HomeController(TradingBotService tradingBotService) {
        this.tradingBotService = tradingBotService;
    }

    @GetMapping("/")
    public String home() {
        return "Auto Trading Bot is running!";
    }

    @GetMapping("/run-bot")
    public String runBot(@RequestParam(defaultValue = "bitcoin,ethereum,solana") String coins) {
        List<String> coinIds = Arrays.asList(coins.split(","));
        tradingBotService.executeTradingLogic(coinIds);
        return "Trading logic executed for " + coinIds;
    }
}