<%@ page session="true" %>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String username = (String) session.getAttribute("user");
    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<%! 
    public String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Sent Messages</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="CSS/sent.css">
    
</head>
<body>
<div class="container">
	<div class="logo-header">
		<img src="images/AegisComm.jpg" alt="Secure Gateway Logo">
	</div>
	<h2>[ <%= username %> ]- Sent Messages</h2>
	<div class="scroll">
	<div class="filter-bar">
    <form method="get" action="sent.jsp">
        <label for="priorityFilter">🛠 Priority Filter:</label>
        <select name="priorityFilter" id="priorityFilter" onchange="this.form.submit()">
            <option value=""> -- All -- </option>
            <option value="High" <%= "High".equals(request.getParameter("priorityFilter")) ? "selected" : "" %>>High</option>
            <option value="Medium" <%= "Medium".equals(request.getParameter("priorityFilter")) ? "selected" : "" %>>Medium</option>
            <option value="Low" <%= "Low".equals(request.getParameter("priorityFilter")) ? "selected" : "" %>>Low</option>
        </select>
    </form>
	</div>
	
		<table border="1">
		    <tr>
		        <th>To</th>
		        <th>Subject</th>
		        <th>Sent Date</th>
		        <th>Body</th>
		        <th>Priority</th>
		    </tr>
		
		<%
			boolean hasMessages = false;
		    String priorityFilter = request.getParameter("priorityFilter");
	
		    try (Connection con = DBConnection.getConnection()) {
		        String query = "SELECT * FROM messages WHERE sender = ?";
		        if (priorityFilter != null && !priorityFilter.isEmpty()) {
		            query += " AND priority = ?";
		        }
	
		        PreparedStatement ps = con.prepareStatement(query);
		        ps.setString(1, username);
		        if (priorityFilter != null && !priorityFilter.isEmpty()) {
		            ps.setString(2, priorityFilter);
		        }
	
		        ResultSet rs = ps.executeQuery();
			
		        while (rs.next()) {
		            hasMessages = true;
		            String receiver = rs.getString("receiver");
		            String subject = rs.getString("subject");
		            Timestamp sentDate = rs.getTimestamp("received_date");
		            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		            String formattedDate = sdf.format(sentDate);
		            String body = rs.getString("body");
		            
		            String rawPriority = rs.getString("priority");
                    String priority = (rawPriority != null && !rawPriority.trim().isEmpty()) ? rawPriority : "Medium";
		%>
		            <tr>
		                <td><%= escapeHtml(receiver) %></td>
		                <td><%= escapeHtml(subject) %></td>
		                <td><%= formattedDate %></td>
		                <td><%= escapeHtml(body) %></td>
		                <td class="priority-<%= priority %>"><%= priority %></td>                
		            </tr>
		<%
		        }
		        if (!hasMessages) {
		%>
		            <tr><td colspan="5">No sent messages found.</td></tr>
		<%
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		        out.println("<tr><td colspan='4'><b>Database error!</b></td></tr>");
		    }
		%>
		</table>
	</div>
	<div class="footer">
		<a href="inbox.jsp" >Back to Inbox</a>
		<a href="compose.jsp" >Compose New Message</a>
		<a href="dashboard.jsp" >Back to Dashboard</a>
		<a href="LogoutServlet" class="logout">Logout</a>
	</div>
</div>
</body>
</html>
