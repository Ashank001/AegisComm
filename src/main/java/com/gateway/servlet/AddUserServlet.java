package com.gateway.servlet;

import javax.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // Import HttpSession
import javax.servlet.http.Part;
import org.mindrot.jbcrypt.BCrypt;
import com.gateway.util.DBConnection;
import com.gateway.util.MailSender;
import com.gateway.util.AuditLogger; // <-- CRITICAL: Import the Audit Logger

@WebServlet("/AddUserServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1MB
    maxFileSize = 1024 * 1024 * 5,      // 5MB
    maxRequestSize = 1024 * 1024 * 10   // 10MB
)
public class AddUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String redirectUrl = "AddUser.jsp";
        Connection conn = null;
        
        // Get the current user's session (the Admin performing the action)
        HttpSession session = request.getSession(false);
        String adminEmail = (String) session.getAttribute("userEmail"); 

        try {
            // Get form parameters
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String role = request.getParameter("role");
            boolean generatePassword = request.getParameter("sendMail") != null;

            String password = generatePassword ? generateRandomPassword(10) : "123";
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            String profileImgFileName = null;
            Part filePart = request.getPart("profileImg");

            // Handle file upload if a file is present
            if (filePart != null && filePart.getSize() > 0) {
                String submittedFileName = filePart.getSubmittedFileName();
                String originalFileName = Paths.get(submittedFileName).getFileName().toString();
                profileImgFileName = UUID.randomUUID().toString() + "_" + originalFileName;

                String uploadDir = getServletContext().getRealPath("/images/profiles");
                Path uploadPath = Paths.get(uploadDir);

                System.out.println("Resolved Upload Path: " + uploadPath);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, uploadPath.resolve(profileImgFileName));
                }
            }

            // Database operations
            conn = DBConnection.getConnection();
            
            // Check if email already exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                redirectUrl += "?error=Email already registered.";
                response.sendRedirect(redirectUrl);
                return;
            }

            // Insert new user
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO users (name, email, password, role, profileImg) VALUES (?, ?, ?, ?, ?)"
            );
            insertStmt.setString(1, name);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, role);
            insertStmt.setString(5, profileImgFileName);

            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                // --- AUDIT LOG INTEGRATION (CRITICAL LINES) ---
                AuditLogger.log(
                    adminEmail,
                    "CREATED USER: " + email + " (Role: " + role + ")"
                );

                if (generatePassword) {
                    String msg = "Hi " + name + ",\n\nYou have been added to the Secure Gateway System.\n" +
                                 "Login using the credentials:\nEmail: " + email + "\nPassword: " + password +
                                 "\n\nChange your password after login for security.";
                    MailSender.send(email, "Your Secure Gateway Access", msg);
                }
                redirectUrl += "?success=User added successfully.";
            } else {
                redirectUrl += "?error=Something went wrong. User not added.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectUrl += "?error=Server Error: " + e.getMessage();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        response.sendRedirect(redirectUrl);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}