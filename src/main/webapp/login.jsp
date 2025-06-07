<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Secure Gateway - Login</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="CSS/login.css">  
</head>
<body>
    <div class="login-box">
    <div class="logo-wrapper">
		<img src="images/AegisComm.jpg" alt="Secure Gateway Logo" class="logo">
	</div><br>
        <h2>Login to AegisComm</h2><br>
        <%
        	String error = request.getParameter("error");
        	if (error != null) {
	    %>
	        <div style="color: red; text-align: center; margin-bottom: 10px;">
	            <%= error %>
	        </div>
	    <%
	        }
	    %>
        
        <form action="LoginServlet" method="post">
            <input type="text" name="username" placeholder="Username" required>
            <input type="password" name="password" placeholder="Password" required>
            <input type="submit" value="Login">
        </form>
    </div>
</body>
</html>