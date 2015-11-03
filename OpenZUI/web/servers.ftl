<!DOCTYPE html>
<html lang="en">
  <#include "header.ftl">
	<body>
    <#include "navigation.ftl">

<!--main-->
<div class="container page-wrap" id="main">
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
          <div class="panel-heading"><h4>Configured servers</h4></div>
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
            <#if servers??>
              <#assign row=1>
              <#list servers as server>
                <tr onclick="detailsServer('${server.name}');">
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
            </#if>
          </tbody>
        </table>
      </div>
       </div>
      </div>
      
  	</div>
  	
  </div><!--/row-->

  
    <div class="clearfix"></div>
          
    
  </div>
</div><!--/main-->

<#include "footer.ftl">

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