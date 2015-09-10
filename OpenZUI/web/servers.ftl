<!DOCTYPE html>
<html lang="en">
	<head>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8">
		<meta charset="utf-8">
		<title>OpenZoo management tool</title>
		<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
		<link href="./libs/bootstrap/css/bootstrap.min.css" rel="stylesheet">
		<link rel="stylesheet" href="./libs/font-awesome-4.4.0/css/font-awesome.min.css">
    <!--[if lt IE 9]>
			<script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
		<link href="./css/styles.css" rel="stylesheet">
	</head>
	<body>
<nav class="navbar navbar-fixed-top header">
 	<div class="col-md-12">
        <div class="navbar-header">
          
          <a href="index.html" class="navbar-brand"><img height="40" src="./img/openzoo.png"></img></a>
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navbar-collapse1">
          <i class="glyphicon glyphicon-search"></i>
          </button>
      
        </div>
        <div class="collapse navbar-collapse" id="navbar-collapse1">
          <form class="navbar-form pull-left">
              <div class="input-group" style="max-width:470px;">
                <input type="text" class="form-control" placeholder="Search" name="srch-term" id="srch-term">
                <div class="input-group-btn">
                  <button class="btn btn-default btn-primary" type="submit"><i class="glyphicon glyphicon-search"></i></button>
                </div>
              </div>
          </form>
          <ul class="nav navbar-nav navbar-right">
             <li><a href="https://github.com/VisualComputingLab" target="_ext">github</a></li>
             <li>
                <a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="glyphicon glyphicon-bell"></i></a>
                <ul class="dropdown-menu">
                  <li><a href="#"><span class="badge pull-right">40</span>Link</a></li>
                  <li><a href="#"><span class="badge pull-right">2</span>Link</a></li>
                  <li><a href="#"><span class="badge pull-right">0</span>Link</a></li>
                  <li><a href="#"><span class="label label-info pull-right">1</span>Link</a></li>
                  <li><a href="#"><span class="badge pull-right">13</span>Link</a></li>
                </ul>
             </li>
             <li><a href="#" id="btnToggle"><i class="glyphicon glyphicon-th-large"></i></a></li>
             <li><a href="#"><i class="glyphicon glyphicon-user"></i></a></li>
           </ul>
        </div>	
     </div>	
</nav>
<div class="navbar navbar-default" id="subnav">
    <div class="col-md-12">
        <div class="collapse navbar-collapse" id="navbar-collapse2">
          <ul class="nav navbar-nav navbar-right">
             <li class="active"><a href="#">Dashboard</a></li>
             <li><a href="#loginModal" role="button" data-toggle="modal">Login</a></li>
             <li><a href="#aboutModal" role="button" data-toggle="modal">About</a></li>
           </ul>
        </div>	
     </div>	
</div>

<!--main-->
<div class="container" id="main">
   <div class="row">
   <div class="col-md-4 col-sm-6">
        
     <div class="well"> 
             <form class="form-horizontal" method="POST" action="Servers">
              <h4>Add new server</h4>
                  <div class="form-group">
                  <label for="srv-name" class="col-sm-4 control-label">server name</label>
                  <div class="col-sm-8">
                  <input type="text" class="form-control" name="srv-name" placeholder="server name">
                  <input type="hidden" class="form-control" name="action" value="create">
                  </div>
                  </div>
                  <div class="form-group">
                  <label for="srv-ip" class="col-sm-4 control-label">IP address</label>
                  <div class="col-sm-8">
                  <input type="text" class="form-control" name="srv-ip" placeholder="ip address">
                  </div>
                  </div>
                  <div class="form-group">
                  <label for="tmc-port" class="col-sm-4 control-label">Tomcat port</label>
                  <div class="col-sm-8">
                  <input type="text" class="form-control" name="tmc-port" placeholder="tomcat port">
                  </div>
                  </div>
                  <div class="form-group">
                  <label for="tmc-user" class="col-sm-4 control-label">Tomcat user</label>
                  <div class="col-sm-8">
                  <input type="text" class="form-control" name="tmc-user" placeholder="username">
                  </div>
                  </div>
                  <div class="form-group">
                  <label for="tmc-pass" class="col-sm-4 control-label">Tomcat pass</label>
                  <div class="col-sm-8">
                  <input type="text" class="form-control" name="tmc-pass" placeholder="password">
                  </div>
                  </div>
                  <div class="form-group">
                  <div class="col-sm-offset-2 col-sm-10">
                  <button type="submit" class="btn btn-success pull-right" id="createBtn">Post</button>
                  </div>
                  </div>
        </form>
        </div>

       
     

	</div>
  	<div class="col-md-8 col-sm-6">
      	 
         
         <div class="panel panel-default">
          <div class="panel-heading"><a href="#" class="pull-right">View all</a> <h4>Configured servers</h4></div>
        <div class="panel-body">
              
        <div class="table-responsive">
        <table class="table table-condensed table-hover">
          <thead>
            <tr>
              <th>#</th>
              <th>name</th>
              <th>IP address</th>
              <th>tmc port</th>
              <th>tmc user</th>
              <th>tmc pass</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <#assign row=1>
            <#list servers as server>
              <tr onclick="detailsServer('${server.name}', '${server.address}', '${server.port?string.computer}', '${server.user}', '${server.passwd}', '${server.status}');">
                <td>${row}</td>
                <td>${server.name}</td>
                <td>${server.address}</td>
                <td>${server.port?string.computer}</td>
                <td>${server.user}</td>
                <td>${server.passwd}</td>
                <td><i class="${server.status} fa fa-check-circle fa-1x"></i></td>
              </tr>
              <#assign row = row + 1>
            </#list>
          </tbody>
        </table>
      </div>
       </div>
      </div>
      
  	</div>
  	
  </div><!--/row-->

  
    <div class="clearfix"></div>
      
    <hr>
    <div class="col-md-12 text-center">
      <p>
        <a href="http://openzoo.org" target="_ext">openzoo website</a>
        <br>
        <a href="http://vcl.iti.gr" target="_ext">vcl@iti.gr</a> || <a href="http://www.certh.gr" target="_ext">CERTH</a>
      </p>
    </div>
    
    
  </div>
</div><!--/main-->

<!--login modal-->
<div id="loginModal" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
          <h2 class="text-center">
            <!-- <img src="./img/openzoo.png" height="50" class="img-circle"><br> -->
            Login</h2>
      </div>
      <div class="modal-body">
          <form class="form col-md-12 center-block">
            <div class="form-group">
              <input type="text" class="form-control input-lg" placeholder="Email">
            </div>
            <div class="form-group">
              <input type="password" class="form-control input-lg" placeholder="Password">
            </div>
            <div class="form-group">
              <button class="btn btn-primary btn-lg btn-block">Sign In</button>
              <span class="pull-right"><a href="#">Register</a></span><span><a href="#">Need help?</a></span>
            </div>
          </form>
      </div>
      <div class="modal-footer">
          <div class="col-md-12">
          <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
		  </div>	
      </div>
  </div>
  </div>
</div>


<!--about modal-->
<div id="aboutModal" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
          <h2 class="text-center">About</h2>
      </div>
      <div class="modal-body">
          <div class="col-md-12 text-center">
            <a href=""><strong>OpenZoo</strong> </a>was made with <i class="glyphicon glyphicon-heart"></i> <br> by the <a href="http://vcl.iti.gr">Visual Computing Lab</a>
            <br><br>
            <a href="http://www.openzoo.org">www.openzoo.org</a>
          </div>
      </div>
      <div class="modal-footer">
          <button class="btn" data-dismiss="modal" aria-hidden="true">OK</button>
      </div>
  </div>
  </div>
</div>

<!-- details modal-->
<div class="modal fade" id="detailsModal">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h2 class="text-center">Details</h2>
      </div>
      <form class="form-horizontal" method="POST" action="Servers">
        <div class="modal-body" id="detailsModalBody">
          <div class="form-group">
            <label for="srv-upd-name" class="col-sm-4 control-label">server name</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="srv-upd-name" name="srv-name" readonly >
              <input type="hidden" class="form-control" id="post-action" name="action" value="">
            </div>
          </div>
          <div class="form-group">
            <label for="srv-upd-ip" class="col-sm-4 control-label">IP address</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="srv-upd-ip" name="srv-ip">
            </div>
          </div>
          <div class="form-group">
            <label for="tmc-upd-port" class="col-sm-4 control-label">Tomcat port</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="tmc-upd-port" name="tmc-port">
            </div>
          </div>
          <div class="form-group">
            <label for="tmc-upd-user" class="col-sm-4 control-label">Tomcat user</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="tmc-upd-user" name="tmc-user">
            </div>
          </div>
          <div class="form-group">
            <label for="tmc-upd-pass" class="col-sm-4 control-label">Tomcat pass</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="tmc-upd-pass" name="tmc-pass">
            </div>
          </div>
          <div class="form-group">
            <label for="tmc-upd-status" class="col-sm-4 control-label">Status</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="tmc-upd-status" name="tmc-status" readonly >
            </div>
          </div>
          <div class="form-group">
            <label for="stats-cpu" class="col-sm-4 control-label">CPU Usage</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="stats-cpu" readonly >
            </div>
          </div>
          <div class="form-group">
            <label for="stats-mem" class="col-sm-4 control-label">Memory Usage</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="stats-mem" readonly >
            </div>
          </div>
          <div class="form-group">
            <label for="stats-disc" class="col-sm-4 control-label">Disc Usage</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="stats-disc" readonly >
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <!--<button class="btn" data-dismiss="modal" aria-hidden="true">OK</button>-->
          <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
          <button id="deleteBtn" type="submit" class="btn btn-primary">Delete</button>
          <button id="updateBtn" type="submit" class="btn btn-primary">Submit</button>
        </div>
      </form>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /. details modal end -->

	<!-- script references -->
		<script src="./libs/jquery/jquery-1.11.3.min.js"></script>
		<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
		<script src="./js/scripts.js"></script>
	</body>
</html>