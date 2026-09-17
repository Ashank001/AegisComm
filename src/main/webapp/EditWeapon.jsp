<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map, com.gateway.model.Weapon" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !role.equalsIgnoreCase("Admin")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }

    Weapon weapon = (Weapon) request.getAttribute("weaponToEdit");
    @SuppressWarnings("unchecked")
    Map<Integer, String> assignableAgents = (Map<Integer, String>) request.getAttribute("assignableAgents");

    if (weapon == null) {
        response.sendRedirect("ViewWeaponsServlet?error=Asset data not found for editing.");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Edit Asset: <%= weapon.getSerialNumber() %></title>
    <link rel="stylesheet" href="CSS/adduser.css">
    <style>
        .input-row select { width: 100%; }
        .input-row > * { flex-basis: 50%; }
        /* Add a style to make the Serial Number readonly distinct */
        input[readonly] { background-color: #f0f0f0; border-style: dashed; } 
    </style>
</head>
<body>
    <div class="container">
        <h2>Edit Asset: <%= weapon.getWeaponName() %></h2>

        <form action="UpdateWeaponServlet" method="post">
            <input type="hidden" name="id" value="<%= weapon.getId() %>">
            
            <label for="name">Weapon Name</label>
            <input type="text" id="name" name="weaponName" value="<%= weapon.getWeaponName() %>" required>

            <label for="serial">Serial Number</label>
            <input type="text" id="serial" name="serialNumber" value="<%= weapon.getSerialNumber() %>" readonly>

            <label for="model">Model Number</label>
            <input type="text" id="model" name="modelNumber" value="<%= weapon.getModelNumber() %>" required>

            <label>Status and Assignment</label>
            <div class="input-row">
                <select name="status" required>
                    <% 
                        String currentStatus = weapon.getStatus();
                        String[] statuses = {"Active", "Maintenance", "Retired"};
                        for (String status : statuses) {
                            String selected = status.equals(currentStatus) ? "selected" : "";
                            out.println("<option value='" + status + "' " + selected + ">" + status + "</option>");
                        }
                    %>
                </select>
                
                <select name="assignedTo" required>
                    <% 
                        int currentAssigneeId = weapon.getAssignedTo();
                        for (Map.Entry<Integer, String> entry : assignableAgents.entrySet()) {
                            int id = entry.getKey();
                            String name = entry.getValue();
                            String label = id == 0 ? name : name + " (ID: " + id + ")";
                            String selected = id == currentAssigneeId ? "selected" : "";
                            out.println("<option value='" + id + "' " + selected + ">" + label + "</option>");
                        }
                    %>
                </select>
            </div>

            <input type="submit" value="Update Asset">
            <a href="ViewWeaponsServlet" class="back-button">← Cancel</a>
        </form>
    </div>
</body>
</html>