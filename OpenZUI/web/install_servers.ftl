<!DOCTYPE html>
<html lang="en">
  <#include "header.ftl">
	<body>
    <#include "navigation.ftl">

<!--main-->
<div class="container page-wrap" id="main">

  <h3>Server setup</h3>
  <div class="row">
    <div class="panel-group" id="accordion">

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapse0"><h4>General</h4></a>
          </h4>
        </div>
        <div id="collapse0" class="panel-collapse collapse in">
          <div class="panel-body">
            <p>For achieving a stream processing task, you need to define a topology (OZTopology).</p>
            <p>The topology is a set of interconnected components (OZServices), which are running on a cluster (OZCluster), consisting of servers (OZServer).</p>
            <p>The creation, update, initiation and monitoring of topologies is done over a graphical user interface (OpenZUI).</p>
          </div>
        </div>
      </div>
    </div>
  </div>
  

  <h3>A. Definition and requirements for various framework components</h3>
  <div class="row">
    <div class="panel-group" id="accordionA">

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseA1"><h4>OpenZUI</h4></a>
          </h4>
        </div>
        <div id="collapseA1" class="panel-collapse collapse">
          <div class="panel-body">
            <p>The OpenZUI is a Tomcat application, which uses MongoDB to store and exchange parameters with the OZServices.</p>
          </div>
        </div>
      </div>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseA2"><h4>OZServer</h4></a>
          </h4>
        </div>
        <div id="collapseA2" class="panel-collapse collapse">
          <div class="panel-body">
            <p>An OZServer is the building unit of an OZCluster. It is an ordinary server with a fixed (static) IP address, running either Linux or Windows and Apache Tomcat.</p>
          </div>
        </div>
      </div>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseA3"><h4>OZTopology</h4></a>
          </h4>
        </div>
        <div id="collapseA3" class="panel-collapse collapse">
          <div class="panel-body">
            <p>An OZTopology is a graph-like structure with OZServices as nodes and RabbitMQ queues as connections. The processing results of the topology are usually saved in a database, in our case MongoDB. You can use one RabbitMQ/MongoDB server per topology, or use one for all your topologies.</p>
          </div>
        </div>
      </div>

    </div>
  </div>

  <h3>B. Step by step instructions for installation/configuration (<a href="http://openzoo.org/install/" target="_blank">Updated and more accurate instgructions</a>)</h3>
  

  <div class="row">
    <div class="panel-group" id="accordionB">

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseB1"><h4>OZServer OS</h4></a>
          </h4>
        </div>
        <div id="collapseB1" class="panel-collapse collapse">
          <div class="panel-body">
            <p>OpenZoo has been tested on 64bit Ubuntu 14.04 LTS Server and Windows XP/7/10. All instructions in the following describe the case of Ubuntu 14.04 LTS.</p>
            <p>The disc space, CPU power and memory consumption depends on the application, but it should have at least 2 GB of RAM and 10 GB of free disc space.</p>
            <p>Be sure to install the latest OS updates. E.g. for Ubuntu:</p>
              <p class="p1tab">> sudo apt-get update</p>
              <p class="p1tab">> sudo apt-get dist-upgrade</p>
          </div>
        </div>
      </div>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseB2"><h4>Static IP</h4></a>
          </h4>
        </div>
        <div id="collapseB2" class="panel-collapse collapse">
          <div class="panel-body">
            <p>An OZServer installed e.g. on a VirtualBox in some corporate network can be configured to use a static IP (as long as the network supports it), as follows (example IP: 111.111.111.11):</p>
              <p class="p1tab">> sudo nano /etc/network/interfaces</p>
                <p class="p2tab">auto eth0</p>
                <p class="p2tab">iface eth0 inet static</p>
                <p class="p2tab">address 111.111.111.11</p>
                <p class="p2tab">netmask 255.255.255.0</p>
                <p class="p2tab">network 111.111.111.0</p>
                <p class="p2tab">gateway 111.111.111.1</p>
                <p class="p2tab">broadcast 111.111.111.255</p>
                <p class="p2tab">dns-nameservers 111.111.111.1</p>
              <p class="p1tab">> sudo /etc/init.d/networking restart</p>
            <p>Ask your administrator for the appropriate values.</p>
          </div>
        </div>
      </div>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseB3"><h4>Apache Tomcat 7, Java 7</h4></a>
          </h4>
        </div>
        <div id="collapseB3" class="panel-collapse collapse">
          <div class="panel-body">
            <p>Install</p>
              <p class="p1tab">> sudo apt-get install tomcat7</p>
              <p class="p1tab">> sudo apt-get install default-jdk</p>
              <p class="p1tab">> sudo nano /etc/environment</p>
                <p class="p2tab">JAVA_HOME=/usr/lib/jvm/default-java</p>
                <p class="p2tab">CATALINA_HOME=/var/lib/tomcat7</p>
              <p class="p1tab">#reconnect</p>
              <p class="p1tab">> sudo nano /var/lib/tomcat7/conf/tomcat-users.xml</p>
                <p class="p2tab">&lt;role rolename=&quot;manager-gui&quot;/&gt;</p>
                <p class="p2tab">&lt;role rolename=&quot;manager-script&quot;/&gt;</p>
                <p class="p2tab">&lt;user username=&quot;admin&quot; password=&quot;OZ_SERVER_PASSWORD&quot; roles=&quot;manager-gui,manager-script&quot;/&gt;</p>
            <p>Set parameters</p>
              <p class="p1tab">Copy <a href="setenv.sh" target="_blank">setenv.sh</a> (created by Terrance A. Snyder) to /usr/share/tomcat7/bin/</p>
                <p class="p2tab">Set JAVA_HOME</p>
                <p class="p2tab">Set appropriate limits for –Xms, Xmx, MaxPermSize if neccessary.</p>
            <p>Restart tomcat</p>
              <p class="p1tab">> sudo service tomcat7 restart</p>
            <p>Install management application</p>
              <p class="p1tab">> sudo apt-get install tomcat7-admin</p>
          </div>
        </div>
      </div>

      <!-- <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseB4"><h4>RedisIO</h4></a>
          </h4>
        </div>
        <div id="collapseB4" class="panel-collapse collapse">
          <div class="panel-body">
            <p>Install</p>
              <p class="p1tab">> sudo nano /etc/apt/sources.list.d/dotdeb.org.list</p>
                <p class="p2tab">deb http://packages.dotdeb.org squeeze all</p>
                <p class="p2tab">deb-src http://packages.dotdeb.org squeeze all</p>
              <p class="p1tab">> wget -q -O - http://www.dotdeb.org/dotdeb.gpg | sudo apt-key add -</p>
              <p class="p1tab">> sudo apt-get update</p>
              <p class="p1tab">> sudo apt-get install redis-server</p>
              <p class="p1tab">> sudo nano /etc/redis/redis.conf</p>
                <p class="p2tab">bind 111.111.111.11 127.0.0.1 # replace with the IP of the server hosting redis</p>
              <p class="p1tab">> sudo service redis-server restart</p>
          </div>
        </div>
      </div> -->

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseB5"><h4>RabbitMQ</h4></a>
          </h4>
        </div>
        <div id="collapseB5" class="panel-collapse collapse">
          <div class="panel-body">
            <p>You can use one RabbitMQ server per topology, or use one for all your topologies.</p>
            <p>We will also create user qadmin with password qpwd.</p>
            <p>Install RabbitMQ</p>
              <p class="p1tab">> sudo nano /etc/apt/sources.list</p>
                <p class="p2tab">deb http://www.rabbitmq.com/debian/ testing main</p>
              <p class="p1tab">> wget http://www.rabbitmq.com/rabbitmq-signing-key-public.asc</p>
              <p class="p1tab">> sudo apt-key add rabbitmq-signing-key-public.asc</p>
              <p class="p1tab">> sudo apt-get update</p>
              <p class="p1tab">> sudo apt-get install rabbitmq-server</p>
              <p class="p1tab">> sudo /usr/lib/rabbitmq/bin/rabbitmq-plugins enable rabbitmq_management</p>
            <p>Create user and delete default user</p>
              <p class="p1tab">> sudo rabbitmqctl add_user qadmin qpwd</p>
              <p class="p1tab">> sudo rabbitmqctl set_user_tags qadmin administrator</p>
              <p class="p1tab">> sudo rabbitmqctl set_permissions qadmin ".*" ".*" ".*"</p>
              <p class="p1tab">> sudo rabbitmqctl delete_user guest</p>
              <p class="p1tab">> sudo service rabbitmq-server restart</p>
          </div>
        </div>
      </div>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#accordion" href="#collapseB6"><h4>MongoDB 3.0</h4></a>
          </h4>
        </div>
        <div id="collapseB6" class="panel-collapse collapse">
          <div class="panel-body">
            <p>MongoDB is used a) for exchanging parameters between the OpenZUI and the OZServices (blackboard) and b) for saving processing results.</p>
            <p>For a) there is one central MongoDB, defined in the config.json of your OpenZUI.</p>
            <p>For b) you can use one MongoDB server per topology, or use one for all your topologies.</p>
            <p>We will also create user dbadmin with password dbpwd and database topodb.</p>
            <p>Install Mongo</p>
              <p class="p1tab">> sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10</p>
              <p class="p1tab">> echo "deb http://repo.mongodb.org/apt/ubuntu "$(lsb_release -sc)"/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb.list</p>
              <p class="p1tab">> sudo apt-get update</p>
              <p class="p1tab">> sudo apt-get install mongodb-org=3.0.0 mongodb-org-server=3.0.0 mongodb-org-shell=3.0.0 mongodb-org-mongos=3.0.0 mongodb-org-tools=3.0.0</p>
              <p class="p1tab">> echo "mongodb-org hold" | sudo dpkg --set-selections</p>
              <p class="p1tab">> echo "mongodb-org-server hold" | sudo dpkg --set-selections</p>
              <p class="p1tab">> echo "mongodb-org-shell hold" | sudo dpkg --set-selections</p>
              <p class="p1tab">> echo "mongodb-org-mongos hold" | sudo dpkg --set-selections</p>
              <p class="p1tab">> echo "mongodb-org-tools hold" | sudo dpkg --set-selections</p>
              <p class="p1tab">> sudo service mongod start</p>
            <p>Disable interface binding</p>
              <p class="p1tab">> sudo nano /etc/mongod.conf</p>
                <p class="p2tab"># bind_ip = 127.0.0.1</p>
            <p>Enable authentication</p>
              <p class="p1tab">> mongo</p>
                <p class="p2tab">> use admin</p>
                <p class="p2tab">> db.createUser(</p>
                <p class="p2tab">{</p>
                <p class="p2tab">user: "dbadmin",</p>
                <p class="p2tab">pwd: "dbpwd",</p>
                <p class="p2tab">roles: [ { role: "userAdminAnyDatabase", db: "admin" } ]</p>
                <p class="p2tab">}</p>
                <p class="p2tab">)</p>
              <p class="p1tab">> sudo nano /etc/mongod.conf</p>
                <p class="p2tab">#noauth = true</p>
              <p class="p1tab">> sudo service mongod restart</p>
            <p>Check authentication</p>
              <p class="p1tab">> mongo topodb –u dbadmin –p dbpwd</p>
            <p>If no login possible:</p>
              <p class="p1tab">> sudo nano /etc/mongod.conf</p>
                <p class="p2tab">noauth = true</p>
              <p class="p1tab">> sudo service mongod restart</p>
              <p class="p1tab">> mongo</p>
                <p class="p2tab">> var schema = db.system.version.findOne({"_id" : "authSchema"})</p>
                <p class="p2tab">> schema.currentVersion = 3</p>
                <p class="p2tab">> db.system.version.save(schema)</p>
              <p class="p1tab">Recreate user</p>
                <p class="p2tab">> use topodb</p>
                <p class="p2tab">> db.createUser(</p>
                <p class="p2tab">{</p>
                <p class="p2tab">user: "dbadmin",</p>
                <p class="p2tab">pwd: "dbpwd",</p>
                <p class="p2tab">roles: [</p>
                <p class="p2tab">{role: "readWrite", db: "topodb"}</p>
                <p class="p2tab">]</p>
                <p class="p2tab">}</p>
                <p class="p2tab">)</p>
              <p class="p1tab">> sudo nano /etc/mongod.conf</p>
                <p class="p2tab">#noauth = true</p>
              <p class="p1tab">> sudo service mongod restart</p>
            <p>Check authentication again</p>
              <p class="p1tab">> mongo topodb –u dbadmin –p dbpwd</p>
            <p>Install something like <a href="http://robomongo.org/" target="_blank">robomongo</a> for managing the database, if needed.</p>
          </div>
        </div>
      </div>

    </div>
  </div>

</div><!--/main-->

<#include "footer.ftl">


  <#include "login-about.ftl">

	<!-- script references -->
		<script src="./libs/jquery/jquery-1.11.3.min.js"></script>
		<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
    <script src="./js/alertify.js"></script>
		<script src="./js/scripts.js"></script>

    <script>
      var logcontainer = [];
      <#if logs??>
        <#list logs as logline>
          logcontainer.push("${logline}");
        </#list>
      </#if>
    </script>

	</body>
</html>