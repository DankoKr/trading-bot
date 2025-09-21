CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    balance DECIMAL(20, 10) NOT NULL DEFAULT 0.0000000000,  
    username VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  
    
    INDEX idx_balance (balance),
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_active (is_active)
);

CREATE TABLE IF NOT EXISTS user_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_token (user_id, token_hash),
    INDEX idx_expires (expires_at)
);

CREATE TABLE IF NOT EXISTS holdings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(25, 15) NOT NULL DEFAULT 0.000000000000000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_symbol (user_id, symbol),
    INDEX idx_user_id (user_id),
    CONSTRAINT chk_quantity_positive CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS trades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    user_id INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(25, 15) NOT NULL,
    price DECIMAL(20, 10) NOT NULL,
    profit_loss DECIMAL(20, 10) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_timestamp (user_id, timestamp),
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

-- demo data
-- danko2003 -> password username -> demo
INSERT INTO users (id, balance, username, email, password_hash, first_name, last_name, is_active) 
VALUES (1, 1000.0000000000, 'demo', 'demo@cryptobot.com', '$2a$10$zrj8mCs8Jv8tXJieO2F.zeWAI2evCyv6RqzBMY90ol.N9IKCRuwya', 'Demo', 'User', TRUE)
ON DUPLICATE KEY UPDATE 
    balance = CASE WHEN balance = 0 THEN 1000.0000000000 ELSE balance END,
    username = COALESCE(username, 'demo'),
    email = COALESCE(email, 'demo@cryptobot.com'),
    password_hash = COALESCE(password_hash, '$2a$10$zrj8mCs8Jv8tXJieO2F.zeWAI2evCyv6RqzBMY90ol.N9IKCRuwya'),
    first_name = COALESCE(first_name, 'Demo'),
    last_name = COALESCE(last_name, 'User'),
    is_active = COALESCE(is_active, TRUE);

INSERT INTO holdings (user_id, symbol, quantity) VALUES (1, 'bitcoin', 0.25)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);