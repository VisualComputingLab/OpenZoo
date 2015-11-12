---
title: Future work
layout: default
---
# Future work
There is a lot of stuff that we would like to add or improve.
If you would like to contribute to OpenZoo, please [feel free to do so](http://visualcomputinglab.github.io/OpenZoo/authors).

## Programming languages
We are currently working on a C++ and a Python template for OpenZoo, meaning that the developer will have more options for writing the services than just Java.
RedisIO, RabbitMQ  and MongoDB communication is the first hurdle, since the clients are OS-dependend and, even worse, compiler dependend.
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

- Redis is not intended for running in the open internet. From the Redis web site:

> Redis is designed to be accessed by trusted clients inside trusted environments. This means that usually it is not a good idea to expose the Redis instance directly to the internet or, in general, to an environment where untrusted clients can directly access the Redis TCP port or UNIX socket.

> Access to the Redis port should be denied to everybody but trusted clients in the network, so the servers running Redis should be directly accessible only by the computers implementing the application using Redis.

This is, in our case, not convenient, since not only OpenZUI, but also all servers in our cluster need access to Redis and we want to be able to add new servers to our cluster without having to update firewall rules.
A solution could be the authentication layer of Redis:

> While Redis does not try to implement Access Control, it provides a tiny layer of authentication that is optionally turned on editing the redis.conf file. When the authorization layer is enabled, Redis will refuse any query by unauthenticated clients. A client can authenticate itself by sending the AUTH command followed by the password. The password is set by the system administrator in clear text inside the redis.conf file.

Again, apart from the performance considerations, this is not the perfect solution, since:

> The AUTH command, like every other Redis command, is sent unencrypted, so it does not protect against an attacker that has enough access to the network to perform eavesdropping.
