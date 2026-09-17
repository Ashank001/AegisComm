package com.gateway.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gateway.model.Border;
import com.gateway.util.DBConnection;

@WebServlet("/EditBorderServlet")
public class EditBorderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        String borderIdStr = request.getParameter("id");

        if (session == null || !"Admin".equalsIgnoreCase(role)) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        int borderId = 0;
        try {
            borderId = Integer.parseInt(borderIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("ViewBordersServlet?error=Invalid border ID format.");
            return;
        }

        Border border = null;

        try (Connection conn = DBConnection.getConnection()) {
            
            String query = "SELECT * FROM borders WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, borderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                border = new Border();
                border.setId(rs.getInt("id"));
                border.setLocationName(rs.getString("location_name"));
                border.setCoordinates(rs.getString("coordinates"));
                border.setThreatLevel(rs.getString("threat_level"));
            } else {
                response.sendRedirect("ViewBordersServlet?error=Border zone not found.");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ViewBordersServlet?error=Database Error: " + e.getMessage());
            return;
        }

        request.setAttribute("borderToEdit", border);
        RequestDispatcher dispatcher = request.getRequestDispatcher("EditBorder.jsp");
        dispatcher.forward(request, response);
    }
}