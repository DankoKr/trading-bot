const API_BASE_URL =
  process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

class ApiService {
  constructor() {
    this.token = localStorage.getItem('authToken');
  }

  getAuthHeaders() {
    const headers = {
      'Content-Type': 'application/json',
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    return headers;
  }

  setToken(token) {
    this.token = token;
    if (token) {
      localStorage.setItem('authToken', token);
    } else {
      localStorage.removeItem('authToken');
    }
  }

  async get(endpoint) {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'GET',
        headers: this.getAuthHeaders(),
      });

      if (response.status === 401) {
        this.setToken(null);
        throw new Error('Authentication required');
      }

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
        headers: this.getAuthHeaders(),
        body: JSON.stringify(data),
      });

      if (response.status === 401) {
        this.setToken(null);
        throw new Error('Authentication required');
      }

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('API POST error:', error);
      throw error;
    }
  }

  // Authentication Methods
  async login(username, password) {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });

    const data = await response.json();

    if (data.success && data.token) {
      this.setToken(data.token);
    }

    return data;
  }

  async register(username, email, password, firstName, lastName) {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username,
        email,
        password,
        firstName,
        lastName,
      }),
    });

    const data = await response.json();

    if (data.success && data.token) {
      this.setToken(data.token);
    }

    return data;
  }

  async logout() {
    try {
      if (this.token) {
        await fetch(`${API_BASE_URL}/auth/logout`, {
          method: 'POST',
          headers: this.getAuthHeaders(),
        });
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      this.setToken(null);
    }
  }

  async getProfile() {
    return this.get('/auth/profile');
  }

  isAuthenticated() {
    return !!this.token;
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
  async runBot(coins = 'bitcoin', mode = 'TRADING') {
    return this.post(`/bot/run?coins=${coins}&mode=${mode}`);
  }

  async runHistoricalTraining(coins = 'bitcoin', days = 365) {
    return this.post(`/bot/historical-training?coins=${coins}&days=${days}`);
  }

  async runBacktest(coinId, days = 365, initialBalance = 1000) {
    return this.post(
      `/backtest?coinId=${coinId}&days=${days}&initialBalance=${initialBalance}`
    );
  }

  // Data Methods
  async getApiStatus() {
    return this.get('/api/status');
  }

  async getAccountBalance() {
    return this.get('/user/balance');
  }

  async getTradeHistory() {
    return this.get('/user/trades');
  }

  async getCurrentPrices(coins = 'bitcoin') {
    return this.get(`/prices?coins=${coins}`);
  }

  async getHistoricalPrices(coinId, days = 30) {
    return this.get(`/prices/historical/${coinId}?days=${days}`);
  }

  async getAccountHoldings() {
    return this.get('/user/holdings');
  }

  async getDetailedHoldings() {
    return this.get('/user/holdings/detailed');
  }

  async getPortfolioSummary() {
    return this.get('/user/portfolio/summary');
  }

  async getBalanceHistory() {
    return this.get('/user/balance/history');
  }
}

const apiService = new ApiService();
export default apiService;
