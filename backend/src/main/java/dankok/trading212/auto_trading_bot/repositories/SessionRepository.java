package dankok.trading212.auto_trading_bot.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveUserSession(int userId, String tokenHash, long expirationMillis) {
        Timestamp expiresAt = new Timestamp(expirationMillis);

        jdbcTemplate.update(
            "INSERT INTO user_sessions (user_id, token_hash, expires_at) VALUES (?, ?, ?)",
            userId, tokenHash, expiresAt
        );
    }

    public boolean isSessionValid(int userId, String tokenHash) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_sessions WHERE user_id = ? AND token_hash = ? AND expires_at > NOW() AND is_active = TRUE",
                Integer.class,
                userId, tokenHash
            );
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void invalidateSession(int userId, String tokenHash) {
        jdbcTemplate.update(
            "UPDATE user_sessions SET is_active = FALSE WHERE user_id = ? AND token_hash = ?",
            userId, tokenHash
        );
    }

    public void invalidateAllUserSessions(int userId) {
        jdbcTemplate.update(
            "UPDATE user_sessions SET is_active = FALSE WHERE user_id = ?",
            userId
        );
    }

    public void cleanupExpiredSessions() {
        jdbcTemplate.update(
            "DELETE FROM user_sessions WHERE expires_at < NOW()"
        );
    }
}
