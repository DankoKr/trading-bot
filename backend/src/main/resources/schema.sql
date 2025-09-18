CREATE TABLE IF NOT EXISTS accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    balance DECIMAL(20, 10) NOT NULL
);

CREATE TABLE IF NOT EXISTS holdings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    quantity DECIMAL(20, 10) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE TABLE IF NOT EXISTS trades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    account_id INT NOT NULL,
    action VARCHAR(4) NOT NULL, -- 'BUY', 'SELL', 'HOLD'
    symbol VARCHAR(10) NOT NULL,
    quantity DECIMAL(20, 10) NOT NULL,
    price DECIMAL(20, 10) NOT NULL,
    profit_loss DECIMAL(20, 10),
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

INSERT INTO accounts (balance) VALUES (1000.00);