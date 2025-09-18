package dankok.trading212.auto_trading_bot.repositories;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class CryptoRepository {

    private final JdbcTemplate jdbcTemplate;

    public CryptoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}