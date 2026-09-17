package com.gateway.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gateway.util.AESUtil;
import com.gateway.util.DBConnection;
import com.gateway.util.MasterKeyUtil;
import com.gateway.util.AuditLogger;
import javax.crypto.SecretKey;

@WebServlet("/ProcessForwardServlet")
public class ProcessForwardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String senderId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (session == null || !role.equalsIgnoreCase("TopOrder")) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        String originalMessageId = request.getParameter("originalMessageId");
        String recipientId = request.getParameter("recipientId");
        String editedMessage = request.getParameter("editedMessage");

        if (recipientId == null || recipientId.isEmpty() || editedMessage == null || editedMessage.isEmpty()) {
            response.sendRedirect("InboxServlet?error=Recipient and message are required.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // New Hybrid Encryption Flow
            // 1. Generate a unique, random AES key for this message
            SecretKey uniqueKey = AESUtil.generateKey();
            
            // 2. Encrypt the edited message with the unique key
            String encryptedMessage = AESUtil.encrypt(editedMessage, uniqueKey);
            
            // 3. Encrypt the unique key with the Master Key
            String encryptedAesKey = MasterKeyUtil.encryptKey(uniqueKey);
            
            // 4. Insert the new message into the database
            String sql = "INSERT INTO messages (sender_id, receiver_id, encrypted_text, encrypted_aes_key) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(senderId));
            pstmt.setInt(2, Integer.parseInt(recipientId));
            pstmt.setString(3, encryptedMessage);
            pstmt.setString(4, encryptedAesKey);
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	String senderEmail = (String) session.getAttribute("userEmail");
                AuditLogger.log(
                    senderEmail, 
                    "Edited and FORWARDED message ID " + originalMessageId + " to Secondary ID " + recipientId
                );
                response.sendRedirect("InboxServlet?success=Message forwarded to Secondary!");
            } else {
                response.sendRedirect("InboxServlet?error=Failed to forward message.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("InboxServlet?error=Server error during forwarding.");
        }
    }
}
