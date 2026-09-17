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

import com.gateway.model.Border;
import com.gateway.util.DBConnection;

@WebServlet("/ViewBordersServlet")
public class ViewBordersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");

        if (session == null || !"Admin".equalsIgnoreCase(role)) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        List<Border> borderZones = new ArrayList<>();
        String dbError = null;

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM borders ORDER BY threat_level DESC, location_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Border border = new Border();
                border.setId(rs.getInt("id"));
                border.setLocationName(rs.getString("location_name"));
                border.setCoordinates(rs.getString("coordinates"));
                border.setThreatLevel(rs.getString("threat_level"));
                border.setLastUpdated(rs.getTimestamp("last_updated"));
                borderZones.add(border);
            }

        } catch (Exception e) {
            e.printStackTrace();
            dbError = "Database error loading border zones: " + e.getMessage();
        }

        request.setAttribute("borderZones", borderZones);
        request.setAttribute("dbError", dbError);
        RequestDispatcher dispatcher = request.getRequestDispatcher("ViewBorders.jsp");
        dispatcher.forward(request, response);
    }
}