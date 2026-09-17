<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    String userName = (String) session.getAttribute("userName");
    String profileImg = (String) session.getAttribute("profileImg");

    // Session validation for Secondary role
    if (role == null || !role.equalsIgnoreCase("Secondary")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Secondary Dashboard</title>
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

    <h2>Welcome, <%= userName != null ? userName : "Secondary" %></h2>
    
    <a class="button" href="InboxServlet">📥 View Inbox</a>
    <a class="button" href="compose.jsp">📤 Dispatch to Soldiers</a>
    <a class="button logout" href="LogoutServlet">Logout</a>
</div>
</body>
</html>