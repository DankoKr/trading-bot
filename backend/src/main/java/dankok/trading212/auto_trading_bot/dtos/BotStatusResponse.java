package dankok.trading212.auto_trading_bot.dtos;

import dankok.trading212.auto_trading_bot.enums.BotStatus;
import java.time.LocalDateTime;

public class BotStatusResponse {
    private BotStatus status;
    private LocalDateTime lastStatusChange;
    private String statusChangeReason;
    private String message;
    private boolean success;

    public BotStatusResponse(BotStatus status, LocalDateTime lastStatusChange, 
                           String statusChangeReason, String message, boolean success) {
        this.status = status;
        this.lastStatusChange = lastStatusChange;
        this.statusChangeReason = statusChangeReason;
        this.message = message;
        this.success = success;
    }

    public BotStatus getStatus() { return status; }
    public void setStatus(BotStatus status) { this.status = status; }
    public LocalDateTime getLastStatusChange() { return lastStatusChange; }
    public void setLastStatusChange(LocalDateTime lastStatusChange) { this.lastStatusChange = lastStatusChange; }
    public String getStatusChangeReason() { return statusChangeReason; }
    public void setStatusChangeReason(String statusChangeReason) { this.statusChangeReason = statusChangeReason; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}