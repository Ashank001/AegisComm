<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    String userName = (String) session.getAttribute("userName");
    String profileImg = (String) session.getAttribute("profileImg");

    // Session validation for Intelligence role
    if (role == null || !role.equalsIgnoreCase("Intelligence")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Intelligence Dashboard</title>
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

    <h2>Welcome, <%= userName != null ? userName : "Intelligence Officer" %></h2>
    
    <a class="button" href="compose.jsp">📤 Send Secure Message</a>
    
    <a class="button" href="InboxServlet">📥 View Inbox</a>
    
    <a class="button logout" href="LogoutServlet">Logout</a>
</div>
</body>
</html>