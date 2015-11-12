---
title: Overview
layout: default
---
# Overview
OpenZoo is an open-source, [MIT licensed](https://opensource.org/licenses/MIT), distributed, stream/batch processing framework.
It has been developed and maintained by the [Visual Computing Lab (VCL)](http://vcl.iti.gr) of the [Information Technologies Institute (ITI)](http://www.iti.gr), which is a founding member of the [Centre for Research & Technology Hellas (CERTH)](http://www.certh.gr/root.en.aspx).
OpenZoo currently consists of a java service template, a management GUI and several test services for demonstrating the use of the framework.

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
OpenZoo has been tested as a real time search and analytics framework, based on images posted in Twitter. It was running for over a year, distributed on several (8-15) servers, processing ca. 6 Mio tweets per week.
It has been also successfully tested as a video processing framework, extracting features out of gigs of video, in a Big Data oriented application.

## Support
OpenZoo supports both Windows (tested on XP and Win7) and Linux (tested on Ubuntu 14.04 LTS).
It uses MongoDB for persistence, RabbitMQ as a communication medium and RedisIO for exchanging parameters.
Services are running on Tomcat (tested on Tomcat 7).
Currently, only Java is supported for service development. C++ and Python service templates are the next major milestone.

