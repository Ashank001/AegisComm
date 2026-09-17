<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !role.equalsIgnoreCase("Admin")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>View All Users</title>
    <link rel="stylesheet" href="CSS/view_users.css">
</head>
<body>
<div class="container">
    <h2>All System Users</h2>
    <ul class="user-list">
        <%
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                conn = DBConnection.getConnection();
                String query = "SELECT id, name, email, role, profileImg FROM users ORDER BY name";
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    int userId = rs.getInt("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String userRole = rs.getString("role");
                    String userProfileImg = rs.getString("profileImg");
        %>
            <li class="user-item">
                <% if (userProfileImg != null && !userProfileImg.isEmpty()) { %>
                    <img src="ImageServlet?name=<%= userProfileImg %>" alt="Profile Picture">
                <% } else { %>
                    <img src="images/profiles/default.png" alt="Profile Picture">
                <% } %>
                <div class="user-info">
                    <h3><%= name %></h3>
                    <p>Email: <%= email %></p>
                    <p>Role: <%= userRole %></p>
                </div>
                <div class="user-actions">
                    <!-- NEW: Edit Button -->
                    <a href="EditUserServlet?id=<%= userId %>" class="edit-btn">Edit</a>
                    <!-- Delete Form -->
                    <form action="DeleteUserServlet" method="post">
                        <input type="hidden" name="userId" value="<%= userId %>">
                        <button type="submit" class="delete-btn" onclick="return confirm('Are you sure you want to delete this user?');">Delete</button>
                    </form>
                </div>
            </li>
        <%
                }
            } catch (Exception e) {
                e.printStackTrace();
        %>
            <li class="user-item">
                <div class="user-info">
                    <p class="error">Error retrieving users: <%= e.getMessage() %></p>
                </div>
            </li>
        <%
            } finally {
                if (rs != null) try { rs.close(); } catch (SQLException e) {}
                if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
                if (conn != null) try { conn.close(); } catch (SQLException e) {}
            }
        %>
    </ul>
    <a href="dashboard.jsp" class="back-button">← Back to Dashboard</a>
</div>
</body>
</html>
