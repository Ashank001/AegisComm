package com.gateway.test;

import com.gateway.util.DBConnection;
import java.sql.*;

public class TestDBConnection {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            if (con != null) {
                System.out.println("Connection to the database is successful!");
            } else {
                System.out.println("Failed to connect to the database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database connection error: " + e.getMessage());
        }
    }
}
