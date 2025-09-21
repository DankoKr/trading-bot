Data Fetching:
GET /prices?coins=bitcoin // Current prices
GET /historical/bitcoin?days=30 // Historical data

Trading Operations:  
GET /run-bot?mode=TRADING&coins=bitcoin // Live trading
GET /run-bot?mode=TRAINING&coins=bitcoin // Training mode
GET /run-historical-training?days=365 // Historical analysis
GET /backtest/bitcoin?days=365 // Backtesting

Bot Control:
POST /bot/hold?reason=market-volatility // Hold
POST /bot/activate?reason=good-conditions // Activate  
POST /bot/stop // Stop
POST /bot/reset?initialBalance=1000 // Reset
GET /bot/status // Status

Account Data:
GET /account/balance // Current balance
GET /account/trades // Trade history
