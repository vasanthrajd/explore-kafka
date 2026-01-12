sequenceDiagram
participant P as Producer<br/>(transactional.id=tx-1)
participant TC as Transaction<br/>Coordinator
participant B1 as Broker/Partition 1
participant B2 as Broker/Partition 2
participant TL as __transaction_state<br/>(Transaction Log)
participant C as Consumer<br/>(read_committed)

    Note over P,C: Phase 1: Initialization
    P->>TC: initTransactions()
    TC->>TL: Register transactional.id
    TC->>P: Return PID (Producer ID) + Epoch

    Note over P,C: Phase 2: Begin Transaction
    P->>TC: beginTransaction()
    TC->>TL: Write BEGIN marker
    TC->>P: Transaction started

    Note over P,C: Phase 3: Produce Messages
    P->>B1: send(topic-A, msg1)
    P->>B2: send(topic-B, msg2)
    P->>B1: send(topic-A, msg3)
    Note over B1,B2: Messages stored but<br/>NOT visible to consumers yet

    Note over P,C: Phase 4: Commit Decision
    P->>TC: commitTransaction()
    TC->>TL: Write PREPARE_COMMIT
    
    Note over P,C: Phase 5: Two-Phase Commit
    TC->>B1: Write COMMIT marker
    TC->>B2: Write COMMIT marker
    TC->>TL: Write COMMITTED state
    TC->>P: Commit successful

    Note over P,C: Phase 6: Consumer Reads
    C->>B1: poll() with isolation.level=read_committed
    B1->>C: Return msg1, msg3 (committed)
    C->>B2: poll()
    B2->>C: Return msg2 (committed)
    
    Note over C: Only sees messages from<br/>COMMITTED transactions

```

```
Kafka Consume
↓

Kafka Transaction Begin
↓

REST Call A (idempotent)
↓

REST Call B (idempotent)
↓

Produce Output Topic
↓

Commit Transaction
```