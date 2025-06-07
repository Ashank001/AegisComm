package com.gateway.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.gateway.util.DBConnection;
import com.gateway.util.AuditLogger;

@WebServlet("/SendMessageServlet")
public class SendMessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String sender = (String) request.getSession().getAttribute("user");
        String receiver = request.getParameter("receiver");
        String subject = request.getParameter("subject");
        String body = request.getParameter("body");
        String priority = request.getParameter("priority"); 
        String sendAll = request.getParameter("sendAll");
        System.out.println(">> Priority: " + priority);
        try (Connection con = DBConnection.getConnection()) {
            if ("on".equals(sendAll)) {
           
            	String sql = "SELECT username FROM users WHERE username != ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, sender);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String toUser = rs.getString("username");
                    PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO messages (sender, receiver, subject, body, priority) VALUES (?, ?, ?, ?, ?)");
                    insert.setString(1, sender);
                    insert.setString(2, toUser);
                    insert.setString(3, subject);
                    insert.setString(4, body);
                    insert.setString(5, priority);
                    insert.executeUpdate();
                }

                AuditLogger.log(sender, "Broadcasted (" + priority + ") message to all users");

            } else {
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO messages (sender, receiver, subject, body, priority) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, sender);
                ps.setString(2, receiver);
                ps.setString(3, subject);
                ps.setString(4, body);
                ps.setString(5, priority);
                ps.executeUpdate();

                AuditLogger.log(sender, "Sent (" + priority + ") message to " + receiver);
            }

            response.sendRedirect("inbox.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h3>Database error while sending message!</h3>");
        }System.out.println("Priority from form: " + request.getParameter("priority"));

    }
}
