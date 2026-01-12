1. Explain about Executor Framework in Java
Answer: The Executor Framework in Java is a high-level concurrency framework that simplifies the management of threads
2. Explain about how Parallel Stream work 
Answer: Parallel Streams in Java utilize the Fork/Join framework to divide a stream's data into multiple substreams that can be 
processed concurrently across multiple threads.

3. What is difference between Parallelism and Concurrency
Answer: Concurrency is the ability of a system to handle multiple tasks at the same time, while parallelism is the simultaneous execution of multiple tasks to achieve faster processing.
4. How to achieve parallelism in Java
Answer: Parallelism in Java can be achieved using several approaches, including:
   1. Parallel Streams: Using the `parallelStream()` method to process collections in parallel.
   2. Fork/Join Framework: Utilizing the `ForkJoinPool` to recursively split tasks into smaller subtasks that can be executed in parallel.
   3. ExecutorService: Creating a thread pool using `Executors.newFixedThreadPool()` or other factory methods to manage and execute multiple threads concurrently.
   4. CompletableFuture: Using `CompletableFuture.supplyAsync()` and related methods to run tasks asynchronously and combine their results.

5. How to achieve concurrency in Java
Answer: Concurrency in Java can be achieved through various mechanisms, including:
   1. Threads: Creating and managing threads using the `Thread` class or implementing the `Runnable` interface.
   2. Executor Framework: Utilizing the `ExecutorService` to manage a pool of threads and execute tasks concurrently.
   3. Synchronized Blocks/Methods: Using the `synchronized` keyword
   4. Locks: Employing `java.util.concurrent.locks.Lock` and `ReentrantLock` for more advanced synchronization control.
   5. Concurrent Collections: Using thread-safe collections from the `java.util.concurrent` package, such as `ConcurrentHashMap` and `CopyOnWriteArrayList`.
   6. Atomic Variables: Utilizing classes from the `java.util.concurrent.atomic` package for lock-free thread-safe operations
   7. CompletableFuture: Leveraging `CompletableFuture` for asynchronous programming and combining multiple concurrent tasks.

6. What is Poison Pill in Kafka and how to handle it?
7. What happens when there is partition re-balance in Kafka Consumer Group?
8. What is out of order sequence
9. How to handle out of order sequence in Kafka Consumer?
10. Explain about Kafka Partitioner strategies
11. When will the LeaderNotAvailableException occur and how to handle it?
12. What are all different semantics available in Kafka Transactions?
13. What is Schema Registry and why it is needed? 
14. What is protocol buffers
15. What is the difference between Avro and Protobuf?
16. How to seek to a specific offset in Kafka Consumer?
17. What is consumer group, how it works and what happens during rebalance?
18. What is virtual threads in Java?
    Answer: Virtual threads are lightweight threads introduced in Java 19 as part of Project Loom.
They are managed by the Java Virtual Machine (JVM) rather than the operating system, allowing for a large number of concurrent threads with minimal resource overhead.
Virtual threads enable developers to write synchronous code that can handle many concurrent tasks without the complexity of traditional asynchronous programming models.
