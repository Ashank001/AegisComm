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
<!DOCTYPE html>
<html>
<head>
    <title>Inbox</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta charset="UTF-8">   
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="CSS/inbox.css">    
</head>
<body>
<div class="container">
	<div class="logo-header">
    	<img src="images/AegisComm.jpg" alt="Secure Gateway Logo">
	</div>
	<h2>[ <%= username %> ] - Inbox</h2>
	<div class="scroll-box">
	<div class="filter-bar">
		<form method="get" action="inbox.jsp">
	    <label for="priorityFilter"> 🛠 Priority Filter:</label>
	    <select name="priorityFilter" id="priorityFilter" onchange="this.form.submit()">
	        <option value=""> -- All -- </option>
	        <option value="High" <%= "High".equals(request.getParameter("priorityFilter")) ? "selected" : "" %>> High </option>
	        <option value="Medium" <%= "Medium".equals(request.getParameter("priorityFilter")) ? "selected" : "" %>> Medium </option>
	        <option value="Low" <%= "Low".equals(request.getParameter("priorityFilter")) ? "selected" : "" %>> Low </option>
	    </select>
		</form>
	</div>
		<table border="1">
		    <tr>
		        <th>Sender</th>
		        <th>Subject</th>
		        <th>Received Date</th>
		        <th>Body</th>
		        <th>Priority</th>
		        <th>Actions</th>
		        <th>Status</th>
		    </tr>
		<%
		    try (Connection con = DBConnection.getConnection()) {
		    	String filter = request.getParameter("priorityFilter");
		        String query = "SELECT * FROM messages WHERE receiver = ?";
		        if (filter != null && !filter.isEmpty()) {
		            query += " AND priority = ?";
		        }
		        PreparedStatement ps = con.prepareStatement(query);
		        ps.setString(1, username);
		        if (filter != null && !filter.isEmpty()){
		            ps.setString(2, filter);
		        }
		        ResultSet rs = ps.executeQuery();
		        while (rs.next()) {
		            int id = rs.getInt("id");
		            PreparedStatement markReadStmt = con.prepareStatement("UPDATE messages SET is_read = TRUE WHERE id = ?");
		            markReadStmt.setInt(1, id);
		            markReadStmt.executeUpdate();
		            String sender = rs.getString("sender");
		            String subject = rs.getString("subject");
		            Date receivedDate = rs.getDate("received_date");
		            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		            String formattedDate = sdf.format(receivedDate);
		            String body = rs.getString("body");
		            String rawPriority = rs.getString("priority");
                    String priority = (rawPriority != null && !rawPriority.trim().isEmpty()) ? rawPriority : "Medium";
		            
		            boolean isRead = rs.getBoolean("is_read");
		            String status = isRead ? "✅ Read" : "📬 Unread";
		            
	               
		            
		
		%>
		            <tr>
		                <td><%= sender %></td>
		                <td><%= subject %></td>
		                <td><%= formattedDate %></td>
		                <td><%= body %></td>
		                <td class="priority-<%=priority %>"><%= priority %></td>
		                                
		                <td class="actions">
						    <form action="compose.jsp" method="get" style="display:inline;">
						        <input type="hidden" name="to" value="<%= sender %>">
						        <input type="hidden" name="subject" value="Re: <%= subject %>">
						        <input type="submit" class="button" value="Reply">
						    </form>
						    <form action="DeleteMessageServlet" method="post" style="display:inline;">
						        <input type="hidden" name="id" value="<%= id %>">
						        <input type="submit" class="button logout" value="Delete">
						    </form>
						</td>                
		                <td><%= status %></td>
		            </tr>
		<%
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		        out.println("<tr><td colspan='5'><b>Database error!</b></td></tr>");
		    }
		%>
		</table>
	</div>
	 <div class="footer-links">
        <a href="compose.jsp">Compose</a>
        <a href="sent.jsp"> Sent</a>
        <a href="dashboard.jsp">Dashboard</a>
        <a href="LogoutServlet" class="logout"> Logout</a>
    </div>
</div>
</body>
</html>
