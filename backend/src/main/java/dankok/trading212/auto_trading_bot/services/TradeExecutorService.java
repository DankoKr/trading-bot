package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.TradeResult;
import dankok.trading212.auto_trading_bot.enums.TradeAction;
import dankok.trading212.auto_trading_bot.repositories.CryptoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TradeExecutorService {

    private final CryptoRepository cryptoRepository;

    @Autowired
    public TradeExecutorService(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Transactional
    public TradeResult executeBuyTrade(int accountId, String symbol, double currentPrice, double amountToInvest) {
        try {
            double quantity = amountToInvest / currentPrice;

            cryptoRepository.updateAccountBalance(accountId, -amountToInvest);

            int count = cryptoRepository.getHoldingsCount(accountId, symbol);
            
            if (count > 0) {
                cryptoRepository.updateHoldingsQuantity(accountId, symbol, quantity);
            } else {
                cryptoRepository.insertHolding(accountId, symbol, quantity);
            }

            cryptoRepository.insertTrade(LocalDateTime.now(), accountId, TradeAction.BUY.name(), symbol, quantity, currentPrice, null);

            return new TradeResult(true, TradeAction.BUY.name(), quantity, currentPrice, amountToInvest, 
                String.format("Successfully bought %.6f %s for $%.2f", quantity, symbol, amountToInvest));
                
        } catch (Exception e) {
            return new TradeResult(false, TradeAction.BUY.name(), 0, currentPrice, 0, 
                "Failed to execute buy trade: " + e.getMessage());
        }
    }

    @Transactional
    public TradeResult executeSellTrade(int accountId, String symbol, double currentPrice) {
        try {
            Double quantityToSell = cryptoRepository.getHoldingQuantity(accountId, symbol);

            if (quantityToSell == null || quantityToSell <= 0) {
                return new TradeResult(false, TradeAction.SELL.name(), 0, currentPrice, 0, 
                    "Cannot sell: No holdings for " + symbol + " in account " + accountId);
            }

            double saleValue = quantityToSell * currentPrice;

            cryptoRepository.updateAccountBalance(accountId, saleValue);
            cryptoRepository.deleteHolding(accountId, symbol);
            cryptoRepository.insertTrade(LocalDateTime.now(), accountId, TradeAction.SELL.name(), symbol, quantityToSell, currentPrice, null);

            return new TradeResult(true, TradeAction.SELL.name(), quantityToSell, currentPrice, saleValue,
                String.format("Successfully sold %.6f %s for $%.2f", quantityToSell, symbol, saleValue));
                
        } catch (Exception e) {
            return new TradeResult(false, TradeAction.SELL.name(), 0, currentPrice, 0, 
                "Failed to execute sell trade: " + e.getMessage());
        }
    }
}