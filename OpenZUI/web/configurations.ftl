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
            <!-- <a class="list-group-item active" onclick="TopoSelectedEvent('TopoTest');"><span class="badge">Started</span> TopoTest</a>
            <a class="list-group-item" onclick="console.log('TopoTwo clicked');"><span class="badge">Deployed</span> TopoTwo</a> 
            <a class="list-group-item disabled" onclick="console.log('TopoThree clicked');"><span class="badge">Designed</span> TopoThree</a>  -->
          </ul>
          <input type="hidden" class="form-control" id="selectedTopo" value="">
        </div>
      </div>

      <!-- Components -->
      <div class="col-md-6 col-sm-6">
        <div class="well clearfix"> 
          <h4>Components</h4>
          <ul class="list-group" id='lg-comp'>
            <!-- <a class="list-group-item active" onclick="console.log('TwitterCrawler clicked');"> TwitterCrawler</a>
            <a class="list-group-item" onclick="console.log('URLValidator clicked');"> URLValidator</a>  -->
          </ul>
          <form class="form-horizontal" method="POST" action="Configurations" id="updateComponentForm">
            <input type="hidden" class="form-control" name="cnf-action" id="cnf-action" >
            <input type="hidden" class="form-control" name="cnf-topo" id="cnf-topo" >
            <input type="hidden" class="form-control" name="cnf-service" id="cnf-service" >
            <div type="button" id="resetCompBtn" class="btn btn-success pull-left" style="width: 45%;"><i class="fa fa-step-backward"></i> Reset</div>
            <div type="button" id="redeployCompBtn" class="btn btn-success pull-right" style="width: 45%;"><i class="fa fa-exchange"></i> Redeploy</div>
            <!-- <button class="btn btn-success pull-left" id="resetCompBtn" style="width: 45%;" onclick="resetComponent();">Reset</button>
            <button class="btn btn-success pull-right" id="redeployCompBtn" style="width: 45%;" onclick="redeployComponent();">Redeploy</button> -->
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
            <!-- <a class="list-group-item active" onclick="console.log('vseen clicked');"><span class="badge">Running</span> vseen</a>
            <a class="list-group-item" onclick="console.log('basement_107 clicked');"><span class="badge">Running</span> basement_107</a>  -->
          </ul>
          <input type="hidden" class="form-control" id="selectedServer" value="">
        </div>
        <div class="well"> 
          <h4>Endpoints (Component level)</h4>
          <ul class="list-group" id='lg-endp-comp'>
            <!-- <li class="list-group-item"><span class="badge">3213 (323.5 Kb)</span> input</li>
            <li class="list-group-item"><span class="badge">2 (23.2 Kb)</span> output_1</li> 
            <li class="list-group-item"><span class="badge">11 (300.3 Kb)</span> output_2</li>  -->
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
            <!-- <a href="#" class="list-group-item">
              <h5 class="list-group-item-heading">CPU</h5>
              <div class="progress">
                <div class="progress-bar progress-bar-info" role="progressbar" aria-valuenow="23" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em; width: 23%;">23 %</div>
              </div>
            </a>
            <a href="#" class="list-group-item">
              <h5 class="list-group-item-heading">Memory (235.3 MB free)</h5>
              <div class="progress">
                <div class="progress-bar progress-bar-warning" role="progressbar" aria-valuenow="67" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em; width: 67%;">67 %</div>
              </div>
            </a>
            <a href="#" class="list-group-item">
              <h5 class="list-group-item-heading">Disc (1234.4 MB free)</h5>
              <div class="progress">
                <div class="progress-bar progress-bar-danger" role="progressbar" aria-valuenow="14" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em; width: 14%;">14 %</div>
              </div>
            </a> -->
          </ul>
        </div>
        <div class="well"> 
          <h4>Endpoints (Instance level)</h4>
          <ul class="list-group"  id='lg-endp-serv'>
            <!-- <li class="list-group-item"><span class="badge">3213 (323.5 Kb)</span> input</li>
            <li class="list-group-item"><span class="badge">2 (23.2 Kb)</span> output_1</li> 
            <li class="list-group-item"><span class="badge">11 (300.3 Kb)</span> output_2</li>  -->
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