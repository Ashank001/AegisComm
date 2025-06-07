<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !role.equals("admin")) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<title> AddUser</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" href="images/AegisComm.jpg">
<link rel="stylesheet" href=CSS/adduser.css>
<% 
    String msg = request.getParameter("msg"); 
%>

<% if (msg != null) { %>
    <div class="alert alert-success">
        <%= msg %>
    </div>
<% } %>
<div class="container">
	<div class="logo-header">
    	<img src="images/AegisComm.jpg" alt="Secure Gateway Logo">
	</div>
	<h2>Add New User</h2>
	 <%
	 	String error = request.getParameter("error");
        if (error != null) {
	 %>
	<div style="color: red; text-align: center; margin-bottom: 10px;">
		<%= error %>
	</div>
	<%}%>
	<form action="AddUserServlet" method="post" class="form-horizontal">
	    <div class="form-group">
	        <label for="username" class="col-sm-2 control-label">Username:</label>
	        <div class="col-sm-10">
	            <input type="text" name="username" id="username" class="form-control" required>
	        </div>
	    </div>
	    <div class="form-group">
	        <label for="role" class="col-sm-2 control-label"> Select Role:</label>
	        <div class="col-sm-10">
	            <select name="role" id="role" class="form-control" required>
	                <option value="user">User</option>
	                <option value="admin">Admin</option>
	            </select>
	        </div>
	    </div>
		<h6> * Default password for new users : 123</h6>
	    <div class="form-group">
	        <div class="col-sm-offset-2 col-sm-10">
	            <input type="submit" value="Add User" class="btn btn-primary">
	        </div>
	    </div>
	</form>
	<a href="dashboard.jsp" class="button" >Back to Dashboard</a>
	
</div>
