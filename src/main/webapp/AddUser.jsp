<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Add New User</title>
    <link rel="stylesheet" href="CSS/adduser.css">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Segoe+UI&display=swap');
    </style>
</head>
<body>
    <div class="container">
        <h2>Add New User</h2>

        <!-- Success/Error Message -->
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
		        

        <form action="AddUserServlet" method="post" enctype="multipart/form-data">
            
            <!-- Full Name -->
            <label for="name">Full Name</label>
            <input type="text" id="name" name="name" required>

            <!-- Email & Role -->
            <label>Email Address and Role</label>
            <div class="input-row">
                <input type="email" name="email" placeholder="Email" required>
                <select name="role" required>
                    <option value="">-- Select Role --</option>
                    <option value="TopOrder">Top Order</option>
                    <option value="Secondary">Secondary</option>
                    <option value="Soldier">Soldier</option>
                </select>
            </div>

            <!-- Profile Picture -->
            <label for="profilePic">Upload Profile Image (optional)</label>
			<input type="file" id="profilePic" name="profileImg" accept="image/*" onchange="previewImage(event)">

            <!-- Preview -->
            <div class="preview-wrapper">
                <img id="preview" src="#" alt="Preview" style="display:none; max-width:100px; border-radius:8px; margin-top:10px;" />
            </div>

            <!-- Checkbox -->
            <div class="checkbox-group">
                <input type="checkbox" id="sendMail" name="sendMail" checked>
                <label for="sendMail">Generate random password and email to user</label>
            </div>

            <!-- Submit + Back -->
            <input type="submit" value="Add User">
            <a href="dashboard.jsp" class="back-button">← Back to Dashboard</a>
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
                    preview.style.display = 'block';
                };
                reader.readAsDataURL(input.files[0]);
            }
        }
    </script>
</body>
</html>