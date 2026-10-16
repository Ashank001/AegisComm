# AegisComm — Project Architecture & File-by-File Technical Guide

This guide explains the foundational Java web architecture (MVC Pattern) used in **AegisComm**, clarifies the differences between **Java Classes, Servlets, Models, and Utilities**, and provides a **complete file-by-file index** of the entire codebase.

---

## 1. Core Architectural Concepts (Interview Essentials)

### A. What is the difference between a Standard Java Class and a Servlet?
* **Standard Java Class (`.java`)**:
  * An ordinary object blueprint running in standard JVM memory.
  * It has **no awareness of the web**, HTTP requests, query parameters, cookies, or browser sessions.
  * *Examples:* `User.java`, `AESUtil.java`, `DBConnection.java`.
* **Servlet Class (`HttpServlet`)**:
  * A specialized Java class that extends `javax.servlet.http.HttpServlet`.
  * Managed by a **Servlet Container / Web Server (Apache Tomcat)**.
  * It has lifecycle hooks (`init()`, `service()`, `destroy()`) and handles HTTP methods:
    * `doGet(HttpServletRequest req, HttpServletResponse resp)`: Handles browser page requests, URL query params, and data fetches.
    * `doPost(HttpServletRequest req, HttpServletResponse resp)`: Handles HTML form submissions, credentials, and mutations.
  * *Analogy:* A standard Java class is like a private notebook or tool. A Servlet is the customer service counter that receives phone calls (HTTP requests) from the outside world.

---

### B. What is a "Model" Class? (The "M" in MVC)
* **Location:** `src/main/java/com/gateway/model/`
* **Definition:** A **POJO (Plain Old Java Object)** or Data Transfer Object (DTO).
* **Purpose:**
  * Represents an entity in your database or business domain (e.g., `User`, `Message`, `Weapon`, `AuditLog`).
  * Contains private attributes, a constructor, and getter/setter methods.
  * **Contains NO business logic, NO SQL queries, and NO HTTP logic.** It only holds structured state in memory so it can be passed cleanly between Servlets and JSP views.

---

### C. What is a "Util" (Utility) Class?
* **Location:** `src/main/java/com/gateway/util/`
* **Definition:** Reusable, stateless helper classes containing static methods.
* **Purpose:**
  * Solves cross-cutting concerns that many different servlets need (e.g., database connectivity, cryptography, mailing, logging, hash calculations).
  * Prevents code duplication. Instead of writing database connection logic in 25 different servlets, all servlets call `DBConnection.getConnection()`.

---

### D. The MVC (Model-View-Controller) Flow in AegisComm
```
   [ Browser / Client ]
            │  1. HTTP POST /login (Form data: email, password)
            ▼
┌─────────────────────────┐
│       CONTROLLER        │  LoginServlet.java
│       (Servlet)         │  - Extracts form parameters
└───────────┬─────────────┘  - Validates input
            │
            │  2. Calls Utility to verify
            ▼
┌─────────────────────────┐
│         UTIL            │  DBConnection.java / BCrypt
│      (Service)          │  - Runs SQL query
└───────────┬─────────────┘  - Verifies hashed password
            │
            │  3. Hydrates database row into memory
            ▼
┌─────────────────────────┐
│         MODEL           │  User.java
│      (Data Object)      │  - new User(id, name, email, role)
└───────────┬─────────────┘
            │
            │  4. Controller binds model to Session & forwards
            ▼
┌─────────────────────────┐
│          VIEW           │  dashboard_top.jsp (or dashboard_soldier.jsp)
│         (JSP)           │  - Renders dynamic HTML using ${sessionScope.userRole}
└─────────────────────────┘
```

---

## 2. File-by-File Breakdown

### Package: `com.gateway.model` (Entities / Data Models)
| File | Role & Purpose |
| :--- | :--- |
| **`User.java`** | Models a system user (`id`, `name`, `email`, `password`, `role`, `profileImg`). Used for authentication, session tracking, and user management. |
| **`Message.java`** | Models a communication record (`id`, `senderId`, `receiverId`, `encryptedText`, `encryptedAesKey`, `decryptedText`, `timestamp`). |
| **`AuditLog.java`** | Models an immutable audit ledger entry (`id`, `userEmail`, `action`, `hashPrevious`, `hashCurrent`, `timestamp`). |

---

### Package: `com.gateway.util` (Helpers & Cross-Cutting Services)
| File | Role & Purpose |
| :--- | :--- |
| **`DBConnection.java`** | Database connection manager. Reads `DB_URL`, `DB_USER`, `DB_PASS` from environment variables and yields a JDBC `java.sql.Connection`. |
| **`AESUtil.java`** | Symmetric encryption utility. Generates fresh 128-bit AES keys and performs AES encryption and decryption on message text. |
| **`MasterKeyUtil.java`** | Envelope encryption / key wrapping utility. Encrypts and decrypts each message's unique AES key using the server-level `MASTER_KEY`. |
| **`SHA256Util.java`** | Hashing engine. Computes cryptographic SHA-256 digests used for tamper-evident blockchain-style hash chains. |
| **`AuditLogger.java`** | Secure ledger engine. Queries the previous audit row's `hash_current`, hashes it together with the new event payload, and writes the chained hash into `audit_logs`. |
| **`MailSender.java`** | SMTP email relay. Connects via Jakarta Mail over TLS (port 587) to Google SMTP to dispatch onboarding credentials and password reset links. |
| **`AppLogger.java`** | Centralized application logger wrapping `java.util.logging.Logger` for consistent log output. |

---

### Package: `com.gateway.servlet` (Controllers / Business Logic)

#### Authentication & Profile Lifecycle:
| File | URL / Method | Description |
| :--- | :--- | :--- |
| **`LoginServlet.java`** | `POST /LoginServlet` | Validates credentials against BCrypt hashes in MySQL, creates `HttpSession`, stores user role/email, and routes to role-specific dashboards. |
| **`LogoutServlet.java`** | `POST /LogoutServlet` | Invalidates active user session, logs audit trail, and redirects to `login.jsp`. |
| **`ChangePasswordServlet.java`** | `POST /ChangePasswordServlet` | Allows an authenticated user to update their existing password. Validates current password hash and commits new BCrypt hash. |
| **`ForgotPasswordServlet.java`** | `POST /ForgotPasswordServlet` | Generates a secure time-limited reset token and dispatches a password recovery email via `MailSender`. |
| **`ResetPasswordServlet.java`** | `POST /ResetPasswordServlet` | Validates the recovery token from the email link and resets the user's password. |

#### Secure Messaging & Envelope Encryption Flow:
| File | URL / Method | Description |
| :--- | :--- | :--- |
| **`SendMessageServlet.java`** | `POST /SendMessageServlet` | Handles message dispatch. Generates a fresh AES key for each recipient, encrypts the text, wraps the key via MasterKeyUtil, inserts rows into DB, and logs audit events. |
| **`InboxServlet.java`** | `GET /InboxServlet` | Fetches incoming messages for the logged-in user, unwraps keys using `MASTER_KEY`, decrypts message payloads, and forwards to `inbox.jsp`. |
| **`DeleteMessageServlet.java`** | `POST /DeleteMessageServlet` | Removes a message from the recipient's inbox with recipient ownership checks. |
| **`ForwardMessageServlet.java`** | `GET /ForwardMessageServlet` | Loads an authorized received message into memory to prepare it for TopOrder forward or modification. |
| **`ProcessForwardServlet.java`** | `POST /ProcessForwardServlet` | Allows TopOrder commanders to forward edited messages to Secondary officers under re-encryption. |
| **`SplitForwardServlet.java`** | `GET /SplitForwardServlet` | Prepares an authorized message for hierarchical split dispatch by Secondary officers. |
| **`ProcessSplitForwardServlet.java`** | `POST /ProcessSplitForwardServlet` | Splits messages into distinct orders and dispatches each fragment to selected Soldiers with unique AES encryption. |

#### Administration & User Management (Admin Only):
| File | URL / Method | Description |
| :--- | :--- | :--- |
| **`AddUserServlet.java`** | `POST /AddUserServlet` | Enforces closed-enrollment RBAC. Admin provisions new personnel, hashes password, stores the record, and optionally dispatches onboarding credentials. |
| **`EditUserServlet.java`** | `GET /EditUserServlet` | Fetches an existing user record for the admin edit form with server-side Admin role validation. |
| **`UpdateUserServlet.java`** | `POST /UpdateUserServlet` | Updates user metadata (name, email, role, profile image) with Admin validation and audit logging. |
| **`DeleteUserServlet.java`** | `POST /DeleteUserServlet` | Deletes a personnel account with Admin validation, self-deletion prevention, and audit logging. |

#### Security & Compliance:
| File | URL / Method | Description |
| :--- | :--- | :--- |
| **`AuditLogServlet.java`** | `GET /AuditLogServlet` | Queries the hash-linked `audit_logs` table, verifies chain continuity, and passes records to `audit.jsp` (Admin only). |
| **`ImageServlet.java`** | `GET /ImageServlet` | Serves profile images with path-traversal safeguards and content-type headers. |

---

### Web Tier: `src/main/webapp/` (JSP Views & Frontend)

#### Role-Based Dashboards:
* **`dashboard.jsp`**: Central administrator dashboard (user provisioning, user management, audit logs).
* **`dashboard_intel.jsp`**: Intelligence Officer dashboard (originating intelligence messages, inbox).
* **`dashboard_top.jsp`**: Top Commander dashboard (reviewing, editing, and forwarding messages to Secondary officers).
* **`dashboard_second.jsp`**: Secondary Officer dashboard (splitting and dispatching orders to Soldiers).
* **`dashboard_soldier.jsp`**: Frontline Soldier dashboard (viewing orders, upward reporting).

#### Views & Interactive Interfaces:
* **`login.jsp`**: Secure login portal with role validation.
* **`compose.jsp`**: Multi-recipient secure message composition form with hierarchical recipient filtering.
* **`inbox.jsp`**: Secure inbox presenting decrypted dispatches.
* **`modify_msg.jsp` / `split_and_dispatch.jsp`**: Tactical message modification and intelligence compartmentalization interfaces.
* **`AddUser.jsp` / `EditUser.jsp` / `view_users.jsp`**: Admin personnel management views.
* **`audit.jsp`**: Tamper-evident hash-linked audit log viewer.
* **`change_password.jsp` / `forgot.jsp` / `reset.jsp`**: Self-service credential management views.
* **`WEB-INF/web.xml`**: Deployment descriptor mapping welcome files.

---

### Root Configuration & DevOps Files
* **`Dockerfile`**: Multi-stage build and deployment container. Uses Maven to compile the WAR and deploys it onto Apache Tomcat 9 running on OpenJDK 11 under a dedicated non-root user.
* **`docker-compose.yml`**: Defines the multi-container stack (`aegiscomm-app` on port 80 and `aegiscomm-db` on private port 3306) on a shared bridge network with persistent named volumes.
* **`db/schema.sql`**: Database DDL schema initializing tables (`users`, `messages`, `weapons`, `borders`, `audit_logs`) and seeding the initial Admin administrator account.
* **`.github/workflows/deploy.yml`**: Continuous Integration and Deployment (CI/CD) pipeline running SonarCloud code quality gates and executing SSH zero-downtime deployment to AWS EC2.
* **`interview_guide.md`**: Master interview preparation document covering cryptography, envelope encryption, Jakarta Mail, and AWS EC2 cloud infrastructure.
