---
title: Overview
layout: default
---
# Overview
OpenZoo is an open-source, [MIT licensed](https://opensource.org/licenses/MIT), distributed, stream/batch processing framework.
OpenZoo enables the development of processing topologies with minimum configuration on easy-to-use User Interfaces.
Multiple languages and cross-platform support enables the creation of complex topologies, deployed either in cloud infrastructures or shared in available PCs in a lab environment.
The current distribution consists of a java service template, a management GUI and several test services for demonstrating the use of the framework. Templates for C++ and Python will be soon available.

## Features
OpenZoo offers the following core functionalities:

* Remote deployment of services
* Basic classes for service registration, intercommunication and monitoring
* Load balancing through the usage of queues
* Data storage
* Data caching
* Easy service topology creation and management
* Transparent allocation of available resources
* Easy exchange of components
* One touch creation of service wrappers
* Abstraction layer over communication, persistence, caching, etc.
* Schema-free JSON as message exchange format

## Applications
A wide range of applications can be developed on top of OpenZoo:

* Real time search and analytics applications
* Streaming and batch processing frameworks
* Distributed and scalable architectures

## Success cases
OpenZoo has been tested as a real time search and analytics framework, based on images shared through Twitter. It was running for over a year, distributed on several (8-15) servers, processing millions of tweets per week.
It has been also successfully tested as a video processing framework, extracting features out of gigs of video, in a Big Data oriented application.

## Support
OpenZoo supports both Windows (tested on XP and Win7) and Linux (tested on Ubuntu 14.04 LTS).
It uses MongoDB for persistence, RabbitMQ as a communication medium and RedisIO for exchanging parameters.
Services are running on Tomcat (tested on Tomcat 7).
Currently, only Java is supported for service development. C++ and Python service templates are the next major milestone.

