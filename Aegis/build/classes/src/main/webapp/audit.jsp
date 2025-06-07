<%@ page session="true" %>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>

<%
    String username = (String) session.getAttribute("user");
    String role = (String) session.getAttribute("role");

    if (username == null || !"admin".equals(role)) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html>
<head>
    <title>Audit Log</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href=CSS/sent.css>    
</head>
<body>
<div class="container">
	<div class="logo-header">
    	<img src="images/AegisComm.jpg" alt="Secure Gateway Logo">
	</div>
    <h2> Audit Logs</h2>
    <div class="scroll">
	    <table border="1">
	        <tr>
	            <th>Username</th>
	            <th>Action</th>
	            <th>Timestamp</th>
	        </tr>
	<%
	    try (Connection con = DBConnection.getConnection()) {
	        String query = "SELECT * FROM audit_log ORDER BY timestamp DESC";
	        PreparedStatement ps = con.prepareStatement(query);
	        ResultSet rs = ps.executeQuery();
	
	        while (rs.next()) {
	            String user = rs.getString("username");
	            String action = rs.getString("action");
	            String time = rs.getString("timestamp");
	%>
	            <tr>
	                <td><%= user %></td>
	                <td><%= action %></td>
	                <td><%= time %></td>
	            </tr>
	<%
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        out.println("<tr><td colspan='3'><b>Error fetching logs</b></td></tr>");
	    }
	%>
	    </table>
	</div>
	<div class="footer">
    	<a href="dashboard.jsp">Back to Dashboard</a>
    </div>
</div>
</body>
</html>
