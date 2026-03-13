# Kafka Production Scenarios & Practice Guide

## 1: Real-Time Production Scenarios

### Scenario 1: E-Commerce Order Processing Pipeline
**Business Context**: Handle order placement, payment processing, inventory updates, and notifications

**Components**:
- **Topics**: `orders`, `payments`, `inventory-updates`, `notifications`, `order-status`
- **Producers**: Order Service, Payment Gateway
- **Consumers**: Inventory Service, Notification Service, Analytics Service
- **Kafka Streams**: Order enrichment, fraud detection

**Challenges to Replicate**:
1. Exactly-once semantics for payment processing
2. Handle out-of-order messages (payment before order confirmation)
3. Dead letter queue for failed payment processing
4. Idempotent producer to prevent duplicate orders
5. Transactional writes across multiple topics
6. Consumer lag monitoring and auto-scaling

**Key Configuration**:
```properties
# Producer - Idempotent & Transactional
enable.idempotence=true
transactional.id=order-processor-1
acks=all
retries=Integer.MAX_VALUE
max.in.flight.requests.per.connection=5

# Consumer - Exactly Once
isolation.level=read_committed
enable.auto.commit=false
```

---

### Scenario 2: Real-Time Clickstream Analytics
**Business Context**: Process user events, sessionization, real-time dashboards

**Components**:
- **Topics**: `user-clicks`, `page-views`, `user-sessions`, `aggregated-metrics`
- **High Volume**: 100K+ events/second
- **Kafka Streams**: Windowing, aggregation, sessionization

**Challenges to Replicate**:
1. High-throughput producer optimization (batching, compression)
2. Partition key strategy for even distribution
3. Time-based windowing (tumbling, hopping, session windows)
4. Late-arriving data handling
5. State store management and recovery
6. Handling hot partitions

**Key Configuration**:
```properties
# High Throughput Producer
batch.size=32768
linger.ms=20
compression.type=snappy
buffer.memory=67108864

# Stream Processing
num.stream.threads=4
state.dir=/var/kafka-streams
```

---

### Scenario 3: IoT Sensor Data Processing
**Business Context**: Process millions of sensor readings, detect anomalies, trigger alerts

**Components**:
- **Topics**: `sensor-readings`, `anomalies`, `alerts`, `aggregated-data`
- **Producers**: IoT Gateways (multiple devices)
- **Consumers**: Anomaly Detection Service, Time-Series DB Writer

**Challenges to Replicate**:
1. Handling network partitions and rebalancing
2. Dealing with slow consumers
3. Custom partitioner for sensor grouping
4. Throttling and backpressure
5. Consumer group scaling
6. Offset management strategies

**Key Configuration**:
```properties
# Consumer Tuning
max.poll.records=500
fetch.min.bytes=50000
fetch.max.wait.ms=500
session.timeout.ms=30000
heartbeat.interval.ms=10000
```

---

### Scenario 4: Financial Transaction Processing
**Business Context**: Banking transactions, fraud detection, compliance

**Components**:
- **Topics**: `transactions`, `fraud-scores`, `approved-transactions`, `rejected-transactions`
- **Requirements**: Zero data loss, audit trails, regulatory compliance

**Challenges to Replicate**:
1. Guaranteed message ordering per account
2. Exactly-once processing for financial accuracy
3. Audit log implementation
4. Handling duplicate detection
5. Request-response pattern with Kafka
6. Long-running transaction coordination

**Key Configuration**:
```properties
# Zero Data Loss
min.insync.replicas=2
replication.factor=3
unclean.leader.election.enable=false
acks=all

# Consumer
max.poll.interval.ms=600000
```

---

### Scenario 5: Log Aggregation from Microservices
**Business Context**: Centralized logging, log analysis, alerting

**Components**:
- **Topics**: `app-logs`, `error-logs`, `audit-logs`, `metrics`
- **Producers**: 50+ microservices
- **Consumers**: ELK Stack, Monitoring Tools

**Challenges to Replicate**:
1. Schema evolution and compatibility
2. Multi-datacenter replication
3. Topic compaction for latest state
4. Custom serializers/deserializers
5. SSL/SASL authentication
6. ACL and security

**Key Configuration**:
```properties
# Schema Registry
value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
schema.registry.url=http://localhost:8081

# Log Compaction
cleanup.policy=compact
min.cleanable.dirty.ratio=0.5
```

---

## Part 2: Important Scenarios & Code Snippets

### Producer Scenarios

#### Scenario 1: Handling Producer Retries and Duplicate Prevention
How do you ensure a message is sent exactly once even with network failures?

**Practice Implementation**:
```java
Properties props = new Properties();
props.put("enable.idempotence", "true");
props.put("acks", "all");
props.put("retries", Integer.MAX_VALUE);
props.put("max.in.flight.requests.per.connection", "5");

// Idempotent producer prevents duplicates
KafkaProducer<String, String> producer = new KafkaProducer<>(props);
```

**Scenario to Test**:
- Simulate network failures during send
- Verify no duplicates in consumer
- Test with broker failures

---

#### Scenario 2: Custom Partitioner Implementation
When would you use a custom partitioner and how?

**Practice Implementation**:
```java
public class AccountPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                        Object value, byte[] valueBytes, Cluster cluster) {
        String accountId = (String) key;
        // Ensure same account always goes to same partition
        int numPartitions = cluster.partitionCountForTopic(topic);
        return Math.abs(accountId.hashCode()) % numPartitions;
    }
}
```

**Scenario to Test**:
- Verify ordering within same account
- Test load distribution
- Handle partition expansion

---

#### Scenario 3: Transactional Producer
"Implement atomic writes across multiple topics"

**Practice Implementation**:
```java
producer.initTransactions();
try {
    producer.beginTransaction();
    producer.send(new ProducerRecord<>("orders", orderId, order));
    producer.send(new ProducerRecord<>("inventory", productId, update));
    producer.commitTransaction();
} catch (Exception e) {
    producer.abortTransaction();
}
```

**Scenario to Test**:
- Partial failures
- Transaction timeout
- Consumer reading committed vs uncommitted

---

### Consumer Scenarios

#### Scenario 4: Manual Offset Management
"When and how do you manually manage offsets?"

**Practice Implementation**:
```java
Properties props = new Properties();
props.put("enable.auto.commit", "false");

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        processRecord(record); // Process and save to DB atomically
        
        // Manual commit after DB write
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        offsets.put(
            new TopicPartition(record.topic(), record.partition()),
            new OffsetAndMetadata(record.offset() + 1)
        );
        consumer.commitSync(offsets);
    }
}
```

**Scenario to Test**:
- Database failures
- Exactly-once processing
- Replay from specific offset

---

#### Scenario 5: Consumer Rebalancing Handling
"How do you handle rebalancing gracefully?"

**Practice Implementation**:
```java
consumer.subscribe(Arrays.asList("orders"), new ConsumerRebalanceListener() {
    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        // Commit offsets before losing partitions
        consumer.commitSync();
        // Clean up resources
    }
    
    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        // Initialize state for new partitions
        // Seek to specific offset if needed
    }
});
```

**Scenario to Test**:
- Add/remove consumers
- Consumer crashes
- Partition reassignment

---

#### Scenario 6: Handling Slow Consumer (Consumer Lag)
"Consumer is falling behind. How do you diagnose and fix?"

**Practice Implementation**:
```java
// Monitoring lag
Map<TopicPartition, Long> endOffsets = consumer.endOffsets(assignedPartitions);
Map<TopicPartition, OffsetAndMetadata> committedOffsets = 
    consumer.committed(assignedPartitions);

// Calculate lag
for (TopicPartition partition : assignedPartitions) {
    long lag = endOffsets.get(partition) - 
               committedOffsets.get(partition).offset();
    if (lag > THRESHOLD) {
        alert("High lag on partition: " + partition);
    }
}

// Solutions:
// 1. Increase max.poll.records
// 2. Reduce processing time
// 3. Add more consumer instances
// 4. Optimize fetch.min.bytes and fetch.max.wait.ms
```

---

### Kafka Streams Scenarios

#### Scenario 7: Windowed Aggregation
"Calculate page view counts per user in 5-minute windows"

**Practice Implementation**:
```java
StreamsBuilder builder = new StreamsBuilder();

KStream<String, PageView> views = builder.stream("page-views");

KTable<Windowed<String>, Long> windowedCounts = views
    .groupByKey()
    .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
    .count();

windowedCounts.toStream()
    .map((windowedKey, count) -> 
        KeyValue.pair(windowedKey.key(), count))
    .to("view-counts");
```

**Scenario to Test**:
- Late-arriving events
- Window retention
- Grace period handling

---

#### Scenario 8: Stream-Table Join
"Enrich order events with customer data"

**Practice Implementation**:
```java
KStream<String, Order> orders = builder.stream("orders");
KTable<String, Customer> customers = builder.table("customers");

KStream<String, EnrichedOrder> enriched = orders.join(
    customers,
    (order, customer) -> new EnrichedOrder(order, customer)
);
```

**Scenario to Test**:
- Customer data updates
- Missing customer records
- State store size management

---

#### Scenario 9: Error Handling in Streams
"Handle poison pills and processing errors"

**Practice Implementation**:
```java
StreamsBuilder builder = new StreamsBuilder();

KStream<String, String>[] branches = builder.stream("input")
    .branch(
        (key, value) -> isValid(value),  // Valid messages
        (key, value) -> true             // Invalid messages
    );

branches[0].process(() -> new Processor<String, String>() {
    @Override
    public void process(String key, String value) {
        try {
            // Process message
        } catch (Exception e) {
            // Send to DLQ
            context().forward(key, value, To.child("error-topic"));
        }
    }
});

branches[1].to("dead-letter-queue");
```

---

## Other Hands-On: Hands-On Practice Plan

### Week 1: Producer Mastery
1. **Day 1-2**: Implement idempotent producer, test retry scenarios
2. **Day 3-4**: Custom partitioner, test ordering guarantees
3. **Day 5-6**: Transactional producer, multi-topic atomic writes
4. **Day 7**: Performance tuning (batching, compression, throughput)

### Week 2: Consumer Mastery
1. **Day 1-2**: Manual offset management, exactly-once processing
2. **Day 3-4**: Rebalancing scenarios, partition assignment
3. **Day 5-6**: Consumer lag monitoring and resolution
4. **Day 7**: Multi-threaded consumers, consumer groups

### Week 3: Kafka Streams
1. **Day 1-2**: Stateless operations (map, filter, branch)
2. **Day 3-4**: Stateful operations (aggregations, windowing)
3. **Day 5-6**: Joins (stream-stream, stream-table, table-table)
4. **Day 7**: Error handling, state store management

### Week 4: Production Scenarios
1. **Day 1-2**: E-commerce order pipeline (end-to-end)
2. **Day 3-4**: Clickstream analytics with high throughput
3. **Day 5-6**: IoT sensor processing with anomaly detection
4. **Day 7**: Integration testing, failure scenarios

---

## Part 4: Testing Checklist

### Producer Testing
- [ ] Send messages with acks=0, 1, all and observe behavior
- [ ] Kill broker during send, verify retry behavior
- [ ] Test idempotence with duplicate sends
- [ ] Measure throughput with different batch sizes
- [ ] Test custom serializers (Avro, Protobuf, JSON)
- [ ] Implement callbacks and error handling

### Consumer Testing
- [ ] Consume with auto-commit enabled/disabled
- [ ] Test consumer group coordination
- [ ] Simulate slow processing, observe rebalancing
- [ ] Test seek operations (to beginning, end, specific offset)
- [ ] Implement dead letter queue pattern
- [ ] Test partition assignment strategies

### Kafka Streams Testing
- [ ] Test topology with TopologyTestDriver
- [ ] Verify state store persistence
- [ ] Test windowing with out-of-order data
- [ ] Implement interactive queries on state stores
- [ ] Test recovery from failures
- [ ] Monitor stream metrics and lag

### Operational Testing
- [ ] Monitor broker metrics (CPU, disk, network)
- [ ] Test rolling restarts
- [ ] Simulate network partitions
- [ ] Test disaster recovery procedures
- [ ] Implement monitoring and alerting
- [ ] Performance benchmarking (throughput, latency)

---

## Part 5: Tools & Setup

### Monitoring Tools
- Kafka Manager / AKHQ for UI
- Prometheus + Grafana for metrics
- Kafka Consumer Groups CLI for lag monitoring
- JMX for JVM metrics

---

## Additional Resources
- Test with Kafka's built-in performance tools: `kafka-producer-perf-test`, `kafka-consumer-perf-test`
- Use Testcontainers for integration tests
- Practice with Confluent's Kafka tutorials
- Read "Kafka: The Definitive Guide"
- Explore Kafka Improvement Proposals (KIPs)