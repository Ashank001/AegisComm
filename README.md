# AegisComm — Secure Hierarchical Communication System

A focused Java web application providing **role-based encrypted communication within a hierarchical organization**. Developed originally for a military-organization use case, AegisComm enables structured, compartmentalized information flow across command tiers while maintaining cryptographic confidentiality at rest and a tamper-evident audit ledger.

> **Scope Note**: Non-communication administrative modules present in earlier iterations (specifically **weapons inventory management** and **border-region / surveillance tracking**) have been formally removed to focus the application strictly on authenticated, role-based secure messaging, hierarchical forwarding, and auditing.

---

## 1. Project Overview & Organizational Use Case

In structured military and organizational hierarchies, operational security requires that information flows through defined chains of command rather than broadcast to all participants:
- Intelligence feeds and strategic orders originate at specialized command tiers.
- Intermediate commanders review, sanitize, and repackage directives before passing them downward.
- Subordinate units receive only the specific orders relevant to their mission, preventing wider leakage of strategic plans.

AegisComm provides the web interface, role-based routing, symmetric message encryption at rest, and audit logging to model this compartmentalized operational workflow.

---

## 2. Core Features

- **Closed-Enrollment User Management**: Only Administrators can provision user accounts and assign roles. Public self-registration is disabled.
- **BCrypt Password Hashing**: Passwords stored using salted BCrypt hashes (work factor 10) preventing plaintext credential exposure.
- **Session-Based Authentication**: Secure HTTP session management with server-side role validation on all protected Servlets and JSP views.
- **AES-128 Symmetric Envelope Encryption**: Each sent message body is encrypted with an individually generated 128-bit AES key. This message key is wrapped using a server-level `MASTER_KEY` before database persistence.
- **Hierarchical Forwarding & Split Dispatch**: Intermediate commanders can review incoming messages, edit directives, and split orders for multi-recipient dispatch.
- **Tamper-Evident SHA-256 Hash-Linked Audit Trail**: Important actions (logins, user lifecycle, message dispatches) are logged into a cryptographic hash chain where each entry includes the SHA-256 digest of the previous entry.
- **JavaMail Integration**: SMTP delivery (via Gmail/TLS) for onboarding credentials and time-limited password recovery tokens.

---

## 3. The Five Hierarchical Roles & Responsibilities

The system defines five roles, each with a dedicated dashboard and operational scope:

| Role | Dashboard | Operational Responsibilities |
| :--- | :--- | :--- |
| **Admin** | `dashboard.jsp` | System administration only: provisions personnel accounts, assigns roles, edits/deactivates accounts, views system audit logs, and sends operational notices. Does not intercept operational military intelligence. |
| **Intelligence** | `dashboard_intel.jsp` | Information origination: collects and drafts initial intelligence dispatches and sends them upward to Top Order leadership. |
| **TopOrder** | `dashboard_top.jsp` | Strategic command: receives intelligence from Intelligence officers, reviews and edits operational directives, and forwards actionable orders to Secondary commanders. |
| **Secondary** | `dashboard_second.jsp` | Tactical command: receives strategic orders from Top Order, compartmentalizes information by splitting orders into distinct task segments, and dispatches them to assigned Soldiers. |
| **Soldier** | `dashboard_soldier.jsp` | Operational personnel: receives compartmentalized instructions, views mission tasks in their personal inbox, and reports status back up to Secondary commanders or Admin support. |

---

## 4. Intended Communication Workflow vs. Server-Side Enforcement

### Intended Operational Workflow
1. **Admin** creates employee accounts and assigns appropriate hierarchical roles.
2. **Intelligence** drafts an intelligence dispatch and transmits it to **TopOrder**.
3. **TopOrder** reviews, edits, and re-encrypts the order, forwarding it to **Secondary**.
4. **Secondary** splits the comprehensive message into individual task orders and dispatches them to selected **Soldiers**.
5. **Soldiers** access and read only the compartmentalized fragments assigned to them.

### Enforced Server-Side Behavior
- **Message Access**: Decryption and viewing are strictly enforced per recipient (`receiver_id = session.userId`). Users cannot read or delete messages belonging to other accounts.
- **Role Isolation**: Forwarding via `ForwardMessageServlet` / `ProcessForwardServlet` strictly requires the `TopOrder` role on the server. Split dispatch via `SplitForwardServlet` / `ProcessSplitForwardServlet` strictly requires the `Secondary` role.
- **Administrative Functions**: `AddUserServlet`, `EditUserServlet`, `UpdateUserServlet`, `DeleteUserServlet`, and `AuditLogServlet` enforce server-side `Admin` role checks; non-admins and unauthenticated requests are rejected.
- **Direct Messaging**: `compose.jsp` UI filters recipient options by hierarchy (e.g. Intelligence to TopOrder, TopOrder to Secondary, Secondary to Soldier, Soldiers reporting to Secondary). The backend validates authenticated session state and records sender and recipient IDs for auditing.

---

## 5. Application Architecture

AegisComm follows the classic Java EE **Model-View-Controller (MVC)** architectural pattern:

```
[ Browser / Client Tier ]
         │
         ▼  (HTTP POST / GET)
[ Controller Tier: Java Servlets ] ───▶ [ Security & Cryptography ]
  - LoginServlet / LogoutServlet          - BCrypt (Password hashing)
  - AddUserServlet / UpdateUserServlet     - AESUtil (128-bit AES per message)
  - SendMessageServlet / InboxServlet     - MasterKeyUtil (AES key wrapping)
  - ForwardMessageServlet                 - AuditLogger (SHA-256 hash chain)
  - ProcessSplitForwardServlet            - MailSender (Jakarta Mail SMTP)
         │                                       │
         ▼                                       ▼
[ View Tier: JSP Pages ]                 [ Persistence: MySQL via JDBC ]
  - Role-specific Dashboards               - `users` (BCrypt credentials & roles)
  - inbox.jsp / compose.jsp                - `messages` (Ciphertext & wrapped keys)
  - split_and_dispatch.jsp                 - `audit_logs` (Hash-linked ledger)
  - audit.jsp                              - `password_reset` (Expiring tokens)
```

---

## 6. Technology Stack

- **Platform**: Java 17 / Java EE Servlet 3.0 / JSP 2.2
- **Web / Application Server**: Apache Tomcat 9.0 (configured to run as non-root)
- **Database**: MySQL 8.0 accessed via JDBC (`mysql-connector-j-9.1.0.jar`)
- **Password Security**: `org.mindrot.jbcrypt` (BCrypt)
- **Cryptography Engine**: Standard Java Cryptography Architecture (`javax.crypto.Cipher`, AES/128, `MessageDigest` SHA-256)
- **Mail Integration**: Jakarta Mail 2.0.1 (`jakarta.mail-2.0.1.jar`, `jakarta.activation-2.0.1.jar`)
- **Containerization**: Docker multi-stage build, Docker Compose
- **Test Framework**: Python 3 `unittest` integration test suite

---

## 7. Authentication & Password Security

1. **Closed Registration**: Account creation is restricted to the Administrator dashboard. The public login page provides no self-signup.
2. **Password Hashing**: User passwords are never stored in plaintext. They are salted and hashed with BCrypt (`BCrypt.hashpw(password, BCrypt.gensalt())`) and verified using `BCrypt.checkpw`.
3. **Session Management**: Upon successful login, the server binds the user's `userId`, `userEmail`, `userName`, `profileImg`, and `role` to an `HttpSession`. Session validity and role checks are enforced before any protected action executes.
4. **Password Recovery**: `ForgotPasswordServlet` generates a 128-bit `UUID` token stored with a 10-minute expiry timestamp in `password_reset`. When submitted to `ResetPasswordServlet`, the token is verified, the password updated with a new BCrypt hash, and the token deleted.

---

## 8. Message Encryption & Key Management

AegisComm implements envelope encryption for message confidentiality at rest:

```
Message Encryption (SendMessage / ProcessForward / ProcessSplit):
  Plaintext ────────► AES-128 (Unique Random Key) ────────► Ciphertext (DB: encrypted_text)
                                │
                          MASTER_KEY (AES-128)
                                │
                                ▼
                       Wrapped Key (DB: encrypted_aes_key)

Message Decryption (InboxServlet / ForwardMessageServlet / SplitForwardServlet):
  Wrapped Key ──────► Unwrapped with MASTER_KEY ──────────► Unique AES Key
                                                                 │
  Ciphertext ────────────────────────────────────────────────────▼
  Decrypted Plaintext (Presented in JSP session)
```

- Each message is encrypted with an isolated, randomly generated 128-bit AES key (`AESUtil.generateKey()`).
- The message key is encrypted (wrapped) with the server-side `MASTER_KEY` (`MasterKeyUtil.encryptKey()`) before storage.
- The database stores only ciphertext and the wrapped key. Compromise of the database alone without the server's `MASTER_KEY` environment variable does not reveal message contents.

---

## 9. SHA-256 Hash-Linked Audit Logging

Audit logs are recorded in a blockchain-style, tamper-evident hash chain:
1. When an event occurs (e.g. login, user creation, message dispatch), `AuditLogger` fetches the latest log row's `hash_current` (or `"0"` if the table is empty).
2. The event string is constructed: `userEmail + ":" + action + ":" + timestamp`.
3. The new row's current hash is computed:
   $$\text{hash\_current} = \text{SHA-256}(\text{hash\_previous} + \text{event\_string})$$
4. The record is inserted with both `hash_previous` and `hash_current`.
5. The Admin's `audit.jsp` validates continuity across rows: if an attacker modifies, deletes, or inserts a row directly into the database, subsequent `hash_previous` pointers no longer match, flagging the chain as broken.

---

## 10. JavaMail Integration

Configured in `com.gateway.util.MailSender`:
- Connects to Google SMTP (`smtp.gmail.com:587`) using TLS (`mail.smtp.starttls.enable`).
- Dispatches automated welcome emails with generated credentials when administrators check the "Generate random password and email to user" box.
- Dispatches password recovery links containing temporary tokens.
- Gracefully skips email dispatch if SMTP environment variables are unconfigured.

---

## 11. Database Setup

The database schema is defined in `db/schema.sql`.

### Canonical Tables
1. **`users`**: Account credentials (`id`, `name`, `email`, `password`, `role`, `profileImg`).
2. **`messages`**: Encrypted messages (`id`, `sender_id`, `receiver_id`, `encrypted_text`, `encrypted_aes_key`, `timestamp`).
3. **`audit_logs`**: Hash-linked audit trail (`id`, `user_email`, `action`, `hash_previous`, `hash_current`, `timestamp`).
4. **`password_reset`**: Temporary recovery tokens (`id`, `email`, `token`, `expiry`).

### Migration Notes for Existing Databases
If updating from an earlier version of AegisComm that included the weapons and borders modules:
```sql
USE `aegiscomm`;

-- Remove obsolete module tables (does not affect users, messages, or audit logs)
DROP TABLE IF EXISTS `weapons`;
DROP TABLE IF EXISTS `borders`;
```

---

## 12. Environment Variables Configuration

The application is configured through environment variables:

| Variable | Purpose | Default / Requirement |
| :--- | :--- | :--- |
| `DB_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/aegiscomm` (Required) |
| `DB_USER` | MySQL database username | `root` (Required) |
| `DB_PASS` | MySQL database password | Empty default (Required in production) |
| `MASTER_KEY` | 16-character string for AES-128 master key wrapping | Required (Must be exactly 16 characters) |
| `MAIL_FROM` | SMTP sender email address | Optional (e.g. `admin@organization.mil`) |
| `MAIL_PASSWORD`| SMTP application-specific password | Optional (Required if email dispatch is used) |

---

## 13. Local Setup & Deployment Instructions

### Prerequisites
- JDK 17 or higher (`javac` and `java` on PATH)
- MySQL 8.0
- Apache Tomcat 9.0 (or Docker & Docker Compose)

### Option A: Running with Docker Compose (Recommended)
```bash
# 1. Clone the repository
git clone https://github.com/Ashank001/AegisComm.git
cd AegisComm

# 2. Copy and customize environment variables
cp .env.example .env
# Set MASTER_KEY (16 characters), DB credentials, and optional SMTP settings

# 3. Build and launch containers
docker compose up --build -d

# 4. Access the web interface
# Open http://localhost:8080 in your browser
```

### Option B: Local Compilation with javac
```powershell
# 1. Initialize schema in MySQL
mysql -u root -p < db/schema.sql

# 2. Set environment variables
$env:MASTER_KEY="AegisCommMasterK"
$env:DB_URL="jdbc:mysql://localhost:3306/aegiscomm"
$env:DB_USER="root"
$env:DB_PASS="your_password"

# 3. Compile Java sources
New-Item -ItemType Directory -Force build\classes
Get-ChildItem -Recurse -Filter *.java src\main\java | Select-Object -ExpandProperty FullName | Set-Content sources.txt
javac -d build\classes -cp "lib/*" "@sources.txt"
Remove-Item -Force sources.txt

# 4. Copy to Tomcat webapps or assemble WAR
```

---

## 14. Current Security Limitations & Known Issues

1. **Transport Layer Security (TLS)**: The application server itself does not enforce HTTPS. In production, AegisComm must be deployed behind a reverse proxy (e.g., NGINX, AWS ALB, Caddy) terminating TLS to protect session cookies and credentials in transit.
2. **AES Transformation Mode**: `AESUtil` initializes cipher instances using `"AES"`, which defaults to ECB mode on default Java providers. While individual random keys mitigate ECB pattern leakage across messages, authenticated modes (such as AES-GCM) with explicit IVs would provide stronger cryptographic guarantees. Existing message compatibility was preserved.
3. **Master Key Lifecycle**: The `MASTER_KEY` is loaded from a single environment variable into static memory. Key rotation, hardware security modules (HSM), or KMS integrations (such as AWS KMS or HashiCorp Vault) are not implemented.
4. **Audit Chain Re-computation**: While the hash-linked audit chain detects modification or deletion of past records, an adversary with direct database write access could recalculate hashes forward from the point of modification. True immutability requires write-once-read-many (WORM) storage or remote append-only log streaming.
5. **Session Management**: Session invalidation is handled in application memory; distributed sessions across multiple horizontal nodes require sticky sessions or an external session store.
