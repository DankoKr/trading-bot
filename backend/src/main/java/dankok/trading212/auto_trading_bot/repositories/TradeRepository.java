package dankok.trading212.auto_trading_bot.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class TradeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TradeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertTrade(int userId, String action, String symbol, double quantity, double price, LocalDateTime timestamp) {
        jdbcTemplate.update(
            "INSERT INTO trades (user_id, action, symbol, quantity, price, timestamp) VALUES (?, ?, ?, ?, ?, ?)",
            userId, action, symbol, quantity, price, timestamp
        );
    }

    public void insertTradeWithProfitLoss(int userId, String action, String symbol, double quantity, double price, 
                                        LocalDateTime timestamp, double profitLoss) {
        String sql = "INSERT INTO trades (user_id, action, symbol, quantity, price, timestamp, profit_loss) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, action, symbol, quantity, price, timestamp, profitLoss);
    }

    public void deleteAllTradesForUser(int userId) {
        jdbcTemplate.update("DELETE FROM trades WHERE user_id = ?", userId);
    }

    public int getTradeCountForUser(int userId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM trades WHERE user_id = ?",
            Integer.class,
            userId
        );
        return count != null ? count : 0;
    }

    public List<Map<String, Object>> getTradesBySymbol(int userId, String symbol) {
        return jdbcTemplate.queryForList(
            "SELECT * FROM trades WHERE user_id = ? AND symbol = ? ORDER BY timestamp DESC",
            userId, symbol
        );
    }

    public double getAverageBuyPrice(int userId, String symbol) {
        String sql = "SELECT SUM(quantity * price) / SUM(quantity) FROM trades WHERE user_id = ? AND symbol = ? AND action = 'BUY'";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class, userId, symbol);
        return avg != null ? avg : 0.0;
    }
}
