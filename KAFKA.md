# KAFKA


## Key component and terms

### Producer

### Broker
Messages are replicated across broker nodes to ensure resiliency.

### Consumer
1. Uses batch pulling mechanism.
2. Prevents consumer overloading and ensures high throughput. 

### Topic
1. Message is produced and pushed to a particular topic in the broker.
2. Message is pulled from a topic.
3. A topic is divided into number of logs called partitions. 
4. Topic load is shared among the partitions.
5. Messages are sent to partition on the basis of key. 
6. Prior to kakfa 2.4, it used to follow round robin. 
7. Post kafka 2.4, it uses sticky partition i.e. messages are sent to same partition every time until the batch capacity is full.

### Topic offset
The last read position in a topic partition for a consumer. It is maintained via internal topic __consumer_offsets.

### Consumer Groups
1. Each consumer is part of some consumer groups.
2. Consumers with in same consumer group will share data on the topic.
3. Only one consumer in a consumer group will consume a message from a given topic at a time.
4. Consumers read from the partition and the broker maintain the same read position via the offset.
5. If we want to deliver same message to different consumers, they need to be in different consumer groups.
6. When a kafka consumer is started and it is not part of any consumer group, then a consumer group is automatically created.

### Kafka Cluster
1. We can create a kafka cluster with multiple brokers.
2. Each broker will have different topics with multiple partitions.
3. There will be lead and follower partitions.
4. Leader partitions will receive messages from the producer and these messages are replicated to follower partitions. This is how replication is achieved.
5. A producer will always write to the lead partition but a consumer can consume from any of the partition.

### Messages, Records, Requests, Commands and Events
1. A message is the envelop. A message is also called Record in Kafka.
2. An envelop can contain events or requests. 
3. A request/command is a type of event.
3. Message is event plus metatdata.


## Message Structure
A record consists of key, metatdata, headers and value. A kafka message doesn't contain a sequence number.

### Payload.
1. Value is the payload.
2. Default maxsize of payload is 1Mb
3. There is also an option to encrypt the sensitive data.

### Headers
Key value pairs.

### Key
1. Optional.
2. By default, it is null.
3. Facilitates guaranteed ordering.
4. A message with the same key is guaranteed to be consumed by the same consumer.
5. If a message is written to a topic without a message key, and the partition to write to is not specified, then there is no guarantee which partition it will be written to.

## Apache Zookeeper

### How it is used with Kafka ?
Manages the metatdata required to run kafka cluster.

### Why it is deprecated ?
1. It requires atleast 3 zookeeper instances to run and manage kafka clusters.
2. It stores kafka metadata out of kafka cluster (there by creating metadata duplication) which create additional overheads.
3. These things in return induces more latency and hinders in scaling up of kafka clusters.


## KRaft

### Working 
1. It is based on event based consensus protocol using logs.
2. It consolidates and manages cluster metadata within kafka system only.
3. KRaft is not responsible for replicating messages between kafka broker nodes. That is managed by in-sync replicas.


## Steps to start KAFKA (Windows Power Shell)

### Setup KAFKA_CLUSTER_ID
The cluster ID uniquely identifies your Kafka cluster within the larger distributed system. This is essential for preventing conflicts, especially when multiple clusters may be deployed in a similar environment. Each broker in a cluster requires the cluster ID to coordinate and maintain its state. It helps brokers know they are part of the same cluster, which is essential for leader election and replication of data across brokers.

Steps:
cd D:\kafka_2.13-3.7.2\
$env:KAFKA_CLUSTER_ID = & "bin\windows\kafka-storage.bat" random-uuid
echo $env:KAFKA_CLUSTER_ID

### Setup log directory

The log directory serves as the persistent storage for message topics and all associated metadata in Kafka. Properly setting it up and formatting this directory ensures that messages are stored in an organized manner, making them accessible for future retrieval.

Steps:
cd D:\kafka_2.13-3.7.2\
vim config/kraft/server.properties
log.dirs=kafka-logs

### Format kafka log directory
bin/windows/kafka-storage.bat format -t $KAFKA_CLUSTER_ID -c config/kraft/server.properties

### Start kafka server
bin/windows/kafka-server-start.bat config/kraft/server.properties

### Start kafka console Consumer
bin/windows/kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic my.first.topic

### Start kafka console Consumer with some specific consumer group
bin/windows/kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic my.first.topic --group my.new.group 

### Start kafka console Producer
bin/windows/kafka-console-producer.bat --bootstrap-server localhost:9092 --topic my.first.topic

### List kafka topics
bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --list

### Create a new Topic
bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --create --topic my.new.topic
bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --create --topic hr.demo.topic --partitions 5

### Describe a Topic
bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic my.new.topic
Output:
PS D:\kafka_2.13-3.7.2> bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic my.new.topic
Topic: my.new.topic     TopicId: 8ilQx61QQgS6N_UycRLIUg PartitionCount: 1       ReplicationFactor: 1    Configs: segment.bytes=1073741824
        Topic: my.new.topic     Partition: 0    Leader: 1       Replicas: 1     Isr: 1
		
### Alter the number of partitions
bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --alter --topic my.new.topic --partitions 3
Output:
PS D:\kafka_2.13-3.7.2> bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --alter --topic my.new.topic --partitions 3
PS D:\kafka_2.13-3.7.2> bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic my.new.topic
Topic: my.new.topic     TopicId: 8ilQx61QQgS6N_UycRLIUg PartitionCount: 3       ReplicationFactor: 1    Configs: segment.bytes=1073741824
        Topic: my.new.topic     Partition: 0    Leader: 1       Replicas: 1     Isr: 1
        Topic: my.new.topic     Partition: 1    Leader: 1       Replicas: 1     Isr: 1
        Topic: my.new.topic     Partition: 2    Leader: 1       Replicas: 1     Isr: 1
		
### Delete a Topic
bin/windows/kafka-topics.bat --bootstrap-server localhost:9092 --delete --topic my.new.topic

### List consumer Groups
bin/windows/kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list

### Describe a consumer group
bin/windows/kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group my.new.group

### Health of a consumer group
bin/windows/kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group my.new.group --state

### Check members of a consumer group
bin/windows/kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group my.new.group --members

## Spring KAFKA

### Create a kafka Consumer
1. Use @KafkaListner
2. Set topic, groupId.
3. Use value-deserializer to convert payload to required data format. Remember, it is not configured by default. It's mandatory.
4. Messages are always stored as byte arrays on kafka broker. Using a JSON deserializer results in the byte array being deserialized to JSON by Spring Kafka.
5. It is best practive set to set value-deserializer to ErrorHandlingDeserializer and then delegate to respective deserializer. This causes Spring Kafka to stop poison pill messages occurring when invalid messages cannot be deserialized. See application.properties of dispatch module.
6. Spring Kafka uses the KafkaListenerContainerFactory to build the container for the @KafkaListener annotated handler.
7. If an exception is thrown from Kafka listener, it should be handled properly or else it will be retried.

### Create a kafka Producer
1. By default, kafka will send message asynchronously i.e. fire and forget. We can't know if the message is delivered to the broker successfully.
2. If we configure kafka to send message synchronously, then we can know if the message is delivered to the broker successfully.