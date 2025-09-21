package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.CryptoPriceResponse;
import dankok.trading212.auto_trading_bot.dtos.User;
import dankok.trading212.auto_trading_bot.repositories.SessionRepository;
import dankok.trading212.auto_trading_bot.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private CryptoDataService cryptoDataService;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        sessionRepository = mock(SessionRepository.class);
        cryptoDataService = mock(CryptoDataService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, sessionRepository, cryptoDataService, passwordEncoder);
    }

    @Test
    void createUser_ShouldThrowIfUsernameExists() {
        when(userRepository.getUserByUsername("user")).thenReturn(Map.of("id", 1));
        assertThrows(RuntimeException.class, () ->
                userService.createUser("user", "email", "pass", "f", "l"));
    }

    @Test
    void createUser_ShouldThrowIfEmailExists() {
        when(userRepository.getUserByUsername("user")).thenReturn(null);
        when(userRepository.getUserByEmail("email")).thenReturn(Map.of("id", 1));
        assertThrows(RuntimeException.class, () ->
                userService.createUser("user", "email", "pass", "f", "l"));
    }

    @Test
    void createUser_ShouldCreateAndReturnUser() {
        when(userRepository.getUserByUsername("user")).thenReturn(null);
        when(userRepository.getUserByEmail("email")).thenReturn(null);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(userRepository.createUser(any(), any(), any(), any(), any(), anyDouble())).thenReturn(1);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", 1);
        userMap.put("username", "user");
        userMap.put("email", "email");
        userMap.put("first_name", "f");
        userMap.put("last_name", "l");
        userMap.put("balance", 1000.0);
        userMap.put("is_active", true);
        userMap.put("created_at", "now");
        userMap.put("last_login", "now");
        when(userRepository.getUserById(1)).thenReturn(userMap);

        User user = userService.createUser("user", "email", "pass", "f", "l");
        assertEquals("user", user.getUsername());
        assertEquals("email", user.getEmail());
    }

    @Test
    void authenticateUser_ShouldReturnNullIfUserNotFound() {
        when(userRepository.getUserByUsername("user")).thenReturn(null);
        assertNull(userService.authenticateUser("user", "pass"));
    }

    @Test
    void authenticateUser_ShouldReturnNullIfInactive() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", 1);
        userMap.put("username", "user");
        userMap.put("email", "email");
        userMap.put("first_name", "f");
        userMap.put("last_name", "l");
        userMap.put("balance", 1000.0);
        userMap.put("is_active", false);
        userMap.put("created_at", "now");
        userMap.put("last_login", "now");
        when(userRepository.getUserByUsername("user")).thenReturn(userMap);
        assertNull(userService.authenticateUser("user", "pass"));
    }

    @Test
    void authenticateUser_ShouldReturnUserIfPasswordMatches() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", 1);
        userMap.put("username", "user");
        userMap.put("email", "email");
        userMap.put("first_name", "f");
        userMap.put("last_name", "l");
        userMap.put("balance", 1000.0);
        userMap.put("is_active", true);
        userMap.put("created_at", "now");
        userMap.put("last_login", "now");
        when(userRepository.getUserByUsername("user")).thenReturn(userMap);
        when(userRepository.getUserPasswordHash("user")).thenReturn("hashed");
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(userRepository.getUserByUsername("user")).thenReturn(userMap);

        User user = userService.authenticateUser("user", "pass");
        assertNotNull(user);
        verify(userRepository).updateLastLogin(1);
    }

    @Test
    void getUserByUsername_ShouldReturnNullIfNotFound() {
        when(userRepository.getUserByUsername("user")).thenReturn(null);
        assertNull(userService.getUserByUsername("user"));
    }

    @Test
    void getUserByUsername_ShouldReturnUserIfFound() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", 1);
        userMap.put("username", "user");
        userMap.put("email", "email");
        userMap.put("first_name", "f");
        userMap.put("last_name", "l");
        userMap.put("balance", 1000.0);
        userMap.put("is_active", true);
        userMap.put("created_at", "now");
        userMap.put("last_login", "now");
        when(userRepository.getUserByUsername("user")).thenReturn(userMap);
        User user = userService.getUserByUsername("user");
        assertEquals("user", user.getUsername());
    }

    @Test
    void resetUserAccount_ShouldReturnTrueOnSuccess() {
        when(userRepository.resetUser(1, 1000.0)).thenReturn(true);
        assertTrue(userService.resetUserAccount(1, 1000.0));
    }

    @Test
    void resetUserAccount_ShouldReturnFalseOnException() {
        when(userRepository.resetUser(1, 1000.0)).thenThrow(new RuntimeException());
        assertFalse(userService.resetUserAccount(1, 1000.0));
    }

    @Test
    void getUserBalance_ShouldReturnBalance() {
        when(userRepository.getUserBalance(1)).thenReturn(100.0);
        assertEquals(100.0, userService.getUserBalance(1));
    }

    @Test
    void getTradeHistory_ShouldReturnList() {
        List<Map<String, Object>> history = List.of(Map.of("id", 1));
        when(userRepository.getUserTradeHistory(1)).thenReturn(history);
        assertEquals(history, userService.getTradeHistory(1));
    }

    @Test
    void getUserHoldings_ShouldReturnList() {
        List<Map<String, Object>> holdings = List.of(Map.of("symbol", "btc"));
        when(userRepository.getUserHoldings(1)).thenReturn(holdings);
        assertEquals(holdings, userService.getUserHoldings(1));
    }

    @Test
    void getDetailedHoldings_ShouldEnrichHoldings() {
        Map<String, Object> holding = new HashMap<>();
        holding.put("symbol", "btc");
        holding.put("quantity", 2.0);
        List<Map<String, Object>> holdings = List.of(holding);
        when(userRepository.getUserHoldings(1)).thenReturn(holdings);
        CryptoPriceResponse resp = new CryptoPriceResponse(true, Map.of("btc", 10.0), null);
        when(cryptoDataService.fetchPrices("btc")).thenReturn(resp);

        List<Map<String, Object>> result = userService.getDetailedHoldings(1);
        assertEquals(10.0, result.get(0).get("current_price"));
        assertEquals(20.0, result.get(0).get("current_value"));
    }

    @Test
    void getDetailedHoldings_ShouldSetZeroOnError() {
        Map<String, Object> holding = new HashMap<>();
        holding.put("symbol", "btc");
        holding.put("quantity", 2.0);
        List<Map<String, Object>> holdings = List.of(holding);
        when(userRepository.getUserHoldings(1)).thenReturn(holdings);
        when(cryptoDataService.fetchPrices("btc")).thenThrow(new RuntimeException());

        List<Map<String, Object>> result = userService.getDetailedHoldings(1);
        assertEquals(0.0, result.get(0).get("current_price"));
        assertEquals(0.0, result.get(0).get("current_value"));
    }

    @Test
    void sessionMethods_ShouldDelegate() {
        userService.saveUserSession(1, "token", 100L);
        verify(sessionRepository).saveUserSession(1, "token", 100L);
        when(sessionRepository.isSessionValid(1, "token")).thenReturn(true);
        assertTrue(userService.isSessionValid(1, "token"));
        userService.invalidateSession(1, "token");
        verify(sessionRepository).invalidateSession(1, "token");
    }

    @Test
    void getUserBalanceHistory_ShouldReturnList() {
        List<Map<String, Object>> history = List.of(Map.of("balance", 100.0));
        when(userRepository.getUserBalanceHistory(1)).thenReturn(history);
        assertEquals(history, userService.getUserBalanceHistory(1));
    }
}
