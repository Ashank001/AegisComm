package com.gateway.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String userEmail;
    private String action;
    private String hashPrevious;
    private String hashCurrent;
    private Timestamp timestamp;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getHashPrevious() { return hashPrevious; }
    public void setHashPrevious(String hashPrevious) { this.hashPrevious = hashPrevious; }
    
    public String getHashCurrent() { return hashCurrent; }
    public void setHashCurrent(String hashCurrent) { this.hashCurrent = hashCurrent; }
    
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}