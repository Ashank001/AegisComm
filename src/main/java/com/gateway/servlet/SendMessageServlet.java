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
import javax.crypto.SecretKey;

import com.gateway.util.AESUtil;
import com.gateway.util.DBConnection;
import com.gateway.util.MasterKeyUtil;
import com.gateway.util.AuditLogger;

@WebServlet("/SendMessageServlet")
public class SendMessageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String senderId = (String) session.getAttribute("userId");
        String senderEmail = (String) session.getAttribute("userEmail"); // For auditing

        if (session == null || senderId == null) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        // CRITICAL CHANGE: Use getParameterValues to get an array of IDs
        String[] recipientIds = request.getParameterValues("recipientIds");
        String message = request.getParameter("message");
        
        if (recipientIds == null || recipientIds.length == 0 || message == null || message.isEmpty()) {
            response.sendRedirect("compose.jsp?error=At least one recipient and a message are required.");
            return;
        }

        int totalMessagesSent = 0;

        try (Connection conn = DBConnection.getConnection()) {
            
            String sql = "INSERT INTO messages (sender_id, receiver_id, encrypted_text, encrypted_aes_key) VALUES (?, ?, ?, ?)";
            
            for (String recipientId : recipientIds) {
                // 1. Generate a unique, random AES key for this message
                SecretKey uniqueKey = AESUtil.generateKey();
                
                // 2. Encrypt the message with the unique key
                String encryptedMessage = AESUtil.encrypt(message, uniqueKey);
                
                // 3. Encrypt the unique key with the Master Key
                String encryptedAesKey = MasterKeyUtil.encryptKey(uniqueKey);
                
                // 4. Execute the database insert for THIS specific recipient
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(senderId));
                pstmt.setInt(2, Integer.parseInt(recipientId));
                pstmt.setString(3, encryptedMessage);
                pstmt.setString(4, encryptedAesKey);
                
                totalMessagesSent += pstmt.executeUpdate();
            }

            if (totalMessagesSent > 0) {
                // AUDIT LOG: Log the bulk action
                AuditLogger.log(
                    senderEmail,
                    "Sent message to " + totalMessagesSent + " recipients (Encrypted by AES)."
                );
                response.sendRedirect("compose.jsp?success=Message dispatched to " + totalMessagesSent + " recipient(s) successfully!");
            } else {
                response.sendRedirect("compose.jsp?error=Failed to dispatch message.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("compose.jsp?error=Server error: " + e.getMessage());
        }
    }
}