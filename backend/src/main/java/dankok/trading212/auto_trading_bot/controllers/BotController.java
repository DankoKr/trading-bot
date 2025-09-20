package dankok.trading212.auto_trading_bot.controllers;

import dankok.trading212.auto_trading_bot.dtos.BotStatusResponse;
import dankok.trading212.auto_trading_bot.dtos.TradingAnalysisResult;
import dankok.trading212.auto_trading_bot.enums.TradingModeEnum;
import dankok.trading212.auto_trading_bot.services.BotStateService;
import dankok.trading212.auto_trading_bot.services.TradingBotService;
import dankok.trading212.auto_trading_bot.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/bot")
public class BotController {

    private final TradingBotService tradingBotService;
    private final BotStateService botStateService;
    private final UserService userService;

    @Autowired
    public BotController(TradingBotService tradingBotService, BotStateService botStateService, UserService userService) {
        this.tradingBotService = tradingBotService;
        this.botStateService = botStateService;
        this.userService = userService;
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    @GetMapping("/status")
    public BotStatusResponse getBotStatus(HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new BotStatusResponse(
                botStateService.getCurrentStatus(),
                LocalDateTime.now(),
                "Authentication required",
                "User not authenticated",
                false
            );
        }
        
        return new BotStatusResponse(
            botStateService.getCurrentStatus(),
            botStateService.getLastStatusChange(),
            botStateService.getStatusChangeReason(),
            botStateService.getStatusSummary(),
            true
        );
    }

    @PostMapping("/run")
    public TradingAnalysisResult runBot(
            @RequestParam(defaultValue = "bitcoin,ethereum,solana") String coins,
            @RequestParam(defaultValue = "TRADING") String mode,
            HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new TradingAnalysisResult(new ArrayList<>(), "Authentication required", false);
        }
        
        List<String> coinIds = Arrays.asList(coins.split(","));
        TradingModeEnum tradingMode = TradingModeEnum.valueOf(mode.toUpperCase());
        return tradingBotService.executeTradingLogic(coinIds, tradingMode);
    }

    @PostMapping("/hold")
    public BotStatusResponse holdBot(@RequestParam(required = false) String reason, HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new BotStatusResponse(
                botStateService.getCurrentStatus(),
                LocalDateTime.now(),
                "Authentication required",
                "User not authenticated",
                false
            );
        }
        
        botStateService.holdBot(reason);
        return new BotStatusResponse(
            botStateService.getCurrentStatus(),
            botStateService.getLastStatusChange(),
            botStateService.getStatusChangeReason(),
            "Bot has been put on hold. Trading suspended.",
            true
        );
    }

    @PostMapping("/activate")
    public BotStatusResponse activateBot(@RequestParam(required = false) String reason, HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new BotStatusResponse(
                botStateService.getCurrentStatus(),
                LocalDateTime.now(),
                "Authentication required",
                "User not authenticated",
                false
            );
        }
        
        botStateService.activateBot(reason);
        return new BotStatusResponse(
            botStateService.getCurrentStatus(),
            botStateService.getLastStatusChange(),
            botStateService.getStatusChangeReason(),
            "Bot has been activated. Trading resumed.",
            true
        );
    }

    @PostMapping("/stop")
    public BotStatusResponse stopBot(@RequestParam(required = false) String reason, HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new BotStatusResponse(
                botStateService.getCurrentStatus(),
                LocalDateTime.now(),
                "Authentication required",
                "User not authenticated",
                false
            );
        }
        
        botStateService.stopBot(reason);
        return new BotStatusResponse(
            botStateService.getCurrentStatus(),
            botStateService.getLastStatusChange(),
            botStateService.getStatusChangeReason(),
            "Bot has been stopped completely.",
            true
        );
    }

    @PostMapping("/reset")
    public BotStatusResponse resetBot(@RequestParam(defaultValue = "1000.0") double initialBalance, HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new BotStatusResponse(
                botStateService.getCurrentStatus(),
                LocalDateTime.now(),
                "Authentication required",
                "User not authenticated",
                false
            );
        }
        
        boolean success = userService.resetUserAccount(userId, initialBalance);
        
        if (success) {
            return new BotStatusResponse(
                botStateService.getCurrentStatus(),
                LocalDateTime.now(),
                "User account reset to initial state",
                String.format("User account reset with balance: $%.2f", initialBalance),
                true
            );
        } else {
            return new BotStatusResponse(
                botStateService.getCurrentStatus(),
                LocalDateTime.now(),
                "Reset failed",
                "Failed to reset user account",
                false
            );
        }
    }

    @PostMapping("/historical-training")
    public TradingAnalysisResult runHistoricalTraining(
            @RequestParam(defaultValue = "bitcoin,ethereum") String coins,
            @RequestParam(defaultValue = "365") int days,
            HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return new TradingAnalysisResult(new ArrayList<>(), "Authentication required", false);
        }
        
        List<String> coinIds = Arrays.asList(coins.split(","));
        return tradingBotService.runTrainingOnHistoricalData(coinIds, days);
    }
}
