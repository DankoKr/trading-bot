package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.repositories.CryptoRepository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final CryptoRepository cryptoRepository;

    @Autowired
    public AccountService(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
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
}