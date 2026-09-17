package com.gateway.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.crypto.SecretKey;

import com.gateway.model.Message;
import com.gateway.util.AESUtil;
import com.gateway.util.DBConnection;
import com.gateway.util.MasterKeyUtil;

@WebServlet("/InboxServlet")
public class InboxServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String userIdStr = (String) session.getAttribute("userId");

        if (userIdStr == null) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        List<Message> messages = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT m.id, m.sender_id, m.encrypted_text, m.encrypted_aes_key, m.timestamp, u.name as sender_name FROM messages m JOIN users u ON m.sender_id = u.id WHERE m.receiver_id = ? ORDER BY m.timestamp DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, Integer.parseInt(userIdStr));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = new Message();
                message.setId(rs.getInt("id"));
                message.setSenderId(rs.getInt("sender_id"));
                message.setSenderName(rs.getString("sender_name"));
                
                try {
                    String encryptedAesKey = rs.getString("encrypted_aes_key");
                    SecretKey uniqueKey = MasterKeyUtil.decryptKey(encryptedAesKey);

                    String encryptedText = rs.getString("encrypted_text");
                    String decryptedText = AESUtil.decrypt(encryptedText, uniqueKey);
                    
                    message.setDecryptedText(decryptedText);
                } catch (Exception e) {
                    message.setDecryptedText("ERROR: Could not decrypt message.");
                    e.printStackTrace();
                }
                
                message.setTimestamp(rs.getTimestamp("timestamp"));
                messages.add(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("messages", messages);
        RequestDispatcher dispatcher = request.getRequestDispatcher("inbox.jsp");
        dispatcher.forward(request, response);
    }
}
