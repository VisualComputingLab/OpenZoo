---
title: Future work
layout: default
---
# Future work
There is a lot of stuff that we would like to add or improve.
If you would like to contribute to OpenZoo, please [feel free to do so]().

## Programming languages
We are currently working on a C++ and a Python template for OpenZoo, meaning that the developer will have more options for writing the services than Java.
RedisIO, RabbitMQ  and MongoDB communication is the first hurdle, since the clients are OS-dependend and, even worse, compiler dependend.
But the main problem is the remote deployment of the services.
What we have in mind is to develop a Tomcat application, that will be deployed together with the ServerResources application on any server participating to the cluster, which will take over the application management.

## Database and Queue alternatives
Supporting other databases than MongoDB is not very hard.
MongoDB sits actually not deep in the framework architecture.
Supporting other Queues than RabbitMQ is a bit harder, but exploiting AMQP should make things a lot easier.

## Fault tolerance
This is a very important topic for us.
We are currently working on a fusion architecture between OpenZoo and Consul (https://www.consul.io/).
