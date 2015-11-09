<!DOCTYPE html>
<html lang="en">
  <#include "header.ftl">
  <body>
    <#include "navigation.ftl">


    <!--main-->
    <div class="container page-wrap" id="main">
      <div class="row">
        <div class="col-md-3">
          <div class="well">
            <h4><label>name:</label> <span id="topologyName">${topology_name}</span></h4>
            <form class="form-horizontal" id="topoSubmitForm" method="POST" action="ProcessTopology">
              <input type="hidden" class="form-control" name="topo-name" value="${topology_name}">
              <input type="hidden" class="form-control" name="topo-graph" id="topo-graph" >
              <div type="button" id="cancelTopoBtn" class="btn btn-default"> Close</div>
              <div type="button" id="submitTopoBtn" class="btn btn-success"><i class="fa fa-send"></i> Submit</div>
            </form>
          </div> 
        </div>
        <div class="col-md-6">
          <div class="well">
            <h4>Add OpenZoo Service / link</h4>
            <form class="form-inline">
              <div class="form-group">
                <div class="input-group">
                  <div class="input-group-addon"><i class="fa fa-cube"></i></div>
                  <select class="form-control" id="openzooServiceSelect">
                  </select>
                </div>
              </div>
              <div type="button" id="openzooServiceSelectBtn" class="btn btn-primary">insert</div>
              <div type="button" id="addLinkBtn" class="btn btn-primary"><i class="fa fa-link"></i> Add link</div>
            </form>
          </div> 
        </div>      
      </div>
        
                <div class="row">

         
            <!-- <div class="col-md-9 minBox850"> -->
            <div class="col-md-9">
            <!-- <div id="paper" class="topologyBox minBox850"> -->
            <div id="paper" class="topologyBox " style="width:100%;">
            </div>   
          	</div>

            <div class="col-md-3"> 
              <div class="row" id="service_manager">

              <div class="well ">
                  <form id="service_form"> 

                  <label for="instances">instances</label>
                  <input type="number" class="form-control" id="instances" name="instances" value="1" min="1" max="100">
                  
                  <label for="tpc">threads per core</label>
                  <input type="number" class="form-control" id="tpc" name="tpc" value="0" min="0" max="16">

                  </form>
              </div>
            </div>
              
              <div class="row" id="connection_manager">

               <div class="well">
                  <form id="connection_form">
                  <div class="form-group">
                    <label for="outEndpointsList"> source </label>
                  <select class="form-control" id="outEndpointsList" name="outEndpointsList">
                  </select>
                  <label for="inEndpointsList"> target </label>
                  <select class="form-control" id="inEndpointsList" name="inEndpointsList">
                  </select>
                  <label for="conn_mapping">connection type </label>
                  <select class="form-control" id="conn_mapping" name="conn_mapping">
                    <option value="blank" disabled selected></option>
                    <option value="conn_all">All</option>
                    <option value="conn_available">Available</option>
                    <option value="conn_route">Route</option>
                  </select>
                  </div>

                  </form>
              </div>
             </div>

             <div class="row" id="routing_manager">

              <div class="well ">
                  <form id="routing_form"> 

                  <label for="instance">instance</label>
                  <select class="form-control" id="route_mapping_instance" name="route_mapping_instance">
                  </select>
                  
                   <label for="keys">keys <i>[comma separated]</i></label>
                  <input type="text" class="form-control" id="route_mapping_keys" name="route_mapping_keys">

                  </form>
              </div>
            </div>

            </div>
          	
          </div><!--/row-->
          	
      <div class="clearfix"></div>
              
            
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
          <div class="modal-body" id="detailsModalBody">
          </div>
          <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">OK</button>
          </div>
        </div>
      </div>
    </div>

    <!-- script references -->
    <script src="./libs/jquery/jquery-1.11.3.min.js"></script>
  	<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
    <script src="./js/alertify.js"></script>
  	<script src="./js/scripts.js"></script>

    <script src="./js/joint.js"></script>
    <script src="./js/joint.shapes.uml.js"></script>
    <script src="./js/umlsc.js"></script>

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