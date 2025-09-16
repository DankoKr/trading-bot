package dankok.trading212.auto_trading_bot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TradeExecutorService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TradeExecutorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void executeBuyTrade(int accountId, String symbol, double currentPrice, double amountToInvest) {
        double quantity = amountToInvest / currentPrice;

        jdbcTemplate.update("UPDATE accounts SET balance = balance - ? WHERE id = ?", amountToInvest, accountId);

        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM holdings WHERE account_id = ? AND symbol = ?", Integer.class, accountId, symbol);
        
        if (count > 0) {
            jdbcTemplate.update("UPDATE holdings SET quantity = quantity + ? WHERE account_id = ? AND symbol = ?",
                    quantity, accountId, symbol);
        } else {
            jdbcTemplate.update("INSERT INTO holdings (account_id, symbol, quantity) VALUES (?, ?, ?)",
                    accountId, symbol, quantity);
        }

        jdbcTemplate.update("INSERT INTO trades (timestamp, account_id, action, symbol, quantity, price, profit_loss) VALUES (?, ?, 'BUY', ?, ?, ?, ?)",
                LocalDateTime.now(), accountId, symbol, quantity, currentPrice, null);
    }

    @Transactional
    public void executeSellTrade(int accountId, String symbol, double currentPrice) {
        Double quantityToSell;
        try {
            quantityToSell = jdbcTemplate.queryForObject(
                    "SELECT quantity FROM holdings WHERE account_id = ? AND symbol = ?",
                    Double.class, accountId, symbol);
        } catch (EmptyResultDataAccessException e) {
            System.err.println("Cannot sell: No holdings for " + symbol + " in account " + accountId);
            return;
        }

        if (quantityToSell == null || quantityToSell <= 0) {
            System.err.println("Cannot sell: No holdings for " + symbol + " in account " + accountId);
            return;
        }

        double saleValue = quantityToSell * currentPrice;

        jdbcTemplate.update("UPDATE accounts SET balance = balance + ? WHERE id = ?", saleValue, accountId);

        jdbcTemplate.update("DELETE FROM holdings WHERE account_id = ? AND symbol = ?", accountId, symbol);

        jdbcTemplate.update("INSERT INTO trades (timestamp, account_id, action, symbol, quantity, price, profit_loss) VALUES (?, ?, 'SELL', ?, ?, ?, ?)",
                LocalDateTime.now(), accountId, symbol, quantityToSell, currentPrice, null);
    }
}