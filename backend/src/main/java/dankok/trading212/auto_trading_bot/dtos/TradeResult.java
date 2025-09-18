package dankok.trading212.auto_trading_bot.dtos;

public class TradeResult {
    private boolean success;
    private String action;
    private double quantity;
    private double price;
    private double totalValue;
    private String message;

    public TradeResult(boolean success, String action, double quantity, double price, double totalValue, String message) {
        this.success = success;
        this.action = action;
        this.quantity = quantity;
        this.price = price;
        this.totalValue = totalValue;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}