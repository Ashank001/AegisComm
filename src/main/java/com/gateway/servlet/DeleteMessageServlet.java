package com.gateway.servlet;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.sql.*;
import com.gateway.util.DBConnection;
import com.gateway.util.AuditLogger;


@WebServlet("/DeleteMessageServlet")
public class DeleteMessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        String userIdStr = (String) session.getAttribute("userId");
        String userEmail = (String) session.getAttribute("userEmail");
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("InboxServlet?error=Message ID required.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String deleteQuery = "DELETE FROM messages WHERE id = ? AND receiver_id = ?";
            try (PreparedStatement deleteStmt = con.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, Integer.parseInt(idParam.trim()));
                deleteStmt.setInt(2, Integer.parseInt(userIdStr));
                int deleted = deleteStmt.executeUpdate();

                if (deleted > 0) {
                    AuditLogger.log(userEmail, "Deleted message ID: " + idParam.trim());
                    response.sendRedirect("InboxServlet?success=Message deleted successfully.");
                    return;
                } else {
                    response.sendRedirect("InboxServlet?error=Message not found or unauthorized.");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("InboxServlet?error=Server error deleting message.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("InboxServlet");
    }
}
