<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.gateway.model.Weapon" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !role.equalsIgnoreCase("Admin")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Weapon> inventory = (List<Weapon>) request.getAttribute("weaponInventory");
    String dbError = (String) request.getAttribute("dbError");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Weapon Inventory</title>
    <link rel="stylesheet" href="CSS/view_inventory.css">
</head>
<body>
<div class="container">
    <h2>Organizational Weapon Inventory</h2>
    
    <% if (dbError != null) { %>
        <div class="alert error"><%= dbError %></div>
    <% } %>

    <div class="inventory-controls">
        <a href="AddWeapon.jsp" class="button add-btn">➕ Add New Weapon</a>
    </div>

    <table class="inventory-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Weapon Name</th>
                <th>Serial Number</th>
                <th>Model</th>
                <th>Status</th>
                <th>Assigned To</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <% if (inventory != null && !inventory.isEmpty()) { %>
            <% for (Weapon w : inventory) { %>
                <tr>
                    <td><%= w.getId() %></td>
                    <td><%= w.getWeaponName() %></td>
                    <td><%= w.getSerialNumber() %></td>
                    <td><%= w.getModelNumber() %></td>
                    <td>
                        <span class="status-badge status-<%= w.getStatus().toLowerCase() %>">
                            <%= w.getStatus() %>
                        </span>
                    </td>
                    <td>
                        <%= w.getAssignedToName() != null ? w.getAssignedToName() : "Unassigned" %>
                    </td>
                    <td>
                        <a href="EditWeaponServlet?id=<%= w.getId() %>" class="action-btn edit-btn">Edit</a>
                        <form action="DeleteWeaponServlet" method="post" style="display:inline; margin-left: 10px;">
                            <input type="hidden" name="weaponId" value="<%= w.getId() %>">
                            <button type="submit" class="action-btn delete-btn" onclick="return confirm('WARNING: Are you sure you want to delete <%= w.getWeaponName() %> (SN: <%= w.getSerialNumber() %>)?');">
                                Delete
                            </button>
                        </form>
                    </td>
                </tr>
            <% } %>
        <% } else { %>
            <tr><td colspan="7" class="no-data">No weapons currently registered in the inventory.</td></tr>
        <% } %>
        </tbody>
    </table>
    <a href="dashboard.jsp" class="back-button">← Back to Dashboard</a>
</div>
</body>
</html>