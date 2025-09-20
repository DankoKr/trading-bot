package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.CryptoPriceResponse;
import dankok.trading212.auto_trading_bot.dtos.User;
import dankok.trading212.auto_trading_bot.repositories.SessionRepository;
import dankok.trading212.auto_trading_bot.repositories.UserRepository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final CryptoDataService cryptoDataService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository cryptoRepository, SessionRepository sessionRepository,CryptoDataService cryptoDataService, PasswordEncoder passwordEncoder) {
        this.userRepository = cryptoRepository;
        this.sessionRepository = sessionRepository;
        this.cryptoDataService = cryptoDataService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String username, String email, String password, String firstName, String lastName) {
        if (getUserByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }
        if (getUserByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        int userId = userRepository.createUser(username, email, hashedPassword, firstName, lastName, 1000.0);

        return getUserById(userId);
    }

    public User authenticateUser(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null || !user.isActive()) {
            return null;
        }

        String storedHash = userRepository.getUserPasswordHash(username);
        if (storedHash != null && passwordEncoder.matches(password, storedHash)) {
            userRepository.updateLastLogin(user.getId());
            return getUserByUsername(username);
        }
        return null;
    }

    public User getUserByUsername(String username) {
        Map<String, Object> userData = userRepository.getUserByUsername(username);
        if (userData == null) return null;
        return mapToUser(userData);
    }

    public User getUserByEmail(String email) {
        Map<String, Object> userData = userRepository.getUserByEmail(email);
        if (userData == null) return null;
        return mapToUser(userData);
    }

    public User getUserById(int userId) {
        Map<String, Object> userData = userRepository.getUserById(userId);
        if (userData == null) return null;
        return mapToUser(userData);
    }

    public void saveUserSession(int userId, String tokenHash, long expirationMillis) {
        sessionRepository.saveUserSession(userId, tokenHash, expirationMillis);
    }

    public boolean isSessionValid(int userId, String tokenHash) {
        return sessionRepository.isSessionValid(userId, tokenHash);
    }

    public void invalidateSession(int userId, String tokenHash) {
        sessionRepository.invalidateSession(userId, tokenHash);
    }

    private User mapToUser(Map<String, Object> userData) {
        return new User(
            ((Number) userData.get("id")).intValue(),
            (String) userData.get("username"),
            (String) userData.get("email"),
            (String) userData.get("first_name"),
            (String) userData.get("last_name"),
            ((Number) userData.get("balance")).doubleValue(),
            (Boolean) userData.get("is_active"),
            userData.get("created_at").toString(),
            userData.get("last_login") != null ? userData.get("last_login").toString() : null
        );
    }

    @Transactional
    public boolean resetUserAccount(int userId, double initialBalance) {
        try {
            return userRepository.resetUser(userId, initialBalance);
        } catch (Exception e) {
            return false;
        }
    }

    public Double getUserBalance(int userId) {
        return userRepository.getUserBalance(userId);
    }

    public List<Map<String, Object>> getTradeHistory(int userId) {
        return userRepository.getUserTradeHistory(userId);
    }

    public List<Map<String, Object>> getUserHoldings(int userId) {
        return userRepository.getUserHoldings(userId);
    }

    public List<Map<String, Object>> getDetailedHoldings(int userId) {
        List<Map<String, Object>> holdings = userRepository.getUserHoldings(userId);

        for (Map<String, Object> holding : holdings) {
            String symbol = (String) holding.get("symbol");
            Double quantity = ((Number) holding.get("quantity")).doubleValue();

            try {
                CryptoPriceResponse priceResponse = cryptoDataService.fetchPrices(symbol);
                if (priceResponse.isSuccess() && priceResponse.getPrices().containsKey(symbol)) {
                    Double currentPrice = priceResponse.getPrices().get(symbol);
                    Double currentValue = quantity * currentPrice;

                    holding.put("current_price", currentPrice);
                    holding.put("current_value", currentValue);
                } else {
                    holding.put("current_price", 0.0);
                    holding.put("current_value", 0.0);
                }
            } catch (Exception e) {
                holding.put("current_price", 0.0);
                holding.put("current_value", 0.0);
            }
        }

        return holdings;
    }
    
    public List<Map<String, Object>> getUserBalanceHistory(int userId) {
    return userRepository.getUserBalanceHistory(userId);
}
}