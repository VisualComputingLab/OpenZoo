<!DOCTYPE html>
<html lang="en">
	<#include "header.ftl">
	<body>
    <#include "navigation.ftl">

<!--main-->
<div class="container" id="main">
   <div class="row">
   
    <div class="col-md-3 col-sm-6">
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

    <div class="col-md-3 col-sm-6">
      <div class="well"> 
        <h4>Components</h4>
        <ul class="list-group" id='lg-comp'>
          <!-- <a class="list-group-item active" onclick="console.log('TwitterCrawler clicked');"> TwitterCrawler</a>
          <a class="list-group-item" onclick="console.log('URLValidator clicked');"> URLValidator</a>  -->
        </ul>
        <input type="hidden" class="form-control" id="selectedComponent" value="">
        <button class="btn btn-success" id="redeployBtn" style="width: 100%;">Redeploy component</button>
      </div>
    </div>

    <div class="col-md-3 col-sm-6">
      <div class="well"> 
        <h4>Servers (Instances)</h4>
        <ul class="list-group">
          <a class="list-group-item active" onclick="console.log('vseen clicked');"><span class="badge">Running</span> vseen</a>
          <a class="list-group-item" onclick="console.log('basement_107 clicked');"><span class="badge">Running</span> basement_107</a> 
        </ul>
        <input type="hidden" class="form-control" id="selectedServer" value="">
      </div>
      <div class="well"> 
        <h4>Endpoints (Component level)</h4>
        <ul class="list-group">
          <li class="list-group-item"><span class="badge">3213 (323.5 Kb)</span> input</li>
          <li class="list-group-item"><span class="badge">2 (23.2 Kb)</span> output_1</li> 
          <li class="list-group-item"><span class="badge">11 (300.3 Kb)</span> output_2</li> 
        </ul>
      </div>
    </div>

    <div class="col-md-3 col-sm-6">
      <div class="well"> 
        <h4>Server load</h4>
        <ul class="list-group">
          <a href="#" class="list-group-item">
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
          </a>
        </ul>
      </div>
      <div class="well"> 
        <h4>Endpoints (Instance level)</h4>
        <ul class="list-group">
          <li class="list-group-item"><span class="badge">3213 (323.5 Kb)</span> input</li>
          <li class="list-group-item"><span class="badge">2 (23.2 Kb)</span> output_1</li> 
          <li class="list-group-item"><span class="badge">11 (300.3 Kb)</span> output_2</li> 
        </ul>
      </div>
    </div>

 
  	
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


  <#include "login-about.ftl">

	<!-- script references -->
		<script src="./libs/jquery/jquery-1.11.3.min.js"></script>
		<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
    <script src="./js/alertify.js"></script>
		<script src="./js/scripts.js"></script>
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