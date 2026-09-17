package com.gateway.servlet;

import java.io.IOException;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.crypto.SecretKey;

import com.gateway.util.DBConnection;
import com.gateway.util.AESUtil;
import com.gateway.util.MasterKeyUtil;

@WebServlet("/SplitForwardServlet")
public class SplitForwardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        String messageIdStr = request.getParameter("id");

        // Security Check: Only Secondary can access this feature
        if (session == null || !role.equalsIgnoreCase("Secondary")) {
            response.sendRedirect("dashboard_second.jsp?error=Unauthorized to split messages.");
            return;
        }

        if (messageIdStr == null || messageIdStr.isEmpty()) {
            response.sendRedirect("InboxServlet?error=Message ID not provided.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT encrypted_text, encrypted_aes_key FROM messages WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, Integer.parseInt(messageIdStr));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String encryptedText = rs.getString("encrypted_text");
                String encryptedAesKey = rs.getString("encrypted_aes_key");

                // Decrypt the message key and content
                SecretKey uniqueKey = MasterKeyUtil.decryptKey(encryptedAesKey);
                String decryptedText = AESUtil.decrypt(encryptedText, uniqueKey);

                // Pass the decrypted message and its ID to the specialized splitting form
                request.setAttribute("messageId", messageIdStr);
                request.setAttribute("originalMessage", decryptedText);

                RequestDispatcher dispatcher = request.getRequestDispatcher("split_and_dispatch.jsp");
                dispatcher.forward(request, response);
            } else {
                response.sendRedirect("InboxServlet?error=Message not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("InboxServlet?error=Server error during decryption.");
        }
    }
}