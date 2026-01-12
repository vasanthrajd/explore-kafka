# Explore-kafka

1)  Cluster: Collections of Brokers (Libraries)
2) Broker (Library): Will have Topic (Book)
3) Topic (Book): Message of the topic will be stored in the partition (volumes of the book)
4) Partition (Volumes of a Book) : message will be stored across the partitions.

```
cluster > broker > topic > partitions of the topic will be distributed across the brokers in the cluster
```

### (Windows) Steps to run Single Node multiple Brokers Kafka Cluster 

1) Download and Install Kafka from the [Apache Kafka website.](https://kafka.apache.org/downloads)
2) Extract the downloaded Kafka package to a directory of your choice.
3) Open multiple terminal windows to run multiple brokers.
4) 
5) In the first terminal, navigate to the Kafka installation directory and start ZooKeeper (Kafka uses ZooKeeper to manage the cluster):
    ```
    .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
    ```
6) Copy the `server.properties` file to create separate configuration files for each broker. For example, create `server-1.properties`, `server-2.properties`, etc.
7) Edit each `server-X.properties` file to set unique broker IDs and ports. For example:
    ```
    broker.id=1
    listeners=PLAINTEXT://:9091 
    log.dirs=D:\\kafka-logs\\server-1
    ```
    ```
    broker.id=2
    listeners=PLAINTEXT://:9092
    log.dirs=D:\\kafka-logs\\server-2
    ```
    ```
    broker.id=3
    listeners=PLAINTEXT://:9093
    log.dirs=D:\\kafka-logs\\server-3
    ```
8) Start each broker in separate terminal windows using the modified configuration files:
    ```
   .\bin\windows\kafka-server-start.bat .\config\server-1.properties
   
    ```
    ```
    .\bin\windows\kafka-server-start.bat .\config\server-2.properties
    ```
    ```
    .\bin\windows\kafka-server-start.bat .\config\server-3.properties
    ```
   
9) Verify that all brokers are running by checking the logs in each terminal window.
10) You now have a single-node Kafka cluster with multiple brokers running. You can create topics
    and produce/consume messages across the brokers as needed.


### Creating a Topic with Multiple Partitions

To create a topic with multiple partitions in your Kafka cluster, follow these steps:
1) Open a terminal window and navigate to the Kafka installation directory.
2) Use the following command to create a topic with a specified number of partitions. Replace `<topic-name>` with your desired topic name and `<num-partitions>` with the number of partitions you

    ```
   bin/kafka-topics.sh --create --topic <topic-name> --bootstrap-server localhost:9091,localhost:9092,localhost:9093 --partitions <num-partitions> --replication-factor 1
    ```
   
### Creating a Consumer
To create a consumer that reads messages from the topic with multiple partitions, follow these steps:
1) Open a terminal window and navigate to the Kafka installation directory.
2) Use the following command to start a consumer for the specified topic. Replace `<topic-name>` with the name of your topic.

    ```
   bin/kafka-console-consumer.sh --topic <topic-name> --bootstrap-server localhost:9091,localhost:9092,localhost:9093 --from-beginning
    ```   
   different approach to read the message from the topic by consumer
   ```
   --from-beginning
   ```
   
   ```declarative
   --partition <partition-id> --offset <offset-number>
   ```

For Connect 

Added docker compose file for kafka with single node multiple brokers setup with single broker

Name|Image|Command|Service|Created|Status|Ports
--|--|--|--|--|--|--
confluent-kafka-for-connect-connect-1 |  confluentinc/cp-kafka-connect:7.6.1 |    "bash -c 'confluent-…"  | connect       |    4 hours ago   |Up 4 hours (healthy) |  0.0.0.0:8083->8083/tcp, [::]:8083->8083/tcp, 9092/tcp
confluent-kafka-for-connect-kafka-ui-1 |  provectuslabs/kafka-ui:latest       |    "/bin/sh -c 'java --…" |  kafka-ui     |     4 hours ago  | Up 4 hours          |   0.0.0.0:8100->8080/tcp, [::]:8100->8080/tcp
confluent-kafka-for-connect-mysql-1   |   mysql:8.0                            |   "docker-entrypoint.s…" |  mysql        |     4 hours ago  | Up 4 hours          |   0.0.0.0:3306->3306/tcp, [::]:3306->3306/tcp, 33060/tcp
confluent-kafka-for-connect-postgres-1 |  postgres:15                           |  "docker-entrypoint.s…"  | postgres     |     4 hours ago  | Up 4 hours          |   0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp
kafka                                 |   confluentinc/cp-kafka:7.6.1            | "/etc/confluent/dock…" |  kafka         |    4 hours ago  | Up 4 hours          |   0.0.0.0:9092->9092/tcp, [::]:9092->9092/tcp
schema-registry                        |  confluentinc/cp-schema-registry:7.6.1   |"/etc/confluent/dock…" |  schema-registry|   4 hours ago  | Up 4 hours          |   0.0.0.0:8081->8081/tcp, [::]:8081->8081/tcp
zookeeper                               | confluentinc/cp-zookeeper:7.6.1        | "/etc/confluent/dock…" |  zookeeper      |   4 hours ago  | Up 4 hours           |  2888/tcp, 0.0.0.0:2181->2181/tcp, [::]:2181->2181/tcp, 3888/tcp
