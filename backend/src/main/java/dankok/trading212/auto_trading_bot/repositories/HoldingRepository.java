package dankok.trading212.auto_trading_bot.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class HoldingRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HoldingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getHolding(int userId, String symbol) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT symbol, quantity, created_at, updated_at FROM holdings WHERE user_id = ? AND symbol = ?",
                userId, symbol
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Double getHoldingQuantity(int userId, String symbol) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT quantity FROM holdings WHERE user_id = ? AND symbol = ?",
                Double.class,
                userId, symbol
            );
        } catch (EmptyResultDataAccessException e) {
            return 0.0;
        }
    }

    public void updateHolding(int userId, String symbol, double quantity) {
        int rowsAffected = jdbcTemplate.update(
            "UPDATE holdings SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ? AND symbol = ?",
            quantity, userId, symbol
        );
        
        if (rowsAffected == 0) {
            jdbcTemplate.update(
                "INSERT INTO holdings (user_id, symbol, quantity) VALUES (?, ?, ?)",
                userId, symbol, quantity
            );
        }
    }

    public void deleteHolding(int userId, String symbol) {
        jdbcTemplate.update(
            "DELETE FROM holdings WHERE user_id = ? AND symbol = ?",
            userId, symbol
        );
    }

    public void deleteAllHoldingsForUser(int userId) {
        jdbcTemplate.update("DELETE FROM holdings WHERE user_id = ?", userId);
    }

    public void cleanupZeroHoldings(int userId) {
        jdbcTemplate.update(
            "DELETE FROM holdings WHERE user_id = ? AND quantity <= 0",
            userId
        );
    }
}
