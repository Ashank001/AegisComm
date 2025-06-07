package com.gateway.servlet;
import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.gateway.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/AddUserServlet")
public class AddUserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String role = request.getParameter("role");
        String defaultPassword = "123";
        String hashed = BCrypt.hashpw(defaultPassword, BCrypt.gensalt());

        try (Connection con = DBConnection.getConnection()) {
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, hashed);
            ps.setString(3, role);
            System.out.println("Adding user: " + username);
            System.out.println("Hashed password: " + hashed);
            
            int result = ps.executeUpdate();

            if (result > 0) {
                response.sendRedirect("dashboard.jsp?msg=User Added");
            } else {
                response.getWriter().println("User not added.");
            }
        }catch (SQLIntegrityConstraintViolationException e) {
            // This specifically catches duplicate entry errors
            response.sendRedirect("AddUser.jsp?error=Username+already+exists");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AddUser.jsp?error=Internal+error+occurred");
        }
    }
}
