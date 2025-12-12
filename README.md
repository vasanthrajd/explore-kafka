# Explore-kafka

1)  Cluster: Collections of Brokers (Libraries)
2) Broker (Library): Will have Topic (Book)
3) Topic (Book): Message of the topic will be stored in the partition (volumes of the book)
4) Partition (Volumes of a Book) : message will be stored across the partitions.

```
cluster > broker > topic > partitions of the topic will be distributed across the brokers in the cluster
```

Steps to run Single Node multiple Brokers Kafka Cluster

1) Download and Install Kafka from the [Apache Kafka website.](https://kafka.apache.org/downloads)
2) Extract the downloaded Kafka package to a directory of your choice.
3) Open multiple terminal windows to run multiple brokers.
4) 
5) In the first terminal, navigate to the Kafka installation directory and start ZooKeeper (Kafka uses ZooKeeper to manage the cluster):
    ```
    bin/zookeeper-server-start.sh config/zookeeper.properties
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
    bin/kafka-server-start.sh config/server-1.properties
    ```
    ```
    bin/kafka-server-start.sh config/server-2.properties
    ```
    ```
    bin/kafka-server-start.sh config/server-3.properties
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
      ```
different approach to read the message from the topic by consumer
```
--from-beginning
```

```declarative
--partition <partition-id> --offset <offset-number>
```
