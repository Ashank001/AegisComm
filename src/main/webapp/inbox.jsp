<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.gateway.model.Message, com.gateway.util.AESUtil" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null) {
        response.sendRedirect("login.jsp?error=Session expired. Please log in again.");
        return;
    }
    
    // Dynamically determine the correct dashboard path to prevent logout loop
    String dashboardPath;
    if (role.equalsIgnoreCase("admin")) {
        dashboardPath = "dashboard.jsp";
    } else if (role.equalsIgnoreCase("Intelligence")) {
        dashboardPath = "dashboard_intel.jsp";
    } else if (role.equalsIgnoreCase("TopOrder")) {
        dashboardPath = "dashboard_top.jsp";
    } else if (role.equalsIgnoreCase("Secondary")) {
        dashboardPath = "dashboard_second.jsp";
    } else {
        dashboardPath = "dashboard_soldier.jsp";
    }
    @SuppressWarnings("unchecked")
    List<Message> messages = (List<Message>) request.getAttribute("messages");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Inbox</title>
    <link rel="stylesheet" href="CSS/inbox.css">
</head>
<body>
    <div class="container">
        <h2>Your Inbox</h2>
        <a href="<%= dashboardPath %>" class="back-button">← Back to Dashboard</a>
        
        <div class="message-list">
            <% if (messages == null || messages.isEmpty()) { %>
                <p class="no-messages">You have no messages.</p>
            <% } else { %>
                <% for (Message message : messages) { %>
                    <div class="message-item">
                        <div class="message-header">
                            <strong>From: <%= message.getSenderName() %></strong>
                            <span><%= message.getTimestamp() %></span>
                        </div>
                        <div class="message-body">
                            <%
                                // Displaying the decrypted text from the servlet-prepared model
                                out.println("<p>" + message.getDecryptedText() + "</p>");
                            %>
                        </div>
                        
                        <!-- Top Order Edit/Forward button -->
                        <% if (role.equalsIgnoreCase("TopOrder")) { %>
                            <div class="message-actions">
                                <a href="ForwardMessageServlet?id=<%= message.getId() %>" class="edit-btn">Edit and Forward to Secondary</a>
                            </div>
                        <% } %>
                        <% if (role.equalsIgnoreCase("Secondary")) { %>
                            <div class="message-actions">
                                <a href="SplitForwardServlet?id=<%= message.getId() %>" class="forward-btn">Split and Dispatch to Soldiers</a>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            <% } %>
        </div>
    </div>
</body>
</html>
