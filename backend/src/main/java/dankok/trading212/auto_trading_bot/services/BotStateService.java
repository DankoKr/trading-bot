package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.enums.BotStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BotStateService {
    
    private BotStatus currentStatus = BotStatus.ACTIVE;
    private LocalDateTime lastStatusChange = LocalDateTime.now();
    private String statusChangeReason = "Bot initialized";

    public BotStatus getCurrentStatus() {
        return currentStatus;
    }

    public boolean isActive() {
        return currentStatus == BotStatus.ACTIVE;
    }

    public boolean isOnHold() {
        return currentStatus == BotStatus.ON_HOLD;
    }

    public void setStatus(BotStatus status, String reason) {
        this.currentStatus = status;
        this.lastStatusChange = LocalDateTime.now();
        this.statusChangeReason = reason;
    }

    public void activateBot(String reason) {
        setStatus(BotStatus.ACTIVE, reason != null ? reason : "Bot activated");
    }

    public void holdBot(String reason) {
        setStatus(BotStatus.ON_HOLD, reason != null ? reason : "Bot put on hold");
    }

    public void stopBot(String reason) {
        setStatus(BotStatus.STOPPED, reason != null ? reason : "Bot stopped");
    }

    public LocalDateTime getLastStatusChange() {
        return lastStatusChange;
    }

    public String getStatusChangeReason() {
        return statusChangeReason;
    }

    public String getStatusSummary() {
        return String.format("Bot Status: %s (Changed: %s, Reason: %s)", 
            currentStatus, lastStatusChange, statusChangeReason);
    }
}