package dankok.trading212.auto_trading_bot.dtos;

public class CoinAnalysis {
    private String coinId;
    private double currentPrice;
    private double shortTermSMA;
    private double longTermSMA;
    private String signal;
    private String status;
    private TradeResult tradeResult;

    public CoinAnalysis(String coinId, double currentPrice, double shortTermSMA, 
                       double longTermSMA, String signal, String status) {
        this.coinId = coinId;
        this.currentPrice = currentPrice;
        this.shortTermSMA = shortTermSMA;
        this.longTermSMA = longTermSMA;
        this.signal = signal;
        this.status = status;
    }

    public String getCoinId() { return coinId; }
    public void setCoinId(String coinId) { this.coinId = coinId; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public double getShortTermSMA() { return shortTermSMA; }
    public void setShortTermSMA(double shortTermSMA) { this.shortTermSMA = shortTermSMA; }
    public double getLongTermSMA() { return longTermSMA; }
    public void setLongTermSMA(double longTermSMA) { this.longTermSMA = longTermSMA; }
    public String getSignal() { return signal; }
    public void setSignal(String signal) { this.signal = signal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public TradeResult getTradeResult() { return tradeResult; }
    public void setTradeResult(TradeResult tradeResult) { this.tradeResult = tradeResult; }
}