package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.CoinAnalysis;
import dankok.trading212.auto_trading_bot.dtos.TradeResult;
import dankok.trading212.auto_trading_bot.dtos.TradingAnalysisResult;
import dankok.trading212.auto_trading_bot.enums.TradingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradingBotService {

    private final CryptoDataService cryptoDataService;
    private final TradeExecutorService tradeExecutorService;
    private final BotStateService botStateService;
    private static final int SHORT_TERM_PERIOD = 10;
    private static final int LONG_TERM_PERIOD = 50;

    @Autowired
    public TradingBotService(CryptoDataService cryptoDataService, TradeExecutorService tradeExecutorService,
                           BotStateService botStateService) {
        this.cryptoDataService = cryptoDataService;
        this.tradeExecutorService = tradeExecutorService;
        this.botStateService = botStateService;
    }

    public TradingAnalysisResult executeTradingLogic(List<String> coinIds, TradingMode mode) {
        if (!botStateService.isActive()) {
            String statusMessage = String.format("Bot is currently %s. Trading suspended.", 
                botStateService.getCurrentStatus());
            return new TradingAnalysisResult(List.of(), statusMessage, false);
        }

        List<CoinAnalysis> analyses = new ArrayList<>();
        int successfulTrades = 0;
        int totalSignals = 0;

        for (String coinId : coinIds) {
            CoinAnalysis analysis = analyzeCoin(coinId, mode);
            analyses.add(analysis);

            if (analysis.getTradeResult() != null) {
                totalSignals++;
                if (analysis.getTradeResult().isSuccess()) {
                    successfulTrades++;
                }
            }
        }

        String modeStr = mode == TradingMode.TRAINING ? "TRAINING" : "LIVE";
        String summary = String.format("[%s MODE] Analyzed %d coins, generated %d trading signals, %d successful trades", 
            modeStr, coinIds.size(), totalSignals, successfulTrades);

        return new TradingAnalysisResult(analyses, summary, true);
    }

    private CoinAnalysis analyzeCoin(String coinId, TradingMode mode) {
        List<Double> historicalPrices = cryptoDataService.fetchHistoricalPrices(coinId, LONG_TERM_PERIOD);

        if (historicalPrices.size() < LONG_TERM_PERIOD) {
            return new CoinAnalysis(coinId, 0, 0, 0, "NONE",
                    "Not enough historical data (need " + LONG_TERM_PERIOD + " days, got " + historicalPrices.size() + ")");
        }

        double shortTermSMA = calculateSMA(historicalPrices, SHORT_TERM_PERIOD);
        double longTermSMA = calculateSMA(historicalPrices, LONG_TERM_PERIOD);
        double currentPrice = historicalPrices.get(historicalPrices.size() - 1);

        String signal;
        String status = "Analysis completed - " + mode.name() + " mode";
        CoinAnalysis analysis;

        if (shortTermSMA > longTermSMA) {
            signal = "BUY";
            analysis = new CoinAnalysis(coinId, currentPrice, shortTermSMA, longTermSMA, signal, status);
            
            if (mode == TradingMode.TRADING && botStateService.isActive()) {
                TradeResult tradeResult = tradeExecutorService.executeBuyTrade(1, coinId, currentPrice, 10.00);
                analysis.setTradeResult(tradeResult);
            } else if (mode == TradingMode.TRAINING) {
                TradeResult simulatedResult = new TradeResult(true, "BUY", 10.00/currentPrice, 
                    currentPrice, 10.00, "Simulated buy trade (training mode)");
                analysis.setTradeResult(simulatedResult);
            } else {
                TradeResult heldResult = new TradeResult(false, "BUY", 0, currentPrice, 0, 
                    "Trade signal generated but bot is on hold");
                analysis.setTradeResult(heldResult);
            }
        } else if (shortTermSMA < longTermSMA) {
            signal = "SELL";
            analysis = new CoinAnalysis(coinId, currentPrice, shortTermSMA, longTermSMA, signal, status);
            
            if (mode == TradingMode.TRADING && botStateService.isActive()) {
                TradeResult tradeResult = tradeExecutorService.executeSellTrade(1, coinId, currentPrice);
                analysis.setTradeResult(tradeResult);
            } else if (mode == TradingMode.TRAINING) {
                TradeResult simulatedResult = new TradeResult(true, "SELL", 1.0, 
                    currentPrice, currentPrice, "Simulated sell trade (training mode)");
                analysis.setTradeResult(simulatedResult);
            } else {
                TradeResult heldResult = new TradeResult(false, "SELL", 0, currentPrice, 0, 
                    "Trade signal generated but bot is on hold");
                analysis.setTradeResult(heldResult);
            }
        } else {
            signal = "HOLD";
            analysis = new CoinAnalysis(coinId, currentPrice, shortTermSMA, longTermSMA, signal, 
                "No trading signal generated - " + mode.name() + " mode");
        }

        return analysis;
    }

    private double calculateSMA(List<Double> prices, int period) {
        int startIndex = prices.size() - period;
        if (startIndex < 0) {
            return 0.0;
        }
        return prices.subList(startIndex, prices.size())
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}