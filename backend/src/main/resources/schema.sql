CREATE TABLE IF NOT EXISTS accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    balance DECIMAL(20, 10) NOT NULL DEFAULT 0.0000000000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_balance (balance)
);

CREATE TABLE IF NOT EXISTS holdings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(25, 15) NOT NULL DEFAULT 0.000000000000000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_account_symbol (account_id, symbol),
    INDEX idx_account_id (account_id),
    CONSTRAINT chk_quantity_positive CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS trades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    account_id INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(25, 15) NOT NULL,
    price DECIMAL(20, 10) NOT NULL,
    profit_loss DECIMAL(20, 10) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    INDEX idx_account_timestamp (account_id, timestamp),
    INDEX idx_symbol_timestamp (symbol, timestamp),
    INDEX idx_action (action),
    CONSTRAINT chk_price_positive CHECK (price > 0),
    CONSTRAINT chk_quantity_positive CHECK (quantity >= 0),
    CONSTRAINT chk_valid_action CHECK (action IN ('BUY', 'SELL', 'BACKTEST_SUMMARY'))
);

CREATE TABLE IF NOT EXISTS crypto_prices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    price DECIMAL(20, 10) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_symbol_timestamp (symbol, timestamp),
    INDEX idx_symbol_timestamp (symbol, timestamp),
    INDEX idx_timestamp (timestamp),
    CONSTRAINT chk_price_positive CHECK (price > 0)
);

INSERT INTO accounts (id, balance) VALUES (1, 1000.0000000000) 
ON DUPLICATE KEY UPDATE balance = CASE 
    WHEN balance = 0 THEN 1000.0000000000 
    ELSE balance 
END;