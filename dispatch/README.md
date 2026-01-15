# KAFKA DISPATCH DEMO

## Flow
1. Create a console producer that will create order like:
   bin/windows/kafka-console-producer.bat --bootstrap-server localhost:9092 --topic order.created
   {"orderId":"90022465-40de-464b-8f2b-b255db2b446b", "item":"item-1"}
2. OrderCreatedHandler is listening on order.created topic and will send the message to 
   order.dispatched and order.tracked topics.
3. Track module will contain consumer listening on order.tracked topic and send the
   message to order.status topic
4. Create a console consumer to listen to order.status topic:
   bin/windows/kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order.status --group dispatch.order.track.consumer

## Pre-requisites:

bin/windows/kafka-server-start.bat config/kraft/server.properties

bin/windows/kafka-console-producer.bat --bootstrap-server localhost:9092 --topic order.created
{"orderId":"90022465-40de-464b-8f2b-b255db2b446b", "item":"item-1"}

bin/windows/kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order.status --group dispatch.order.track.consumer

## Steps to run
1. Make sure you complete pre-requisite steps.
2. Start application dispatch.
3. Start application track.
4. Create below message via console producer on topic order.created:
   {"orderId":"90022465-40de-464b-8f2b-b255db2b446b", "item":"item-1"}
5. Check message received on console consumer listening on topic order.status.