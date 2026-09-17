<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (!role.equalsIgnoreCase("TopOrder")) {
        response.sendRedirect("dashboard.jsp?error=Unauthorized access");
        return;
    }

    String messageId = (String) request.getAttribute("messageId");
    String originalMessage = (String) request.getAttribute("originalMessage");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Modify Message</title>
    <link rel="stylesheet" href="CSS/compose.css"> </head>
<body>
    <div class="container">
        <h2>Edit and Forward to Secondary</h2>
        <p class="alert success">Message Decrypted. Make necessary changes below.</p>
        
        <form action="ProcessForwardServlet" method="post">
            <input type="hidden" name="originalMessageId" value="<%= messageId %>">
            
            <label for="recipient">Forward Recipient (Secondary Level)</label>
            <select id="recipient" name="recipientId" required>
                <option value="">-- Select Secondary Officer --</option>
                <%
                    Connection conn = null;
                    PreparedStatement pstmt = null;
                    ResultSet rs = null;
                    try {
                        conn = DBConnection.getConnection();
                        // Get all Secondary level users
                        String query = "SELECT id, name FROM users WHERE role = 'Secondary'";
                        pstmt = conn.prepareStatement(query);
                        rs = pstmt.executeQuery();
                        
                        while (rs.next()) {
                            out.println("<option value='" + rs.getInt("id") + "'>" + rs.getString("name") + "</option>");
                        }
                    } catch (Exception e) {
                        out.println("<option disabled>Error loading recipients</option>");
                    } finally {
                        if (rs != null) try { rs.close(); } catch (SQLException e) {}
                        if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
                        if (conn != null) try { conn.close(); } catch (SQLException e) {}
                    }
                %>
            </select>

            <label for="message">Message (Edit Below)</label>
            <textarea id="message" name="editedMessage" rows="10" required><%= originalMessage %></textarea>

            <input type="submit" value="Re-encrypt & Forward">
            <a href="InboxServlet" class="back-button">← Cancel</a>
        </form>
    </div>
</body>
</html>