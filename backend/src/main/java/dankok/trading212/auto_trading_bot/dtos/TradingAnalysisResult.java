package dankok.trading212.auto_trading_bot.dtos;

import java.util.List;

public class TradingAnalysisResult {
    private List<CoinAnalysis> analyses;
    private String summary;
    private boolean success;

    public TradingAnalysisResult(List<CoinAnalysis> analyses, String summary, boolean success) {
        this.analyses = analyses;
        this.summary = summary;
        this.success = success;
    }

    public List<CoinAnalysis> getAnalyses() { return analyses; }
    public void setAnalyses(List<CoinAnalysis> analyses) { this.analyses = analyses; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}