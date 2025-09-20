package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    @GetMapping("/balance")
    public Map<String, Object> getUserAccountBalance(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            response.put("error", "Authentication required");
            response.put("balance", 0.0);
            return response;
        }
        
        try {
            Double balance = userService.getUserBalance(userId);
            response.put("balance", balance);
            response.put("success", true);
        } catch (Exception e) {
            response.put("error", "Failed to get balance: " + e.getMessage());
            response.put("balance", 0.0);
            response.put("success", false);
        }
        
        return response;
    }

    @GetMapping("/trades")
    public List<Map<String, Object>> getTradeHistory(HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        return userService.getTradeHistory(userId);
    }

    @GetMapping("/holdings")
    public List<Map<String, Object>> getUserAccountHoldings(HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        return userService.getUserHoldings(userId);
    }

    @GetMapping("/holdings/detailed")
    public List<Map<String, Object>> getDetailedHoldings(HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        return userService.getDetailedHoldings(userId);
    }

    @GetMapping("/portfolio/summary")
    public Map<String, Object> getPortfolioSummary(HttpServletRequest request) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                summary.put("error", "Authentication required");
                return summary;
            }
            
            Double balance = userService.getUserBalance(userId);
            List<Map<String, Object>> holdings = userService.getDetailedHoldings(userId);
            
            Double holdingsValue = holdings.stream()
                .mapToDouble(holding -> ((Number) holding.getOrDefault("current_value", 0)).doubleValue())
                .sum();
            
            Double totalPortfolioValue = balance + holdingsValue;
            
            summary.put("cash_balance", balance);
            summary.put("holdings_value", holdingsValue);
            summary.put("total_portfolio_value", totalPortfolioValue);
            summary.put("holdings_count", holdings.size());
            summary.put("holdings", holdings);
            summary.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            summary.put("error", "Failed to calculate portfolio summary: " + e.getMessage());
        }
        
        return summary;
    }

    @GetMapping("/balance/history")
    public List<Map<String, Object>> getBalanceHistory(HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new ArrayList<>();
        }
        return userService.getUserBalanceHistory(userId);
    }
}
