<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Denied page</title> 
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
                    <h3>OpenZoo user interface</h3>
                </div>
                <div class="panel-body">
                    <form class="form-horizontal" role="form">
                        <div class="form-group" align="center">
                            Not logged in or session expired!<br><br>
                            Please <a href="login.jsp">login</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </body>
</html>
