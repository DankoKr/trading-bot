package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.dtos.BacktestResult;
import dankok.trading212.auto_trading_bot.services.BacktestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@RestController
@RequestMapping("/backtest")
public class BacktestController {

    private final BacktestService backtestService;

    @Autowired
    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    @PostMapping
    public BacktestResult runBacktest(
            @RequestParam String coinId,
            @RequestParam(defaultValue = "365") int days,
            @RequestParam(defaultValue = "1000.0") double initialBalance,
            HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new BacktestResult(false, coinId, null, null, initialBalance, initialBalance,
                0, 0, 0, 0, new ArrayList<>(), "Authentication required");
        }
        
        return backtestService.runBacktest(coinId, days, initialBalance);
    }
}
