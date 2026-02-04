----- RULE 1: High-Value Transactions

CREATE STREAM high_value_transactions AS
SELECT
    transaction_id,
    user_id,
    amount,
    merchant_id,
    location,
    transaction_time,
    'HIGH_VALUE' as fraud_type,
    'HIGH' as severity,
    CONCAT('Amount exceeds threshold: $', CAST(amount AS VARCHAR)) as alert_message
FROM transactions_stream
    WHERE amount > 10000
    EMIT CHANGES;

----- RULE 2: Transactions from High-Risk Locations

CREATE TABLE transaction_velocity_5min AS
SELECT
    user_id,
    COUNT(*) AS transaction_count,
    SUM(amount) AS total_amount,
    COLLECT_LIST(transaction_id) AS transaction_ids,
    AS_VALUE(user_id) as user_id_copy,
    WINDOWSTART AS window_start,
    WINDOWEND AS window_end
FROM transactions_stream
    WINDOW TUMBLING (SIZE 5 MINUTES)
    GROUP BY user_id
    EMIT CHANGES;

-- Create velocity-based fraud alerts

CREATE STREAM velocity_fraud_alerts AS
SELECT
    user_id,
    transaction_count,
    total_amount,
    transaction_ids,
    'VELOCITY_FRAUD' as fraud_type,
    CASE
        WHEN total_amount > 10000 OR TRANSACTION_COUNT > 20 THEN 'CRITICAL'
        WHEN total_amount > 5000 OR TRANSACTION_COUNT > 10 THEN 'HIGH'
        ELSE 'MEDIUM'
    END as severity,
    CONCAT('High transaction velocity detected: ', CAST(transaction_count AS VARCHAR), ' transactions totaling $', CAST(total_amount AS VARCHAR), ' in 5 minutes.') as alert_message
FROM transaction_velocity_5min
    WHERE total_amount > 5000 OR TRANSACTION_COUNT > 10
    EMIT CHANGES;


-- NOTE: ABOVE RULE 2 USES A TABLE WITH A TUMBLING WINDOW TO AGGREGATE TRANSACTIONS OVER A 5-MINUTE INTERVAL. CREATING A STREAM FROM THIS TABLE IS NOT ALLOWED FROM WINDOWED TABLES. INSTEAD, WE CAN CREATE A STREAM DIRECTLY FROM THE AGGREGATION.
---- RULE 3: Location Anomaly Detection

CREATE STREAM location_anomalies AS
SELECT

FROM transactions_stream t
LEFT JOIN user_profiles u
    ON t.user_id = u.user_id
WHERE t.location != u.home_location
    AND t.amount > u.avg_transaction_amount * 3 -- 1 day after account creation
    EMIT CHANGES;

---- RULE 4: Amount Anomaly Detection

CREATE STREAM amount_anomalies AS
SELECT
    t.transaction_id,
    t.user_id,
    t.amount,
    u.avg_transaction_amount,
    (t.amount / u.avg_transaction_amount) as deviation_ratio,
    t.transaction_time,
    'AMOUNT_ANOMALY' as fraud_type,
    CASE
        WHEN t.amount > u.avg_transaction_amount * 10 THEN 'CRITICAL'
        WHEN t.amount > u.avg_transaction_amount * 5 THEN 'HIGH'
        ELSE 'MEDIUM'
    END as severity,
    CONCAT('Amount $', CAST(t.amount AS VARCHAR),
           ' is ', CAST((t.amount / u.avg_transaction_amount) AS VARCHAR),
           'x user average') as alert_message
FROM transactions_stream t
INNER JOIN user_profiles u ON t.user_id = u.user_id
WHERE t.amount > u.avg_transaction_amount * 5
EMIT CHANGES;

