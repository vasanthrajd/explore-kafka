#### How Threads and Non-Blocking I/O Work in Tomcat

##### Traditional Blocking I/O
```declarative
// Tomcat's traditional connector
Client ──TCP Connection──> Tomcat Thread
                           [Thread reads request]
                           [Thread processes]
                           [Thread writes response]
                           [Connection closes]
```
Thread is tied to connection from start to finish.

##### Async Servlet 3.0+ (NIO Connector)

```declarative
// Modern NIO connector
Client ──TCP Connection──> OS Kernel (epoll/kqueue)
                                |
                                ├──> Tomcat NIO Poller Thread
                                |    (monitors multiple connections)
                                |
                                └──> Worker Thread Pool
                                     (only when data arrives)
```

**How it works:**

1. **Acceptor Thread** accepts new connections
2. **Connection** registered with OS-level selector (epoll on Linux)
3. **Poller Thread** monitors hundreds/thousands of connections using `select()`/`epoll()`
4. **Worker Thread** assigned only when:
    - Request data arrives
    - Response needs to be sent
5. **Connection stays open** in OS, consuming almost no resources

---

## **2. When Does Thread Context Come Into Picture?**

Complete async request lifecycle:

### **Phase 1: Request Arrival**
```
[HTTP Connection established]
      ↓
[TCP socket opened by OS]
      ↓
[Tomcat Acceptor Thread detects new connection]
      ↓
[Connection registered with NIO Poller]
      ↓
[Poller detects data available to read]
      ↓
[Worker Thread 1 assigned from pool] ← THREAD CONTEXT STARTS
      ↓
[Thread reads HTTP request]
      ↓
[DispatcherServlet routes to Controller]
      ↓
[Controller returns CompletableFuture]
      ↓
[Spring creates DeferredResult]
      ↓
[Thread calls startAsync() on ServletRequest]
      ↓
[Worker Thread 1 released back to pool] ← THREAD CONTEXT ENDS
```

**Thread Usage:** ~2-5ms

### **Phase 2: Waiting for Kafka**
```
[HTTP Connection still open in OS]
      ↓
[Connection in "ASYNC STARTED" state]
      ↓
[Poller thread monitors connection (no worker thread needed)]
      ↓
[Kafka processing in background...]
      ↓
[NO THREADS from Tomcat pool being used] ← KEY POINT
```

**Thread Usage:** 0 threads from pool

### **Phase 3: Response Completion**
```
[CompletableFuture completes with offset]
      ↓
[Spring's DeferredResult notified]
      ↓
[Worker Thread 2 assigned from pool] ← NEW THREAD CONTEXT STARTS
      ↓
[Thread writes ResponseEntity to socket]
      ↓
[Thread flushes response]
      ↓
[Worker Thread 2 released back to pool] ← THREAD CONTEXT ENDS
      ↓
[Connection closed]
```

**Thread Usage:** ~1-2ms

### **Total Thread Time**
- Traditional blocking: **150ms** (entire Kafka wait)
- Async approach: **3-7ms** (just I/O operations)

**Efficiency gain: ~20-50x per thread!**

---

## **3. How Tomcat Manages Multiple HTTP Connections Internally**

### **Architecture Overview**
```
┌─────────────────────────────────────────────────────────┐
│                    Tomcat Server                         │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────┐                                     │
│  │ Acceptor Thread│ (1-2 threads)                       │
│  │  (Port 8080)   │ Accepts new TCP connections         │
│  └────────┬───────┘                                     │
│           │                                              │
│           ├──────────────────────────────┐              │
│           ↓                               ↓              │
│  ┌─────────────────┐           ┌─────────────────┐     │
│  │ NIO Poller (1)  │           │ NIO Poller (2)  │     │
│  │ Monitors 1000+  │           │ Monitors 1000+  │     │
│  │ connections     │           │ connections     │     │
│  └────────┬────────┘           └────────┬────────┘     │
│           │                              │              │
│           └──────────┬───────────────────┘              │
│                      ↓                                   │
│           ┌──────────────────────┐                      │
│           │  Worker Thread Pool  │                      │
│           │  (200 threads)       │                      │
│           │  ┌───┐ ┌───┐ ┌───┐  │                      │
│           │  │ T1│ │T2 │ │T3 │  │                      │
│           │  └───┘ └───┘ └───┘  │                      │
│           └──────────────────────┘                      │
│                                                          │
└─────────────────────────────────────────────────────────┘

```

##### Connection Status 

```
// Internal state machine
IDLE → READING → DISPATCHED → ASYNC_STARTED → ASYNC_DISPATCHED → WRITING → CLOSED
```

Example with 10,000 connections:

State | Connections | Threads Usage
----- |-------------| --------------
IDLE (Keep-alive)| 7,000       | 0 threads (monitored by poller)
READING | 100         | 100 Threads
ASYNC_STARTED (waiting for Kafka) | 2,500       | 0 threads (monitored by poller)
WRITING | 100         | 100 Threads
Total | 9700       | ~200 Threads

With blocking I/O, you'd need 9,700 threads!

### **Understanding the Relationships**
```
┌──────────────────────────────────────────────────────────┐
│                    max-connections: 10000                 │
│  ┌────────────────────────────────────────────────────┐  │
│  │         Active Connections (in various states)     │  │
│  │                                                     │  │
│  │  ┌─────────────┐  ┌──────────────────────────┐    │  │
│  │  │ Processing  │  │ Async/Keep-alive/Idle    │    │  │
│  │  │ (200 max)   │  │ (9,800 max)              │    │  │
│  │  │             │  │                          │    │  │
│  │  │  Need       │  │  No threads needed!      │    │  │
│  │  │  threads    │  │  Just socket in OS       │    │  │
│  │  │             │  │                          │    │  │
│  │  └─────────────┘  └──────────────────────────┘    │  │
│  └────────────────────────────────────────────────────┘  │
│             ↑                                             │
│             Limited by threads.max: 200                   │
└──────────────────────────────────────────────────────────┘
             │
             │ If full...
             ↓
┌──────────────────────────┐
│  accept-count: 100       │
│  (Connection queue)      │
│                          │
│  New connections wait    │
│  here temporarily        │
└──────────────────────────┘
             │
             │ If this fills up...
             ↓
      Connection Refused!
```

```declarative
server:
  tomcat:
    max-connections: 10000
    threads:
      max: 200
    async-timeout: 30000
```

##### Load:
    5,000 users browsing (keep-alive connections)
    2,000 users checking out (async payment processing)
    500 users uploading images (slow I/O)

##### Resource Usage
Activity | Connections | State | Threads Used
-------- |-------------|-------|--------------
Browsing | 5,000       | IDLE  | 0
Payment Waiting | 2,000       | ASYNC_STARTED | 0
Image Upload | 500         | READING/WRITING | 50 (I/O)
Active Requests | 200         | PROCESSING | 200

Total Connections: 7,700

Total Threads Used: 250

##### Capacity remaining:
    Connections: 2,350 more (10000 - 7650)
    Threads: 0 available, but 7,000 connections don't need threads!


##### Without Async (Blocking I/O)  
    You'd need 7,650 threads to handle the same load!
    Memory: 7,650 threads × 1MB = ~7.6GB just for stacks
    Context switching overhead would crush performance