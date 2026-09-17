<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%
    // Server-side session validation to restrict access to Soldier role
    String role = (String) session.getAttribute("role");
    String userName = (String) session.getAttribute("userName");
    String profileImg = (String) session.getAttribute("profileImg");

    if (role == null || !role.equalsIgnoreCase("Soldier")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Soldier Dashboard</title>
    <link rel="stylesheet" href="CSS/dash.css">
</head>
<body>
<div class="dashboard">
    <%
        String imageUrl;
        if (profileImg != null && !profileImg.isEmpty()) {
            imageUrl = "ImageServlet?name=" + profileImg;
        } else {
            imageUrl = "images/profiles/hope.jpg";
        }
    %>
    <img src="<%= imageUrl %>" alt="Profile Picture" class="profile-img">

    <h2>Welcome, <%= userName != null ? userName : "Soldier" %></h2>

    <a class="button" href="InboxServlet">📜 View Inbox</a>
    <a class="button logout" href="LogoutServlet">Logout</a>
</div>
</body>
</html>