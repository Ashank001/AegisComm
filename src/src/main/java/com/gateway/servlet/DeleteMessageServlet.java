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

        String username = (String) request.getSession().getAttribute("user");
        String idParam = request.getParameter("id");

        try (Connection con = DBConnection.getConnection()) {
            String checkQuery = "SELECT receiver FROM messages WHERE id = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkQuery);
            checkStmt.setInt(1, Integer.parseInt(idParam));
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String receiver = rs.getString("receiver");

                if (receiver.equals(username)) {
                    String deleteQuery = "DELETE FROM messages WHERE id = ?";
                    PreparedStatement deleteStmt = con.prepareStatement(deleteQuery);
                    deleteStmt.setInt(1, Integer.parseInt(idParam));
                    deleteStmt.executeUpdate();
                    AuditLogger.log(username, "Deleted message ID: " + idParam);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect("inbox.jsp");
    }
}
