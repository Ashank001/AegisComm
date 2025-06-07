<%@ page session="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>

<%
    String username = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");

    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    int unreadCount = 0;
    try (Connection con = DBConnection.getConnection()) {
        String query = "SELECT COUNT(*) FROM messages WHERE receiver = ? AND is_read = FALSE";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            unreadCount = rs.getInt(1);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="CSS/dash.css">
   
</head>
<body>
    <div class="container">
    	<div class="logo-header">
    		<img src="images/AegisComm.jpg" alt="Secure Gateway Logo">
		</div>
        <h2>Welcome, <%= username %>!</h2>

        <a href="inbox.jsp" class="button">📩 Inbox
            <% if (unreadCount > 0) { %>
                <span class="badge"><%= unreadCount %></span>
            <% } %>
        </a>
        <a href="compose.jsp" class="button">🖊 Compose</a>
        <a href="sent.jsp" class="button">📤 Sent</a>
        <a href="change_password.jsp" class="button"> 🔐 Change password</a>

        <% if ("admin".equals(role)) { %>
            <a href="AddUser.jsp" class="button">👤 Add User</a>
            <a href="user_management.jsp" class="button">🧑‍💼 Manage Users</a>
            <a href="audit.jsp" class="button">📊 Audit Logs</a>
        <% } %>

        <a href="LogoutServlet" class="button logout">🔓 Logout</a>
    </div>
</body>
</html>
