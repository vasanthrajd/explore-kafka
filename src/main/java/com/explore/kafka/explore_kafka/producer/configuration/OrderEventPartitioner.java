package com.explore.kafka.explore_kafka.producer.configuration;

import com.explore.kafka.explore_kafka.producer.dto.OrderEvent;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

public class OrderEventPartitioner implements Partitioner {
    @Override
    public int partition(String s, Object key, byte[] keyBytes, Object o1, byte[] valueBytes, Cluster cluster) {
        Integer keyId = Integer.parseInt(((OrderEvent)key).orderId());
        if (keyId % 3 == 0) {
            return 2;
        }
        List<PartitionInfo> partitions = cluster.partitionsForTopic("order-events");
        int numPartitions = partitions.size();

        if (keyBytes == null) {
            return -1; // Default to broker/sticky partitioning if no key
        }

        // Example: Standard hash-based partitioning for all other keys
        return Math.abs(key.hashCode()) % numPartitions;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}
