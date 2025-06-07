<%@ page session="true" %>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    String username = (String) session.getAttribute("user");
    if (username == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    String toParam = request.getParameter("to") != null ? request.getParameter("to") : "";
    String subjectParam = request.getParameter("subject") != null ? request.getParameter("subject") : "";
%>

<!DOCTYPE html>
<html>
<head>
    <title>Compose Message</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="CSS/compose.css">
    <meta charset="UTF-8">
    
</head>
<body>
<div class="container">
	<div class="logo-header">
    	<img src="images/AegisComm.jpg" alt="Secure Gateway Logo">
	</div>
    <h2>Compose Message</h2>
    <form action="SendMessageServlet" method="post">
        <input type="checkbox" name="sendAll" id="sendAll">
        <label for="sendAll">Send to All Users</label><br><br>

        <input type="text" id="receiver" name="receiver" placeholder="To:" value="<%= toParam %>">
        <input type="text" id="subject" name="subject" placeholder="Subject:" value="<%= subjectParam %>" required>
        <label for="priority">Priority:</label><br>
        <select id="priority" name="priority" required>
            <option value="High"> 🔴 High</option>
            <option value="Medium" selected>🟡 Medium</option>
            <option value="Low">🟢 Low</option>
        </select><br><br>
        <textarea id="body" name="body" rows="5" cols="40" required placeholder="Message:"></textarea>
		
        <input type="submit" value="Send">
    </form>

    <div class="footer-buttons">
        <a href="inbox.jsp" class="back">Back to Inbox</a>
        <a href="LogoutServlet" class="logout">Logout</a>
    </div>
</div>

<script>
    const sendAllCheckbox = document.getElementById("sendAll");
    const receiverInput = document.getElementById("receiver");

    function toggleReceiver() {
        if (sendAllCheckbox.checked) {
            receiverInput.disabled = true;
            receiverInput.removeAttribute("required");
            receiverInput.value = "";
        } else {
            receiverInput.disabled = false;
            receiverInput.setAttribute("required", "true");
        }
    }

    sendAllCheckbox.addEventListener("change", toggleReceiver);
    window.onload = toggleReceiver;  
</script>
</body>
</html>
