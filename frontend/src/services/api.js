const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

class ApiService {
  async get(endpoint) {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('API GET error:', error);
      throw error;
    }
  }

  async post(endpoint, data = {}) {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('API POST error:', error);
      throw error;
    }
  }

  // Bot Control Methods
  async getBotStatus() {
    return this.get('/bot/status');
  }

  async activateBot(reason = '') {
    return this.post(`/bot/activate?reason=${encodeURIComponent(reason)}`);
  }

  async holdBot(reason = '') {
    return this.post(`/bot/hold?reason=${encodeURIComponent(reason)}`);
  }

  async stopBot(reason = '') {
    return this.post(`/bot/stop?reason=${encodeURIComponent(reason)}`);
  }

  async resetBot(initialBalance = 1000) {
    return this.post(`/bot/reset?initialBalance=${initialBalance}`);
  }

  // Trading Methods
  async runBot(coins = 'bitcoin,ethereum,solana', mode = 'TRADING') {
    return this.get(`/run-bot?coins=${coins}&mode=${mode}`);
  }

  async runHistoricalTraining(coins = 'bitcoin,ethereum', days = 365) {
    return this.get(`/run-historical-training?coins=${coins}&days=${days}`);
  }

  async runBacktest(coinId, days = 365, initialBalance = 1000) {
    return this.get(
      `/backtest/${coinId}?days=${days}&initialBalance=${initialBalance}`
    );
  }

  // Data Methods
  async getApiStatus() {
    return this.get('/api/status');
  }

  async getAccountBalance() {
    return this.get('/account/balance');
  }

  async getTradeHistory() {
    return this.get('/account/trades');
  }

  async getCurrentPrices(coins = 'bitcoin,ethereum,solana') {
    return this.get(`/prices?coins=${coins}`);
  }

  async getHistoricalPrices(coinId, days = 30) {
    return this.get(`/historical/${coinId}?days=${days}`);
  }

  async getAccountHoldings() {
    return this.get('/account/holdings');
  }

  async getDetailedHoldings() {
    return this.get('/account/holdings/detailed');
  }

  async getPortfolioSummary() {
    return this.get('/account/portfolio/summary');
  }
}

const apiService = new ApiService();
export default apiService;
