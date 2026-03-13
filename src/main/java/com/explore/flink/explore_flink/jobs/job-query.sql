-- Job 1: Order Enrichment with Customer Data
INSERT INTO enriched_orders_kafka
SELECT
  o.order_id,
  o.customer_id,
  COALESCE(c.customer_name, 'Unknown') AS customer_name,
  COALESCE(c.risk_score, 0) AS customer_risk_score,
  o.product_id,
  o.amount,
  o.payment_method,
  o.ip_address,
  o.event_time
FROM orders o
LEFT JOIN customer_dim FOR SYSTEM_TIME AS OF o.event_time AS c
  ON o.customer_id = c.customer_id;

-- Job 2: Velocity-Based Fraud Detection
-- Alert on customers making >3 orders in 5 minutes
INSERT INTO fraud_alerts_kafka
SELECT
  CONCAT('VEL_', customer_id, '_', CAST(UNIX_TIMESTAMP(CAST(window_start AS STRING)) AS STRING)) AS alert_id,
  'HIGH_VELOCITY' AS alert_type,
  order_ids AS order_id,
  customer_id,
  CONCAT('Customer made ', CAST(order_count AS STRING), ' orders in 5 minutes') AS reason,
  CASE
    WHEN order_count >= 5 THEN 'CRITICAL'
    WHEN order_count >= 3 THEN 'HIGH'
    ELSE 'MEDIUM'
  END AS risk_level,
  CONCAT('Total amount: $', CAST(total_amount AS STRING), ', Orders: ', order_ids) AS details,
  window_end AS alert_time
FROM (
  SELECT
    customer_id,
    COUNT(*) AS order_count,
    SUM(amount) AS total_amount,
    LISTAGG(order_id, ',') AS order_ids,
    window_start,
    window_end
  FROM TABLE(
    TUMBLE(TABLE orders, DESCRIPTOR(event_time), INTERVAL '5' MINUTES)
  )
  GROUP BY customer_id, window_start, window_end
  HAVING COUNT(*) > 3
);

-- Job 3: High-Risk Customer Alert
-- Alert on orders from customers with risk_score > 50
INSERT INTO fraud_alerts_kafka
SELECT
  CONCAT('RISK_', o.order_id) AS alert_id,
  'HIGH_RISK_CUSTOMER' AS alert_type,
  o.order_id,
  o.customer_id,
  CONCAT('Customer risk score: ', CAST(c.risk_score AS STRING)) AS reason,
  CASE
    WHEN c.risk_score >= 75 THEN 'CRITICAL'
    WHEN c.risk_score >= 50 THEN 'HIGH'
    ELSE 'MEDIUM'
  END AS risk_level,
  CONCAT('Amount: $', CAST(o.amount AS STRING), ', Account status: ', c.account_status) AS details,
  o.event_time AS alert_time
FROM orders o
JOIN customer_dim FOR SYSTEM_TIME AS OF o.event_time AS c
  ON o.customer_id = c.customer_id
WHERE c.risk_score > 50;

-- Job 4: Large Transaction Alert
-- Alert on individual orders > $1000
INSERT INTO fraud_alerts_kafka
SELECT
  CONCAT('LARGE_', order_id) AS alert_id,
  'LARGE_TRANSACTION' AS alert_type,
  order_id,
  customer_id,
  CONCAT('Large transaction amount: $', CAST(amount AS STRING)) AS reason,
  CASE
    WHEN amount >= 5000 THEN 'CRITICAL'
    WHEN amount >= 2000 THEN 'HIGH'
    ELSE 'MEDIUM'
  END AS risk_level,
  CONCAT('Payment method: ', payment_method, ', IP: ', ip_address) AS details,
  event_time AS alert_time
FROM orders
WHERE amount > 1000;

-- Job 5: Real-time Order Metrics (1-minute windows)
INSERT INTO order_metrics_db
SELECT
  window_start,
  window_end,
  COUNT(*) AS order_count,
  COUNT(DISTINCT customer_id) AS unique_customers,
  SUM(amount) AS total_gmv,
  AVG(amount) AS avg_order_value
FROM TABLE(
  TUMBLE(TABLE orders, DESCRIPTOR(event_time), INTERVAL '1' MINUTE)
)
GROUP BY window_start, window_end;

-- Job 6: Persist fraud alerts to database
INSERT INTO fraud_alerts_db
SELECT * FROM fraud_alerts_kafka;
