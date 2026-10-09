# AegisComm — Secure Military Communication System

A Java web application for **role-based encrypted messaging**, built with JSP/Servlets, JDBC/MySQL, and deployed on Apache Tomcat 9.

## Features

- **BCrypt Authentication** — Password hashing with BCrypt for all user accounts
- **AES-128 Message Encryption** — Each message is encrypted with a unique random AES key; the AES key itself is wrapped with a master key before storage
- **Role-Based Access Control** — Five roles: Admin, Intelligence, TopOrder, Secondary, Soldier — each with a dedicated dashboard
- **Hash-Linked Audit Trail** — Tamper-evident audit logs where each entry's SHA-256 hash chains to the previous entry
- **Admin CRUD** — User management, weapon inventory, and border zone surveillance
- **Password Recovery** — Forgot-password flow with time-limited email tokens via Gmail SMTP
- **Message Forwarding & Split Dispatch** — Forward messages to multiple recipients with re-encryption

## Tech Stack

| Layer        | Technology                         |
| ------------ | ---------------------------------- |
| Frontend     | JSP, HTML5, CSS3                   |
| Backend      | Java Servlets (Java 17)            |
| Database     | MySQL 8.0 via JDBC                 |
| Crypto       | `javax.crypto` (AES-128), BCrypt   |
| Server       | Apache Tomcat 9.0                  |
| Deployment   | Docker, Docker Compose, AWS EC2    |

## Architecture

```
Browser → JSP / HttpServlet → JDBC / PreparedStatement → MySQL

Message Encryption:
  plaintext → random AES key → ciphertext
                    ↓
              key wrapped with MASTER_KEY → stored in DB

Inbox Decryption:
  stored encrypted_aes_key → unwrap with MASTER_KEY → decrypt message → JSP
```

## Quick Start (Docker)

```bash
# 1. Clone the repository
git clone https://github.com/Ashank001/AegisComm.git
cd AegisComm

# 2. Create .env from template
cp .env.example .env
# Edit .env with your production values

# 3. Build and run
docker compose up --build

# 4. Access the application
# Open http://localhost:8080 in your browser
```

## Environment Variables

| Variable       | Purpose                          | Required |
| -------------- | -------------------------------- | -------- |
| `DB_URL`       | JDBC connection URL              | Yes      |
| `DB_USER`      | MySQL username                   | Yes      |
| `DB_PASS`      | MySQL password                   | Yes      |
| `MASTER_KEY`   | AES key-wrapping key (16 chars)  | Yes      |
| `MAIL_FROM`    | SMTP sender email                | Optional |
| `MAIL_PASSWORD`| Gmail SMTP app password          | Optional |

## Project Structure

```
src/main/
├── java/com/gateway/
│   ├── model/          # Data models (User, Message, AuditLog, Weapon, Border)
│   ├── servlet/        # HTTP request handlers (28 servlets)
│   └── util/           # Utilities (AESUtil, MasterKeyUtil, DBConnection, etc.)
└── webapp/
    ├── CSS/            # Stylesheets
    ├── images/         # Static assets & profile uploads
    ├── WEB-INF/        # web.xml deployment descriptor
    └── *.jsp           # View templates
```

## License

This project is for educational and portfolio purposes.
