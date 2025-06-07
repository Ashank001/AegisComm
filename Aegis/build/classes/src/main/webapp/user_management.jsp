<%@ page session="true" %>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>


<%	
	response.setCharacterEncoding("UTF-8");
    String username = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");

    if (username == null || role == null || !role.equals("admin")) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Management - Admin</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">   
    <link rel="stylesheet" href=CSS/sent.css>
</head>
<body>
<div class="container">
	<div class="logo-header">
    	<img src="images/AegisComm.jpg" alt="Secure Gateway Logo">
	</div>
    <h2>👮 Admin Panel </h2>
    <div class="scroll">
	    <table border="1">
	        <tr>
	            <th>Username</th>
	            <th>Role</th>
	            <th>Messages Sent</th>
	            <th>Messages Received</th>
	        </tr>
	
		<%
		    try (Connection con = DBConnection.getConnection()) {
		        String userQuery = "SELECT username, role FROM users";
		        PreparedStatement userStmt = con.prepareStatement(userQuery);
		        ResultSet userRs = userStmt.executeQuery();
		
		        while (userRs.next()) {
		            String u = userRs.getString("username");
		            String r = userRs.getString("role");
		            String sentQuery = "SELECT COUNT(*) FROM messages WHERE sender = ?";
		            PreparedStatement sentStmt = con.prepareStatement(sentQuery);
		            sentStmt.setString(1, u);
		            ResultSet sentRs = sentStmt.executeQuery();
		            sentRs.next();
		            int sentCount = sentRs.getInt(1);
		            String recvQuery = "SELECT COUNT(*) FROM messages WHERE receiver = ?";
		            PreparedStatement recvStmt = con.prepareStatement(recvQuery);
		            recvStmt.setString(1, u);
		            ResultSet recvRs = recvStmt.executeQuery();
		            recvRs.next();
		            int recvCount = recvRs.getInt(1);
		%>
	        <tr>
	            <td><%= u %></td>
	            <td><%= r %></td>
	            <td><%= sentCount %></td>
	            <td><%= recvCount %></td>
	        </tr>
		<%
	        }
	    }catch (Exception e) {
	        e.printStackTrace();
	        out.println("<tr><td colspan='4'><b>Error loading user data</b></td></tr>");
	    }
		%>
	    </table>
	</div>
	<div class="footer">
	    <a href="dashboard.jsp"> Back to Dashboard</a>
	    <a href="LogoutServlet" class="logout">Logout</a>
    </div>
</div>
</body>
</html>
