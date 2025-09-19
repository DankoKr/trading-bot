package dankok.trading212.auto_trading_bot.repositories;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class CryptoRepository {

    private final JdbcTemplate jdbcTemplate;

    public CryptoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void savePrice(String symbol, double price) {
    jdbcTemplate.update(
        "INSERT INTO crypto_prices (symbol, price) VALUES (?, ?)",
        symbol, price
    );
}

    public void updateAccountBalance(int accountId, double amount) {
        jdbcTemplate.update("UPDATE accounts SET balance = balance + ? WHERE id = ?", amount, accountId);
    }

    public Integer getHoldingsCount(int accountId, String symbol) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM holdings WHERE account_id = ? AND symbol = ?", 
            Integer.class, accountId, symbol
        );
    }

    public void updateHoldingsQuantity(int accountId, String symbol, double quantity) {
        jdbcTemplate.update(
            "UPDATE holdings SET quantity = quantity + ? WHERE account_id = ? AND symbol = ?",
            quantity, accountId, symbol
        );
    }

    public void insertHolding(int accountId, String symbol, double quantity) {
        jdbcTemplate.update(
            "INSERT INTO holdings (account_id, symbol, quantity) VALUES (?, ?, ?)",
            accountId, symbol, quantity
        );
    }

    public Double getHoldingQuantity(int accountId, String symbol) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT quantity FROM holdings WHERE account_id = ? AND symbol = ?",
                Double.class, accountId, symbol
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void deleteHolding(int accountId, String symbol) {
        jdbcTemplate.update(
            "DELETE FROM holdings WHERE account_id = ? AND symbol = ?", 
            accountId, symbol
        );
    }

    public void insertTrade(LocalDateTime timestamp, int accountId, String action, 
                           String symbol, double quantity, double price, Double profitLoss) {
        jdbcTemplate.update(
            "INSERT INTO trades (timestamp, account_id, action, symbol, quantity, price, profit_loss) VALUES (?, ?, ?, ?, ?, ?, ?)",
            timestamp, accountId, action, symbol, quantity, price, profitLoss
        );
    }

    public Double getAccountBalance(int accountId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT balance FROM accounts WHERE id = ?",
                    Double.class, accountId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public List<Map<String, Object>> getTradeHistory(int accountId) {
        return jdbcTemplate.queryForList(
            "SELECT timestamp, action, symbol, quantity, price, profit_loss FROM trades WHERE account_id = ? ORDER BY timestamp DESC LIMIT 100",
            accountId
        );
    }

    public List<Map<String, Object>> getAccountHoldings(int accountId) {
        return jdbcTemplate.queryForList(
            "SELECT symbol, quantity, created_at, updated_at FROM holdings WHERE account_id = ? AND quantity > 0 ORDER BY symbol",
            accountId
        );
    }

    public Map<String, Object> getHolding(int accountId, String symbol) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT symbol, quantity, created_at, updated_at FROM holdings WHERE account_id = ? AND symbol = ?",
                accountId, symbol
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void resetAccount(int accountId, double initialBalance) {
        jdbcTemplate.update("UPDATE accounts SET balance = ? WHERE id = ?", initialBalance, accountId);
        jdbcTemplate.update("DELETE FROM holdings WHERE account_id = ?", accountId);
        jdbcTemplate.update("DELETE FROM trades WHERE account_id = ?", accountId);
    }
}