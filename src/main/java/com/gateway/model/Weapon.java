package com.gateway.model;

import java.io.Serializable;

public class Weapon implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String serialNumber;
    private String weaponName;
    private String modelNumber;
    private String status;
    private int assignedTo; 
    private String assignedToName; 

    // Getters and Setters (Necessary for JSP/Servlet communication)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getWeaponName() { return weaponName; }
    public void setWeaponName(String weaponName) { this.weaponName = weaponName; }

    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAssignedTo() { return assignedTo; }
    public void setAssignedTo(int assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
}