package com.gateway.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;
import com.gateway.util.DBConnection;

@WebServlet("/UpdateUserServlet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,    // 1MB
    maxFileSize = 1024 * 1024 * 5,      // 5MB
    maxRequestSize = 1024 * 1024 * 10   // 10MB
)
public class UpdateUserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"Admin".equalsIgnoreCase((String) session.getAttribute("role"))) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        String userIdStr = request.getParameter("id");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String existingProfileImg = request.getParameter("existingProfileImg"); // Used to handle no new upload

        String profileImgFileName = existingProfileImg;

        try {
            Part filePart = request.getPart("profileImg");

            // Handle new file upload if a file is present
            if (filePart != null && filePart.getSize() > 0) {
                String submittedFileName = filePart.getSubmittedFileName();
                String originalFileName = Paths.get(submittedFileName).getFileName().toString();
                profileImgFileName = UUID.randomUUID().toString() + "_" + originalFileName;

                String uploadDir = getServletContext().getRealPath("/images/profiles");
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, uploadPath.resolve(profileImgFileName));
                }
            }
            
            // Database operations
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE users SET name = ?, email = ?, role = ?, profileImg = ? WHERE id = ?";
                PreparedStatement insertStmt = conn.prepareStatement(sql);
                insertStmt.setString(1, name);
                insertStmt.setString(2, email);
                insertStmt.setString(3, role);
                insertStmt.setString(4, profileImgFileName);
                insertStmt.setInt(5, Integer.parseInt(userIdStr));

                int rowsAffected = insertStmt.executeUpdate();

                if (rowsAffected > 0) {
                    response.sendRedirect("view_users.jsp?success=User updated successfully.");
                } else {
                    response.sendRedirect("view_users.jsp?error=Something went wrong. User not updated.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("view_users.jsp?error=Server Error: " + e.getMessage());
        }
    }
}