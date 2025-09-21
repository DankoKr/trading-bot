package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.enums.BotStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BotStateServiceTest {

    private BotStateService botStateService;

    @BeforeEach
    void setUp() {
        botStateService = new BotStateService();
    }

    @Test
    void initialState_ShouldBeActive() {
        assertEquals(BotStatusEnum.ACTIVE, botStateService.getCurrentStatus());
        assertTrue(botStateService.isActive());
        assertFalse(botStateService.isOnHold());
        assertEquals("Bot initialized", botStateService.getStatusChangeReason());
    }

    @Test
    void setStatus_ShouldUpdateStatusAndReason() {
        String reason = "Manual status change";
        botStateService.setStatus(BotStatusEnum.ON_HOLD, reason);

        assertEquals(BotStatusEnum.ON_HOLD, botStateService.getCurrentStatus());
        assertTrue(botStateService.isOnHold());
        assertFalse(botStateService.isActive());
        assertEquals(reason, botStateService.getStatusChangeReason());
        assertNotNull(botStateService.getLastStatusChange());
    }

    @Test
    void activateBot_WithReason_ShouldSetActiveStatus() {
        botStateService.setStatus(BotStatusEnum.STOPPED, "Test");
        String reason = "Activation requested";
        
        botStateService.activateBot(reason);

        assertEquals(BotStatusEnum.ACTIVE, botStateService.getCurrentStatus());
        assertTrue(botStateService.isActive());
        assertEquals(reason, botStateService.getStatusChangeReason());
    }

    @Test
    void activateBot_WithNullReason_ShouldUseDefaultReason() {
        botStateService.activateBot(null);

        assertEquals(BotStatusEnum.ACTIVE, botStateService.getCurrentStatus());
        assertEquals("Bot activated", botStateService.getStatusChangeReason());
    }

    @Test
    void holdBot_WithReason_ShouldSetOnHoldStatus() {
        String reason = "Maintenance mode";
        
        botStateService.holdBot(reason);

        assertEquals(BotStatusEnum.ON_HOLD, botStateService.getCurrentStatus());
        assertTrue(botStateService.isOnHold());
        assertFalse(botStateService.isActive());
        assertEquals(reason, botStateService.getStatusChangeReason());
    }

    @Test
    void holdBot_WithNullReason_ShouldUseDefaultReason() {
        botStateService.holdBot(null);

        assertEquals(BotStatusEnum.ON_HOLD, botStateService.getCurrentStatus());
        assertEquals("Bot put on hold", botStateService.getStatusChangeReason());
    }

    @Test
    void stopBot_WithReason_ShouldSetStoppedStatus() {
        String reason = "Emergency stop";
        
        botStateService.stopBot(reason);

        assertEquals(BotStatusEnum.STOPPED, botStateService.getCurrentStatus());
        assertFalse(botStateService.isActive());
        assertFalse(botStateService.isOnHold());
        assertEquals(reason, botStateService.getStatusChangeReason());
    }

    @Test
    void stopBot_WithNullReason_ShouldUseDefaultReason() {
        botStateService.stopBot(null);

        assertEquals(BotStatusEnum.STOPPED, botStateService.getCurrentStatus());
        assertEquals("Bot stopped", botStateService.getStatusChangeReason());
    }

    @Test
    void getStatusSummary_ShouldReturnFormattedString() {
        botStateService.setStatus(BotStatusEnum.ON_HOLD, "Test reason");
        
        String summary = botStateService.getStatusSummary();

        assertNotNull(summary);
        assertTrue(summary.contains("ON_HOLD"));
        assertTrue(summary.contains("Test reason"));
        assertTrue(summary.contains("Bot Status:"));
    }

    @Test
    void lastStatusChange_ShouldUpdateOnStatusChange() {
        LocalDateTime beforeChange = LocalDateTime.now();
        
        botStateService.setStatus(BotStatusEnum.STOPPED, "Test");
        
        LocalDateTime afterChange = botStateService.getLastStatusChange();
        assertTrue(afterChange.isAfter(beforeChange) || afterChange.isEqual(beforeChange));
    }

    @Test
    void multipleStatusChanges_ShouldUpdateCorrectly() {
        botStateService.activateBot("First activation");
        assertEquals(BotStatusEnum.ACTIVE, botStateService.getCurrentStatus());
        
        botStateService.holdBot("Put on hold");
        assertEquals(BotStatusEnum.ON_HOLD, botStateService.getCurrentStatus());
        assertEquals("Put on hold", botStateService.getStatusChangeReason());
        
        botStateService.stopBot("Emergency stop");
        assertEquals(BotStatusEnum.STOPPED, botStateService.getCurrentStatus());
        assertEquals("Emergency stop", botStateService.getStatusChangeReason());
    }
}
