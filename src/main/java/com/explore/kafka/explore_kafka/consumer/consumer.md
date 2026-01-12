
#### When a new consumer joins a consumer group, Kafka assigns partitions to the new consumer and may reassign partitions from existing consumers to balance the load. This process is known as rebalancing.
Sequence of Message when a new consumer under a Consumer Group.

	Discovery of Co-ordinator 
	|
	|
	Request to join the group with Client-id and unique id 
	|
	|
	Successful joining information and Notifying assignor about new assignment
	|
	|
	Partition allocation
	|
	|
	Sync the ifnormation about Current Offset. 
	|
	|
	Start Receiving Message from Topic.

```
[Consumer clientId=CA2-0, groupId=consumer-group-101] Cluster ID: U3JyDuVCSOyb32URFGkklg
[Consumer clientId=CA2-0, groupId=consumer-group-101] Discovered group coordinator MY_MACHINE.com:9094 (id: 2147483546 rack: null isFenced: false)
[Consumer clientId=CA2-0, groupId=consumer-group-101] (Re-)joining group
[Consumer clientId=CA2-0, groupId=consumer-group-101] Request joining group due to: need to re-join with the given member-id: CA2-0-33574914-d953-448e-add8-4922a5001065
[Consumer clientId=CA2-0, groupId=consumer-group-101] (Re-)joining group
[Consumer clientId=CA2-0, groupId=consumer-group-101] Successfully joined group with generation Generation{generationId=16, memberId='CA2-0-33574914-d953-448e-add8-4922a5001065', protocol='range'}
[Consumer clientId=CA2-0, groupId=consumer-group-101] Successfully synced group in generation Generation{generationId=16, memberId='CA2-0-33574914-d953-448e-add8-4922a5001065', protocol='range'}
[Consumer clientId=CA2-0, groupId=consumer-group-101] Notifying assignor about the new Assignment(partitions=[order-events-2])
[Consumer clientId=CA2-0, groupId=consumer-group-101] Adding newly assigned partitions: [order-events-2]
Setting offset for partition order-events-2 to the committed offset FetchPosition{offset=19, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[MY_MACHINE.com:9096 (id: 103 rack: null isFenced: false)], epoch=42}}
consumer-group-101: partitions assigned: [order-events-2]
```

#### Assigning Cu


#### Assigning Deserializers for Value

If we don't set trusted packages, we may get the following error:

Example 
```
org.apache.kafka.common.errors.SerializationException: Error deserializing object of type class com.explore.kafka.explore_kafka.model.OrderEvent
Caused by: java.lang.IllegalArgumentException: The class 'com.explore.kafka.explore_kafka.consumer.dto.OrderEvent' is not in the trusted packages: [java.util, java.lang]. If you believe this class is safe to deserialize, please provide its name. If the serialization is only done by a trusted source, you can also enable trust all (*).
```
To fix this, we need to set the trusted packages in the consumer configuration.

1) By Properties (recommended way)
```java
#Trust a specific package
spring.kafka.consumer.properties.spring.json.trusted.packages=com.explore.kafka.explore_kafka.consumer.dto

# OR trust all packages (not recommended for production)
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```
2) By Java Configuration in Consumer Factory (flexible way)
```declarative
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.JsonDeserializer");
props.put("spring.json.trusted.packages", "com.explore.kafka.explore_kafka.model");
```
3) Direct Deserializer Configuration
```declarative
JsonDeserializer<OrderEvent> deserializer = new JsonDeserializer<>(OrderEvent.class);
    deserializer.addTrustedPackages("com.explore.kafka.explore_kafka.consumer.dto");

    return new DefaultKafkaConsumerFactory<>(
        consumerConfigs(), 
        new StringDeserializer(), 
        deserializer
    );
```

Important Considerations: 
1) Case Sensitivity: Ensure that the package names are case-sensitive and match exactly with the class package.
2) Security Implications: Be cautious when using wildcard (*) to trust all packages, as it
3) Mapping:  If the producer and consumer have the class in different packages, you should use Type Mappings instead of just trusting packages to bridge the gap


##### Logs of Poison Pill Scenario
```

2025-12-20T10:21:11.915+05:30 ERROR 28124 --- [ntainer#1-0-C-1] o.a.k.c.c.internals.CompletedFetch       : [Consumer clientId=CA1-0, groupId=consumer-group-101] Value Deserializers with error: Deserializers{keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer@30079da7, valueDeserializer=org.springframework.kafka.support.serializer.JacksonJsonDeserializer@64e2a646}
2025-12-20T10:21:11.915+05:30 ERROR 28124 --- [ntainer#1-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : Consumer exception

java.lang.IllegalStateException: This error handler cannot process 'SerializationException's directly; please consider configuring an 'ErrorHandlingDeserializer' in the value and/or key deserializer
	at org.springframework.kafka.listener.DefaultErrorHandler.handleOtherException(DefaultErrorHandler.java:192) ~[spring-kafka-4.0.0.jar:4.0.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.handleConsumerException(KafkaMessageListenerContainer.java:1999) ~[spring-kafka-4.0.0.jar:4.0.0]
	at org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer.run(KafkaMessageListenerContainer.java:1397) ~[spring-kafka-4.0.0.jar:4.0.0]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run$$$capture(CompletableFuture.java:1804) ~[na:na]
	at java.base/java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

```


##### What is a poison pill?
Before we deep dive into the code and learn how to protect our Kafka applications against poison pills, let’s look into the definition first:

A poison pill (in the context of Kafka) is a record that has been produced to a Kafka topic and always fails when consumed, no matter how many times it is attempted.

So a poison pill can come in different forms:

1) A corrupted record (I have never encountered this myself using Kafka)
2) A deserialization failure

Saviour is: Spring Kafka to the rescue! Configure the ErrorHandlingDeserializer.

The consumer of the topic should configure the correct deserializer to be able to deserialize the bytes of the producer’s serialized Java object.

As long as both the producer and the consumer are using the same compatible serializers and deserializers, everything works fine.

You will end up in a poison pill scenario when the producer serializer and the consumer(s) deserializer are incompatible. This incompatibility can occur in both key and value deserializers.

##### Experimentation
1) One topic
2) Messages of different formats 
   1) Plain string
   2) JSON

3) Two @KafkaListeners on the same topic
   1) One expects String
   2) One expects JSON

4) Same consumer group (implicitly or explicitly)

Is it Standard: NO [Kafka delivers a message to exactly ONE consumer instance per consumer group per partition]

Reason: Kafka topics are schema-homogeneous by design.
One topic = one message contract

Even if we wanted to achieve this then we need to follow any one 
1) Single Listener + Dynamic Dispatch (Recommended)
2) Headers-Based Routing (Cleaner & Scalable) 
3) Two Consumer Groups (Broadcast Behavior)

Anti-Pattern (Do NOT Do This)

Anti-pattern |	Why |
--- | --- |
Mixed schemas in same topic	| Breaks consumers
Multiple listeners expecting different payloads	| Deserialization failure
Relying on ErrorHandlingDeserializer for routing| 	Not its purpose
Catching exceptions inside listener	| Too late

Correct Kafka Design Principle (Important)

Kafka topics should be schema-homogeneous

If schemas differ:

1) Use different topics
2) Or use headers / envelope pattern
3) Or use Schema Registry with union schemas