package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.enums.BotStatusEnum;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BotStateService {
    
    private BotStatusEnum currentStatus = BotStatusEnum.ACTIVE;
    private LocalDateTime lastStatusChange = LocalDateTime.now();
    private String statusChangeReason = "Bot initialized";

    public BotStatusEnum getCurrentStatus() {
        return currentStatus;
    }

    public boolean isActive() {
        return currentStatus == BotStatusEnum.ACTIVE;
    }

    public boolean isOnHold() {
        return currentStatus == BotStatusEnum.ON_HOLD;
    }

    public void setStatus(BotStatusEnum status, String reason) {
        this.currentStatus = status;
        this.lastStatusChange = LocalDateTime.now();
        this.statusChangeReason = reason;
    }

    public void activateBot(String reason) {
        setStatus(BotStatusEnum.ACTIVE, reason != null ? reason : "Bot activated");
    }

    public void holdBot(String reason) {
        setStatus(BotStatusEnum.ON_HOLD, reason != null ? reason : "Bot put on hold");
    }

    public void stopBot(String reason) {
        setStatus(BotStatusEnum.STOPPED, reason != null ? reason : "Bot stopped");
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