
### Deploy the connector 

1. Create the connector
```declarative
curl POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @postgres-orders-source.json
curl POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @postgres-orders-source-raw.json
```
Result: 
```declarative

```

2. Verify the connector is running
```declarative
curl -X GET http://localhost:8083/connectors/postgres-source-connector/status
```
Result:
```declarative
{
  "name": "postgres-orders-source",
  "connector": {
    "state": "RUNNING",
    "worker_id": "connect:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "connect:8083"
    }
  ],
  "type": "source"
}
```

3. Check the snapshot phase
```declarative
# List Kafka topics
docker exec -it <kafka-container-id> kafka-topics --bootstrap-server localhost:9092 --list
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Result: 
```declarative
__consumer_offsets
_schemas
connect-configs
connect-offsets
connect-status
postgres.public.orders
```

4. Consume messages from the topic
```declarative
# Consume from beginning
docker exec -it <kafka-container-id> kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic postgres.public.orders \
--from-beginning \
--max-messages 5

docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic postgres.public.orders --from-beginning --max-messages 5

```

Result : 
```declarative
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"},{"type":"string","optional":true,"field":"__deleted"}],"optional":false,"name":"postgres.public.orders.Value"},"payload":{"order_id":1,"customer_name":"John Doe","product":"Laptop","quantity":1,"price":"AdTA","order_date":1767610078945635,"status":"COMPLETED","__deleted":"false"}}
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"},{"type":"string","optional":true,"field":"__deleted"}],"optional":false,"name":"postgres.public.orders.Value"},"payload":{"order_id":2,"customer_name":"Jane Smith","product":"Mouse","quantity":2,"price":"CfY=","order_date":1767610078945635,"status":"PENDING","__deleted":"false"}}
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"},{"type":"string","optional":true,"field":"__deleted"}],"optional":false,"name":"postgres.public.orders.Value"},"payload":{"order_id":3,"customer_name":"Bob Johnson","product":"Keyboard","quantity":1,"price":"HUw=","order_date":1767610078945635,"status":"SHIPPED","__deleted":"false"}}
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"},{"type":"string","optional":true,"field":"__deleted"}],"optional":false,"name":"postgres.public.orders.Value"},"payload":{"order_id":4,"customer_name":"Alice Brown","product":"Monitor","quantity":1,"price":"dTA=","order_date":1767610078945635,"status":"PENDING","__deleted":"false"}}
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"},{"type":"string","optional":true,"field":"__deleted"}],"optional":false,"name":"postgres.public.orders.Value"},"payload":{"order_id":5,"customer_name":"Charlie Wilson","product":"Headphones","quantity":3,"price":"E4g=","order_date":1767610078945635,"status":"COMPLETED","__deleted":"false"}}
```

#### Little information about the transformations 
```
"transforms": "unwrap",
"transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState"
```
This will help to extract the actual row data from the Debezium change event envelope, making it easier to work with the data in Kafka topics.

#### Without unwrap Consume from beginning
```
docker exec -it <kafka-container-id> kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic postgres-raw.public.orders \
--from-beginning \
--max-messages 5

docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic postgres-raw.public.orders --from-beginning --max-messages 5

```

Result: 
```declarative
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":1,"customer_name":"John Doe","product":"Laptop","quantity":1,"price":"AdTA","order_date":1767610078945635,"status":"COMPLETED"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767611532462,"snapshot":"first","db":"testdb","sequence":"[null,\"26843720\"]","schema":"public","table":"orders","txId":742,"lsn":26843720,"xmin":null},"op":"r","ts_ms":1767611532655,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":2,"customer_name":"Jane Smith","product":"Mouse","quantity":2,"price":"CfY=","order_date":1767610078945635,"status":"PENDING"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767611532462,"snapshot":"true","db":"testdb","sequence":"[null,\"26843720\"]","schema":"public","table":"orders","txId":742,"lsn":26843720,"xmin":null},"op":"r","ts_ms":1767611532658,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":3,"customer_name":"Bob Johnson","product":"Keyboard","quantity":1,"price":"HUw=","order_date":1767610078945635,"status":"SHIPPED"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767611532462,"snapshot":"true","db":"testdb","sequence":"[null,\"26843720\"]","schema":"public","table":"orders","txId":742,"lsn":26843720,"xmin":null},"op":"r","ts_ms":1767611532659,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":4,"customer_name":"Alice Brown","product":"Monitor","quantity":1,"price":"dTA=","order_date":1767610078945635,"status":"PENDING"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767611532462,"snapshot":"true","db":"testdb","sequence":"[null,\"26843720\"]","schema":"public","table":"orders","txId":742,"lsn":26843720,"xmin":null},"op":"r","ts_ms":1767611532659,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":5,"customer_name":"Charlie Wilson","product":"Headphones","quantity":3,"price":"E4g=","order_date":1767610078945635,"status":"COMPLETED"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767611532462,"snapshot":"last","db":"testdb","sequence":"[null,\"26843720\"]","schema":"public","table":"orders","txId":742,"lsn":26843720,"xmin":null},"op":"r","ts_ms":1767611532659,"transaction":null}}
```

##### Core Raw Change Data Capture Values
```declarative
{
"payload": {
    "before": null,
    "after": {
    "order_id": 1,
    "customer_name": "John Doe",
    "product": "Laptop",
    "quantity": 1,
    "price": "AdTA",
    "order_date": 1767610078945635,
    "status": "COMPLETED"
},
"source": {
    "version": "2.5.4.Final",
    "connector": "postgresql",
    "name": "postgres-raw",
    "ts_ms": 1767611532462,
    "snapshot": "first",
    "db": "testdb",
    "sequence": "[null,\"26843720\"]",
    "schema": "public",
    "table": "orders",
    "txId": 742,
    "lsn": 26843720,
    "xmin": null
    },
    "op": "r",
    "ts_ms": 1767611532655,
    "transaction": null
    }
}
```

**NOTE**: "slot.name": "debezium_orders_slot"
Impact:

Critical for exactly-once semantics
Stores connector's read position in WAL (Write-Ahead Log)
Prevents WAL from being cleaned up before connector reads it
```declarative
 SELECT * FROM pg_replication_slots;
        slot_name         |  plugin  | slot_type | datoid | database | temporary | active | active_pid | xmin | catalog_xmin | restart_lsn | confirmed_flush_lsn | wal_status | safe_wal_size | two_phase
--------------------------+----------+-----------+--------+----------+-----------+--------+------------+------+--------------+-------------+---------------------+------------+---------------+-----------
 debezium_orders_slot     | pgoutput | logical   |  16384 | testdb   | f         | t      |        114 |      |          743 | 0/1999B18   | 0/199A020           | reserved   |               | f
 debezium_orders_slot_raw | pgoutput | logical   |  16384 | testdb   | f         | t      |        116 |      |          743 | 0/1999B18   | 0/199A020           | reserved   |               | f

SELECT
slot_name,
pg_size_pretty(pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn)) as lag_size,
active
FROM pg_replication_slots;
slot_name         |  lag_size  | active
--------------------------+------------+--------
debezium_orders_slot     | 2672 bytes | t
debezium_orders_slot_raw | 2672 bytes | t


-- If connector is removed, manually drop the slot
SELECT pg_drop_replication_slot('debezium_orders_slot');
ERROR:  replication slot "debezium_orders_slot" is active for PID 114

```

```declarative
"snapshot.mode": "initial"
```

Mode|Behavior|When to Use
-----|--------|-----------
initial|Snapshot + streaming (default) | Most common - Capture existing data then stream changes
always|Snapshot on EVERY restart | Reprocess all data each time (rare)
never| Only streaming, no snapshot | Data already in Kafka, only need new changes
initial_only| Snapshot once, then stop | One-time data migration, no ongoing changes
when_needed| Snapshot if no valid offset found | Use when unsure if snapshot is needed, (Restart after long downtime)
exported| Use pg_export_snapshot() | Consistent snapshot across multiple connectors
custom|Custom snapshot query| Partial snapshot with WHERE clause

Common Transform Types 

```declarative
// Routing
"transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter"

// Masking
"transforms.mask.type": "org.apache.kafka.connect.transforms.MaskField$Value"

// Add fields
"transforms.addMeta.type": "org.apache.kafka.connect.transforms.InsertField$Value"

// Flatten nested structures
"transforms.flatten.type": "org.apache.kafka.connect.transforms.Flatten$Value"

// Filter records
"transforms.filter.type": "io.debezium.transforms.Filter"

// Cast types
"transforms.cast.type": "org.apache.kafka.connect.transforms.Cast$Value"
```





Testing the real time changes
a) Inserting new records 
```declarative
docker exec -it confluent-kafka-for-connect-postgres-1 psql -U postgres -d testdb
INSERT INTO orders (customer_name, product, quantity, price, status) VALUES
('David Lee', 'Tablet', 1, 450.00, 'PENDING');
```

Result:
```declarative
{
"payload": {
"before": null,
"after": {
"order_id": 6,
"customer_name": "David Lee",
"product": "Tablet",
"quantity": 1,
"price": "AK/I",
"order_date": 1767857981759757,
"status": "PENDING"
},
"source": {
"version": "2.5.4.Final",
"connector": "postgresql",
"name": "postgres-raw",
"ts_ms": 1767857981760,
"snapshot": "false",
"db": "testdb",
"sequence": "[\"26843872\",\"26844264\"]",
"schema": "public",
"table": "orders",
"txId": 743,
"lsn": 26844264,
"xmin": null
},
"op": "c",
"ts_ms": 1767857981923,
"transaction": null
}
}
```

b) Updating existing records
```declarative
-- Update order status
UPDATE orders SET status = 'SHIPPED' WHERE order_id = 2;
```
**Note**: The field before shows null because of the default configuration of the connector. To capture the before state, you need to enable "include.before.state" in the connector configuration.
Result:
```declarative
{
"payload": {
"before": null,
"after": {
"order_id": 2,
"customer_name": "Jane Smith",
"product": "Mouse",
"quantity": 2,
"price": "CfY=",
"order_date": 1767857660162510,
"status": "SHIPPED"
},
"source": {
"version": "2.5.4.Final",
"connector": "postgresql",
"name": "postgres-raw",
"ts_ms": 1767858287993,
"snapshot": "false",
"db": "testdb",
"sequence": "[\"26845216\",\"26845504\"]",
"schema": "public",
"table": "orders",
"txId": 744,
"lsn": 26845504,
"xmin": null
},
"op": "u",
"ts_ms": 1767858288089,
"transaction": null
}
}

```

c) Deleting records
```declarative
DELETE FROM orders WHERE order_id = 5;
```

Result:
```declarative
{
  "payload": {
    "before": {
      "order_id": 5,
      "customer_name": "",
      "product": "",
      "quantity": 0,
      "price": "AA==",
      "order_date": 0,
      "status": "PENDING"
    },
    "after": null,
    "source": {
      "version": "2.5.4.Final",
      "connector": "postgresql",
      "name": "postgres-raw",
      "ts_ms": 1767859856577,
      "snapshot": "false",
      "db": "testdb",
      "sequence": "[\"26846312\",\"26846600\"]",
      "schema": "public",
      "table": "orders",
      "txId": 745,
      "lsn": 26846600,
      "xmin": null
    },
    "op": "d",
    "ts_ms": 1767859856843,
    "transaction": null
  }
}
```

**Scenario**: Test Offset Management & Fault Tolerance
This tests exactly-once semantics and recovery.

Check Current Offsets (as there are 7 message at the time of testing this data)
```declarative
# Consume connect-offsets topic
docker exec -it <kafka-container-id> kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic connect-offsets \
  --from-beginning \
  --property print.key=true \
  --max-messages 7


docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic connect-offsets --from-beginning --property print.key=true --max-messages 7
```
Output:
```declarative
["postgres-orders-source",{"server":"postgres"}]        {"transaction_id":null,"lsn_proc":26843632,"messageType":"INSERT","lsn":26843632,"txId":742,"ts_usec":1767857660164579}
["postgres-orders-source",{"server":"postgres"}]        {"transaction_id":null,"lsn_proc":26844264,"messageType":"INSERT","lsn_commit":26843872,"lsn":26844264,"txId":743,"ts_usec":1767857981760202}
["postgres-orders-source",{"server":"postgres"}]        {"transaction_id":null,"lsn_proc":26845504,"messageType":"UPDATE","lsn_commit":26845216,"lsn":26845504,"txId":744,"ts_usec":1767858287993923}
["postgres-orders-source-raw",{"server":"postgres-raw"}]        {"transaction_id":null,"lsn_proc":26843632,"messageType":"INSERT","lsn":26843632,"txId":742,"ts_usec":1767857660164579}
["postgres-orders-source-raw",{"server":"postgres-raw"}]        {"transaction_id":null,"lsn_proc":26844264,"messageType":"INSERT","lsn_commit":26843872,"lsn":26844264,"txId":743,"ts_usec":1767857981760202}
["postgres-orders-source-raw",{"server":"postgres-raw"}]        {"transaction_id":null,"lsn_proc":26845504,"messageType":"UPDATE","lsn_commit":26845216,"lsn":26845504,"txId":744,"ts_usec":1767858287993923}
["postgres-orders-source-raw",{"server":"postgres-raw"}]        {"transaction_id":null,"lsn_proc":26846600,"messageType":"DELETE","lsn_commit":26846312,"lsn":26846600,"txId":745,"ts_usec":1767859856577769}
```

Simulate Connector Failure
```declarative
 curl -X PUT http://localhost:8083/connectors/postgres-orders-source/pause

 curl -X PUT http://localhost:8083/connectors/postgres-orders-source-raw/pause

```

Insert Records to the DB while connector is down
```declarative
INSERT INTO orders (customer_name, product, quantity, price) VALUES
('Emma Watson', 'Camera', 1, 800.00),
('Frank Miller', 'Tripod', 2, 120.00);
```

Resume the connector 

```declarative
curl -X PUT http://localhost:8083/connectors/postgres-orders-source/resume
curl -X PUT http://localhost:8083/connectors/postgres-orders-source-raw/resume
```
Consume the message from the topic



```declarative
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":1,"customer_name":"John Doe","product":"Laptop","quantity":1,"price":"AdTA","order_date":1767857660162510,"status":"COMPLETED"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767857660164,"snapshot":"false","db":"testdb","sequence":"[null,\"26842832\"]","schema":"public","table":"orders","txId":742,"lsn":26842832,"xmin":null},"op":"c","ts_ms":1767857660536,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":2,"customer_name":"Jane Smith","product":"Mouse","quantity":2,"price":"CfY=","order_date":1767857660162510,"status":"PENDING"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767857660164,"snapshot":"false","db":"testdb","sequence":"[null,\"26843104\"]","schema":"public","table":"orders","txId":742,"lsn":26843104,"xmin":null},"op":"c","ts_ms":1767857660537,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":3,"customer_name":"Bob Johnson","product":"Keyboard","quantity":1,"price":"HUw=","order_date":1767857660162510,"status":"SHIPPED"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767857660164,"snapshot":"false","db":"testdb","sequence":"[null,\"26843280\"]","schema":"public","table":"orders","txId":742,"lsn":26843280,"xmin":null},"op":"c","ts_ms":1767857660537,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":4,"customer_name":"Alice Brown","product":"Monitor","quantity":1,"price":"dTA=","order_date":1767857660162510,"status":"PENDING"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767857660164,"snapshot":"false","db":"testdb","sequence":"[null,\"26843456\"]","schema":"public","table":"orders","txId":742,"lsn":26843456,"xmin":null},"op":"c","ts_ms":1767857660537,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":5,"customer_name":"Charlie Wilson","product":"Headphones","quantity":3,"price":"E4g=","order_date":1767857660162510,"status":"COMPLETED"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767857660164,"snapshot":"false","db":"testdb","sequence":"[null,\"26843632\"]","schema":"public","table":"orders","txId":742,"lsn":26843632,"xmin":null},"op":"c","ts_ms":1767857660538,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":6,"customer_name":"David Lee","product":"Tablet","quantity":1,"price":"AK/I","order_date":1767857981759757,"status":"PENDING"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767857981760,"snapshot":"false","db":"testdb","sequence":"[\"26843872\",\"26844264\"]","schema":"public","table":"orders","txId":743,"lsn":26844264,"xmin":null},"op":"c","ts_ms":1767857981923,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":2,"customer_name":"Jane Smith","product":"Mouse","quantity":2,"price":"CfY=","order_date":1767857660162510,"status":"SHIPPED"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767858287993,"snapshot":"false","db":"testdb","sequence":"[\"26845216\",\"26845504\"]","schema":"public","table":"orders","txId":744,"lsn":26845504,"xmin":null},"op":"u","ts_ms":1767858288089,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":{"order_id":5,"customer_name":"","product":"","quantity":0,"price":"AA==","order_date":0,"status":"PENDING"},"after":null,"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767859856577,"snapshot":"false","db":"testdb","sequence":"[\"26846312\",\"26846600\"]","schema":"public","table":"orders","txId":745,"lsn":26846600,"xmin":null},"op":"d","ts_ms":1767859856843,"transaction":null}}
null
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":7,"customer_name":"Emma Watson","product":"Camera","quantity":1,"price":"ATiA","order_date":1767861502279856,"status":"PENDING"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767861502290,"snapshot":"false","db":"testdb","sequence":"[\"26847352\",\"26847744\"]","schema":"public","table":"orders","txId":746,"lsn":26847744,"xmin":null},"op":"c","ts_ms":1767861502354,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"default":0,"field":"order_id"},{"type":"string","optional":false,"field":"customer_name"},{"type":"string","optional":false,"field":"product"},{"type":"int32","optional":false,"field":"quantity"},{"type":"bytes","optional":false,"name":"org.apache.kafka.connect.data.Decimal","version":1,"parameters":{"scale":"2","connect.decimal.precision":"10"},"field":"price"},{"type":"int64","optional":true,"name":"io.debezium.time.MicroTimestamp","version":1,"default":0,"field":"order_date"},{"type":"string","optional":true,"default":"PENDING","field":"status"}],"optional":true,"name":"postgres-raw.public.orders.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false,incremental"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"name":"event.block","version":1,"field":"transaction"}],"optional":false,"name":"postgres-raw.public.orders.Envelope","version":1},"payload":{"before":null,"after":{"order_id":8,"customer_name":"Frank Miller","product":"Tripod","quantity":2,"price":"LuA=","order_date":1767861502279856,"status":"PENDING"},"source":{"version":"2.5.4.Final","connector":"postgresql","name":"postgres-raw","ts_ms":1767861502290,"snapshot":"false","db":"testdb","sequence":"[\"26847352\",\"26848816\"]","schema":"public","table":"orders","txId":746,"lsn":26848816,"xmin":null},"op":"c","ts_ms":1767861502357,"transaction":null}}


```

Things to Observe:

Connector resumes from last committed offset
No data loss - both new records appear
Messages arrive in order

If you are running with two different connectors then you will observe failure for postgres-source-connector.json

```declarative
Caused by: org.apache.kafka.connect.errors.DataException: Struct schemas do not match.
tat org.apache.kafka.connect.data.ConnectSchema.validateValue(ConnectSchema.java:249)
tat org.apache.kafka.connect.data.Struct.put(Struct.java:216)
tat org.apache.kafka.connect.data.Struct.put(Struct.java:203)
tat io.debezium.transforms.ExtractNewRecordState.updateValue(ExtractNewRecordState.java:301)
tat io.debezium.transforms.ExtractNewRecordState.addFields(ExtractNewRecordState.java:197)
tat io.debezium.transforms.ExtractNewRecordState.doApply(ExtractNewRecordState.java:144)
tat io.debezium.transforms.AbstractExtractNewRecordState.apply(AbstractExtractNewRecordState.java:109)
tat org.apache.kafka.connect.runtime.TransformationStage.apply(TransformationStage.java:57)
tat org.apache.kafka.connect.runtime.TransformationChain.lambda$apply$0(TransformationChain.java:54)
tat org.apache.kafka.connect.runtime.errors.RetryWithToleranceOperator.execAndRetry(RetryWithToleranceOperator.java:180)
tat org.apache.kafka.connect.runtime.errors.RetryWithToleranceOperator.execAndHandleError(RetryWithToleranceOperator.java:214)
```
What Caused the Error
The Problem 
If we add "transforms.unwrap.add.fields": "op,before" and restarted the connector, 
it tried to write messages with a different schema to the same topic that already contains messages with the old schema.

Solution 1: Reset and Start Fresh (Easiest for Learning)
Solution 2: Use a New Topic (Recommended for Learning Schema Evolution)
Solution 3: Disable Schema Validation (Not Recommended, but Educational)
Solution 4: Use Avro with Schema Registry (Production Approach)

**Test Schema Evolution**:
1. Add a Column
```declarative
ALTER TABLE orders ADD COLUMN delivery_address VARCHAR(200);

UPDATE orders SET delivery_address = '123 Main St' WHERE order_id = 1;

```

New Records by adding additional column

```declarative
INSERT INTO orders (customer_name, product, quantity, price, delivery_address) VALUES
('Grace Hopper', 'Printer', 1, 250.00, '456 Oak Ave');
```
Check the topic for new records with the additional column.
