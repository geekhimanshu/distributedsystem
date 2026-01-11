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
The last read position in a topic partition for a consumer.

### Consumer Groups
1. Each consumer is part of some consumer groups.
2. Consumers with in same consumer group will share data on the topic.
3. Only one consumer in a consumer group will consume a message.
3. Consumers read from the partition and the broker maintain the same read position via the offset.

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