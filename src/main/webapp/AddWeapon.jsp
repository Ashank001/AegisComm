<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !role.equalsIgnoreCase("Admin")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Add Weapon</title>
    <link rel="stylesheet" href="CSS/adduser.css">
    <style>
        .input-row select { width: 100%; }
        /* Reusing some styles from adduser for consistency */
        .input-row { display: flex; gap: 10px; margin-bottom: 18px; }
        .input-row > * { flex-basis: 50%; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Add New Weapon to Inventory</h2>

        <% if (success != null) { %>
            <div class="alert success"><%= success %></div>
        <% } else if (error != null) { %>
            <div class="alert error"><%= error %></div>
        <% } %>

        <form action="AddWeaponServlet" method="post">
            
            <label for="name">Weapon Name</label>
            <input type="text" id="name" name="weaponName" required>

            <label for="serial">Serial Number (Unique)</label>
            <input type="text" id="serial" name="serialNumber" required>

            <label for="model">Model Number</label>
            <input type="text" id="model" name="modelNumber" required>

            <label>Status and Assignment</label>
            <div class="input-row">
                <select name="status" required>
                    <option value="Active">Active</option>
                    <option value="Maintenance">Maintenance</option>
                    <option value="Retired">Retired</option>
                </select>
                <select name="assignedTo" required>
                    <option value="0">Unassigned</option>
                    <% 
                        Connection conn = null;
                        PreparedStatement pstmt = null;
                        ResultSet rs = null;
                        try {
                            conn = DBConnection.getConnection();
                            // Only Soldiers can be assigned weapons
                            String query = "SELECT id, name FROM users WHERE ROLE = 'Soldier' ORDER BY name"; 
                            pstmt = conn.prepareStatement(query);
                            rs = pstmt.executeQuery();
                            
                            while (rs.next()) {
                                out.println("<option value='" + rs.getInt("id") + "'>" + rs.getString("name") + "</option>");
                            }
                        } catch (Exception e) {
                            out.println("<option disabled>Error loading soldiers</option>");
                        } finally {
                            if (rs != null) try { rs.close(); } catch (SQLException e) {}
                            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
                            if (conn != null) try { conn.close(); } catch (SQLException e) {}
                        }
                    %>
                </select>
            </div>

            <input type="submit" value="Add Weapon">
            <a href="dashboard.jsp" class="back-button">← Back to Dashboard</a>
        </form>
    </div>
</body>
</html>