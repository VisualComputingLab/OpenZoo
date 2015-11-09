<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login page</title>
        <style>
            /* CSS used here will be applied after bootstrap.css */
            body { 
/*              background: url('./css/bg2.png') no-repeat center center fixed; */
             -webkit-background-size: cover;
             -moz-background-size: cover;
             -o-background-size: cover;
             background-size: cover;
            }
        </style>
        <link href="./libs/bootstrap/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="./libs/font-awesome-4.4.0/css/font-awesome.min.css">
        <!--[if lt IE 9]>
            <script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
	<![endif]-->
        <link href="./css/styles.css" rel="stylesheet">
    </head>
    <body>
        <div class="container" style="padding-top:200px">
            <div class="col-md-4 col-md-offset-4 panel panel-default">
                <div >
                    <h3 >OpenZoo user interface</h3>
                </div>
                <div class="panel-body">
                    <form class="form-horizontal" role="form" action="AuthorizationServlet" method="post">
                        <div class="form-group">
                            <label for="inputEmail3" class="col-sm-3 control-label">Username</label>
                            <div class="col-sm-9">
                                <input type="text" name="email" class="form-control required" id="inputEmail3" placeholder="email" >
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="inputPassword3" class="col-sm-3 control-label">Password</label>
                            <div class="col-sm-9">
                                <input type="password" class="form-control required" id="inputPassword3" placeholder="Password" required="" name="password">
                            </div>
                        </div>
                        <div class="form-group last">
                            <div class="col-sm-offset-3 col-sm-9">
                                <button type="submit" class="btn btn-success btn-sm">Sign in</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </body>
</html>