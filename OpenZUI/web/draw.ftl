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
            <form class="form-horizontal" method="POST" action="ProcessTopology">
              <h4>Edit topology</h4>
              <div class="form-group">
                <label for="topo-name" class="col-sm-4 control-label">Name</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-name" value="${topology.name}">
                </div>
              </div>
              <div class="form-group">
                <label for="topo-descr" class="col-sm-4 control-label">Description</label>
                <div class="col-sm-8">
                  <textarea type="text" class="form-control" name="topo-descr"  rows="2" >${topology.description}</textarea>
                </div>
              </div>
              <h5>RabbitMQ</h5>
              <div class="form-group">
                <label for="topo-rabbit-host" class="col-sm-4 control-label">Host</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-rabbit-host" value="${topology.rabbit_host}">
                 </div>
              </div>
              <div class="form-group">
                <label for="topo-rabbit-port" class="col-sm-4 control-label">Port</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-rabbit-port" value="${topology.rabbit_port?string.computer}">
                 </div>
              </div>
              <div class="form-group">
                <label for="topo-rabbit-user" class="col-sm-4 control-label">User</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-rabbit-user" value="${topology.rabbit_user}">
                 </div>
              </div>
              <div class="form-group">
                <label for="topo-rabbit-pass" class="col-sm-4 control-label">Password</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-rabbit-pass" value="${topology.rabbit_passwd}">
                 </div>
              </div>
              <h5>MongoDB</h5>
              <div class="form-group">
                <label for="topo-mongo-host" class="col-sm-4 control-label">Host</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-mongo-host" value="${topology.mongo_host}">
                 </div>
              </div>
              <div class="form-group">
                <label for="topo-mongo-port" class="col-sm-4 control-label">Port</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-mongo-port" value="${topology.mongo_port?string.computer}">
                 </div>
              </div>
              <div class="form-group">
                <label for="topo-mongo-user" class="col-sm-4 control-label">User</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-mongo-user" value="${topology.mongo_user}">
                 </div>
              </div>
              <div class="form-group">
                <label for="topo-mongo-pass" class="col-sm-4 control-label">Password</label>
                <div class="col-sm-8">
                  <input type="text" class="form-control" name="topo-mongo-pass" value="${topology.mongo_passwd}">
                 </div>
              </div>
              <div class="form-group">
                <div class="col-sm-offset-2 col-sm-10">
                  <button type="submit" class="btn btn-success pull-right" id="submitTopologyBtn">Ready</button>
                </div>
              </div>
            </form>
          </div>

        </div>
    
        <div class="col-md-8 col-sm-6">
         
         
          <div class="panel panel-default">
            <div class="panel-heading"><a href="#" class="pull-right">click row to edit</a> <h4>Services invoked</h4></div>
            <div class="panel-body">
              
              <div class="table-responsive">
                <table class="table table-condensed table-hover">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Name</th>
                      <th>Requires</th>
                    </tr>
                  </thead>
                  <tbody>
                    <#assign row=1>
                    <#list services as service>
                      <tr onclick="detailsService('${service.filename}');">
                        <td>${row}</td>
                        <td>${service.filename}</td>
                        <td>
                        <#list service.requires as require>
                        ${require} 
                        </#list>
                        </td>
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

  <!-- script references -->
    <script src="./libs/jquery/jquery-1.11.3.min.js"></script>
    <script src="./libs/bootstrap/js/bootstrap.min.js"></script>
    <script src="./js/scripts.js"></script>
  </body>
</html>