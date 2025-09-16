package dankok.trading212.auto_trading_bot.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class CryptoRepository {

    private final JdbcTemplate jdbcTemplate;

    public CryptoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void savePrice(String symbol, double price) { // not used anymore
        jdbcTemplate.update(
            "INSERT INTO crypto_prices (symbol, price, timestamp) VALUES (?, ?, ?)",
            symbol,
            price,
            new Timestamp(System.currentTimeMillis())
        );
    }
}