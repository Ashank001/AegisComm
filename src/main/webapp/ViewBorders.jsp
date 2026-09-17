<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.gateway.model.Border" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !role.equalsIgnoreCase("Admin")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Border> borderZones = (List<Border>) request.getAttribute("borderZones");
    String dbError = (String) request.getAttribute("dbError");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Border Zone Management</title>
    <link rel="stylesheet" href="CSS/view_inventory.css"> <style>
        .threat-level-Low { background-color: #d4edda; color: #155724; }
        .threat-level-Medium { background-color: #fff3cd; color: #856404; }
        .threat-level-High { background-color: #f8d7da; color: #721c24; }
        .threat-level-Critical { background-color: #f00; color: white; }
    </style>
</head>
<body>
<div class="container">
    <h2>Secure Border Zone Status</h2>
    
    <% if (dbError != null) { %>
        <div class="alert error"><%= dbError %></div>
    <% } %>

    <div class="inventory-controls">
        <a href="AddBorder.jsp" class="button add-btn">➕ Register New Zone</a>
    </div>

    <table class="inventory-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Location Name</th>
                <th>Coordinates</th>
                <th>Threat Level</th>
                <th>Last Update</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <% if (borderZones != null && !borderZones.isEmpty()) { %>
            <% for (Border b : borderZones) { %>
                <tr>
                    <td><%= b.getId() %></td>
                    <td><%= b.getLocationName() %></td>
                    <td><%= b.getCoordinates() %></td>
                    <td>
                        <span class="status-badge threat-level-<%= b.getThreatLevel().toLowerCase() %>">
                            <%= b.getThreatLevel() %>
                        </span>
                    </td>
                    <td><%= b.getLastUpdated() %></td>
                    <td>
                        <a href="EditBorderServlet?id=<%= b.getId() %>" class="action-btn edit-btn">Edit</a>
                        <form action="DeleteBorderServlet" method="post" style="display:inline; margin-left: 10px;">
                            <input type="hidden" name="borderId" value="<%= b.getId() %>">
                            <button type="submit" class="action-btn delete-btn" onclick="return confirm('WARNING: Are you sure you want to delete the <%= b.getLocationName() %> border zone?');">
                                Delete
                            </button>
                        </form>
                    </td>
                </tr>
            <% } %>
        <% } else { %>
            <tr><td colspan="6" class="no-data">No border zones currently registered.</td></tr>
        <% } %>
        </tbody>
    </table>
    <a href="dashboard.jsp" class="back-button">← Back to Dashboard</a>
</div>
</body>
</html>