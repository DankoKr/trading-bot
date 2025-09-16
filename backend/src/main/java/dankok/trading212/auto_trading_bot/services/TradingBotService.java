package dankok.trading212.auto_trading_bot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TradingBotService {

    private final CryptoDataService cryptoDataService;
    private final TradeExecutorService tradeExecutorService;
    private static final int SHORT_TERM_PERIOD = 10;
    private static final int LONG_TERM_PERIOD = 50;

    @Autowired
    public TradingBotService(CryptoDataService cryptoDataService, TradeExecutorService tradeExecutorService) {
        this.cryptoDataService = cryptoDataService;
        this.tradeExecutorService = tradeExecutorService;
    }

    public void executeTradingLogic(List<String> coinIds) {
        for (String coinId : coinIds) {
            System.out.println("Analyzing " + coinId + "...");
            List<Double> historicalPrices = cryptoDataService.fetchHistoricalPrices(coinId, LONG_TERM_PERIOD);

            if (historicalPrices.size() < LONG_TERM_PERIOD) {
                System.err.println("Not enough historical data for " + coinId);
                continue;
            }

            double shortTermSMA = calculateSMA(historicalPrices, SHORT_TERM_PERIOD);
            double longTermSMA = calculateSMA(historicalPrices, LONG_TERM_PERIOD);
            double currentPrice = historicalPrices.get(historicalPrices.size() - 1);

            System.out.printf("   - Current Price: $%.2f%n", currentPrice);
            System.out.printf("   - Short-term SMA: %.2f%n", shortTermSMA);
            System.out.printf("   - Long-term SMA: %.2f%n", longTermSMA);

            if (shortTermSMA > longTermSMA) {
                System.out.println("   - BUY signal detected!");
                tradeExecutorService.executeBuyTrade(1, coinId, currentPrice, 10.00);
            } else if (shortTermSMA < longTermSMA) {
                System.out.println("   - SELL signal detected!");
                tradeExecutorService.executeSellTrade(1, coinId, currentPrice);
            }
        }
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