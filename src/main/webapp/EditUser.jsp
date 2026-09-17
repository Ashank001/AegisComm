<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.gateway.model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Edit User</title>
    <link rel="stylesheet" href="CSS/adduser.css">
</head>
<body>
    <%
        User user = (User) request.getAttribute("userToEdit");
        if (user == null) {
            response.sendRedirect("view_users.jsp?error=User data not available.");
            return;
        }
    %>

    <div class="container">
        <h2>Edit User</h2>

        <form action="UpdateUserServlet" method="post" enctype="multipart/form-data">
            <input type="hidden" name="id" value="<%= user.getId() %>">
            <!-- New hidden input to pass the existing image filename -->
            <input type="hidden" name="existingProfileImg" value="<%= user.getProfileImg() %>">
            
            <!-- Full Name -->
            <label for="name">Full Name</label>
            <input type="text" id="name" name="name" value="<%= user.getName() %>" required>

            <!-- Email & Role -->
            <label>Email Address &amp; Role</label>
            <div class="input-row">
                <input type="email" name="email" value="<%= user.getEmail() %>" required>
                <select name="role" required>
                    <option value="TopOrder" <%= "TopOrder".equals(user.getRole()) ? "selected" : "" %>>Top Order</option>
                    <option value="Secondary" <%= "Secondary".equals(user.getRole()) ? "selected" : "" %>>Secondary</option>
                    <option value="Soldier" <%= "Soldier".equals(user.getRole()) ? "selected" : "" %>>Soldier</option>
                    <option value="Admin" <%= "Admin".equals(user.getRole()) ? "selected" : "" %>>Admin</option>
                </select>
            </div>
            
            <!-- Profile Picture -->
            <label>Current Profile Image</label>
            <div class="preview-wrapper">
                <img id="preview" src="ImageServlet?name=<%= user.getProfileImg() %>" alt="Current Profile" />
            </div>
            <label for="profilePic">Change Profile Image (optional)</label>
			<input type="file" id="profilePic" name="profileImg" accept="image/*" onchange="previewImage(event)">

            <input type="submit" value="Update User">

            <a href="view_users.jsp" class="back-button">← Cancel</a>
        </form>
    </div>
    <script>
        function previewImage(event) {
            const input = event.target;
            const preview = document.getElementById("preview");
            if (input.files && input.files[0]) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    preview.src = e.target.result;
                };
                reader.readAsDataURL(input.files[0]);
            }
        }
    </script>
</body>
</html>
