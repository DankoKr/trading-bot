package dankok.trading212.auto_trading_bot.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class BacktestResult {
    private boolean success;
    private String coinId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private double initialBalance;
    private double finalBalance;
    private double totalReturn;
    private double totalReturnPercentage;
    private int totalTrades;
    private int successfulTrades;
    private List<TradeResult> trades;
    private String summary;

    public BacktestResult(boolean success, String coinId, LocalDateTime startDate, LocalDateTime endDate,
                         double initialBalance, double finalBalance, double totalReturn, 
                         double totalReturnPercentage, int totalTrades, int successfulTrades,
                         List<TradeResult> trades, String summary) {
        this.success = success;
        this.coinId = coinId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initialBalance = initialBalance;
        this.finalBalance = finalBalance;
        this.totalReturn = totalReturn;
        this.totalReturnPercentage = totalReturnPercentage;
        this.totalTrades = totalTrades;
        this.successfulTrades = successfulTrades;
        this.trades = trades;
        this.summary = summary;
    }

    public boolean isSuccess() { return success; }
    public String getCoinId() { return coinId; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public double getInitialBalance() { return initialBalance; }
    public double getFinalBalance() { return finalBalance; }
    public double getTotalReturn() { return totalReturn; }
    public double getTotalReturnPercentage() { return totalReturnPercentage; }
    public int getTotalTrades() { return totalTrades; }
    public int getSuccessfulTrades() { return successfulTrades; }
    public List<TradeResult> getTrades() { return trades; }
    public String getSummary() { return summary; }
}
