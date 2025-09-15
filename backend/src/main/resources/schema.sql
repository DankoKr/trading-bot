CREATE TABLE IF NOT EXISTS crypto_prices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    price DECIMAL(20, 10) NOT NULL,
    timestamp DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS trade_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    trade_type VARCHAR(10) NOT NULL, -- 'BUY' or 'SELL'
    quantity DECIMAL(20, 10) NOT NULL,
    price DECIMAL(20, 10) NOT NULL,
    profit_loss DECIMAL(20, 10),
    timestamp DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS account_balance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    balance DECIMAL(20, 10) NOT NULL,
    timestamp DATETIME NOT NULL
);