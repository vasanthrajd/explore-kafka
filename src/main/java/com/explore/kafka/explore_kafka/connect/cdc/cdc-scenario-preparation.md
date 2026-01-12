
#### CDC Scenario

### Overview
Change Data Capture (CDC) is a design pattern that captures and tracks changes made to data in

##### Verify Environment

1. Check the list of available connectors:
```declarative
//(windows)
wget http://localhost:8083/connector-plugins
// (linux)
curl http://localhost:8083/connector-plugins
```

Result: 
```declarative
[
{
"class": "com.mongodb.kafka.connect.MongoSinkConnector",
"type": "sink",
"version": "1.11.0"
},
{
"class": "io.confluent.connect.jdbc.JdbcSinkConnector",
"type": "sink",
"version": "10.7.4"
},
{
"class": "com.mongodb.kafka.connect.MongoSourceConnector",
"type": "source",
"version": "1.11.0"
},
{
"class": "io.confluent.connect.jdbc.JdbcSourceConnector",
"type": "source",
"version": "10.7.4"
},
{
"class": "io.debezium.connector.postgresql.PostgresConnector",
"type": "source",
"version": "2.5.4.Final"
},
{
"class": "org.apache.kafka.connect.mirror.MirrorCheckpointConnector",
"type": "source",
"version": "7.6.1-ccs"
},
{
"class": "org.apache.kafka.connect.mirror.MirrorHeartbeatConnector",
"type": "source",
"version": "7.6.1-ccs"
},
{
"class": "org.apache.kafka.connect.mirror.MirrorSourceConnector",
"type": "source",
"version": "7.6.1-ccs"
}
]
```

#### Pre-requisites

1. Ensure Confluent Connect have the following things installed
        i.  Debezium Connector for PostgreSQL
       ii. Debezium-connector-mysql
      iii. confluentinc/kafka-connect-jdbc
       iv. confluentinc/kafka-connect-mongodb

For the scenario listed we will be using above-mentioned connectors.

2. Connect to confluent connect container (as below mentioned version is comptable with kafka )
```declarative
1) docker exec -it confluent-kafka-for-connect-connect-1 -- /bin/sh
2) confluent-hub install --no-prompt debezium/debezium-connector-postgresql:2.5.4
3) confluent-hub install --no-prompt debezium/debezium-connector-mysql:2.5.4
4) confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest
5) confluent-hub install --no-prompt mongodb/kafka-connect-mongodb:latest
```


### Preparation
Step 1: Data Setup = Create a database in Postgres and tables 

```declarative
//Connect to PostgreSQL container
docker exec -it <postgres-container-id> psql -U postgres -d testdb

docker exec -it confluent-kafka-for-connect-postgres-1 psql -U postgres -d testdb
```

sql commands to create table and insert data

```sql
-- Create orders table
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    product VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING'
);

-- Insert initial data
INSERT INTO orders (customer_name, product, quantity, price, status) VALUES
('John Doe', 'Laptop', 1, 1200.00, 'COMPLETED'),
('Jane Smith', 'Mouse', 2, 25.50, 'PENDING'),
('Bob Johnson', 'Keyboard', 1, 75.00, 'SHIPPED'),
('Alice Brown', 'Monitor', 1, 300.00, 'PENDING'),
('Charlie Wilson', 'Headphones', 3, 50.00, 'COMPLETED');

-- Verify data
SELECT * FROM orders;

-- Check PostgreSQL replication settings (must show 'logical')
SHOW wal_level;

-- Exit psql
\q
```
