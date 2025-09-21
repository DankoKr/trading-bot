package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.TradeResult;
import dankok.trading212.auto_trading_bot.enums.TradeActionEnum;
import dankok.trading212.auto_trading_bot.repositories.HoldingRepository;
import dankok.trading212.auto_trading_bot.repositories.TradeRepository;
import dankok.trading212.auto_trading_bot.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
@Service
public class TradeExecutorService {

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;

    @Autowired
    public TradeExecutorService(UserRepository userRepository, HoldingRepository holdingRepository, TradeRepository tradeRepository) {
        this.userRepository = userRepository;
        this.holdingRepository = holdingRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public TradeResult executeBuyTrade(int userId, String symbol, double currentPrice, double amountToInvest) {
        try {
            Double currentBalance = userRepository.getUserBalance(userId);
            if (currentBalance == null || currentBalance < amountToInvest) {
                return new TradeResult(false, TradeActionEnum.BUY.name(), 0, currentPrice, 0, 
                    "Insufficient balance for purchase");
            }

            double quantity = amountToInvest / currentPrice;

            userRepository.updateUserBalance(userId, -amountToInvest);

            holdingRepository.updateHolding(userId, symbol, quantity);

            tradeRepository.insertTrade(userId, TradeActionEnum.BUY.name(), symbol, quantity, currentPrice, LocalDateTime.now());

            return new TradeResult(true, TradeActionEnum.BUY.name(), quantity, currentPrice, amountToInvest, 
                String.format("Successfully bought %.6f %s for $%.2f", quantity, symbol, amountToInvest));
                
        } catch (Exception e) {
            return new TradeResult(false, TradeActionEnum.BUY.name(), 0, currentPrice, 0, 
                "Failed to execute buy trade: " + e.getMessage());
        }
    }

    @Transactional
    public TradeResult executeSellTrade(int userId, String symbol, double currentPrice) {
        try {
            Double quantityToSell = holdingRepository.getHoldingQuantity(userId, symbol);

            if (quantityToSell == null || quantityToSell <= 0) {
                return new TradeResult(false, TradeActionEnum.SELL.name(), 0, currentPrice, 0, 
                    "Cannot sell: No holdings for " + symbol + " in user " + userId);
            }

            double avgBuyPrice = tradeRepository.getAverageBuyPrice(userId, symbol);
            double saleValue = quantityToSell * currentPrice;
            double profitLoss = (currentPrice - avgBuyPrice) * quantityToSell;

            userRepository.updateUserBalance(userId, saleValue);
            holdingRepository.updateHolding(userId, symbol, -quantityToSell);

            tradeRepository.insertTradeWithProfitLoss(
                userId,
                TradeActionEnum.SELL.name(),
                symbol,
                quantityToSell,
                currentPrice,
                LocalDateTime.now(),
                profitLoss
            );

            return new TradeResult(true, TradeActionEnum.SELL.name(), quantityToSell, currentPrice, saleValue,
                String.format("Successfully sold %.6f %s for $%.2f (P/L: $%.2f)", quantityToSell, symbol, saleValue, profitLoss));
                
        } catch (Exception e) {
            return new TradeResult(false, TradeActionEnum.SELL.name(), 0, currentPrice, 0, 
                "Failed to execute sell trade: " + e.getMessage());
        }
    }
}