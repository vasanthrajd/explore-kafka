-- Set processing guarantees
SET 'processing.guarantee' = 'exactly_once';
SET 'auto.offset.reset' = 'earliest';

-- Create transactions stream
CREATE STREAM transactions_stream (
    transaction_id VARCHAR KEY,
    user_id VARCHAR,
    amount DECIMAL(10,2),
    merchant_id VARCHAR,
    location VARCHAR,
    transaction_time BIGINT,
    card_last_four VARCHAR
) WITH (
    KAFKA_TOPIC='transactions',
    VALUE_FORMAT='AVRO',
    PARTITIONS=6,
    REPLICAS=1,
    TIMESTAMP='transaction_time'
);

-- Create user profiles table
CREATE TABLE user_profiles (
    user_id VARCHAR PRIMARY KEY,
    avg_transaction_amount DECIMAL(10,2),
    home_location VARCHAR,
    account_creation_date BIGINT,
    risk_score INT
) WITH (
    KAFKA_TOPIC='user-profiles',
    VALUE_FORMAT='AVRO',
    PARTITIONS=6,
    REPLICAS=1
);

-- Verify creation
SHOW STREAMS;
SHOW TABLES;
