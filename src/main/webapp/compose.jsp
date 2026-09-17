<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    String userId = session.getAttribute("userId") != null ? session.getAttribute("userId").toString() : null;

    if (role == null || userId == null) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }

    // --- FIX: Dynamic Dashboard Path ---
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
    // -----------------------------------

    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Compose Message</title>
    <link rel="stylesheet" href="CSS/compose.css">
</head>
<body>
    <div class="container">
        <h2>Send a Secure Message</h2>

        <%
		    String success = request.getParameter("success");
		    String error = request.getParameter("error");
		    if (success != null) {
		%>
		    <div class="alert success"><%= success %></div>
		<%
		    } else if (error != null) {
		%>
		    <div class="alert error"><%= error %></div>
		<%
		    }
		%>

        <form action="SendMessageServlet" method="post">
            <label for="recipient">Recipient</label>
            <select id="recipient" name="recipientId" multiple required style="height: 150px;">
                <option value="">-- Select Recipient --</option>
                <%
                    try {
                        conn = DBConnection.getConnection();
                        String query = "SELECT id, name, role FROM users WHERE id != ?";
                        pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, userId);
                        rs = pstmt.executeQuery();
                        
                        while (rs.next()) {
                            int recipientId = rs.getInt("id");
                            String recipientName = rs.getString("name");
                            String recipientRole = rs.getString("role");
                            
                            // --- HIERARCHICAL FILTERING LOGIC ---
                            
                            // Intelligence sends to Top Order
                            if (role.equalsIgnoreCase("Intelligence") && recipientRole.equalsIgnoreCase("TopOrder")) {
                                out.println("<option value='" + recipientId + "'>" + recipientName + " (" + recipientRole + ")</option>");
                            } 
                            // Top Order sends to Secondary
                            else if (role.equalsIgnoreCase("TopOrder") && recipientRole.equalsIgnoreCase("Secondary")) {
                                out.println("<option value='" + recipientId + "'>" + recipientName + " (" + recipientRole + ")</option>");
                            } 
                            // Secondary sends to Soldier
                            else if (role.equalsIgnoreCase("Secondary") && recipientRole.equalsIgnoreCase("Soldier")) {
                                out.println("<option value='" + recipientId + "'>" + recipientName + " (" + recipientRole + ")</option>");
                            } 
                            // Admins can send to anyone
                            else if (role.equalsIgnoreCase("Admin")) {
                                out.println("<option value='" + recipientId + "'>" + recipientName + " (" + recipientRole + ")</option>");
                            }
                            // All users can send to Admin (for support/reporting)
                            else if (recipientRole.equalsIgnoreCase("Admin")) {
                                out.println("<option value='" + recipientId + "'>" + recipientName + " (" + recipientRole + " - SUPPORT)</option>");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        out.println("<option disabled>Error loading users</option>");
                    } finally {
                        if (rs != null) try { rs.close(); } catch (SQLException e) {}
                        if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
                        if (conn != null) try { conn.close(); } catch (SQLException e) {}
                    }
                %>
            </select>

            <label for="message">Message</label>
            <textarea id="message" name="message" rows="8" required></textarea>

            <input type="submit" value="Send Message">
            <a href="<%= dashboardPath %>" class="back-button">← Back to Dashboard</a>
        </form>
    </div>
</body>
</html>