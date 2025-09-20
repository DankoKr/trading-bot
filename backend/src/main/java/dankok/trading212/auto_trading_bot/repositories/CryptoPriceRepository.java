package dankok.trading212.auto_trading_bot.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class CryptoPriceRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CryptoPriceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void savePrice(String symbol, double price, LocalDateTime timestamp) {
        try {
            jdbcTemplate.update(
                "INSERT INTO crypto_prices (symbol, price, timestamp) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE price = VALUES(price)",
                symbol, price, timestamp
            );
        } catch (Exception e) {
            System.err.println("Failed to save price for " + symbol + ": " + e.getMessage());
        }
    }

    public Double getLatestPrice(String symbol) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT price FROM crypto_prices WHERE symbol = ? ORDER BY timestamp DESC LIMIT 1",
                Double.class,
                symbol
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Map<String, Object>> getHistoricalPrices(String symbol, int days) {
        return jdbcTemplate.queryForList(
            "SELECT symbol, price, timestamp FROM crypto_prices " +
            "WHERE symbol = ? AND timestamp >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
            "ORDER BY timestamp ASC",
            symbol, days
        );
    }

    public void deletePricesOlderThan(int days) {
        jdbcTemplate.update(
            "DELETE FROM crypto_prices WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)",
            days
        );
    }
}
