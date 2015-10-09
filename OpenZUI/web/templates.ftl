<!DOCTYPE html>
<html lang="en">
	<#include "header.ftl">
	<body>
    <#include "navigation.ftl">

<!--main-->
<div class="container" id="main">
  <div class="row">
  
  	<div class="col-md-12 col-sm-6">
      <div class="well"> 
        <form class="form-horizontal" method="POST" action="Templates">
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
          <div class="form-group">
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
          </div>
          <div class="form-group">
            <label for="tmpl-description" class="col-sm-4 control-label">Description</label>
            <div class="col-sm-8">
              <textarea type="text" class="form-control" name="tmpl-description" rows="2" required></textarea>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-hasInput" class="col-sm-4 control-label">Has input endpoint</label>
            <div class="col-sm-8">
              <input type="checkbox" class="form-control" name="tmpl-hasInput" value="O" checked>
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-numOutputs" class="col-sm-4 control-label">Number of output endpoints</label>
            <div class="col-sm-8">
              <input type="number" class="form-control" name="tmpl-numOutputs" min="0" max="10" value="1">
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-queueLogging" class="col-sm-4 control-label">Uses queue logging</label>
            <div class="col-sm-8">
              <input type="checkbox" class="form-control" name="tmpl-queueLogging" value="O" checked >
            </div>
          </div>
          <div class="form-group">
            <label for="tmpl-usesMongo" class="col-sm-4 control-label">Uses MongoDB</label>
            <div class="col-sm-8">
              <input type="checkbox" class="form-control" name="tmpl-usesMongo" value="O" >
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
      
  <hr>
  <div class="col-md-12 text-center">
    <p>
      <a href="http://openzoo.org" target="_ext">openzoo website</a>
      <br>
      <a href="http://vcl.iti.gr" target="_ext">vcl@iti.gr</a> || <a href="http://www.certh.gr" target="_ext">CERTH</a>
    </p>
  </div>
</div><!--/main-->





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