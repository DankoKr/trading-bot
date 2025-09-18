package dankok.trading212.auto_trading_bot.dtos;

import java.util.Map;

public class CryptoPriceResponse {
    private boolean success;
    private Map<String, Double> prices;
    private String errorMessage;

    public CryptoPriceResponse(boolean success, Map<String, Double> prices, String errorMessage) {
        this.success = success;
        this.prices = prices;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public Map<String, Double> getPrices() { return prices; }
    public void setPrices(Map<String, Double> prices) { this.prices = prices; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}