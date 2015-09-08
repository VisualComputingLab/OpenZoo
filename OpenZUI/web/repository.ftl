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
        <!-- <div class="navbar-header">
          
          <a href="#" style="margin-left:40px;" class="navbar-btn btn btn-default btn-plus dropdown-toggle" data-toggle="dropdown"><i class="glyphicon glyphicon-home" style="color:#dd1111;"></i> Home <small><i class="glyphicon glyphicon-chevron-down"></i></small></a>
          <ul class="nav dropdown-menu">
              <li><a href="#"><i class="glyphicon glyphicon-user" style="color:#1111dd;"></i> Profile</a></li>
              <li><a href="#"><i class="glyphicon glyphicon-dashboard" style="color:#0000aa;"></i> Dashboard</a></li>
              <li><a href="#"><i class="glyphicon glyphicon-inbox" style="color:#11dd11;"></i> Pages</a></li>
              <li class="nav-divider"></li>
              <li><a href="#"><i class="glyphicon glyphicon-cog" style="color:#dd1111;"></i> Settings</a></li>
              <li><a href="#"><i class="glyphicon glyphicon-plus"></i> More..</a></li>
          </ul>
          
          
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navbar-collapse2">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          </button>
      
        </div> -->
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
          <form class="form-horizontal" method="POST" action="Repository">
            <h4>Repository parameters / FTP</h4>
            <div class="form-group">
              <label for="ftp-host" class="col-sm-4 control-label">host</label>
              <div class="col-sm-8">
                <input type="text" class="form-control" name="ftp-host" value="${ftp.host}">
              </div>
            </div>
            <div class="form-group">
              <label for="ftp-port" class="col-sm-4 control-label">port</label>
              <div class="col-sm-8">
                <input type="text" class="form-control" name="ftp-port" value="${ftp.port}">
              </div>
            </div>
            <div class="form-group">
              <label for="ftp-username" class="col-sm-4 control-label">username</label>
              <div class="col-sm-8">
                <input type="text" class="form-control" name="ftp-user" value="${ftp.user}">
              </div>
            </div>
            <div class="form-group">
              <label for="ftp-pass" class="col-sm-4 control-label">password</label>
              <div class="col-sm-8">
                <input type="text" class="form-control" name="ftp-pass" value="${ftp.passwd}">
              </div>
            </div>
            <div class="form-group">
              <label for="repo-path" class="col-sm-4 control-label">Repo path</label>
              <div class="col-sm-8">
                <input type="text" class="form-control" name="ftp-path" value="${ftp.path}">
              <input type="hidden" class="form-control" name="action" value="updateRepo">
            </div>
            </div>
            <div class="form-group">
              <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-success pull-right">Update</button>
              </div>
            </div>
          </form>
        </div>

       
     

	</div>
  	<div class="col-md-4 col-sm-6">
      <div class="panel panel-default">
        <div class="panel-heading">
          <h4>Upload WAR files</h4>
        </div>
        <div class="panel-body">
             
          <form class="form-horizontal" action="Repository" method="POST" enctype="multipart/form-data">
            <div class="form-group">
              <label for="fileToUpload" class="col-sm-4 control-label">Select file:</label>
              <div class="col-sm-8">
                <input type="file" name="fileToUpload" id="fileToUpload">
                <input type="hidden" class="form-control" name="action" value="uploadFile">
              </div>
            </div>
            <div class="form-group">
              <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-success pull-right">Upload</button>
              </div>
            </div>
          </form>
        </div>
      </div>
      
  	</div>

    <div class="col-md-4 col-sm-6">
         
         
         <div class="panel panel-default">
          <div class="panel-heading"><a href="#" class="pull-right">View all</a> <h4>WAR files in repo</h4></div>
          <div class="panel-body">
              
              <div class="table-responsive">
        <table class="table table-condensed table-hover">
          <thead>
            <tr>
              <th>#</th>
              <th>Filename</th>
              <th>Folder</th>
              <th>Version</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <#assign row=1>
            <#list warfiles as warfile>
              <tr onclick="detailsWarfile('${warfile.filename}', '${warfile.folder}', '${warfile.version}', '${warfile.status}');">
                <td>${row}</td>
                <td>${warfile.filename}</td>
                <td>${warfile.folder}</td>
                <td>${warfile.version}</td>
                <td><i class="${warfile.status} fa fa-check-circle fa-1x"></i></td>
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

  
  <!--
  <hr>
   <div class="row">
    <div class="col-md-12"><h2>Repository</h2></div>
    <div class="col-md-4 col-sm-6"></div>
    <div class="col-md-4 col-sm-6"></div>
    <div class="col-md-4 col-sm-6"></div>
  </div> -->
 
  	
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
      <form class="form-horizontal" method="POST" action="Repository">
        <div class="modal-body" id="detailsModalBody">
        
          <div class="form-group">
            <label for="war-upd-filename" class="col-sm-4 control-label">filename</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="war-upd-filename" name="war-filename" readonly >
              <input type="hidden" class="form-control" id="post-action" name="action" value="">
            </div>
          </div>
          <div class="form-group">
            <label for="war-upd-folder" class="col-sm-4 control-label">folder</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="war-upd-folder" name="war-folder" readonly >
            </div>
          </div>
          <div class="form-group">
            <label for="war-upd-version" class="col-sm-4 control-label">version</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="war-upd-version" name="war-version" readonly >
            </div>
          </div>
          <div class="form-group">
            <label for="war-upd-status" class="col-sm-4 control-label">status</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="war-upd-status" name="war-status" readonly >
            </div>
          </div>

        </div>
      <div class="modal-footer">
          <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
          <button id="deleteBtn" type="submit" class="btn btn-primary">Delete</button>
          <!--<button id="submitBtn" type="submit" class="btn btn-primary">Submit</button>-->
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /. details modal end -->

	<!-- script references -->
		<script src="./libs/jquery/jquery-1.11.3.min.js"></script>
		<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
		<script src="./js/scripts.js"></script>
	</body>
</html>