<!DOCTYPE html>
<html lang="en">
	<#include "header.ftl">
	<body>
    <#include "navigation.ftl">

<!--main-->
<div class="container page-wrap" id="main">
  <div class="row">

    <div class="col-md-6 col-sm-12">
      <!-- Topologies -->
      <div class="col-md-6 col-sm-6">
        <div class="well"> 
          <h4>Topologies</h4>
          <ul class="list-group" id='lg-topo'>
          </ul>
          <input type="hidden" class="form-control" id="selectedTopo" value="">
        </div>
      </div>

      <!-- Components -->
      <div class="col-md-6 col-sm-6">
        <div class="well clearfix"> 
          <h4>Components</h4>
          <ul class="list-group" id='lg-comp'>
          </ul>
          <form class="form-horizontal" method="POST" action="Configurations" id="updateComponentForm">
            <input type="hidden" class="form-control" name="cnf-action" id="cnf-action" >
            <input type="hidden" class="form-control" name="cnf-topo" id="cnf-topo" >
            <input type="hidden" class="form-control" name="cnf-service" id="cnf-service" >
            <div type="button" id="resetCompBtn" class="btn btn-success pull-left" style="width: 45%;"><i class="fa fa-step-backward"></i> Reset</div>
            <div type="button" id="redeployCompBtn" class="btn btn-success pull-right" style="width: 45%;"><i class="fa fa-exchange"></i> Redeploy</div>
          </form>
          <input type="hidden" class="form-control" id="selectedComponent" value="">
        </div>
      </div>
      <div class="col-md-12 col-sm-12 minBox550">
        <!-- Topology paper -->
        <div class="well"> 
          <h4>Topology overview</h4>
          <div id="paper" class="minBox550">
          </div>
        </div>
      </div>
    </div>

    <!-- Servers -->
    <div class="col-md-3 col-sm-6">
      <div style="background-color: rgba(51, 122, 183, 0.2); padding: 5px">
        <div class="well"> 
          <h4>Servers (Instances)</h4>
          <ul class="list-group" id='lg-serv'>
          </ul>
          <input type="hidden" class="form-control" id="selectedServer" value="">
        </div>
        <div class="well"> 
          <h4>Endpoints (Component level)</h4>
          <ul class="list-group" id='lg-endp-comp'>
          </ul>
        </div>
      </div>
    </div>

    <!-- Endpoints -->
    <div class="col-md-3 col-sm-6">
      <div style="background-color: rgba(51, 122, 183, 0.2); padding: 5px">
        <div class="well"> 
          <h4>Server load</h4>
          <ul class="list-group" id='lg-serv-load'>
          </ul>
        </div>
        <div class="well"> 
          <h4>Endpoints (Instance level)</h4>
          <ul class="list-group"  id='lg-endp-serv'>
          </ul>
        </div>
      </div>
    </div> 

  </div>

  <hr>

  <div class="row">
    <div class="col-md-12 col-sm-6">
      <div class="panel panel-default">
        <div class="panel-heading"><h4>Topology service logs</h4></div>
        <div class="panel-body">
          Service logs
          <select id="logLevelDropdown" name="logLevelDropdown" class="selectpicker" title='Select minimum logging level'>
            <option>debug</option>
            <option>info</option>
            <option>error</option>
          </select>
          <input type="checkbox" checked data-toggle="toggle" data-on="<i class='fa fa-play'></i> Play" data-off="<i class='fa fa-pause'></i> Pause" data-onstyle="success" id="logsToggleButton">
          <button class="btn btn-success" id="logsClearButton" onclick="$('#serviceLogTextArea').val('');">Clear</button>
          <div class="well well-sm">
            <textarea style="width:100%" rows="10" id="serviceLogTextArea" wrap="off"></textarea>
          </div>
        </div>
      </div> 
    </div>
  </div>

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
    <script src="./js/joint.js"></script>
    <script src="./js/joint.shapes.uml.js"></script>
    <script src="./js/configs.js"></script>

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