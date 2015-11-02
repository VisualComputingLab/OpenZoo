<!DOCTYPE html>
<html lang="en">
  <#include "header.ftl">
  <body>
    <#include "navigation.ftl">


    <!--main-->
    <div class="container" id="main">
      <div class="row">
        <div class="col-md-3">
          <div class="well">
            <h4><label>name:</label> <span id="topologyName">${topology_name}</span></h4>
            <form class="form-horizontal" id="topoSubmitForm" action="ConfirmServerConfig" method="POST">
              <input type="hidden" class="form-control" name="action" value="deploy_services" >
              <input type="hidden" class="form-control" name="topo-name" value="${topology_name}">
              <input type="hidden" class="form-control" name="topo-config" id="topo-config" >
                <div type="button" id="cancelTopoBtn" class="btn btn-default"> Close</div>
                <div type="button" id="submitTopoBtn" class="btn btn-success"><i class="fa fa-send"></i> Submit</div>
            </form>
          </div> 
        </div>
        <div class="col-md-9">
          <div class="well">
            <h4>Update server configuration</h4>
            <p>The system suggests the following configuration, based on the current server capacities, load and already installed services. You can alter the automatically generated configuration at your own risk, since no other check will be performed after this point.</p><p>This is especially usefull, when broker services are present, which need to be accessible at a specific server.</p><p>The numbers beside each service instance are the instance IDs. Only one instance of each service can be installed on a specific server.</p>
          </div> 
        </div>
      </div>
        
      <div class="row">         
        <div class="col-md-12">
          <div id="servers_container">
            <#if server2instances??>
              <#list server2instances?keys as server>
                <div class="well col-md-12">
                  <h4>${server}</h4>
                  <div id="${server}" class="list-group list-group-horizontal serverConf">
                    <#list server2instances[server] as instance>
                      <#if instance.component_type == "operator">
                      <div class="list-group-item" style="background-color: rgba(101, 176, 69, 0.7);"><span class="badge">${instance.server_conf.instance_id}</span>${instance.component_id}</div>
                      <#else>
                      <div class="list-group-item" style="background-color: rgba(236, 151, 31, 0.7);"><span class="badge">${instance.server_conf.instance_id}</span>${instance.component_id}</div>
                      </#if>
                    </#list>
                  </div>
                </div>
              </#list>
            </#if>
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
    <script src="./libs/bootstrap/js/bootstrap-toggle.min.js"></script>
    <script src="./js/alertify.js"></script>
    <script src="./js/scripts.js"></script>
    <script src="./js/Sortable.js"></script>
    <script src="./js/serverConfig.js"></script>

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