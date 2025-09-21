package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.CoinAnalysis;
import dankok.trading212.auto_trading_bot.dtos.TradeResult;
import dankok.trading212.auto_trading_bot.dtos.TradingAnalysisResult;
import dankok.trading212.auto_trading_bot.enums.TradeActionEnum;
import dankok.trading212.auto_trading_bot.enums.TradingModeEnum;
import dankok.trading212.auto_trading_bot.enums.TradingSignalEunum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradingBotServiceTest {

    private CryptoDataService cryptoDataService;
    private TradeExecutorService tradeExecutorService;
    private BotStateService botStateService;
    private BacktestService backtestService;
    private TradingBotService tradingBotService;

    @BeforeEach
    void setUp() {
        cryptoDataService = mock(CryptoDataService.class);
        tradeExecutorService = mock(TradeExecutorService.class);
        botStateService = mock(BotStateService.class);
        backtestService = mock(BacktestService.class);
        tradingBotService = new TradingBotService(cryptoDataService, tradeExecutorService, botStateService, backtestService);
    }

    @Test
    void executeTradingLogic_ShouldReturnSuspendedIfInactive() {
        when(botStateService.isActive()).thenReturn(false);
        TradingAnalysisResult result = tradingBotService.executeTradingLogic(List.of("btc"), TradingModeEnum.TRADING);
        assertFalse(result.isSuccess());
        assertTrue(result.getSummary().contains("Trading suspended"));
    }

    @Test
    void executeTradingLogic_ShouldAnalyzeCoinsAndReturnResult() {
        when(botStateService.isActive()).thenReturn(true);
        List<Double> prices = new ArrayList<>();
        for (int i = 0; i < 50; i++) prices.add(100.0 + i);
        when(cryptoDataService.fetchHistoricalPrices("btc", 50)).thenReturn(prices);
        when(tradeExecutorService.executeBuyTrade(anyInt(), anyString(), anyDouble(), anyDouble()))
                .thenReturn(new TradeResult(true, TradeActionEnum.BUY.name(), 1, 150.0, 150.0, "Buy"));
        TradingAnalysisResult result = tradingBotService.executeTradingLogic(List.of("btc"), TradingModeEnum.TRADING);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getAnalyses().size());
    }

    @Test
    void analyzeCoin_ShouldReturnNotEnoughDataIfInsufficient() {
        when(cryptoDataService.fetchHistoricalPrices("btc", 50)).thenReturn(List.of(1.0, 2.0));
        CoinAnalysis analysis = invokeAnalyzeCoin("btc", TradingModeEnum.TRADING);
        assertEquals(TradingSignalEunum.NONE.name(), analysis.getSignal());
        assertTrue(analysis.getStatus().contains("Not enough historical data"));
    }

    private CoinAnalysis invokeAnalyzeCoin(String coinId, TradingModeEnum mode) {
        try {
            java.lang.reflect.Method method = TradingBotService.class.getDeclaredMethod("analyzeCoin", String.class, TradingModeEnum.class);
            method.setAccessible(true);
            return (CoinAnalysis) method.invoke(tradingBotService, coinId, mode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
