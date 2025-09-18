package dankok.trading212.auto_trading_bot.dtos;

import java.util.List;

public class HistoricalPriceResponse {
    private boolean success;
    private String coinId;
    private List<Double> prices;
    private int requestedDays;
    private int actualDays;
    private String errorMessage;

    public HistoricalPriceResponse(boolean success, String coinId, List<Double> prices, 
                                 int requestedDays, int actualDays, String errorMessage) {
        this.success = success;
        this.coinId = coinId;
        this.prices = prices;
        this.requestedDays = requestedDays;
        this.actualDays = actualDays;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getCoinId() { return coinId; }
    public void setCoinId(String coinId) { this.coinId = coinId; }
    public List<Double> getPrices() { return prices; }
    public void setPrices(List<Double> prices) { this.prices = prices; }
    public int getRequestedDays() { return requestedDays; }
    public void setRequestedDays(int requestedDays) { this.requestedDays = requestedDays; }
    public int getActualDays() { return actualDays; }
    public void setActualDays(int actualDays) { this.actualDays = actualDays; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}