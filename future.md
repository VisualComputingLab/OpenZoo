---
title: Future work
layout: default
---
# Future work
There is a lot of stuff that we would like to add or improve.
If you would like to contribute to OpenZoo, please [feel free to do so](http://OpenZoo.org/authors).

## Programming languages
We are currently working on a C++ and a Python template for OpenZoo, meaning that the developer will have more options for writing the services than just Java.
RabbitMQ and MongoDB communication is the first hurdle, since the clients are OS-dependend and, even worse, compiler dependend.
But the main problem is the remote deployment of the services.
What we have in mind is to develop a Tomcat application, that will be deployed together with the ServerResources application on any server participating to the cluster, and will take over the application management.

## Database and Queue alternatives
Supporting other databases than MongoDB is not very hard.
MongoDB sits actually not deep in the framework architecture.
Supporting other queues than RabbitMQ is a bit harder, but exploiting AMQP should make things a lot easier.

## Fault tolerance
This is a very important topic for us.
We are currently working on a fusion architecture between OpenZoo and [Consul](https://www.consul.io/).

## Security considerations
OpenZoo is intended for running in a trusted environment.
There are two reasons for this:

- There is currently no authentication for the exposed interfaces of the components (GETs, POSTs, etc.). This can be done in future versions.

- ~~Redis is used for topology parameter storage and exchange, which is not intended for running in the open internet.~~ **UPDATE**: Redis has been replaced through MongoDB.

