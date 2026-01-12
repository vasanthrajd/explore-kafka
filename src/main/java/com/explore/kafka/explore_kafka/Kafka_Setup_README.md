
#### Creating the Topic in Windows Environment
```
.\bin\windows\kafka-topics.bat --create --topic order-events --bootstrap-server localhost:2181 --partitions 3
```

#### Creating the Consumer in Windows Environment
```
.\bin\windows\kafka-console-consumer.bat --topic order-events --bootstrap-server localhost:9094,localhost:9095,localhost:9096 --from-beginning
```
