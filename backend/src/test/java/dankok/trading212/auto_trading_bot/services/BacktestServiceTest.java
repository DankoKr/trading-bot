package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.BacktestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BacktestServiceTest {

    @Mock
    private CryptoDataService cryptoDataService;

    @InjectMocks
    private BacktestService backtestService;

    private List<Double> sufficientPriceData;
    private List<Double> insufficientPriceData;

    @BeforeEach
    void setUp() {
        sufficientPriceData = Arrays.asList(
            100.0, 101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0,
            110.0, 111.0, 112.0, 113.0, 114.0, 115.0, 116.0, 117.0, 118.0, 119.0,
            120.0, 121.0, 122.0, 123.0, 124.0, 125.0, 126.0, 127.0, 128.0, 129.0,
            130.0, 131.0, 132.0, 133.0, 134.0, 135.0, 136.0, 137.0, 138.0, 139.0,
            140.0, 141.0, 142.0, 143.0, 144.0, 145.0, 146.0, 147.0, 148.0, 149.0,
            150.0, 155.0, 160.0, 165.0, 170.0, 175.0, 180.0, 185.0, 190.0, 195.0
        );
        
        insufficientPriceData = Arrays.asList(100.0, 101.0, 102.0, 103.0, 104.0);
    }

    @Test
    void runBacktest_WithSufficientData_ShouldReturnSuccessfulResult() {
        when(cryptoDataService.fetchHistoricalPrices("bitcoin", 60))
            .thenReturn(sufficientPriceData);

        BacktestResult result = backtestService.runBacktest("bitcoin", 60, 1000.0);

        assertTrue(result.isSuccess());
        assertEquals("bitcoin", result.getCoinId());
        assertEquals(1000.0, result.getInitialBalance());
        assertNotNull(result.getSummary());
        assertTrue(result.getSummary().contains("Backtest completed"));
        verify(cryptoDataService).fetchHistoricalPrices("bitcoin", 60);
    }

    @Test
    void runBacktest_WithInsufficientData_ShouldReturnFailureResult() {
        when(cryptoDataService.fetchHistoricalPrices("bitcoin", 30))
            .thenReturn(insufficientPriceData);

        BacktestResult result = backtestService.runBacktest("bitcoin", 30, 1000.0);

        assertFalse(result.isSuccess());
        assertEquals("bitcoin", result.getCoinId());
        assertEquals(1000.0, result.getInitialBalance());
        assertEquals(0, result.getTotalTrades());
        assertEquals("Insufficient historical data for backtesting", result.getSummary());
        verify(cryptoDataService).fetchHistoricalPrices("bitcoin", 30);
    }

    @Test
    void runBacktest_WithException_ShouldReturnFailureResult() {
        when(cryptoDataService.fetchHistoricalPrices("bitcoin", 60))
            .thenThrow(new RuntimeException("API Error"));

        BacktestResult result = backtestService.runBacktest("bitcoin", 60, 1000.0);

        assertFalse(result.isSuccess());
        assertEquals("bitcoin", result.getCoinId());
        assertEquals(1000.0, result.getInitialBalance());
        assertEquals(0, result.getTotalTrades());
        assertTrue(result.getSummary().contains("Backtest failed"));
        verify(cryptoDataService).fetchHistoricalPrices("bitcoin", 60);
    }

    @Test
    void runBacktest_WithVolatilePrices_ShouldExecuteTrades() {
        List<Double> volatilePrices = Arrays.asList(
            100.0, 101.0, 102.0, 103.0, 104.0, 105.0, 106.0, 107.0, 108.0, 109.0,
            110.0, 111.0, 112.0, 113.0, 114.0, 115.0, 116.0, 117.0, 118.0, 119.0,
            120.0, 121.0, 122.0, 123.0, 124.0, 125.0, 126.0, 127.0, 128.0, 129.0,
            130.0, 131.0, 132.0, 133.0, 134.0, 135.0, 136.0, 137.0, 138.0, 139.0,
            140.0, 141.0, 142.0, 143.0, 144.0, 145.0, 146.0, 147.0, 148.0, 149.0,
            180.0, 185.0, 190.0, 195.0, 200.0,
            150.0, 140.0, 130.0, 120.0, 110.0
        );

        when(cryptoDataService.fetchHistoricalPrices("ethereum", 60))
            .thenReturn(volatilePrices);

        BacktestResult result = backtestService.runBacktest("ethereum", 60, 1000.0);

        assertTrue(result.isSuccess());
        assertTrue(result.getTotalTrades() > 0);
        assertNotNull(result.getTrades());
        verify(cryptoDataService).fetchHistoricalPrices("ethereum", 60);
    }

    @Test
    void runBacktest_CalculatesCorrectReturnPercentage() {
        when(cryptoDataService.fetchHistoricalPrices("bitcoin", 60))
            .thenReturn(sufficientPriceData);

        BacktestResult result = backtestService.runBacktest("bitcoin", 60, 1000.0);

        assertTrue(result.isSuccess());
        double expectedReturnPercentage = (result.getTotalReturn() / 1000.0) * 100;
        assertEquals(expectedReturnPercentage, result.getTotalReturnPercentage(), 0.01);
    }

    @Test
    void runBacktest_WithZeroInitialBalance_ShouldHandleGracefully() {
        when(cryptoDataService.fetchHistoricalPrices("bitcoin", 60))
            .thenReturn(sufficientPriceData);

        BacktestResult result = backtestService.runBacktest("bitcoin", 60, 0.0);

        assertTrue(result.isSuccess());
        assertEquals(0.0, result.getInitialBalance());
        assertEquals(0, result.getTotalTrades());
    }
}
