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

import com.gateway.model.Weapon;
import com.gateway.util.DBConnection;

@WebServlet("/ViewWeaponsServlet")
public class ViewWeaponsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");

        // Security check: Only Admin can view weapon logs
        if (session == null || !"Admin".equalsIgnoreCase(role)) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        List<Weapon> weaponInventory = new ArrayList<>();
        String dbError = null;

        try (Connection conn = DBConnection.getConnection()) {
            // SQL JOIN: Get weapon details and the name of the assigned soldier
            String query = "SELECT w.id, w.serial_number, w.weapon_name, w.model_number, w.status, w.assigned_to, u.name AS assigned_name " +
                           "FROM weapons w LEFT JOIN users u ON w.assigned_to = u.id ORDER BY w.weapon_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Weapon weapon = new Weapon();
                weapon.setId(rs.getInt("id"));
                weapon.setSerialNumber(rs.getString("serial_number"));
                weapon.setWeaponName(rs.getString("weapon_name"));
                weapon.setModelNumber(rs.getString("model_number"));
                weapon.setStatus(rs.getString("status"));
                weapon.setAssignedTo(rs.getInt("assigned_to"));
                
                // Set the assigned name (will be null if unassigned)
                weapon.setAssignedToName(rs.getString("assigned_name")); 
                
                weaponInventory.add(weapon);
            }

        } catch (Exception e) {
            e.printStackTrace();
            dbError = "Database error loading inventory: " + e.getMessage();
        }

        request.setAttribute("weaponInventory", weaponInventory);
        request.setAttribute("dbError", dbError);
        RequestDispatcher dispatcher = request.getRequestDispatcher("ViewWeapons.jsp");
        dispatcher.forward(request, response);
    }
}