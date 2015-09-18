<!DOCTYPE html>
<html lang="en">
  <#include "header.ftl">
	<body>
    <#include "navigation.ftl">

    <!--main-->
    <div class="container" id="main">
      <div class="row">
        <div class="col-md-4 col-sm-6">
          <a href="Servers">
            <div class="well"> 
              <h4>Setup Servers</h4>
              <div  style="padding:5px;">
                <h4><i class="fa fa-server fa-5x"></i></h4> 
              </div>
            </div>
          </a>
        </div>
        <div class="col-md-4 col-sm-6">
          <a href="Repository">
            <div class="well"> 
              <h4>Manage repository</h4>
              <div  style="padding:5px;">
                <h4><i class="fa fa-cloud-upload fa-5x"></i></h4> 
              </div>
            </div>
          </a>
        </div>
        <div class="col-md-4 col-sm-6">
          <a href="Topologies">
            <div class="well"> 
              <h4>Topologies</h4>
              <div  style="padding:5px;">
                <h4><i class="fa fa-cubes fa-5x"></i></h4> 
              </div>
            </div>
          </a>
        </div>
      </div><!--/row-->

      <hr>

      <div class="row">
        <div class="col-md-12"><h2>Running Topologies</h2></div>
        <div class="col-md-4 col-sm-6">
          <div class="panel panel-default">
            <div class="panel-heading"><a href="#" class="pull-right">View all</a> <h4>CPU load</h4></div>
            <div class="panel-body">
              <img src="https://lh5.googleusercontent.com/-b0-k99FZlyE/AAAAAAAAAAI/AAAAAAAAAAA/eu7opA4byxI/photo.jpg?sz=70" class="img-circle pull-right"> <a href="#">Guidance and Tools</a>
              <div class="clearfix"></div>
              <hr>
              <p>Migrating from Bootstrap 2.x to 3 is not a simple matter of swapping out the JS and CSS files.
                Bootstrap 3 is a major overhaul, and there are a lot of changes from Bootstrap 2.x. <a href="http://bootply.com/bootstrap-3-migration-guide">This guidance</a> is intended to help 2.x developers transition to 3.
              </p>
              <h5><a href="http://google.com/+bootply">More on Upgrading from +Bootply</a></h5>
            </div>
          </div> 
        </div>
        <div class="col-md-4 col-sm-6">
          <div class="panel panel-default">
            <div class="panel-heading"><a href="#" class="pull-right">View all</a> <h4>Memory</h4></div>
            <div class="panel-body">
              Check out some of our member profiles..
              <hr>
              <div class="well well-sm">
                <div class="media">
                  <a class="thumbnail pull-left" href="#">
                    <img class="media-object" src="//placehold.it/80">
                  </a>
                  <div class="media-body">
                    <h4 class="media-heading">John Doe</h4>
                  	<p><span class="label label-info">10 photos</span> <span class="label label-primary">89 followers</span></p>
                    <p>
                      <a href="#" class="btn btn-xs btn-default"><span class="glyphicon glyphicon-comment"></span> Message</a>
                      <a href="#" class="btn btn-xs btn-default"><span class="glyphicon glyphicon-heart"></span> Favorite</a>
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div> 
        </div>
        <div class="col-md-4 col-sm-6">
          <div class="panel panel-default">
            <div class="panel-heading"><a href="#" class="pull-right">View all</a> <h4>Storage</h4></div>
            <div class="panel-body">
              <img src="//placehold.it/150" class="img-circle pull-right"> <a href="#">Articles</a>
              <div class="clearfix"></div>
              <hr>
              <div class="clearfix"></div>
              <img src="http://placehold.it/120x90/3333CC/FFF" class="img-responsive img-thumbnail pull-right">
              <p>The more powerful (and 100% fluid) Bootstrap 3 grid now comes in 4 sizes (or "breakpoints"). Tiny (for smartphones), Small (for tablets), Medium (for laptops) and Large (for laptops/desktops).</p>
              <div class="clearfix"></div>              
            </div>
          </div> 
        </div><!--/articles-->
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
      <hr>
    </div>
  </div><!--/main-->

    <#include "login-about.ftl">



	<!-- script references -->
		<script src="./libs/jquery/jquery-1.11.3.min.js"></script>
		<script src="./libs/bootstrap/js/bootstrap.min.js"></script>
		<script src="./js/scripts.js"></script>
	</body>
</html>