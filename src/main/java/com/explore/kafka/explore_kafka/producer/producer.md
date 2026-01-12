
##### JSON as a Value Serializer

```declarative
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.JsonSerializer");
```



##### Producer can push the message to a specific partition by specifying the partition number while creating the ProducerRecord.

```java
new producerRecord<>(topic, partition, key, value);
``` 
##### Custom Partitioner can be created by implementing the Partitioner interface and overriding the partition method to define custom partitioning logic.

```java
public class OrderPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                         Object value, byte[] valueBytes,
                         Cluster cluster) {
        int partitions = cluster.partitionCountForTopic(topic);
        return Math.abs(key.hashCode()) % partitions;
    }
}
```

```declarative
props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, OrderPartitioner.class);
```
Different Partitioning Strategies:

1) Default Partitioning: If a key is provided, Kafka uses a hash of the key to determine the partition. If no key is provided, messages are distributed in a round-robin fashion across all available partitions.
2) Sticky Partitioner: Introduced in Kafka 2.4.0, this partitioner sends all messages in a batch to the same partition to improve throughput. It switches partitions only when a new batch is started.
3) Custom Partitioner: Users can implement their own partitioning logic by creating a custom partitioner class that implements the Partitioner interface.
4) Key-based Partitioning: Messages with the same key are always sent to the same partition, ensuring order for messages with the same key.
5) Manual Partitioning: The producer can explicitly specify the partition number when sending messages, allowing complete control over message distribution.
6) Round-Robin Partitioning: When no key is provided, messages are distributed evenly across all partitions in a round-robin manner.
7) Consistent Hashing: A custom partitioner can implement consistent hashing to ensure that messages are evenly distributed and that the addition or removal of partitions causes minimal disruption to the existing partition assignments.
8) Load-based Partitioning: A custom partitioner can monitor the load on each partition and direct messages to less loaded partitions to balance the workload.
9) Geo-based Partitioning: A custom partitioner can route messages to partitions based on geographic location or other criteria relevant to the application's needs.
10) Time-based Partitioning: A custom partitioner can direct messages to different partitions based on time intervals, such as sending messages to different partitions for different hours of the day.
11) Weighted Partitioning: A custom partitioner can assign weights to partitions and distribute messages based on these weights, allowing for more messages to be sent to higher-capacity partitions.
12) Hybrid Partitioning: A combination of multiple strategies, such as key-based and load-based partitioning, can be implemented in a custom partitioner to meet specific application requirements.


Experiment 

Testing | Result
--- | ---
Send Same Key | Message goes to the same partition
Send Different Key | Message goes to different partitions based on hash value
Kill Broker | (Message fails -Producer and Consumer sees Node not accessible / disconnected)

Problem in kill Broker is (Producer Side Issue) 
1) No leader awareness
2) No fallback
3) No metadata validation 
4) No handling for unavailable partitions
5) This makes your producer non-resilient.

To fix the kill broker, we can enable the following properties in Producer configuration:

1) Leader-Aware Custom Partitioner (Recommended)
Instead of forcing a partition blindly, you must:
    a) check available partitions
    b) Ensure leader exists
    c) Fall back if not
   What This Fixes

-> Handles broker failure
-> Preserves availability
-> Avoids producer crash

Note: Ordering for that key may break during failure only

Consumer Side Issue

1) No handling for unavailable partitions
2) No leader awareness
3) No fallback


1) Let Kafka Retry Automatically
2) Configure Consumer for Graceful Failure Recovery

metadata.max.age.ms=30000
request.timeout.ms=30000
retry.backoff.ms=100
reconnect.backoff.max.ms=1000

spring.kafka.consumer.properties.reconnect.backoff.ms=50
spring.kafka.consumer.properties.reconnect.backoff.max.ms=1000

spring.kafka.consumer.properties.retry.backoff.ms=100
spring.kafka.consumer.request.timeout.ms=30000

spring.kafka.consumer.metadata.max.age.ms=30000


