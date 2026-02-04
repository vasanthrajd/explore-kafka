docker exec kafka-1 kafka-streams-application-reset --application-id atm-withdrawal-processor --bootstrap-servers localhost:9092

docker exec kafka-1 kafka-streams-application-reset --application-id atm-withdrawal-processor --bootstrap-servers kafka-1:9092


rmdir /s /q D:\vasanth-git\explore-kafka\src\main\resources\kafka-streams-state\atm-withdrawal-processor

rmdir /s /q C:\Users\Administrator\AppData\Local\Temp\kafka-streams\atm-stream-app-v1