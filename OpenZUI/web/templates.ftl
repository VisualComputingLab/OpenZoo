<!DOCTYPE html>
<html lang="en">
	<#include "header.ftl">
	<body>
    <#include "navigation.ftl">

<!--main-->
<div class="container page-wrap" id="main">
  <div class="row">
  
  	<div class="col-md-6 col-sm-6 col-md-offset-3">
      <div class="well"> 
        <form class="form-horizontal" id="template_create" method="POST" action="Templates">
          <h4>Template parameters</h4>
          <div class="form-group">
            <label for="tmpl-proglang" class="col-sm-4 control-label">Programming Language</label>
            <div class="col-sm-8">
              <select class="form-control" id="tmpl-proglang" name="tmpl-proglang" required>
                <option value="Java" selected>Java</option>
                <option value="C++">C++ (Not supported yet)</option>
                <option value="Python">Python (Not supported yet)</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-author" class="col-sm-4 control-label">Author</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="tmpl-author" value="OpenZoo User <user@openzoo.org>" required>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-componentID" class="col-sm-4 control-label">Component ID</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="tmpl-componentID" value="" required>
            </div>
          </div>
          <!-- <div class="form-group">
            <label for="tmpl-serviceID" class="col-sm-4 control-label">Service ID</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="tmpl-serviceID" value="" required>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-workerID" class="col-sm-4 control-label">Worker ID</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="tmpl-workerID" value="" required>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-resourcePath" class="col-sm-4 control-label">Resource path</label>
            <div class="col-sm-8">
              <input type="text" class="form-control" name="tmpl-resourcePath" value="" required>
            </div>
          </div> -->
          <div class="form-group">
            <label for="tmpl-description" class="col-sm-4 control-label">Description</label>
            <div class="col-sm-8">
              <textarea type="text" class="form-control" name="tmpl-description" rows="2" required></textarea>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-numOutputs" class="col-sm-4 control-label">Number of output endpoints</label>
            <div class="col-sm-8">
              <input type="number" class="form-control" name="tmpl-numOutputs" id="tmpl-numOutputs" min="0" max="10" value="1">
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-hasInput" class="col-sm-4 control-label">Has input endpoint</label>
            <div class="col-sm-4">
              <input type="checkbox" class="form-control" data-toggle="toggle" data-on="<i class='fa fa-plug'></i> Yes" data-off="<i class='fa fa-times'></i> No" data-onstyle="success" data-offstyle="warning" id="tmpl-hasInput" name="tmpl-hasInput" value="O" data-width="100%" checked>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-queueLogging" class="col-sm-4 control-label">Uses queue logging</label>
            <div class="col-sm-4">
              <input type="checkbox" class="form-control" data-toggle="toggle" data-on="<i class='fa fa-pencil-square-o'></i> Yes" data-off="<i class='fa fa-times'></i> No" data-onstyle="success" data-offstyle="warning" id="tmpl-queueLogging" name="tmpl-queueLogging" value="O" data-width="100%" checked>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-usesMongo" class="col-sm-4 control-label">Uses MongoDB</label>
            <div class="col-sm-4">
              <input type="checkbox" class="form-control" data-toggle="toggle" data-on="<i class='fa fa-database'></i> Yes" data-off="<i class='fa fa-times'></i> No" data-onstyle="success" data-offstyle="warning" id="tmpl-usesMongo" name="tmpl-usesMongo" value="O" data-width="100%">
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-workerType" class="col-sm-4 control-label">Worker type</label>
            <div class="col-sm-4">
              <input type="checkbox" class="form-control" data-toggle="toggle" data-on="<i class='fa fa-long-arrow-right'></i> Operator" data-off="<i class='fa fa-arrows-alt'></i> Broker" data-onstyle="success" data-offstyle="warning" id="tmpl-workerType" name="tmpl-workerType" value="O" data-width="100%" checked>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-requiredParameters" class="col-sm-4 control-label">Required Parameters (Comma separated list)</label>
            <div class="col-sm-8">
              <textarea type="text" class="form-control" name="tmpl-requiredParameters" rows="2"></textarea>
            </div>
          </div>

          <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
              <button type="submit" class="btn btn-success pull-right">Create</button>
            </div>
          </div>
        </form>
      </div>
  	</div>
  </div><!--/row-->
  	
  <div class="clearfix"></div>
      
</div><!--/main-->

<#include "footer.ftl">



  <#include "login-about.ftl">

	<!-- script references -->
		<script src="./libs/jquery/jquery-1.11.3.min.js"></script>
		<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
    <script src="./libs/bootstrap/js/bootstrap-toggle.min.js"></script>
    <script src="./js/alertify.js"></script>
		<script src="./js/scripts.js"></script>
    <script src="./js/templatesScripts.js"></script>

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