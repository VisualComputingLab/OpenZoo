---
title: Framework demonstration
layout: default
---
# Demonstration
In the following, the OpenZUI interfaces will be demostrated, using the provided test topology.

## Preparation
Let's consider the following scenario:

We would like to build an application that filters out fashion related images from Twitter and informs us about the most retweeted ones.
We will break down this task to several subtasks and deploy the resulting services on a cluster consisting of two servers running Ubuntu, named *Castor* and *Polydeuces*.

We have also another server at our disposal, *Leda*, for management purposes. Leda could, of course, be used also for deploying services, but for now we will keep it apart for clarity purposes.

Let's assume that we have installed the following software on each server, as described in the Installation page:

- ***Leda***: **Tomcat** (credentials: admin/tpwd), **RedisIO**, **RabbitMQ** (credentials: qadmin/qpwd), **MongoDB** (credentials: dbadmin/dbpwd), **OpenZUI** (credentials: admin/open)
- ***Castor***: **Tomcat** (user: admin, pwd: tpwd)
- ***Polydeuces***: **Tomcat** (user: admin, pwd: tpwd)

We should not forget to build the ServerResources project before we build and deploy the OpenZUI tool, since OpenZUI needs it for the server registration.

## Login to OpenZUI

Using the credentials that we set at the OpenZUI web/config.json file, we log into OpenZUI.

![OpenZUI login](http://OpenZoo.org/images/0_Login.png)

After the successfull login, we see the main page of OpenZUI.

![OpenZUI main](http://OpenZoo.org/images/1_Main.png)

## Server registration

If all servers are setup as described above, we don't need to read the instructions under "Server setup".
We can proceed directly to "Server registration".

![OpenZUI server registration 1](http://OpenZoo.org/images/2_Server_Registration_1.png)

We add both our servers by setting the server name, server IP, Tomcat port and Tomcat credentials:

![OpenZUI server registration 2](http://OpenZoo.org/images/2_Server_Registration_2.png)

If everything runs smoothly, a green status tick will be visible beside each server.
We can then proceed to the next step by clicking on the red OpenZoo icon on the upper left of the page.

## Service templates

We now proceed to the "Service templates" page.
Here, we can create an empty, runnable component, with all its dependencies, customized to our needs.

![OpenZUI service template 1](http://OpenZoo.org/images/3_Service_Template_1.png)

The parameters to be set are the following:

**Programming Language**:
Currently, only Java-based components are supported. In the future, also C++ and Python will be supported.

**Author**:
The name and email of the author, in the form 'Name <email\>'. This information will be embedded to all created files that contain code.

**Component ID**:
A unique string for identifying the component through the entire OpenZoo framework. It must be a single word.

**Description**:
Some words about the service functionality.

**Number of output endpoints**:
The service can have zero or more output endpoints.
A service that saves results on the database/filesystem and does not forward any messages to other components, would not need an output endpoint.

**Has input endpoint**:
The service can have zero or one input endpoint.
A crawler that does not accept any input from other components, but creates input for other components, would not need an input endpoint.

**Uses queue logging**:
If set, the user can see logging information from this service on the OpenZUI Topology monitoring page, after the service is started.

**Uses MongoDB**:
If set, the appropriate imports and sample functions are included in the template, so that the developer can access the MongoDB easily.
A database manager is such a typical case, but also other services could access the database for storing intermediate results or reading necessary information.

**Worker type**:
The Operator/Broker service types have been explained in the "How it works" page.

**Required Parameters**:
At this point, a comma separated list of parameter names can be specified.
Values for these parameters will then be requested during the creation of the topology.
Currently, all parameters are handled as string values, and it is upon the developer to convert them in the desired types, after they are read inside the service (function "public String getRequiredParameter(String param)" of the worker, see "How it works"/"Worker interfaces").

![OpenZUI service template 2](http://OpenZoo.org/images/3_Service_Template_2.png)

After all parameters are set, we press the Create button and a zip file with the generated wrapper is offered for download.

We can download the zip, unzip it and copy the contained folder into the folder where the OpenZoo repository has been fetched.
In any case, the service folder must be at the same level as the *OpenZooService* folder.

After that, we can open the project with Netbeans.
Netbeans version 8 is recommended, although any version above 7.3.1 should do.
Versions 7.3.1 and below use an older version of Jersey and will not run.
Probably we won't need to change anything else but the code in the Worker file.

For a demonstration using the test components, comming together with OpenZoo, we can skip this step.

## Service repository

After downloading the template services and altering their code, we would need to upload the final services to the OpenZoo repository through the "Service repository" interface.

![OpenZUI service repository 1](http://OpenZoo.org/images/4_Service_Repository_1.png)

A set of test components are provided for demonstrating the usage of the OpenZoo framework.
We have to open the corresponding projects with Netbeans 8, build them and upload the resulting war files into the service repository, through OpenZUI.

![OpenZUI service repository 2](http://OpenZoo.org/images/4_Service_Repository_2.png)

If everything runs smoothly, a green status tick will be visible beside each component.
We can then proceed to the next step by clicking on the red OpenZoo icon on the upper left of the page.

## Components used for the demonstration

Our test components are the following:

<table style="width: 100%; margin-bottom: 20px; margin-top: 20px">
  <tr>
    <th>Name</th>
    <th>Type</th>
    <th>Ins/Outs</th>
    <th>Required params</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>TwitterListener</td>
    <td>Operator</td>
    <td>0/1</td>
    <td><a href="https://dev.twitter.com/oauth/overview/application-owner-access-tokens">Twitter credentials</a>, keyword list</td>
    <td style="text-align: left; padding: 10px;">Given a set of keywords, it retrieves relevant tweets using the Twitter Streaming API and posts them to its only output.</td>
  </tr>
  <tr>
    <td>URLUnwrapper</td>
    <td>Operator</td>
    <td>1/2</td>
    <td></td>
    <td style="text-align: left; padding: 10px;">
Given a tweet, it checks if it contains a URL.
If yes, it unwrapps it (Twitter uses mainly shortened URLs) and sends it to its first output.
If the tweet contains also information about senders location (coordinates), it is routed with the routing key "coord", otherwise with the routing key "nocoord".
If the tweet does not contain a URL, it is forwarded to its second output.
    </td>
  </tr>
  <tr>
    <td>ImageDownloader</td>
    <td>Operator</td>
    <td>1/1</td>
    <td></td>
    <td style="text-align: left; padding: 10px;">
This component should use the URLs from incoming messages to download the images, pointed by them.
Since this is only a demonstration, the downloading functionality has not been implemented, so the component just forwards messages from its input to its output.
    </td>
  </tr>
  <tr>
    <td>Researcher</td>
    <td>Broker</td>
    <td>1/1</td>
    <td></td>
    <td style="text-align: left; padding: 10px;">
The Researcher receives queries from the web
e.g. POST http://Tomcat_IP:Tomcat_Port/Researcher/resources/manage with a body like {"method": "hotter", "num":20} or {"method": "newer", "num":20}.
An OpenZoo message is created with a header containing the parameter {"search": true} and sent to the output.
It then waits for a response at its input, and when it is there, it is returned to the caller.
    </td>
  </tr>
<tr>
    <td style="padding:10px;">MongoManager</td>
    <td style="padding:10px;">Operator</td>
    <td style="padding:10px;">1/1</td>
    <td style="padding:10px;">Database name, images collection, messages collection</td>
    <td style="text-align: left; padding: 10px;">
The MongoManager has two tasks to fulfill:
a) When it receives a message with a message header containing the parameter {"search": true}, it queries the database for the most retweeted or newer tweets, depending on the method parameter.
It then sends to its output a message containing the top num results.
b) For any other kind of messages, it inserts or updates the URL in the images collection and saves the message in the messages collection.
    </td>
</table>


## Topology management

We can now create our topology in the "Topology management" interface.

![OpenZUI topology management 1](http://OpenZoo.org/images/5_Topology_Management_1.png)

We input a unique name for the topology, a description and access information for RabbitMQ and MongoDB.
Each topology can have its own RabbitMQ and MongoDB servers, but we can also use one RabbitMQ and one MongoDB for all our topologies.

After clicking the Create button, we are redirected to a drawing interface.

![OpenZUI topology management 2](http://OpenZoo.org/images/5_Topology_Management_2.png)

We can now, through drag & drop, define our topology.

The combobox at the top contains all our components.
We can use the **insert** button to insert our components into the drawing area. Each component can be inserted just once.

Each time we insert a component, we can click on it for opening its service configuration panel.
It contains the required parameters, as they were defined during the service template creation, as well as two parameters common for all services:

- The **instances** parameter, through which we can define how many instances of this component we wish to have (on how many servers this component should run).
This number should be greater than zero and less than the total number of servers in our cluster, since each server can hold just one instance of each service.

- The **threads per core** parameter, through which we can define how many identical worker threads per CPU core should run on each server.
So, if a server has two cores and a value of 2, it will run 4 worker threads.
This functionality is for utilizing the full capacity of a server and not wasting resources.
It should be selected having in mind the following:
Services that accomplish light tasks should use bigger values, while services that accomplish hard tasks should use smaller values.
In general, a value of less than or equal to 4 is a good choise.

A threads per core value of 0 has a special meaning: It means that only one worker thread should be created, regardless of number of CPU cores.

All broker service have an instances value of 1 and a threads per core value of 0.

We define 1 instance of TwitterListener, 2 instances of URLUnwrapper, 2 instances of ImageDownloader, 1 instance of MongoManager and 1 instance of Researcher.

![OpenZUI topology management 3](http://OpenZoo.org/images/5_Topology_Management_3.png)

![OpenZUI topology management 4](http://OpenZoo.org/images/5_Topology_Management_4.png)

![OpenZUI topology management 5](http://OpenZoo.org/images/5_Topology_Management_5.png)

![OpenZUI topology management 6](http://OpenZoo.org/images/5_Topology_Management_6.png)

![OpenZUI topology management 7](http://OpenZoo.org/images/5_Topology_Management_7.png)

After defining the topology nodes (components), we should define the connections between them.

Pressing the **Add link** button, an arrow is inserted between the two black points at the upper right of the drawing.
We can now drag the source and the target of the arrow to the appropriate components, by placing the mouse pointer over the arrow head or tail, until the mouse pointer transforms to a hand, and drag it to the desired destination.

After setting both the source and the target, a connection configuration panel appears.
We can now set the source and the target endpoint of the connection, since a component can have more than one outputs, which serve as inputs to the next component.

Also, we can set the connection type, if needed.
The connection type defines what happens in the situation that a component sends a message to another component that has more than one instances.

There are 3 connection types:

- The **Available** connection type, which defines that the first free instance should get the message.
- The **Route** connection type, which defines that each instance should have a list of routing keys and receive all messages containing the appropriate key (Message interface: void setRoutingKey("coord")).
- The **All** connection type, which defines that all instances should receive all messages.

![OpenZUI topology management 81](http://OpenZoo.org/images/5_Topology_Management_81.png)
![OpenZUI topology management 82](http://OpenZoo.org/images/5_Topology_Management_82.png)

The connection from URLUnwrapper to ImageDownloader is of type Route.
We have defined two instances of ImageDownloader, so we will have to define a list of routing keys for each instance.
We use "coord" for instance 0 and "nocoord" for instance 1.

The result of this action is that all tweets, containing images and senders coordinates will be delivered to instance 0.
All tweets with images, but no senders coordinates will be delivered to instance 1.
Since the ImageDownloader just forwards the messages, this does not have any practical meaning, but it is used as a demonstration of the routing functionality.
This would be usefull, for example, if we would use a distributed indexer, where different types of input get indexed on different servers (index instances).

![OpenZUI topology management 83](http://OpenZoo.org/images/5_Topology_Management_83.png)
![OpenZUI topology management 84](http://OpenZoo.org/images/5_Topology_Management_84.png)

The connection of the Researcher to the MongoManager is a two-way connection:

![OpenZUI topology management 85](http://OpenZoo.org/images/5_Topology_Management_85.png)
![OpenZUI topology management 86](http://OpenZoo.org/images/5_Topology_Management_86.png)

After configuring all components and connections, our topology is ready for deployment, so we can click on Submit.

# Server configuration confirmation

We are now back to the Topology table.

![OpenZUI topology management 91](http://OpenZoo.org/images/5_Topology_Management_91.png)

We can now click on the table row that contains our topology and press the Deploy button.

![OpenZUI topology management 92](http://OpenZoo.org/images/5_Topology_Management_92.png)

We see now a list of the cluster servers, each with a subset of our services.
The number beside each service is the instance number of this instance.
Green boxes depict operator services, while orange boxes depict broker services.

This configuration has been created automatically, taking into account the following parameters:

- The number of available servers
- The free memory, CPU load and disc space available on each server
- The services already running at each server
- The number of instances set by the user while drawing the topology

We can change the configuration by drag & drop, but we have to keep in mind that we cannot have two instances of the same service on the same server.
When is changing this configuration neccessary? An example would be the case where we need a broker at a specific server, so that it answers requests.

![OpenZUI topology management 93](http://OpenZoo.org/images/5_Topology_Management_93.png)

When we are done with changing the server configuration, we can click on Submit, so that our topology is deployed.

After a while, we are redirected back to our Topology table.

We can now click on the table row that contains our topology and press the Start button.

![OpenZUI topology management 94](http://OpenZoo.org/images/5_Topology_Management_94.png)

![OpenZUI topology management 95](http://OpenZoo.org/images/5_Topology_Management_95.png)

We can later stop and undeploy our topology at the same place.

## Topology monitoring

After deploying and starting the topology, statistics can be seen in the Topology monitoring interface.
The results of the processing can be seen in the MongoDB.

![OpenZUI topology monitoring 1](http://OpenZoo.org/images/6_Topology_Monitoring_1.png)

We can see 4 columns.

On the left column (**Topologies**), all topologies are listed.

On the middle left column (**Components**), all components of the selected topology are listed.

On the middle right column (**Servers (Instances)**), the servers are listed, where an instance of the selected component is running and below that (**Endpoints (Component level)**), the endpoints of the selected component are listed, summing all messages (in number and size) that came through all instances of this component.

On the right column (**Server load**), we can see the server load for the selected server and below that (**Endpoints (Instance level)**), the endpoints of the selected component are listed again, this time summing all messages (in number and size) that came through the instance of this component that is running on the selected server.

Below the first two columns (**Topology overview**), we can see an overview of the topology.

At the bottom side of the page (**Topology service logs**), we can see the service logs, for which the option *queueLogging* was set during the template creation. These services use a logging connection.
The service logs can be cleared, paused and filtered by the logging level (debug, info, error).

![OpenZUI topology monitoring 2](http://OpenZoo.org/images/6_Topology_Monitoring_2.png)

There are two buttons in the Components column:

Through the **Reset** button, all instances of a service can be stopped and restarted, without restarting the whole topology.
This is usefull for the case that we update some service parameters in the Topology management interface (Topology table, click on topology row, update button), e.g. the keywords of the TwitterListener or the database of the MongoManager.

Through the **Redeploy** button, all instances of a service can be stopped, undeployed, redeployed and started, without redeploying the whole topology.
This is usefull for the case that we update the service itself, i.e. changing the code, recreating the war file and uploading the updated war file to the service repository.


