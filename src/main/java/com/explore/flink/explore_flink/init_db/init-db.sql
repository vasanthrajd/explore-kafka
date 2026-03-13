-- Customer dimension table
CREATE TABLE customers (
    customer_id VARCHAR(50) PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    risk_score INTEGER DEFAULT 0,
    account_status VARCHAR(50) DEFAULT 'ACTIVE'
);

-- Insert sample customer data
INSERT INTO customers (customer_id, customer_name, email, phone, risk_score, account_status) VALUES
('C001', 'John Doe', 'john.doe@email.com', '+1-555-0101', 10, 'ACTIVE'),
('C002', 'Jane Smith', 'jane.smith@email.com', '+1-555-0102', 5, 'ACTIVE'),
('C003', 'Bob Johnson', 'bob.johnson@email.com', '+1-555-0103', 75, 'WATCH_LIST'),
('C004', 'Alice Williams', 'alice.williams@email.com', '+1-555-0104', 8, 'ACTIVE'),
('C005', 'Charlie Brown', 'charlie.brown@email.com', '+1-555-0105', 3, 'ACTIVE');

-- Order processing results table
CREATE TABLE processed_orders (
    order_id VARCHAR(100) PRIMARY KEY,
    customer_id VARCHAR(50),
    product_id VARCHAR(50),
    amount DECIMAL(10, 2),
    payment_method VARCHAR(50),
    customer_risk_score INTEGER,
    processing_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fraud alerts table
CREATE TABLE fraud_alerts (
    alert_id VARCHAR(100) PRIMARY KEY,
    alert_type VARCHAR(50),
    order_id VARCHAR(100),
    customer_id VARCHAR(50),
    reason TEXT,
    risk_level VARCHAR(20),
    details TEXT,
    alert_time TIMESTAMP
);

-- Real-time metrics table
CREATE TABLE order_metrics (
    window_start TIMESTAMP,
    window_end TIMESTAMP,
    order_count BIGINT,
    unique_customers BIGINT,
    total_gmv DECIMAL(15, 2),
    avg_order_value DECIMAL(10, 2),
    PRIMARY KEY (window_start, window_end)
);