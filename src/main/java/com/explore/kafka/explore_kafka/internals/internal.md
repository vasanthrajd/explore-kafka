##### Zoo Keeper 
ZooKeeper is a centralized service for maintaining configuration information, naming, providing distributed synchronization, and providing group services. It is used by Kafka to manage and coordinate the brokers in the cluster.

##### Registering Broker with Zoo Keeper 

Logs from Zookeeper
```declarative
[2025-12-22 15:37:11,258] INFO Creating /brokers/ids/101 (is it secure? false) (kafka.zk.KafkaZkClient)
[2025-12-22 15:37:11,280] INFO Stat of the created znode at /brokers/ids/101 is: 5316,5316,1766398031271,1766398031271,1,0,0,72058843160707072,226,0,5316
 (kafka.zk.KafkaZkClient)
[2025-12-22 15:37:11,281] INFO Registered broker 101 at path /brokers/ids/101 with addresses: PLAINTEXT://QBX-LT-15.QBrainX.com:9094, czxid (broker epoch): 5316 (kafka.zk.KafkaZkClient)
[2025-12-22 15:37:11,346] INFO [ExpirationReaper-101-topic]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2025-12-22 15:37:11,358] INFO [ExpirationReaper-101-Heartbeat]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2025-12-22 15:37:11,359] INFO [ExpirationReaper-101-Rebalance]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2025-12-22 15:37:11,380] INFO [GroupCoordinator 101]: Starting up. (kafka.coordinator.group.GroupCoordinator)
[2025-12-22 15:37:11,407] INFO [GroupCoordinator 101]: Startup complete. (kafka.coordinator.group.GroupCoordinator)
[2025-12-22 15:37:11,444] INFO [TransactionCoordinator id=101] Starting up. (kafka.coordinator.transaction.TransactionCoordinator)
[2025-12-22 15:37:11,452] INFO [TxnMarkerSenderThread-101]: Starting (kafka.coordinator.transaction.TransactionMarkerChannelManager)
[2025-12-22 15:37:11,452] INFO [TransactionCoordinator id=101] Startup complete. (kafka.coordinator.transaction.TransactionCoordinator)
[2025-12-22 15:37:11,551] INFO [ExpirationReaper-101-AlterAcls]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2025-12-22 15:37:11,586] INFO [/config/changes-event-process-thread]: Starting (kafka.common.ZkNodeChangeNotificationListener$ChangeEventProcessThread)
```

When a Kafka broker starts up, it registers itself with ZooKeeper by creating a znode (ZooKeeper node) under the path `/brokers/ids/<broker_id>`. This znode contains information about the broker, such as its host and port. The registration process allows other brokers and clients to discover the broker's presence in the cluster.

```declarative
PS C:\kafka_2.12-3.9.1> .\bin\windows\zookeeper-shell.bat localhost:2181 ls /brokers/ids
Connecting to localhost:2181

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
[101, 102]
PS C:\kafka_2.12-3.9.1>
```

## 1) TEST HOW REPLICA DATA CAN BE CONSUMED

command to check the replicas of a topic
```declarative
./bin/kafka-topics.sh --describe --topic test-topic --bootstrap-server localhost:9092


Topic: test-topic	PartitionCount: 3	ReplicationFactor: 2	Configs: segment.bytes=1048576```
    Topic: test-topic	Partition: 0	Leader: 101	Replicas: 101,102	Isr: 101,102
    Topic: test-topic	Partition: 1	Leader: 102	Replicas: 102,101	Isr: 102,101
    Topic: test-topic	Partition: 2	Leader: 101	Replicas: 101,102	Isr: 101,102

```

