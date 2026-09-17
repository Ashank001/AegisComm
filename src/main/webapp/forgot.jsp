<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title> Password Reset </title>
	<link rel="stylesheet" href="CSS/login.css">
</head>
<body>
<div class="login-box">
    <h2>Reset Password</h2>
    <form action="ForgotPasswordServlet" method="post">
        <input type="email" name="email" placeholder="Enter your registered email" required>
        <input type="submit" value="Send Reset Link">
    </form>

    <div class="message">
    	<%
		    String msg = request.getParameter("msg");
		    if (msg != null && !msg.isEmpty()) {
		%>
		    <p style="color: red; text-align: center; font-size: 14px; margin-top: 10px;">
        <%= msg %>
    </p>
		<%
		    }
		%>
    </div>
</div>
</body>
</html>