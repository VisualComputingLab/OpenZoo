<!DOCTYPE html>
<html lang="en">
  <#include "header.ftl">
	<body>
    <#include "navigation.ftl">

    <!--main-->
    <div class="container page-wrap" id="main">
      <div class="row">
        <div class="col-md-6 col-sm-6">
          <a href="InstallServers">
            <div class="well"> 
              <i class="fa fa-wrench fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Server setup</h2>
                <p class="pblock">Install and configure necessary components for participating to the cluster</p>
              </div>
            </div>
          </a>
        </div>
        <div class="col-md-6 col-sm-6">
          <a href="Servers">
            <div class="well"> 
              <i class="fa fa-server fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Server registration</h2>
                <p class="pblock">Register servers in cluster</p>
              </div>
            </div>
          </a>
        </div>
        <div class="clearfix"></div>
        <div class="col-md-6 col-sm-6">
          <a href="Templates">
            <div class="well"> 
              <i class="fa fa-cloud-download fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Service templates</h2>
                <p class="pblock">Download component templates</p>
              </div>
            </div>
          </a>
        </div>
        <div class="col-md-6 col-sm-6">
          <a href="Repository">
            <div class="well"> 
              <i class="fa fa-cloud-upload fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Service repository</h2>
                <p class="pblock">Upload implemented components</p>
              </div>
            </div>
          </a>
        </div>
        <div class="clearfix"></div>
        <div class="col-md-6 col-sm-6">
          <a href="Topologies">
            <div class="well"> 
              <i class="fa fa-cubes fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Topology management</h2>
                <p class="pblock">Draw, deploy and run topologies</p>
              </div>
            </div>
          </a>
        </div>
        <div class="col-md-6 col-sm-6">
          <a href="Configurations">
            <div class="well"> 
              <i class="fa fa-cogs fa-5x fa-fw"></i>
              <div class="text" style="padding:15px;">
                <h2>Topology monitoring</h2>
                <p class="pblock">Monitor running topologies</p>
              </div>
            </div>
          </a>
        </div>
      </div><!--/row-->

<!--       <input type="hidden" id="uiLogTextArea">
</input> -->


      <div class="clearfix"></div>
    </div>
  </div><!--/main-->

  <#include "footer.ftl">

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