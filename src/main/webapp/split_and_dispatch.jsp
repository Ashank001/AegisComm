<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, com.gateway.util.DBConnection" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    
    // Check 1: Prevent NullPointerException if session/role is missing
    if (role == null || !role.equalsIgnoreCase("Secondary")) {
        response.sendRedirect("dashboard.jsp?error=Unauthorized access");
        return;
    }

    // Retrieve data passed from SplitForwardServlet
    String messageId = (String) request.getAttribute("messageId");
    String originalMessage = (String) request.getAttribute("originalMessage");

    // Check 2: Prevent Crash if Servlet failed to pass data
    if (messageId == null || originalMessage == null) {
        response.sendRedirect("InboxServlet?error=Error loading message for dispatch. Message may be corrupted or missing.");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Split and Dispatch Orders</title>
    <link rel="stylesheet" href="CSS/compose.css">
    <style>
        .split-section { margin-top: 25px; padding-top: 15px; border-top: 1px dashed #ccc; }
        .split-section label { font-weight: bold; }
        .split-section textarea, .split-section select { margin-bottom: 10px; }
        .add-split-btn { 
            background: #28a745; color: white; border: none; padding: 8px 15px; 
            border-radius: 6px; cursor: pointer; display: block; margin-bottom: 20px;
        }
        .add-split-btn:hover { background: #1e7e34; }
        .recipient-select-group { 
            display: flex; gap: 10px; margin-bottom: 15px; 
            align-items: center; 
        }
        /* Styles copied from compose.css for the table layout */
        .container { max-width: 700px; }
        #ordersTable { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        #ordersTable th { text-align: left; padding: 10px 0; border-bottom: 2px solid #ccc; }
        #ordersTable td { padding: 10px 0; border-bottom: 1px solid #eee; vertical-align: top; }
        #ordersTable textarea { width: 100%; min-height: 80px; resize: vertical; padding: 5px; box-sizing: border-box; }
        #ordersTable select { width: 100%; height: 100px; padding: 5px; box-sizing: border-box; }
        .add-row-btn { background: #28a745; color: white; border: none; padding: 10px 15px; border-radius: 6px; cursor: pointer; margin-top: 10px; display: block; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Split and Dispatch Orders to Soldiers</h2>
        
        <label>Original Decrypted Message (Read Only)</label>
        <textarea rows="6" readonly><%= originalMessage %></textarea>

        <!-- Form for Splitting and Sending -->
        <form action="ProcessSplitForwardServlet" method="post">
            <input type="hidden" name="originalMessageId" value="<%= messageId %>">
            
            <table id="ordersTable">
                <thead>
                    <tr>
                        <th style="width: 50%;">Action Command</th>
                        <th style="width: 40%;">Select Soldiers</th>
                        <th style="width: 10%;"></th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Initial Row - Content populated by script/JSP -->
                    <tr>
                        <td><textarea name="orders[]" rows="4" required placeholder="Specific command..."></textarea></td>
                        <td>
                            <select name="recipients[]" multiple required>
                                <!-- Options will be loaded by JSP helper -->
                                <%= loadSoldierOptions() %>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
            
            <button type="button" class="add-row-btn" onclick="addRow()">+ Add Another Order</button>

            <input type="submit" value="Dispatch Orders Securely">
            <a href="InboxServlet" class="back-button">← Cancel</a>
        </form>
    </div>

    <%! 
        // Helper method to load Soldier options once for the page
        private String loadSoldierOptions() {
            StringBuilder options = new StringBuilder();
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                conn = DBConnection.getConnection();
                String query = "SELECT id, name FROM users WHERE ROLE = 'Soldier' ORDER BY name"; 
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    options.append("<option value='").append(rs.getInt("id")).append("'>")
                           .append(rs.getString("name")).append("</option>");
                }
            } catch (Exception e) {
                 System.err.println("Database error loading soldiers: " + e.getMessage());
                 return ""; 
            } finally {
                if (rs != null) try { rs.close(); } catch (SQLException e) {}
                if (pstmt != null) try { pstmt.close(); } catch (SQLException e) {}
                if (conn != null) try { conn.close(); } catch (SQLException e) {}
            }
            // CRITICAL FIX: Escape the string for JS. Remove newlines, escape single quotes.
            return options.toString().replaceAll("[\r\n]", "").replace("'", "\\'");
        }
    %>
    
    <!-- JavaScript to Add Dynamic Rows -->
    <script>
        // The options are loaded directly into the first row by the JSP.
        // We use the innerHTML of that first select box as the template for new rows.
        function addRow() {
            const tableBody = document.querySelector('#ordersTable tbody');
            const templateRow = tableBody.querySelector('tr');
            
            // Clone the existing row structure
            const newRow = templateRow.cloneNode(true); 
            
            // Clear any existing values and make sure the names are correct
            newRow.querySelectorAll('textarea').forEach(textarea => {
                textarea.value = '';
            });
            newRow.querySelectorAll('select').forEach(select => {
                select.selectedIndex = -1; // Deselect all options
            });
            
            // Add a "Remove" button to the new row
            const removeCell = document.createElement('td');
            removeCell.innerHTML = '<button type="button" onclick="this.closest(\'tr\').remove()" style="background: red; color: white; border: none; padding: 5px; border-radius: 4px; cursor: pointer;">X</button>';
            newRow.appendChild(removeCell);
            
            tableBody.appendChild(newRow);
        }
    </script>
</body>
</html>
