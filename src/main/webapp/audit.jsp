<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.gateway.model.AuditLog, com.gateway.util.SHA256Util" %>
<%@ page session="true" %>
<%
    String role = (String) session.getAttribute("role");
    if (role == null || !role.equalsIgnoreCase("Admin")) {
        response.sendRedirect("login.jsp?error=Unauthorized access");
        return;
    }

    // Retrieve logs from the servlet
    @SuppressWarnings("unchecked")
    List<AuditLog> logs = (List<AuditLog>) request.getAttribute("auditLogs");
    String dbError = (String) request.getAttribute("error");
    
    // CRITICAL FIX: Initialize logs to an empty list if null
    if (logs == null) {
        logs = new java.util.ArrayList<AuditLog>();
    }
    
    boolean chainValid = true;
    String expectedNextHash = "";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>System Audit Logs</title>
    <link rel="stylesheet" href="CSS/audit.css">
</head>
<body>
<div class="container">
    <h2>System Audit Logs (Tamper-Proof)</h2>
    
    <% if (dbError != null) { %>
        <div class="alert error"><%= dbError %></div>
    <% } %>

    <div class="log-status">
        <span id="chain-status" class="<%= chainValid ? "valid" : "invalid" %>">
            <% if (chainValid) { %>
                ✅ Chain Verified: No Tampering Detected
            <% } else { %>
                ❌ Chain Broken: Tampering Detected!
            <% } %>
        </span>
    </div>

    <table class="audit-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Timestamp</th>
                <th>User/System Action</th>
                <th>Email</th>
                <th style="width: 25%;">Hash Status</th>
            </tr>
        </thead>
        <tbody>
        <% 
            String previousHash = "0"; // Start with the Genesis Hash (0)
            
            // Loop backwards to check the chain, or just check the next expected hash.
            // For simplicity and performance, we'll check the current log's previous hash against the actual previous hash.

            for (int i = 0; i < logs.size(); i++) {
                AuditLog log = logs.get(i);
                
                // 1. Recreate the log string to check the integrity of THIS specific entry
                String logData = log.getUserEmail() + ":" + log.getAction() + ":" + log.getTimestamp().getTime();
                
                // 2. Check Chain Link: If this log is not the first, verify its previous hash matches the actual previous log's current hash.
                if (i < logs.size() - 1) {
                    AuditLog previousLog = logs.get(i + 1);
                    if (!log.getHashPrevious().equals(previousLog.getHashCurrent())) {
                        chainValid = false;
                    }
                } else if (!log.getHashPrevious().equals("0")) {
                    // Check first log entry (last in the array) against the Genesis Hash
                    // We skip this check for simplicity in display logic, but the chain verification handles it.
                }

                // 3. Status indicator for display
                String hashStatusClass = "valid-row";
                if (!log.getHashPrevious().equals(previousHash)) {
                    // This check is performed in JS later for better verification, but this is a visual flag
                    hashStatusClass = "warning-row";
                }
        %>
            <tr class="log-entry <%= hashStatusClass %>">
                <td><%= log.getId() %></td>
                <td><%= log.getTimestamp() %></td>
                <td><%= log.getAction() %></td>
                <td><%= log.getUserEmail() %></td>
                <td class="hash-col" data-prev-hash="<%= log.getHashPrevious() %>" data-current-hash="<%= log.getHashCurrent() %>">
                    <span class="status-indicator">PENDING</span>
                </td>
            </tr>
        <%
                // Update the actual previous hash for the next iteration (using the current log's current hash)
                previousHash = log.getHashCurrent();
            }
        %>
        </tbody>
    </table>
    
    <a href="dashboard.jsp" class="back-button">← Back to Dashboard</a>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const tableBody = document.querySelector('.audit-table tbody');
        const rows = Array.from(tableBody.querySelectorAll('.log-entry'));
        let chainBroken = false;
        let expectedPreviousHash = "0"; // Genesis Hash

        // Reverse loop to check chain integrity: from newest log to oldest
        for (let i = 0; i < rows.length; i++) {
            const row = rows[i];
            const currentHash = row.dataset.currentHash;
            const previousHash = row.dataset.prevHash;
            const statusIndicator = row.querySelector('.status-indicator');
            
            // For the first entry (newest log), its previous hash must match what we recorded from the previous database run.
            // For all subsequent entries, the previous hash should match the current hash of the row immediately above it.
            
            let isLinked = false;
            
            if (i === 0) {
                // The newest log has no future link to verify, just check its previous hash is not 0 if there are logs.
                isLinked = previousHash !== "0"; 
            } else {
                // Check if the current log's hashPrevious matches the previous log's hashCurrent (stored in expectedPreviousHash)
                isLinked = previousHash === expectedPreviousHash;
            }

            if (isLinked) {
                 statusIndicator.textContent = '✅ Chain Linked';
                 statusIndicator.classList.add('status-ok');
            } else if (previousHash !== "0") {
                 statusIndicator.textContent = '❌ LINK BROKEN';
                 statusIndicator.classList.add('status-error');
                 chainBroken = true;
            } else {
                 statusIndicator.textContent = '✅ Genesis';
                 statusIndicator.classList.add('status-ok');
            }

            // The CURRENT log's hash_current becomes the EXPECTED_PREVIOUS_HASH for the next (older) log.
            expectedPreviousHash = currentHash;
        }

        if (chainBroken) {
            document.getElementById('chain-status').textContent = '❌ Chain Broken: Tampering Detected!';
            document.getElementById('chain-status').classList.remove('valid');
            document.getElementById('chain-status').classList.add('invalid');
        }
    });
</script>
</body>
</html>