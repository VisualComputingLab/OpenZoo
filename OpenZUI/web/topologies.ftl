<!DOCTYPE html>
<html lang="en">
	<#include "header.ftl">
	<body>
    <#include "navigation.ftl">

<!--main-->
<div class="container" id="main">
   <div class="row">
   <div class="col-md-4 col-sm-6">
        
     <div class="well"> 
        <form class="form-horizontal" method="POST" action="Topologies">
          <h4>Create new topology</h4>
          <div class="form-group">
            <label for="topo-name" class="col-sm-4 control-label">Name</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-name" placeholder="topology name">
              <input type="hidden" class="form-control" name="action" value="create">
             </div>
          </div>
          <div class="form-group">
            <label for="topo-descr" class="col-sm-4 control-label">Description</label>
            <div class="col-sm-8">
              <textarea type="text" class="form-control" name="topo-descr"  rows="2" ></textarea>
            </div>
          </div>
          <h5>RabbitMQ</h5>
          <div class="form-group">
            <label for="topo-rabbit-host" class="col-sm-4 control-label">Host</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-rabbit-host" placeholder="host">
             </div>
          </div>
          <div class="form-group">
            <label for="topo-rabbit-port" class="col-sm-4 control-label">Port</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-rabbit-port" placeholder="port">
             </div>
          </div>
          <div class="form-group">
            <label for="topo-rabbit-user" class="col-sm-4 control-label">User</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-rabbit-user" placeholder="user">
             </div>
          </div>
          <div class="form-group">
            <label for="topo-rabbit-pass" class="col-sm-4 control-label">Password</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-rabbit-pass" placeholder="password">
             </div>
          </div>
          <h5>MongoDB</h5>
          <div class="form-group">
            <label for="topo-mongo-host" class="col-sm-4 control-label">Host</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-mongo-host" placeholder="host">
             </div>
          </div>
          <div class="form-group">
            <label for="topo-mongo-port" class="col-sm-4 control-label">Port</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-mongo-port" placeholder="port">
             </div>
          </div>
          <div class="form-group">
            <label for="topo-mongo-user" class="col-sm-4 control-label">User</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-mongo-user" placeholder="user">
             </div>
          </div>
          <div class="form-group">
            <label for="topo-mongo-pass" class="col-sm-4 control-label">Password</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="topo-mongo-pass" placeholder="password">
             </div>
          </div>
          <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
              <button type="submit" class="btn btn-success pull-right" id="createBtn">Create</button>
            </div>
          </div>
        </form>
        </div>

       
     

	</div>
  	
    <div class="col-md-8 col-sm-6">
         
         
         <div class="panel panel-default">
          <div class="panel-heading"><a href="#" class="pull-right">click row to edit</a> <h4>Topologies</h4></div>
          <div class="panel-body">
              
        <div class="table-responsive">
        <table class="table table-condensed table-hover">
          <thead>
            <tr>
              <th>#</th>
              <th>Name</th>
              <th>Description</th>
              <th>RabbitMQ</th>
              <th>MongoDB</th>
            </tr>
          </thead>
          <tbody>
            <#assign row=1>
            <#list topologies as topo>
              <tr onclick="detailsTopology('${topo.name}');">
                <td>${row}</td>
                <td>${topo.name}</td>
                <td>${topo.description}</td>
                <td>${topo.rabbit_host}:${topo.rabbit_port?string.computer}@${topo.rabbit_user}:${topo.rabbit_passwd}</td>
                <td>${topo.mongo_host}:${topo.mongo_port?string.computer}@${topo.mongo_user}:${topo.mongo_passwd}</td>
              </tr>
            <#assign row = row + 1>
            </#list>
          </tbody>
        </table>
      </div>
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


<!-- details modal-->
<div class="modal fade" id="detailsModal">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
          <h2 class="text-center">Details</h2>
      </div>
      <form class="form-horizontal" method="POST" action="Topologies">
        <div class="modal-body" id="detailsModalBody">
          <div class="form-group">
            <label for="topo-upd-name" class="col-sm-4 control-label">Topology name</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-name" name="topo-name" readonly >
              <input type="hidden" class="form-control" id="post-action" name="action" value="">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-descr" class="col-sm-4 control-label">Description</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-descr" name="topo-descr">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-rhost" class="col-sm-4 control-label">RabbitMQ host</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-rhost" name="topo-rabbit-host">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-rport" class="col-sm-4 control-label">RabbitMQ port</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-rport" name="topo-rabbit-port">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-ruser" class="col-sm-4 control-label">RabbitMQ user</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-ruser" name="topo-rabbit-user">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-rpass" class="col-sm-4 control-label">RabbitMQ pass</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-rpass" name="topo-rabbit-pass">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-mhost" class="col-sm-4 control-label">MongoDB host</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-mhost" name="topo-mongo-host">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-mport" class="col-sm-4 control-label">MongoDB port</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-mport" name="topo-mongo-port">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-muser" class="col-sm-4 control-label">MongoDB user</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-muser" name="topo-mongo-user">
            </div>
          </div>
          <div class="form-group">
            <label for="topo-upd-mpass" class="col-sm-4 control-label">MongoDB pass</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="topo-upd-mpass" name="topo-mongo-pass">
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
          <button id="deleteBtn" type="submit" class="btn btn-primary">Delete</button>
          <button id="updateBtn" type="submit" class="btn btn-primary">Update</button>
          <button id="deployBtn" type="submit" class="btn btn-primary">Deploy</button>
          <button id="undeployBtn" type="submit" class="btn btn-primary">Undeploy</button>
          <button id="startBtn" type="submit" class="btn btn-primary">Start</button>
          <button id="stopBtn" type="submit" class="btn btn-primary">Stop</button>
        </div>
      </form>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div><!-- /. details modal end -->

  <#include "login-about.ftl">

	<!-- script references -->
		<script src="./libs/jquery/jquery-1.11.3.min.js"></script>
		<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
		<script src="./js/scripts.js"></script>
	</body>
</html>