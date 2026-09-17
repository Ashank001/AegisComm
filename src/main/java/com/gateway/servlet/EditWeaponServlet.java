package com.gateway.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gateway.model.Weapon;
import com.gateway.util.DBConnection;

@WebServlet("/EditWeaponServlet")
public class EditWeaponServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        String weaponIdStr = request.getParameter("id");

        if (session == null || !"Admin".equalsIgnoreCase(role)) {
            response.sendRedirect("login.jsp?error=Unauthorized access");
            return;
        }

        int weaponId = 0;
        try {
            weaponId = Integer.parseInt(weaponIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect("ViewWeaponsServlet?error=Invalid weapon ID format.");
            return;
        }

        Weapon weapon = null;
        Map<Integer, String> assignableAgents = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {
            
            // 1. Fetch Weapon Details
            String weaponQuery = "SELECT * FROM weapons WHERE id = ?";
            PreparedStatement pstmtW = conn.prepareStatement(weaponQuery);
            pstmtW.setInt(1, weaponId);
            ResultSet rsW = pstmtW.executeQuery();
            
            if (rsW.next()) {
                weapon = new Weapon();
                weapon.setId(rsW.getInt("id"));
                weapon.setSerialNumber(rsW.getString("serial_number"));
                weapon.setWeaponName(rsW.getString("weapon_name"));
                weapon.setModelNumber(rsW.getString("model_number"));
                weapon.setStatus(rsW.getString("status"));
                weapon.setAssignedTo(rsW.getInt("assigned_to"));
            } else {
                response.sendRedirect("ViewWeaponsServlet?error=Asset not found.");
                return;
            }

            // 2. Fetch Assignable Operational Agents (Soldiers)
            String agentQuery = "SELECT id, name FROM users WHERE ROLE = 'Soldier' ORDER BY name";
            PreparedStatement pstmtA = conn.prepareStatement(agentQuery);
            ResultSet rsA = pstmtA.executeQuery();
            
            assignableAgents.put(0, "Unassigned"); // Add the unassigned option
            while (rsA.next()) {
                assignableAgents.put(rsA.getInt("id"), rsA.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ViewWeaponsServlet?error=Database Error: " + e.getMessage());
            return;
        }

        request.setAttribute("weaponToEdit", weapon);
        request.setAttribute("assignableAgents", assignableAgents);
        RequestDispatcher dispatcher = request.getRequestDispatcher("EditWeapon.jsp");
        dispatcher.forward(request, response);
    }
}