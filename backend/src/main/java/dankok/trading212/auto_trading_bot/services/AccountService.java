package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.CryptoPriceResponse;
import dankok.trading212.auto_trading_bot.repositories.CryptoRepository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final CryptoRepository cryptoRepository;
    private final CryptoDataService cryptoDataService;

    @Autowired
    public AccountService(CryptoRepository cryptoRepository, CryptoDataService cryptoDataService) {
        this.cryptoRepository = cryptoRepository;
        this.cryptoDataService = cryptoDataService;
    }

    @Transactional
    public boolean resetAccount(int accountId, double initialBalance) {
        try {
            cryptoRepository.resetAccount(accountId, initialBalance);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Double getAccountBalance(int accountId) {
        return cryptoRepository.getAccountBalance(accountId);
    }

    public List<Map<String, Object>> getTradeHistory(int accountId) {
        return cryptoRepository.getTradeHistory(accountId);
    }

    public List<Map<String, Object>> getAccountHoldings(int accountId) {
        return cryptoRepository.getAccountHoldings(accountId);
    }

    public List<Map<String, Object>> getDetailedHoldings(int accountId) {
        List<Map<String, Object>> holdings = cryptoRepository.getAccountHoldings(accountId);

        for (Map<String, Object> holding : holdings) {
            String symbol = (String) holding.get("symbol");
            Double quantity = ((Number) holding.get("quantity")).doubleValue();

            try {
                CryptoPriceResponse priceResponse = cryptoDataService.fetchPrices(symbol);
                if (priceResponse.isSuccess() && priceResponse.getPrices().containsKey(symbol)) {
                    Double currentPrice = priceResponse.getPrices().get(symbol);
                    Double currentValue = quantity * currentPrice;

                    holding.put("current_price", currentPrice);
                    holding.put("current_value", currentValue);
                } else {
                    holding.put("current_price", 0.0);
                    holding.put("current_value", 0.0);
                }
            } catch (Exception e) {
                holding.put("current_price", 0.0);
                holding.put("current_value", 0.0);
            }
        }

        return holdings;
    }
}