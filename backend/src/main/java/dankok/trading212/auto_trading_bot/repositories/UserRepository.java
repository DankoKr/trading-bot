package dankok.trading212.auto_trading_bot.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getUserByUsername(String username) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT id, username, email, first_name, last_name, balance, is_active, created_at, last_login FROM users WHERE username = ?",
                username
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Map<String, Object> getUserByEmail(String email) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT id, username, email, first_name, last_name, balance, is_active, created_at, last_login FROM users WHERE email = ?",
                email
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Map<String, Object> getUserById(int userId) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT id, username, email, first_name, last_name, balance, is_active, created_at, last_login FROM users WHERE id = ?",
                userId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public String getUserPasswordHash(String username) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE username = ?",
                String.class,
                username
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int createUser(String username, String email, String passwordHash, String firstName, String lastName, double initialBalance) {
        jdbcTemplate.update(
            "INSERT INTO users (username, email, password_hash, first_name, last_name, balance) VALUES (?, ?, ?, ?, ?, ?)",
            username, email, passwordHash, firstName, lastName, initialBalance
        );
        
        Integer userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE username = ?",
            Integer.class,
            username
        );
        return userId != null ? userId : -1;
    }

    public void updateLastLogin(int userId) {
        jdbcTemplate.update(
            "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?",
            userId
        );
    }

    public Double getUserBalance(int userId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT balance FROM users WHERE id = ?",
                Double.class,
                userId
            );
        } catch (EmptyResultDataAccessException e) {
            return 0.0;
        }
    }

    public void updateUserBalance(int userId, double newBalance) {
        jdbcTemplate.update(
                "UPDATE users SET balance = ? WHERE id = ?",
                newBalance,
                userId);
    }
    
    public List<Map<String, Object>> getUserHoldings(int userId) {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT symbol, quantity, created_at, updated_at FROM holdings WHERE user_id = ? AND quantity > 0 ORDER BY symbol",
                    userId);
        } catch (Exception e) {
            return jdbcTemplate.queryForList(
                    "SELECT symbol, quantity, NOW() as created_at, NOW() as updated_at FROM holdings WHERE user_id = ? AND quantity > 0 ORDER BY symbol",
                    userId);
        }
    }

    
    public List<Map<String, Object>> getUserTradeHistory(int userId) {
        return jdbcTemplate.queryForList(
                "SELECT timestamp, action, symbol, quantity, price, profit_loss, created_at " +
                        "FROM trades WHERE user_id = ? ORDER BY timestamp DESC",
                userId);
    }
    
    
    public List<Map<String, Object>> getUserBalanceHistory(int userId) {
        String sql = """
            SELECT 
                t.timestamp,
                SUM(
                    CASE 
                        WHEN t.action = 'BUY' THEN -t.price * t.quantity
                        WHEN t.action = 'SELL' THEN t.price * t.quantity
                        ELSE 0
                    END
                ) OVER (ORDER BY t.timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) + u.balance AS balance
            FROM trades t
            JOIN users u ON u.id = t.user_id
            WHERE t.user_id = ?
            ORDER BY t.timestamp
        """;
        return jdbcTemplate.queryForList(sql, userId);
    }
    
    public boolean resetUser(int userId, double initialBalance) {
        try {
            jdbcTemplate.update("UPDATE users SET balance = ? WHERE id = ?", initialBalance, userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
