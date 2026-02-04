1. Stateful Stream Processing with State Stores

State management is the foundation of complex event processing. Enterprise systems require exactly-once semantics and fault-tolerant state.

- Aggregate customer transaction events into running totals using RocksDB-backed state stores
- Implement changelog topics for state recovery
- Configure state store TTL and compaction strategies

Environment Requirement: 

- kafka running in local
- kafka ui to monitor the kafka related information

[create-topics](./create-topics.sh)
```declarative
// create topics 

sh create-topics.sh
```

Response: 
```declarative
Created topic transactions.
Created topic account-balances.
Topics created successfully
```

a) Observe State Aggregation: 
    
- start StatefulTransactionProcessor [StatefulTransactionProcessor](./StatefulTransactionProcessor) which acts as a streams (receive message from topic, process it and produce to another topic)
- start BalanceConsumer [BalanceConsumer](./BalanceConsumer)
- start TransactionProducer [TransactionProducer](./TransactionProducer)

Post running observed few issues 

```declarative
11:47:19.171 [kafka-coordinator-heartbeat-thread | stateful-transaction-processor] INFO org.apache.kafka.clients.consumer.internals.SubscriptionState -- [Consumer clientId=stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2-consumer, groupId=stateful-transaction-processor] Resetting offset for partition transactions-1 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[localhost:9092 (id: 1 rack: null isFenced: false)], epoch=0}}.
11:47:19.273 [kafka-coordinator-heartbeat-thread | stateful-transaction-processor] INFO org.apache.kafka.clients.consumer.internals.SubscriptionState -- [Consumer clientId=stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-1-consumer, groupId=stateful-transaction-processor] Resetting offset for partition transactions-2 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[localhost:9092 (id: 1 rack: null isFenced: false)], epoch=0}}.
11:47:19.273 [kafka-coordinator-heartbeat-thread | stateful-transaction-processor] INFO org.apache.kafka.clients.consumer.internals.SubscriptionState -- [Consumer clientId=stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-1-consumer, groupId=stateful-transaction-processor] Resetting offset for partition transactions-0 to position FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[localhost:9092 (id: 1 rack: null isFenced: false)], epoch=0}}.
11:48:18.630 [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-1] WARN org.apache.kafka.streams.processor.internals.StreamsProducer -- stream-thread [main] Timeout exception caught trying to initialize transactions. The broker is either slow or in bad state (like not having enough replicas) in responding to the request, or the connection to broker was interrupted sending the request or receiving the response. Will retry initializing the task in the next loop. Consider overwriting max.block.ms to a larger value to avoid timeout errors
11:48:18.630 [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] WARN org.apache.kafka.streams.processor.internals.StreamsProducer -- stream-thread [main] Timeout exception caught trying to initialize transactions. The broker is either slow or in bad state (like not having enough replicas) in responding to the request, or the connection to broker was interrupted sending the request or receiving the response. Will retry initializing the task in the next loop. Consider overwriting max.block.ms to a larger value to avoid timeout errors
11:48:18.630 [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] ERROR org.apache.kafka.streams.processor.internals.TaskManager -- stream-thread [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] Get exceptions for the following tasks: {0_1=org.apache.kafka.common.errors.TimeoutException: Timeout expired after 60000ms while awaiting InitProducerId}
11:48:18.630 [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] ERROR org.apache.kafka.streams.KafkaStreams -- stream-client [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404] Encountered the following exception during processing and the registered exception handler opted to SHUTDOWN_CLIENT. The streams client is going to shut down now.
org.apache.kafka.streams.errors.StreamsException: org.apache.kafka.common.errors.TimeoutException: Timeout expired after 60000ms while awaiting InitProducerId
	at org.apache.kafka.streams.processor.internals.TaskManager.maybeThrowTaskExceptions(TaskManager.java:426)
	at org.apache.kafka.streams.processor.internals.TaskManager.addTasksToStateUpdater(TaskManager.java:1009)
	at org.apache.kafka.streams.processor.internals.TaskManager.checkStateUpdater(TaskManager.java:918)
	at org.apache.kafka.streams.processor.internals.StreamThread.checkStateUpdater(StreamThread.java:1433)
	at org.apache.kafka.streams.processor.internals.StreamThread.runOnceWithoutProcessingThreads(StreamThread.java:1239)
	at org.apache.kafka.streams.processor.internals.StreamThread.runLoop(StreamThread.java:926)
	at org.apache.kafka.streams.processor.internals.StreamThread.run(StreamThread.java:886)
Caused by: org.apache.kafka.common.errors.TimeoutException: Timeout expired after 60000ms while awaiting InitProducerId
11:48:18.634 [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] INFO org.apache.kafka.streams.KafkaStreams -- stream-client [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404] State transition from REBALANCING to PENDING_ERROR
11:48:18.636 [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] INFO org.apache.kafka.streams.processor.internals.StreamThread -- stream-thread [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] State transition from PARTITIONS_ASSIGNED to PENDING_SHUTDOWN
11:48:18.636 [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] INFO org.apache.kafka.streams.processor.internals.StreamThread -- stream-thread [stateful-transaction-processor-ea58f7cc-dec9-4502-b563-313915c2d404-StreamThread-2] Shutting down unclean

```

Cause of the issue:
The Problem:

- Configured EXACTLY_ONCE_V2 which requires transactional producers
- Transactional coordination needs properly configured broker settings
- Single-broker Kafka (especially in KRaft mode) has default settings that timeout transaction initialization
- The broker needs to allocate transaction IDs and coordinate state, which requires specific configurations

Impact:

- This would cause complete application failure
- No message processing occurs despite messages being available
- Proper Broker configuration is critical

Resolution Steps:

1) To Stick with Exactly-Once Semantics, then we need to ensure we have proper broker configuration
2) Change from Exactly Once Semantics to At-Least-Once Semantics by updating the Streams configuration

I changed the broker configuration which in turn fixed the issue. Below is the configuration changes made to broker

```declarative
# CRITICAL: Transaction and Exactly-Once Settings
KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
KAFKA_TRANSACTION_MAX_TIMEOUT_MS: 900000
KAFKA_TRANSACTION_ABORT_TIMED_OUT_TRANSACTION_CLEANUP_INTERVAL_MS: 10000
# Request handling timeouts
KAFKA_REQUEST_TIMEOUT_MS: 30000
KAFKA_REPLICA_LAG_TIME_MAX_MS: 30000

# Log settings for better performance
KAFKA_LOG_FLUSH_INTERVAL_MESSAGES: 10000
KAFKA_LOG_FLUSH_INTERVAL_MS: 1000

# Group coordinator settings
KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0

```

2) Verify State Recovery:
Simulate a failure by stopping the StatefulTransactionProcessor and then restarting it. Observe that the state is recovered from the changelog topic and processing resumes correctly.
3) Cleanup:
After verification, stop all components and delete the created topics to clean up the environment.
4) Teardown Topics

```declarative
sh delete-topics.sh
``` 
```declarative
Deleted topic transactions.
Deleted topic account-balances.
Topics deleted successfully
```
