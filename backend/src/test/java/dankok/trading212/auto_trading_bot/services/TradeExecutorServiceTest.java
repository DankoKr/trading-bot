package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.TradeResult;
import dankok.trading212.auto_trading_bot.enums.TradeActionEnum;
import dankok.trading212.auto_trading_bot.repositories.HoldingRepository;
import dankok.trading212.auto_trading_bot.repositories.TradeRepository;
import dankok.trading212.auto_trading_bot.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeExecutorServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private HoldingRepository holdingRepository;
    
    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeExecutorService tradeExecutorService;

    @Test
    void executeBuyTrade_WithSufficientBalance_ShouldReturnSuccess() {
        int userId = 1;
        String symbol = "bitcoin";
        double currentPrice = 50000.0;
        double amountToInvest = 100.0;
        double expectedQuantity = amountToInvest / currentPrice;

        when(userRepository.getUserBalance(userId)).thenReturn(500.0);

        TradeResult result = tradeExecutorService.executeBuyTrade(userId, symbol, currentPrice, amountToInvest);

        assertTrue(result.isSuccess());
        assertEquals(TradeActionEnum.BUY.name(), result.getAction());
        assertEquals(expectedQuantity, result.getQuantity(), 0.000001);
        assertEquals(currentPrice, result.getPrice());
        assertTrue(result.getMessage().contains("Successfully bought"));

        verify(userRepository).updateUserBalance(userId, -amountToInvest);
        verify(holdingRepository).updateHolding(userId, symbol, expectedQuantity);
        verify(tradeRepository).insertTrade(eq(userId), eq(TradeActionEnum.BUY.name()), 
            eq(symbol), eq(expectedQuantity), eq(currentPrice), any());
    }

    @Test
    void executeBuyTrade_WithInsufficientBalance_ShouldReturnFailure() {
        int userId = 1;
        String symbol = "bitcoin";
        double currentPrice = 50000.0;
        double amountToInvest = 100.0;

        when(userRepository.getUserBalance(userId)).thenReturn(50.0);

        TradeResult result = tradeExecutorService.executeBuyTrade(userId, symbol, currentPrice, amountToInvest);

        assertFalse(result.isSuccess());
        assertEquals(TradeActionEnum.BUY.name(), result.getAction());
        assertEquals(0, result.getQuantity());
        assertEquals("Insufficient balance for purchase", result.getMessage());

        verify(userRepository, never()).updateUserBalance(anyInt(), anyDouble());
        verify(holdingRepository, never()).updateHolding(anyInt(), anyString(), anyDouble());
        verify(tradeRepository, never()).insertTrade(anyInt(), anyString(), anyString(), anyDouble(), anyDouble(), any());
    }

    @Test
    void executeBuyTrade_WithNullBalance_ShouldReturnFailure() {
        int userId = 1;
        String symbol = "bitcoin";
        double currentPrice = 50000.0;
        double amountToInvest = 100.0;

        when(userRepository.getUserBalance(userId)).thenReturn(null);

        TradeResult result = tradeExecutorService.executeBuyTrade(userId, symbol, currentPrice, amountToInvest);

        assertFalse(result.isSuccess());
        assertEquals("Insufficient balance for purchase", result.getMessage());
    }

    @Test
    void executeBuyTrade_WithException_ShouldReturnFailure() {
        int userId = 1;
        String symbol = "bitcoin";
        double currentPrice = 50000.0;
        double amountToInvest = 100.0;

        when(userRepository.getUserBalance(userId)).thenThrow(new RuntimeException("Database error"));

        TradeResult result = tradeExecutorService.executeBuyTrade(userId, symbol, currentPrice, amountToInvest);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Failed to execute buy trade"));
    }

    @Test
    void executeSellTrade_WithValidHoldings_ShouldReturnSuccess() {
        int userId = 1;
        String symbol = "bitcoin";
        double currentPrice = 55000.0;
        double quantityToSell = 0.002;
        double avgBuyPrice = 50000.0;
        double expectedSaleValue = quantityToSell * currentPrice;
        double expectedProfitLoss = (currentPrice - avgBuyPrice) * quantityToSell;

        when(holdingRepository.getHoldingQuantity(userId, symbol)).thenReturn(quantityToSell);
        when(tradeRepository.getAverageBuyPrice(userId, symbol)).thenReturn(avgBuyPrice);

        TradeResult result = tradeExecutorService.executeSellTrade(userId, symbol, currentPrice);

        assertTrue(result.isSuccess());
        assertEquals(TradeActionEnum.SELL.name(), result.getAction());
        assertEquals(quantityToSell, result.getQuantity());
        assertEquals(currentPrice, result.getPrice());
        assertTrue(result.getMessage().contains("Successfully sold"));

        verify(userRepository).updateUserBalance(userId, expectedSaleValue);
        verify(holdingRepository).updateHolding(userId, symbol, -quantityToSell);
        verify(tradeRepository).insertTradeWithProfitLoss(eq(userId), eq(TradeActionEnum.SELL.name()),
            eq(symbol), eq(quantityToSell), eq(currentPrice), any(), eq(expectedProfitLoss));
    }

    @Test
    void executeSellTrade_WithNoHoldings_ShouldReturnFailure() {
        int userId = 1;
        String symbol = "bitcoin";
        double currentPrice = 55000.0;

        when(holdingRepository.getHoldingQuantity(userId, symbol)).thenReturn(null);

        TradeResult result = tradeExecutorService.executeSellTrade(userId, symbol, currentPrice);

        assertFalse(result.isSuccess());
        assertEquals(TradeActionEnum.SELL.name(), result.getAction());
        assertEquals(0, result.getQuantity());
        assertTrue(result.getMessage().contains("Cannot sell: No holdings"));

        verify(userRepository, never()).updateUserBalance(anyInt(), anyDouble());
        verify(holdingRepository, never()).updateHolding(anyInt(), anyString(), anyDouble());
        verify(tradeRepository, never()).insertTradeWithProfitLoss(anyInt(), anyString(), anyString(), anyDouble(), anyDouble(), any(), anyDouble());
    }

    @Test
    void executeSellTrade_WithZeroHoldings_ShouldReturnFailure() {
        int userId = 1;
        String symbol = "bitcoin";
        double currentPrice = 55000.0;

        when(holdingRepository.getHoldingQuantity(userId, symbol)).thenReturn(0.0);

        TradeResult result = tradeExecutorService.executeSellTrade(userId, symbol, currentPrice);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Cannot sell: No holdings"));
    }

    @Test
    void executeSellTrade_WithException_ShouldReturnFailure() {
        int userId = 1;
        String symbol = "bitcoin";
        double currentPrice = 55000.0;

        when(holdingRepository.getHoldingQuantity(userId, symbol)).thenThrow(new RuntimeException("Database error"));

        TradeResult result = tradeExecutorService.executeSellTrade(userId, symbol, currentPrice);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Failed to execute sell trade"));
    }
}
