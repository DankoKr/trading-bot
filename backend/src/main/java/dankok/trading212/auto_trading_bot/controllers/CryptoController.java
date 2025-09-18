package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.dtos.BacktestResult;
import dankok.trading212.auto_trading_bot.dtos.BotStatusResponse;
import dankok.trading212.auto_trading_bot.dtos.CryptoPriceResponse;
import dankok.trading212.auto_trading_bot.dtos.HistoricalPriceResponse;
import dankok.trading212.auto_trading_bot.dtos.TradingAnalysisResult;
import dankok.trading212.auto_trading_bot.enums.TradingMode;
import dankok.trading212.auto_trading_bot.services.BacktestService;
import dankok.trading212.auto_trading_bot.services.BotStateService;
import dankok.trading212.auto_trading_bot.services.CryptoDataService;
import dankok.trading212.auto_trading_bot.services.TradingBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class CryptoController {

    private final TradingBotService tradingBotService;
    private final CryptoDataService cryptoDataService;
    private final BacktestService backtestService;
    private final BotStateService botStateService;

    @Autowired
    public CryptoController(TradingBotService tradingBotService, CryptoDataService cryptoDataService,
                           BacktestService backtestService, BotStateService botStateService) {
        this.tradingBotService = tradingBotService;
        this.cryptoDataService = cryptoDataService;
        this.backtestService = backtestService;
        this.botStateService = botStateService;
    }

    @GetMapping("/")
    public String home() {
        return "Auto Trading Bot is running!";
    }

    @GetMapping("/run-bot")
    public TradingAnalysisResult runBot(
            @RequestParam(defaultValue = "bitcoin,ethereum,solana") String coins,
            @RequestParam(defaultValue = "TRADING") String mode) {
        List<String> coinIds = Arrays.asList(coins.split(","));
        TradingMode tradingMode = TradingMode.valueOf(mode.toUpperCase());
        return tradingBotService.executeTradingLogic(coinIds, tradingMode);
    }

    @PostMapping("/bot/hold")
    public BotStatusResponse holdBot(@RequestParam(required = false) String reason) {
        botStateService.holdBot(reason);
        return new BotStatusResponse(
            botStateService.getCurrentStatus(),
            botStateService.getLastStatusChange(),
            botStateService.getStatusChangeReason(),
            "Bot has been put on hold. Trading suspended.",
            true
        );
    }

    @PostMapping("/bot/activate")
    public BotStatusResponse activateBot(@RequestParam(required = false) String reason) {
        botStateService.activateBot(reason);
        return new BotStatusResponse(
            botStateService.getCurrentStatus(),
            botStateService.getLastStatusChange(),
            botStateService.getStatusChangeReason(),
            "Bot has been activated. Trading resumed.",
            true
        );
    }

    @PostMapping("/bot/stop")
    public BotStatusResponse stopBot(@RequestParam(required = false) String reason) {
        botStateService.stopBot(reason);
        return new BotStatusResponse(
            botStateService.getCurrentStatus(),
            botStateService.getLastStatusChange(),
            botStateService.getStatusChangeReason(),
            "Bot has been stopped completely.",
            true
        );
    }

    @GetMapping("/bot/status")
    public BotStatusResponse getBotStatus() {
        return new BotStatusResponse(
            botStateService.getCurrentStatus(),
            botStateService.getLastStatusChange(),
            botStateService.getStatusChangeReason(),
            botStateService.getStatusSummary(),
            true
        );
    }

    @GetMapping("/backtest/{coinId}")
    public BacktestResult runBacktest(
            @PathVariable String coinId,
            @RequestParam(defaultValue = "365") int days,
            @RequestParam(defaultValue = "1000.0") double initialBalance) {
        return backtestService.runBacktest(coinId, days, initialBalance);
    }

    @GetMapping("/prices")
    public CryptoPriceResponse getCurrentPrices(@RequestParam(defaultValue = "bitcoin,ethereum,solana") String coins) {
        String[] coinIds = coins.split(",");
        return cryptoDataService.fetchPrices(coinIds);
    }

    @GetMapping("/prices/{coinId}")
    public Double getCurrentPrice(@PathVariable String coinId) {
        return cryptoDataService.getCurrentPrice(coinId);
    }

    @GetMapping("/historical/{coinId}")
    public HistoricalPriceResponse getHistoricalPrices(
            @PathVariable String coinId,
            @RequestParam(defaultValue = "30") int days) {
        return cryptoDataService.fetchHistoricalPricesWithMetadata(coinId, days);
    }
}