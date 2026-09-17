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

@WebServlet("/ProcessSplitForwardServlet")
public class ProcessSplitForwardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String senderId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        // Security Check: Only Secondary can access this
        if (session == null || !role.equalsIgnoreCase("Secondary")) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        String originalMessageId = request.getParameter("originalMessageId"); 
        
        // CRITICAL FIX: Use getParameterValues for all array inputs
        // This receives an array where orders[i] corresponds to the recipient list recipientsArrays[i]
        String[] orders = request.getParameterValues("orders[]");
        String[] recipientArrays = request.getParameterValues("recipients[]");
        
        if (orders == null || orders.length == 0 || recipientArrays == null || recipientArrays.length == 0) {
            response.sendRedirect("InboxServlet?error=Orders or recipients missing.");
            return;
        }

        if (orders.length != recipientArrays.length) {
             response.sendRedirect("InboxServlet?error=Mismatched number of orders and recipient lists.");
             return;
        }

        int totalMessagesSent = 0;

        try (Connection conn = DBConnection.getConnection()) {
            
            String sql = "INSERT INTO messages (sender_id, receiver_id, encrypted_text, encrypted_aes_key) VALUES (?, ?, ?, ?)";
            
            for (int i = 0; i < orders.length; i++) {
                String orderText = orders[i];
                String recipientString = recipientArrays[i]; 
                
                // Recipients are sent as a single string of comma-separated IDs if multiple are selected
                String[] individualRecipientIds = recipientString.split(","); 
                
                for (String soldierId : individualRecipientIds) {
                    if (soldierId == null || soldierId.trim().isEmpty()) continue;

                    // --- CRITICAL FIX ---
                    // 1. Clean the ID string: Remove ALL non-digit characters (quotes, backslashes, etc.)
                    String cleanSoldierId = soldierId.trim().replaceAll("[^0-9]", "");
                    
                    if (cleanSoldierId.isEmpty()) continue; // Skip if cleaning resulted in an empty string
                    
                    // 2. Generate and encrypt message components
                    SecretKey uniqueKey = AESUtil.generateKey();
                    String encryptedMessage = AESUtil.encrypt(orderText, uniqueKey);
                    String encryptedAesKey = MasterKeyUtil.encryptKey(uniqueKey);
                    
                    // 3. Execute the database insert
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, Integer.parseInt(senderId));
                    pstmt.setInt(2, Integer.parseInt(cleanSoldierId)); // Use the now clean and safe ID
                    pstmt.setString(3, encryptedMessage);
                    pstmt.setString(4, encryptedAesKey);
                    
                    totalMessagesSent += pstmt.executeUpdate();
                }
            }

            if (totalMessagesSent > 0) {
            	String senderEmail = (String) session.getAttribute("userEmail");
                AuditLogger.log(
                    senderEmail, 
                    "SPLIT and DISPATCHED " + totalMessagesSent + " orders (Original ID " + originalMessageId + ")."
                );
                response.sendRedirect("InboxServlet"); 
            } else {
                response.sendRedirect("InboxServlet?error=No orders were successfully dispatched.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: Redirect to Inbox with a generic error
            response.sendRedirect("InboxServlet?error=Dispatch failed due to server logic error.");
        }
    }
}