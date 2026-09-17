package com.gateway.servlet;

import com.gateway.util.DBConnection;
import com.gateway.model.AuditLog;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/AuditLogServlet")
public class AuditLogServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");

        // Security check: Only Admin can view audit logs
        if (session == null || !"Admin".equalsIgnoreCase(role)) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        List<AuditLog> logs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM audit_logs ORDER BY timestamp DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getInt("id"));
                log.setUserEmail(rs.getString("user_email"));
                log.setAction(rs.getString("action"));
                log.setHashPrevious(rs.getString("hash_previous"));
                log.setHashCurrent(rs.getString("hash_current"));
                log.setTimestamp(rs.getTimestamp("timestamp"));
                logs.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Store error message to display in the JSP
            request.setAttribute("error", "Database error loading logs: " + e.getMessage());
        }

        request.setAttribute("auditLogs", logs);
        RequestDispatcher dispatcher = request.getRequestDispatcher("audit.jsp");
        dispatcher.forward(request, response);
    }
}