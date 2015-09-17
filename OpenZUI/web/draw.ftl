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
    <link href="./css/joint.css" rel="stylesheet">
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
        <div class="col-md-3">
          <div class="well">
            <!--<h4><label>name:</label> <span id="topologyName">$(topology_name)</span></h4>-->
            <h4><label>name:</label> <span id="topologyName">${topology_name}</span></h4>
            <form class="form-horizontal" id="topoSubmitForm" method="POST" action="ProcessTopology">
              <input type="hidden" class="form-control" name="topo-name" value="${topology_name}">
              <input type="hidden" class="form-control" name="topo-graph" id="topo-graph" >
              <div type="button" id="submitTopoBtn" class="btn btn-success"><i class="fa fa-send"></i> Submit</div>
            </form>
          </div> 
        </div>
        <div class="col-md-6">
          <div class="well">
            <h4>Add OpenZoo Service / link</h4>
            <form class="form-inline">
              <div class="form-group">
                <div class="input-group">
                  <div class="input-group-addon"><i class="fa fa-cube"></i></div>
                  <select class="form-control" id="openzooServiceSelect">
                  </select>
                </div>
              </div>
              <div type="button" id="openzooServiceSelectBtn" class="btn btn-primary">insert</div>
              <div type="button" id="addLinkBtn" class="btn btn-primary"><i class="fa fa-link"></i> Add link</div>
            </form>
          </div> 
        </div>

        <!--div class="col-md-3">
        <div class="well">
        <h4>Add new data path</h4>
        <div type="button" id="addLinkBtn" class="btn btn-primary"><i class="fa fa-link"></i> Add link</div-->
        <!--div type="button" id="add1wayLinkBtn" class="btn btn-primary"><i class="fa fa-link"></i> Add 1 way link</div-->
        <!--div type="button" id="add2wayLinkBtn" class="btn btn-primary"><i class="fa fa-link"></i> Add 2 way link</div-->
        <!--/div>    
        </div-->
      
      </div>
        
      <div class="row">
        <div class="col-md-9">
          <div id="paper" class="topologyBox">
          </div>   
        </div>
        <div class="col-md-3"> 
          <div class="row" id="service_manager">
            <div class="well">
              <form id="service_form"> 
                <label for="instances">instances</label>
                <input type="number" class="form-control" id="instances" name="instances">
                <label for="wpc">workers per core</label>
                <input type="number" class="form-control" id="wpc" name="wpc">
                <hr>
                <input type="submit" id="save_service_config" class="btn btn-default" value="Save" />
              </form>
            </div>
          </div>

          <div class="row" id="connection_manager">
            <div class="well">
              <form id="connection_form">
                <div class="form-group">
                  <label for="outEndpointsList"> source </label>
                  <select class="form-control" id="outEndpointsList" name="outEndpointsList">
                  </select>
                  <label for="inEndpointsList"> target </label>
                  <select class="form-control" id="inEndpointsList" name="inEndpointsList">
                  </select>
                  <label for="conn_mapping">connection type </label>
                  <select class="form-control" id="conn_mapping" name="conn_mapping">
                    <option value="conn_all">All</option>
                    <option value="conn_available" selected>Available</option>
                    <option value="conn_route">Route</option>
                  </select>
                  <div id="routing_field">
                    <label for="routing">routing keys</label>
                    <input type="text" class="form-control" id="routing" name="routing">
                  </div>
                </div>
                <input type="submit" id="save_conn_config" class="btn btn-default" value="Save" />
              </form>
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


    <!-- details modal-->
    <div class="modal fade" id="detailsModal">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h2 class="text-center">Details</h2>
          </div>
          <div class="modal-body" id="detailsModalBody">
          </div>
          <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">OK</button>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /. details modal end -->

    <!-- script references -->
    <script src="./libs/jquery/jquery-1.11.3.min.js"></script>
  	<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
  	<script src="./js/scripts.js"></script>

    <script src="./js/joint.js"></script>
    <script src="./js/joint.shapes.uml.js"></script>
    <script src="./js/umlsc.js"></script>

    <!--script src="./js/joint.shapes.openzoo.js"></script>
    <script src="./js/openzoo.js"></script-->
    <!--script src="./js/joint.shapes.devs.js"></script>
    <script src="./js/devs.js"></script -->
  </body>
</html>