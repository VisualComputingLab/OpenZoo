---
title: Installation
layout: default
---
# Installation overview
Two major steps must be followed for installing OpenZoo: the installation and configuration of the third party software and the installation of the OpenZoo management GUI (OpenZUI), along with the templates and the examples.
In the following, detailed instructions are given for both.

# Prerequisites
- Netbeans 8 EE for developing the services and creating the war files of the services and the OpenZUI.
- Apache Tomcat 7 for the OpenZUI and the servers. Apache Tomcat 8 should also work, but it is not tested yet.
- RedisIO for storing and exchanging framework parameters.
- RabbitMQ for the communication between the services.
- MongoDB 2.6 or higher for storing results. Version 3.0 or higher is recommended.
- [Twitter authorization tokens](https://dev.twitter.com/oauth/overview/application-owner-access-tokens) for using the demonstration topology described in the sequel.

# Installation of third party software
The following instructions are for Ubuntu 14.04 LTS. The installation on Windows should work similarly.
## Platform (Cluster servers)
Disc space, CPU and memory requirements depend on the deployed services. The framework itself does not need more than 512 MBs of memory. 2 GBs of memory per server is a reasonable value.
The servers need a static IP, in order to be accessible over a long time period. DHCP can also be used for short time periods.
Be sure to install the latest OS updates, e.g. for Ubuntu:

> sudo apt-get update

> sudo apt-get dist-upgrade


## [Apache Tomcat 7](http://tomcat.apache.org/), along with [Java 7](http://openjdk.java.net/)
Tomcat needs to be installed on every server that will participate to the OpenZoo server cluster, as well as to the server where the OpenZUI management application will be deployed.


- Install tomcat and java

> sudo apt-get install tomcat7

> sudo apt-get install default-jdk

- Setup environment variables

> sudo nano /etc/environment

Add the following:

~~~~~~~~~
JAVA_HOME=/usr/lib/jvm/default-java
CATALINA_HOME=/var/lib/tomcat7
~~~~~~~~~

- Reconnect

- Add password protected user

> sudo nano /var/lib/tomcat7/conf/tomcat-users.xml

Add the following:

~~~~~~
<role rolename="manager-gui"/>
<role rolename="manager-script"/>
<user username="admin" password="OZ_SERVER_PASSWORD" roles="manager-gui,manager-script"/>
~~~~~~

- Setup memory usage parameters ([setenv.sh](/scripts/setenv.sh) created by Terrance A. Snyder)

> sudo cp setenv.sh /usr/share/tomcat7/bin/

Set JAVA_HOME path and appropriate limits for –Xms, Xmx, MaxPermSize in the script, if neccessary.
The default values for memory are 1GB for initial and maximum heap size and 512 MB for PermGen.

~~~~~~
JAVA_HOME="/usr/lib/jvm/default-java"
-Xms1024m
-Xmx1024m
-XX:MaxPermSize=512m
~~~~~~

More information on choosing meaningful parameters can be found in this brief, excelent post about [Tomcat memory settings](https://rimuhosting.com/knowledgebase/linux/java/-Xmx-settings).

- Restart tomcat


> sudo service tomcat7 restart

- Install management application

> sudo apt-get install tomcat7-admin

The Tomcat credentials are needed later for registering the servers through OpenZUI.

## [RedisIO](http://redis.io/)
Redis needs to be installed just once, preferably on the server where the OpenZUI management tool will be installed.

- Install server

> sudo nano /etc/apt/sources.list.d/dotdeb.org.list

Add the following:

~~~~~~
deb http://packages.dotdeb.org squeeze all
deb-src http://packages.dotdeb.org squeeze all
~~~~~~

> wget -q -O - http://www.dotdeb.org/dotdeb.gpg | sudo apt-key add -

> sudo apt-get update

> sudo apt-get install redis-server

- Allow remote connections

> sudo nano /etc/redis/redis.conf

Find the following line

~~~~~~
bind 127.0.0.1
~~~~~~

and put in the IP of the server hosting redis (X.X.X.X)

~~~~~~
bind X.X.X.X 127.0.0.1
~~~~~~

- Restart server

> sudo service redis-server restart

The Redis IP will used later, before deploying the OpenZUI application.

## [RabbitMQ](https://www.rabbitmq.com/)
You can install one RabbitMQ server per topology, or use one for all your topologies.

- Install RabbitMQ

> sudo nano /etc/apt/sources.list

Add the following:

~~~~~~
deb http://www.rabbitmq.com/debian/ testing main
~~~~~~

> wget http://www.rabbitmq.com/rabbitmq-signing-key-public.asc

> sudo apt-key add rabbitmq-signing-key-public.asc

> sudo apt-get update

> sudo apt-get install rabbitmq-server

> sudo /usr/lib/rabbitmq/bin/rabbitmq-plugins enable rabbitmq_management


- Create user qadmin with password qpwd and delete default user. You can of course select a different username/password.

> sudo rabbitmqctl add_user qadmin qpwd

> sudo rabbitmqctl set_user_tags qadmin administrator

> sudo rabbitmqctl set_permissions qadmin ".*" ".*" ".*"

> sudo rabbitmqctl delete_user guest

> sudo service rabbitmq-server restart

This information (username and password) will be used later when creating a topology.


## [MongoDB](https://www.mongodb.org/)
You can install one MongoDB server per topology, or use one for all your topologies, selecting different database names for each topology.

- Install Mongo 2.6 or higher (3.0 or higher recommended)

> sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10

> echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb.list

> sudo apt-get update

> sudo apt-get install mongodb-org=3.0.0 mongodb-org-server=3.0.0 mongodb-org-shell=3.0.0 mongodb-org-mongos=3.0.0 mongodb-org-tools=3.0.0

- Hold Mongo version if we want to prevent version upgrades in the future

> echo "mongodb-org hold" | sudo dpkg --set-selections

> echo "mongodb-org-server hold" | sudo dpkg --set-selections

> echo "mongodb-org-shell hold" | sudo dpkg --set-selections

> echo "mongodb-org-mongos hold" | sudo dpkg --set-selections

> echo "mongodb-org-tools hold" | sudo dpkg --set-selections


- Disable interface binding, so that clients can connect from anywhere

> sudo nano /etc/mongod.conf

Find and comment out the following line

~~~~~~
# bind_ip = 127.0.0.1
~~~~~~

- Start Mongo

> sudo service mongod start

- Create users for the database administration

> mongo

Give in the following commands in the Mongo shell:

~~~~~~
use admin
var schema = db.system.version.findOne({"_id" : "authSchema"})
schema.currentVersion = 3
db.system.version.save(schema)
db.createUser( { user: "dbowner", pwd: "dbownpwd", roles: [ { role: "dbOwner", db: "admin" } ] } )
use testdb
db.createUser( { user: "dbadmin", pwd: "dbpwd", roles: [ {role: "readWrite", db: "testdb"} ] } )
~~~~~~

After setting the authSchema version, we create a *dbowner* user (please use a different password) with the role dbOwner.
This user can do anything with any database on the server. It is intended for general database administration and will not be used by OpenZoo. Please use it with caution.

The last two lines create a *testdb database* and a user with read/write rights on that database.
Please use your own names for the database, the user and, of course, a different password.
The dbadmin credentials will be used later when creating a topology. The database name will be passed to the appropriate component through the OpenZUI.

- Enable authentication

> sudo nano /etc/mongod.conf

Find and comment out the following line for Mongo <= 3.0

~~~~~~
#noauth = true
~~~~~~

or add the following for Mongo > 3.0

~~~~~~
security:
  authorization: enabled
~~~~~~

- Restart Mongo

> sudo service mongod restart

- Check authentication

> mongo testdb –u dbadmin –p dbpwd

- Install something like [robomongo](http://robomongo.org/) for managing the database, if needed.



# Installation of the OpenZoo framework
1. Download the whole repository, containing the following services:
    - **OpenZUI**: Administration GUI for the OpenZoo framework
    - **OpenZooService**: The basic service functionality, to be inherited by all services
    - **ServerResources**: Web service for retrieving server capacity/load information, to be (automatically) installed on every OpenZoo server
    - **ImageDownloader**: Test (dummy) service for downloading images
    - **MongoManager**: Test service for accessing MongoDB
    - **TwitterListener**: Test service for wrapping the TwitterStream API
    - **URLUnwrapper**: Test service for unwrapping short URLs
    - **Researcher**: Test service for forwarding user requests

2. Run Tomcat 7 server on all available servers and create a password protected user on each tomcat server, as described in the section above.

3. Install RedisIO on a server, preferably on the same server where OpenZUI will be installed, as described in the section above.

4. Open all projects with Netbeans 8.

5. Build projects, starting by ServerResources, since it needs to be included in the OpenZUI war later. The inclusion is going to be done automatically (on -post-dist).

6. Edit OpenZUI/web/config.json:
    - update keyvalue.host and keyvalue.port with the RedisIO server and port
    - update localRepository with a folder with read/write permissions for the tomcat user, anywhere on the server where OpenZUI will be installed.
    - update demouser.username and demouser.passwd, if necessary

7. Deploy OpenZUI project to a tomcat server. Everything else can be done through OpenZUI, which will be accessible through http://TOMCAT_SERVER:TOMCAT_PORT/OpenZUI/.
