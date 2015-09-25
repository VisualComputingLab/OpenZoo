<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Logout page</title>
    </head>
    <body>
        <jsp:useBean id="userBean" scope="session" class="gr.iti.openzoo.ui.LoggedInUser" />
        <jsp:setProperty name="userBean" property="password" value="" />
        <jsp:forward page="/login.jsp" />
    </body>
</html>
