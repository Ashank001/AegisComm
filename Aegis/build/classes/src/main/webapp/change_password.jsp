<%@ page session="true" %>
<%
    String username = (String) session.getAttribute("user");
    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String error = request.getParameter("error");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Change Password</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="CSS/change_password.css">
</head>
<body>
    <div class="container">
	    <div class="logo-header">
	    	<img src="images/AegisComm.jpg" alt="Secure Gateway Logo">
		</div>
        <h2>Change Password</h2>

        <% if (error != null) { %>
            <div class="alert error"><%= error %></div>
        <% } %>

        <% if (msg != null) { %>
            <div class="alert success"><%= msg %></div>
        <% } %>

        <form action="ChangePasswordServlet" method="post">
            <input type="password" name="currentPassword" placeholder="Current Password" required><br>
            <input type="password" name="newPassword" placeholder="New Password" required><br>
            <input type="password" name="confirmPassword" placeholder="Confirm New Password" required><br>
            <input type="submit" value="Update Password">
        </form>

        <div class="footer-buttons">
            <a href="dashboard.jsp" class="back">Back to Dashboard</a>
            <a href="LogoutServlet" class="logout">Logout</a>
        </div>
    </div>
</body>
</html>
