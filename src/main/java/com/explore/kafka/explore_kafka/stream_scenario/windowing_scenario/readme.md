#### Windowing Concept in Stream. Windowing listed below: 

- Tumbling Window (Fixed Window)
- Hopping Window (Fixed with Overlap)
- Sliding Window (Variable Window based on Event)
- Session Window (Based on Inactivity Gap)

### 1. Tumbling Windows (Non-Overlapping)

**Definition:** Fixed-size, non-overlapping windows.
```
Timeline:
├──────┼──────┼──────┼──────┼──────┤
0      5      10     15     20     25 (minutes)
│◄────►│◄────►│◄────►│◄────►│◄────►│
Window Window Window Window Window
  #1     #2     #3     #4     #5

Each event belongs to EXACTLY ONE window
```


### 2. Hopping Windows (Overlapping)

**Definition:** Fixed-size windows that advance by a smaller interval (creating overlap).
```
Window Size: 10 minutes
Advance Interval: 5 minutes

Timeline:
├─────────┼─────────┼─────────┼─────────┤
0         5         10        15        20 (minutes)
│◄───────────────►│           Window #1 [0-10]
          │◄───────────────►│  Window #2 [5-15]
                    │◄───────────────►│ Window #3 [10-20]

Events in minute 7 appear in BOTH Window #1 and Window #2
```


### 3. Sliding Windows (Event-Driven)

**Definition:** Window size is determined by the **time difference between events**, not fixed intervals.
```
Window Size: 10 minutes
(Triggered by EACH event)

Events:
E1 at 09:00
E2 at 09:03
E3 at 09:07
E4 at 09:20

Windows created:
E1 triggers: [08:50 - 09:00]  (looks back 10 min from E1)
E2 triggers: [08:53 - 09:03]  (looks back 10 min from E2)
E3 triggers: [08:57 - 09:07]  (looks back 10 min from E3)
E4 triggers: [09:10 - 09:20]  (looks back 10 min from E4)

Window for E1-E2-E3 join: Events within 10 min of each other
```
### 4. Session Windows (Inactivity-Based)

**Definition:** Windows that grow dynamically based on activity, separated by periods of inactivity.
```
Inactivity Gap: 5 minutes

Events:
E1 at 09:00
E2 at 09:02  (2 min after E1 - same session)
E3 at 09:04  (2 min after E2 - same session)
E4 at 09:15  (11 min after E3 - NEW session, gap > 5 min)
E5 at 09:16  (1 min after E4 - same session as E4)

Sessions:
Session #1: [09:00 - 09:04]  (E1, E2, E3)
Session #2: [09:15 - 09:16]  (E4, E5)
```
