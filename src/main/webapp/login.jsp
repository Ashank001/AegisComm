<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Secure Gateway - Login</title>
    <link rel="icon" href="images/AegisComm.jpg">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="CSS/login.css">  
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
    <div class="login-box">
        <div class="logo-wrapper">
            <img src="images/AegisComm.jpg" alt="Secure Gateway Logo" class="logo">
            <h2>Secure Gateway</h2>
        </div>

        <form action="LoginServlet" method="post">
            <input type="email" name="email" placeholder="Email" required>

            <div class="password-wrapper">
			    <input type="password" name="password" id="password" placeholder="Password" required>
			    <span class="toggle-password" onclick="togglePassword()">
			        <i id="eyeIcon" class="fas fa-eye"></i>
			    </span>
			</div>
			            

            <input type="submit" value="Login">

            <div class="message">
                <%
                    String error = request.getParameter("error");
                    if (error != null) {
                        out.println("<span class='error'>" + error + "</span>");
                    }
                %>
            </div>

            <div class="bottom-links">
                <a href="forgot.jsp" class="left-link">Forgot Password?</a>
            </div>
        </form>
    </div>

    <script>
	    function togglePassword() {
	        const passwordInput = document.getElementById("password");
	        const eyeIcon = document.getElementById("eyeIcon");
	
	        if (passwordInput.type === "password") {
	            passwordInput.type = "text";
	            eyeIcon.classList.remove("fa-eye");
	            eyeIcon.classList.add("fa-eye-slash");
	        } else {
	            passwordInput.type = "password";
	            eyeIcon.classList.remove("fa-eye-slash");
	            eyeIcon.classList.add("fa-eye");
	        }
	    }

    </script>
</body>
</html>
