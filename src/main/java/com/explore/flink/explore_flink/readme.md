#### Real Time E-Commerce Order processing & Fraud Detection using Apache Flink

- Multi Source Data Ingestion
- Stateful Stream Processing
- Temporal Joins
- Windowed Aggregations
- Complex Event Processing (CEP)
- Dual Writes Prevention
- Exactly Once Semantics

-----------

**Functional**
- Ingest Orders from Web/Mobile/POS Systems via Kafka
- Enrich Orders with Customer Profile Data
- Detect Fraudulent Patterns in real time (velocity checks, anomaly detection)
- Calculate real time metrics (GMV, conversion rates, cart abandonment)
- Generate Alerts for Suspicious Activities
- Maintain Materialized views for Dashboards


-----

Data Sources:
- Order Stream (Kafka)
- Customer Dimension (PostgreSQL CDC via Debezium)
- Product Catalog (REST API)
- Payment Events (Kafka)


Processing Layer:
- Order Enrichment Job
- Fraud Detection Job
- Real-time Analytics Job
- Sessionization Job

Sinks:
- Fraud Alerts (Kafka -> Alerting System)
- Enriched Orders (Kafka -> Data Warehouse)
- Metrics (InfluxDB/Prometheus)
- Materialized Views (PostgreSQL)

Flow Diagram 
![flow_diagram](https://user-images.githubusercontent.com/123456789/flow_diagram.png)

***Environment Setup:***
- Zookeeper
- Kafka 
- Kafka UI
- Flink Job Manager
- Flink Task Manager
- PostgreSQL (for CDC)
- KSQL Server
- KSQL DB
- Flink SQL Client

Steps 
1) Create Directories for Kafka and Flink
   1) checkpoints
   2) savepoints
   3) jobs
   4) connectors
   5) generator
   6) init_db (./init_db/init_db.sql)
   7) sql_client (./sql_client/sql-client-config.yaml)

2) Execute download-connectors.sh 



```declarative
docker-compose up -d

docker-compose ps 



docker-compose logs postgres

---logs should have following 

postgres  | server started
postgres  | CREATE DATABASE
postgres  |
postgres  |
postgres  | /usr/local/bin/docker-entrypoint.sh: running /docker-entrypoint-initdb.d/init-db.sql
postgres  | CREATE TABLE
postgres  | INSERT 0 5
postgres  | CREATE TABLE
postgres  | CREATE TABLE
postgres  | CREATE TABLE
postgres  |

```

4) Create Topics

```declarative
./setup-kafka-topics.sh
```

5) Run data generator

6) 