<!DOCTYPE html>
<html lang="en">
  <#include "header.ftl">
	<body>
    <#include "navigation.ftl">

    <!--main-->
    <div class="container" id="main">
      <div class="row">
        <div class="col-md-6 col-sm-6">
          <a href="Servers">
            <!-- <div class="well"> 
              <h4>Setup Servers</h4>
              <div style="padding:5px;">
                <h4><i class="fa fa-server fa-5x"></i></h4>
              </div>
            </div> -->
            <div class="well"> 
              <i class="fa fa-server fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Setup Servers</h2>
                <p>Register cluster servers</p>
              </div>
            </div>
          </a>
        </div>
        <div class="col-md-6 col-sm-6">
          <a href="Repository">
            <!-- <div class="well"> 
              <h4>Manage repository</h4>
              <div style="padding:5px;">
                <h4><i class="fa fa-cloud-upload fa-5x"></i></h4> 
              </div>
            </div> -->
            <div class="well"> 
              <i class="fa fa-cloud-upload fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Manage repository</h2>
                <p>Download service templates and upload services</p>
              </div>
            </div>
          </a>
        </div>
        <div class="col-md-6 col-sm-6">
          <a href="Topologies">
            <!-- <div class="well"> 
              <h4>Topologies</h4>
              <div  style="padding:5px;">
                <h4><i class="fa fa-cubes fa-5x"></i></h4> 
              </div>
            </div> -->
            <div class="well"> 
              <i class="fa fa-cubes fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Topologies</h2>
                <p>Draw, deploy and run topologies</p>
              </div>
            </div>
          </a>
        </div>
        <div class="col-md-6 col-sm-6">
          <a href="Configurations">
            <!-- <div class="well"> 
              <h4>Configurations</h4>
              <div  style="padding:5px;"> 
                <h4><i class="fa fa-cogs fa-5x"></i></h4> 
              </div>
            </div> -->
            <div class="well"> 
              <i class="fa fa-cogs fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Configurations</h2>
                <p>Monitor running topologies</p>
              </div>
            </div>
          </a>
        </div>
      </div><!--/row-->

<!--       <input type="hidden" id="uiLogTextArea">
</input> -->


      <div class="clearfix"></div>
      <hr>
        <div class="col-md-12 text-center">
          <p>
            <a href="http://openzoo.org" target="_ext">openzoo website</a>
            <br>
            <a href="http://vcl.iti.gr" target="_ext">vcl@iti.gr</a> || <a href="http://www.certh.gr" target="_ext">CERTH</a>
          </p>
        </div>
      <hr>
    </div>
  </div><!--/main-->

    <#include "login-about.ftl">

	<!-- script references -->
		<script src="./libs/jquery/jquery-1.11.3.min.js"></script>
		<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
    <script src="./libs/bootstrap/js/bootstrap-toggle.min.js"></script>
    <script src="./js/alertify.js"></script>
		<script src="./js/scripts.js"></script>
    <script src="./js/mainpage.js"></script>

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