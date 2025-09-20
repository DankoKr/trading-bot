package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.BacktestResult;
import dankok.trading212.auto_trading_bot.dtos.TradeResult;
import dankok.trading212.auto_trading_bot.enums.TradeActionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BacktestService {

    private final CryptoDataService cryptoDataService;

    @Autowired
    public BacktestService(CryptoDataService cryptoDataService) {
        this.cryptoDataService = cryptoDataService;
    }

    public BacktestResult runBacktest(String coinId, int days, double initialBalance) {
        try {
            List<Double> historicalPrices = cryptoDataService.fetchHistoricalPrices(coinId, days);
            
            if (historicalPrices.size() < 50) {
                return new BacktestResult(false, coinId, null, null, initialBalance, initialBalance, 
                    0, 0, 0, 0, List.of(), "Insufficient historical data for backtesting");
            }

            double balance = initialBalance;
            double holdings = 0;
            List<TradeResult> trades = new ArrayList<>();
            
            for (int i = 50; i < historicalPrices.size(); i++) {
                List<Double> priceWindow = historicalPrices.subList(0, i + 1);
                double shortSMA = calculateSMA(priceWindow, 10);
                double longSMA = calculateSMA(priceWindow, 50);
                double currentPrice = historicalPrices.get(i);
                
                if (shortSMA > longSMA && holdings == 0 && balance >= 10) {
                    double investAmount = Math.min(balance, 100);
                    holdings = investAmount / currentPrice;
                    balance -= investAmount;
                    
                    trades.add(new TradeResult(true, TradeActionEnum.BUY.name(), holdings, currentPrice, investAmount,
                        "Backtest buy executed"));
                }
                else if (shortSMA < longSMA && holdings > 0) {
                    double saleValue = holdings * currentPrice;
                    balance += saleValue;
                    
                    trades.add(new TradeResult(true, TradeActionEnum.SELL.name(), holdings, currentPrice, saleValue,
                        "Backtest sell executed"));
                    holdings = 0;
                }
            }

            double finalPrice = historicalPrices.get(historicalPrices.size() - 1);
            double finalValue = balance + (holdings * finalPrice);
            double totalReturn = finalValue - initialBalance;
            double returnPercentage = (totalReturn / initialBalance) * 100;

            String summary = String.format("Backtest completed: %.2f%% return, %d trades executed",
                returnPercentage, trades.size());

            return new BacktestResult(true, coinId, LocalDateTime.now().minusDays(days), 
                LocalDateTime.now(), initialBalance, finalValue, totalReturn, returnPercentage,
                trades.size(), trades.size(), trades, summary);

        } catch (Exception e) {
            return new BacktestResult(false, coinId, null, null, initialBalance, initialBalance,
                0, 0, 0, 0, List.of(), "Backtest failed: " + e.getMessage());
        }
    }

    private double calculateSMA(List<Double> prices, int period) {
        int startIndex = prices.size() - period;
        if (startIndex < 0) return 0.0;
        
        return prices.subList(startIndex, prices.size())
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}