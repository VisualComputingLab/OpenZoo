<!DOCTYPE html>
<html lang="en">
	<#include "header.ftl">
	<body>
    <#include "navigation.ftl">

<!--main-->
<div class="container page-wrap" id="main">
  <div class="row">
  

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

    <div class="col-md-8 col-sm-6">
         
      <div class="panel panel-default">
        <div class="panel-heading"><h4>WAR files in repo</h4></div>
        <div class="panel-body">
          <div class="table-responsive">
            <table class="table table-condensed table-hover">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Comp ID</th>
                  <th>Description</th>
                  <th>Filename</th>
                  <th>Version</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <#if warfiles??>
                  <#assign row=1>
                  <#list warfiles as warfile>
                    <tr onclick="detailsWarfile('${warfile.component_id}');">
                      <td>${row}</td>
                      <td>${warfile.component_id}</td>
                      <td>${warfile.description}</td>
                      <td>${warfile.filename}</td>
                      <td>${warfile.version}</td>
                      <td><i class="${warfile.status} fa fa-check-circle fa-1x"></i></td>
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
            <label for="war-upd-compoid" class="col-sm-4 control-label">component id</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="war-upd-compoid" name="war-compoid" readonly >
            </div>
          </div>
          <div class="form-group">
            <label for="war-upd-name" class="col-sm-4 control-label">name</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="war-upd-name" name="war-name" readonly >
            </div>
          </div>
          <div class="form-group">
            <label for="war-upd-servpath" class="col-sm-4 control-label">service path</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="war-upd-servpath" name="war-servpath" readonly >
            </div>
          </div>
          <div class="form-group">
            <label for="war-upd-descr" class="col-sm-4 control-label">description</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" id="war-upd-descr" name="war-descr" readonly >
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
      </div>
    </div>
  </div>
</div>


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