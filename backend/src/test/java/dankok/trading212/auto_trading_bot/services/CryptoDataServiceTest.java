package dankok.trading212.auto_trading_bot.services;

import dankok.trading212.auto_trading_bot.dtos.CryptoPriceResponse;
import dankok.trading212.auto_trading_bot.dtos.HistoricalPriceResponse;
import dankok.trading212.auto_trading_bot.repositories.CryptoPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoDataServiceTest {

    @Mock
    private RestClient coinGeckoRestClient;
    
    @Mock
    private CryptoPriceRepository cryptoPriceRepository;
    
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private CryptoDataService cryptoDataService;

    @BeforeEach
    void setUp() {
        when(coinGeckoRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void fetchPrices_WithValidResponse_ShouldReturnSuccessfulResponse() {
        Map<String, Object> mockResponse = Map.of(
            "bitcoin", Map.of("usd", 50000.0),
            "ethereum", Map.of("usd", 3000.0)
        );
        
        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        CryptoPriceResponse result = cryptoDataService.fetchPrices("bitcoin", "ethereum");

        assertTrue(result.isSuccess());
        assertEquals(2, result.getPrices().size());
        assertEquals(50000.0, result.getPrices().get("bitcoin"));
        assertEquals(3000.0, result.getPrices().get("ethereum"));
        assertNull(result.getErrorMessage());
        
        verify(cryptoPriceRepository, times(2)).savePrice(anyString(), anyDouble(), any());
    }

    @Test
    void fetchPrices_WithEmptyResponse_ShouldReturnFailure() {
        when(responseSpec.body(Map.class)).thenReturn(Map.of());

        CryptoPriceResponse result = cryptoDataService.fetchPrices("bitcoin");

        assertFalse(result.isSuccess());
        assertTrue(result.getPrices().isEmpty());
        assertEquals("No price data received from API", result.getErrorMessage());
    }

    @Test
    void fetchPrices_WithNullResponse_ShouldReturnFailure() {
        when(responseSpec.body(Map.class)).thenReturn(null);

        CryptoPriceResponse result = cryptoDataService.fetchPrices("bitcoin");

        assertFalse(result.isSuccess());
        assertEquals("No price data received from API", result.getErrorMessage());
    }

    @Test
    void fetchPrices_WithRestClientException_ShouldReturnFailure() {
        when(responseSpec.body(Map.class)).thenThrow(new RestClientException("Network error"));

        CryptoPriceResponse result = cryptoDataService.fetchPrices("bitcoin");

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Error fetching crypto prices"));
    }

    @Test
    void fetchPrices_WithRateLimitError_ShouldReturnSpecificError() {
        when(responseSpec.body(Map.class)).thenThrow(new RestClientException("429 rate limit"));

        CryptoPriceResponse result = cryptoDataService.fetchPrices("bitcoin");

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Rate limit exceeded"));
    }

    @Test
    void fetchHistoricalPricesWithMetadata_WithValidResponse_ShouldReturnSuccessfulResponse() {
        List<List<Object>> pricesData = Arrays.asList(
            Arrays.asList(1234567890000L, 50000.0),
            Arrays.asList(1234567900000L, 51000.0),
            Arrays.asList(1234567910000L, 52000.0)
        );
        
        Map<String, Object> mockResponse = Map.of("prices", pricesData);
        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        HistoricalPriceResponse result = cryptoDataService.fetchHistoricalPricesWithMetadata("bitcoin", 30);

        assertTrue(result.isSuccess());
        assertEquals("bitcoin", result.getCoinId());
        assertEquals(3, result.getPrices().size());
        assertEquals(50000.0, result.getPrices().get(0));
        assertEquals(51000.0, result.getPrices().get(1));
        assertEquals(52000.0, result.getPrices().get(2));
        assertEquals(3, result.getActualDays());
    }

    @Test
    void fetchHistoricalPricesWithMetadata_WithNoPricesKey_ShouldReturnFailure() {
        Map<String, Object> mockResponse = Map.of("volumes", Arrays.asList());
        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        HistoricalPriceResponse result = cryptoDataService.fetchHistoricalPricesWithMetadata("bitcoin", 30);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("No historical price data available"));
    }

    @Test
    void fetchHistoricalPrices_ShouldReturnPricesList() {
        List<List<Object>> pricesData = Arrays.asList(
            Arrays.asList(1234567890000L, 50000.0),
            Arrays.asList(1234567900000L, 51000.0)
        );
        
        Map<String, Object> mockResponse = Map.of("prices", pricesData);
        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        List<Double> result = cryptoDataService.fetchHistoricalPrices("bitcoin", 30);

        assertEquals(2, result.size());
        assertEquals(50000.0, result.get(0));
        assertEquals(51000.0, result.get(1));
    }

    @Test
    void getCurrentPrice_WithValidResponse_ShouldReturnPrice() {
        Map<String, Object> mockResponse = Map.of("bitcoin", Map.of("usd", 50000.0));
        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        Double result = cryptoDataService.getCurrentPrice("bitcoin");

        assertEquals(50000.0, result);
    }

    @Test
    void getCurrentPrice_WithInvalidResponse_ShouldReturnNull() {
        when(responseSpec.body(Map.class)).thenReturn(Map.of());

        Double result = cryptoDataService.getCurrentPrice("bitcoin");

        assertNull(result);
    }

    @Test
    void hasSufficientData_WithSufficientData_ShouldReturnTrue() {
        List<List<Object>> pricesData = Arrays.asList(
            Arrays.asList(1234567890000L, 50000.0),
            Arrays.asList(1234567900000L, 51000.0),
            Arrays.asList(1234567910000L, 52000.0)
        );
        
        Map<String, Object> mockResponse = Map.of("prices", pricesData);
        when(responseSpec.body(Map.class)).thenReturn(mockResponse);

        boolean result = cryptoDataService.hasSufficientData("bitcoin", 3);

        assertTrue(result);
    }

}
