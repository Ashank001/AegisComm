<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    String userName = (String) session.getAttribute("userName");
    String profileImg = (String) session.getAttribute("profileImg");

    if (role == null || !role.equalsIgnoreCase("Admin")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="CSS/dash.css">
</head>
<body>
<div class="dashboard">
    <%
        String imageUrl;
        if (profileImg != null && !profileImg.isEmpty()) {
            // Use ImageServlet for uploaded images
            imageUrl = "ImageServlet?name=" + profileImg;
        } else {
            // Use local fallback for users without an uploaded image
            imageUrl = "images/profiles/hope.jpg";
        }
    %>
    <img src="<%= imageUrl %>" alt="Profile Picture" class="profile-img">

    <h2>Welcome, <%= userName != null ? userName : "User" %></h2>

    <a class="button" href="AddUser.jsp">➕ Add New User</a>
    <a class="button" href="compose.jsp">✉️ Compose</a>
    <a class="button" href="InboxServlet">📥 View Inbox</a>
    <a class="button" href="view_users.jsp">👥 View All Users</a>
   	<a class="button" href="ViewWeaponsServlet">🔫 Manage Weapons</a>
   	<a class="button" href="ViewBordersServlet">🌍 Manage Zones</a>
    <a class="button" href="AuditLogServlet">📜 View Activity Logs</a>
    <a class="button" href="change_password.jsp">🔒 Change Password</a>
    <a class="button logout" href="LogoutServlet">Logout</a>
</div>
</body>
</html>