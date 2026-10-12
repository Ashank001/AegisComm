# AegisComm — Technical & Cryptography Interview Guide

## 1. Important Concept Check: Symmetric vs. Asymmetric Cryptography

> [!IMPORTANT]
> **Key Interview Distinction:**
> - **Asymmetric Encryption (RSA, ECC, PGP):** Uses a **Public Key / Private Key** pair. You share your public key with everyone to encrypt; you keep your private key secret to decrypt.
> - **Symmetric Encryption (AES):** Uses the **exact same secret key** for both encryption and decryption. **There are NO public/private keys in AES.**

In AegisComm, we use **Symmetric Encryption (AES-128)** combined with an enterprise security pattern called **Envelope Encryption (Key Wrapping)**. 

---

## 2. How AES & Encryption Work in AegisComm

Instead of having per-user public/private keypairs, AegisComm implements **Envelope Encryption (Data Encryption Key + Key Encryption Key)**:

```
[SENDER COMPOSES MESSAGE]
        │
        ▼
1. Generate fresh random 128-bit AES Key (DEK)
   └─ AESUtil.generateKey()
        │
        ├─────────────────────────────────────────────────┐
        ▼                                                 ▼
2. Encrypt message plaintext with DEK            3. Wrap/Encrypt DEK with MASTER_KEY (KEK)
   └─ AESUtil.encrypt(message, uniqueKey)           └─ MasterKeyUtil.encryptKey(uniqueKey)
        │                                                 │
        ▼                                                 ▼
   encrypted_text (Ciphertext)                      encrypted_aes_key
        │                                                 │
        └────────────────────────┬────────────────────────┘
                                 ▼
                     4. Store both in MySQL DB
                        (messages table)

──────────────────────────────────────────────────────────────────────────

[RECEIVER OPENS INBOX]
        │
        ▼
1. Fetch record from DB matching receiver_id
        │
        ▼
2. Unwrap/Decrypt encrypted_aes_key using server MASTER_KEY
   └─ MasterKeyUtil.decryptKey(encrypted_aes_key)  --> returns DEK
        │
        ▼
3. Decrypt ciphertext using DEK
   └─ AESUtil.decrypt(encrypted_text, uniqueKey)   --> returns Plaintext
        │
        ▼
4. Display decrypted plaintext in inbox.jsp
```

---

## 3. Detailed Component Breakdown

### A. Data Encryption Key (DEK) — `AESUtil.java`
- **Location:** `src/main/java/com/gateway/util/AESUtil.java`
- For **every single message sent**, a new 128-bit AES key (`SecretKey`) is dynamically generated:
  ```java
  KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
  keyGenerator.init(128);
  SecretKey uniqueKey = keyGenerator.generateKey();
  ```
- The plaintext string is converted to UTF-8 bytes and encrypted into ciphertext using `Cipher.ENCRYPT_MODE`:
  ```java
  Cipher cipher = Cipher.getInstance("AES");
  cipher.init(Cipher.ENCRYPT_MODE, secretKey);
  byte[] cipherBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
  String encryptedText = Base64.getEncoder().encodeToString(cipherBytes);
  ```

### B. Key Encryption Key (KEK / Master Key) — `MasterKeyUtil.java`
- **Location:** `src/main/java/com/gateway/util/MasterKeyUtil.java`
- We cannot store the raw `uniqueKey` in plaintext in the database (otherwise anyone with database access could read all messages).
- Instead, the server loads a 16-character master key from an environment variable (`MASTER_KEY`):
  ```java
  SecretKeySpec masterKey = new SecretKeySpec(
      System.getenv("MASTER_KEY").getBytes(StandardCharsets.UTF_8), 
      "AES"
  );
  ```
- The unique AES key's raw bytes (`uniqueKey.getEncoded()`) are encrypted using the `MASTER_KEY`:
  ```java
  cipher.init(Cipher.ENCRYPT_MODE, masterKey);
  String encryptedAesKey = Base64.getEncoder().encodeToString(
      cipher.doFinal(keyToEncrypt.getEncoded())
  );
  ```

### C. Message Dispatch — `SendMessageServlet.java`
- **Location:** `src/main/java/com/gateway/servlet/SendMessageServlet.java`
- When a user sends a message to one or more recipients:
  1. For each recipient, it generates a fresh `uniqueKey`.
  2. Encrypts message -> `encryptedMessage`.
  3. Encrypts the key -> `encryptedAesKey`.
  4. Inserts `(sender_id, receiver_id, encrypted_text, encrypted_aes_key)` into MySQL.
  5. Records an immutable hash-linked audit log entry via `AuditLogger`.

### D. Message Retrieval & Decryption — `InboxServlet.java`
- **Location:** `src/main/java/com/gateway/servlet/InboxServlet.java`
- When a recipient logs in and views their inbox:
  1. The server checks session authentication and queries messages where `receiver_id = session.userId`.
  2. For each message:
     - Unwraps the message key: `SecretKey uniqueKey = MasterKeyUtil.decryptKey(encryptedAesKey);`
     - Decrypts the message: `String decryptedText = AESUtil.decrypt(encryptedText, uniqueKey);`
  3. Sets `decryptedText` on the `Message` model and forwards it to `inbox.jsp`.

---

## 4. Top Interview Questions & Answers

### Q1: "Is AES symmetric or asymmetric? How does your system work without public/private keys?"
**Answer:**
> *"AES is strictly a **symmetric key cipher**, meaning the exact same key is used for both encryption and decryption. In our project, rather than using asymmetric key pairs (like RSA or Diffie-Hellman), we use **Envelope Encryption**—the industry standard used by AWS KMS and Google Cloud KMS.*
>
> *Each message is encrypted with a unique, randomly generated AES-128 Data Encryption Key (DEK). That DEK is then encrypted with a server-level Master Key (Key Encryption Key or KEK) stored securely in environment variables. To decrypt, the server unwraps the DEK using the Master Key, and then decrypts the message payload."*

### Q2: "Why generate a unique AES key for every message instead of using one key for the entire system?"
**Answer:**
> 1. **Blast Radius Reduction:** If a single message's AES key were ever compromised, only that one individual message is exposed. All other messages remain secure.
> 2. **Cryptographic Wear-Out Prevention:** Encrypting massive amounts of data under a single AES key can lead to pattern exposure and cryptographic vulnerabilities.
> 3. **Forward Secrecy at the Message Level:** Every message is cryptographically decoupled from every other message.

### Q3: "What happens if an attacker gets full read access to the MySQL database?"
**Answer:**
> *"Even with full SQL database dumps, the attacker cannot read the messages. In the database, they will only see:*
> - `encrypted_text` *(AES-128 ciphertext)*
> - `encrypted_aes_key` *(AES key wrapped with Master Key)*
>
> *Because the `MASTER_KEY` is not stored in the database and is injected via environment variables at runtime, the attacker cannot unwrap the message keys or decrypt the payloads without compromising the host server's environment."*

### Q4: "How does AegisComm differ from End-to-End Encryption (E2EE) like WhatsApp or Signal?"
**Answer:**
> - **In E2EE (WhatsApp/Signal):** The encryption and decryption happen **on the client devices** using asymmetric key pairs (e.g., Signal Protocol / Curve25519). The server never sees plaintext.
> - **In AegisComm (Server-Side Envelope Encryption):** The server manages the keys and decrypts messages on behalf of authenticated, role-verified military personnel upon request. This allows centralized role-based access control (RBAC), supervisor message review, forwarding workflows, and tamper-evident audit logging.

---

## 5. How Email Works (Jakarta Mail / JavaMail API) & SMTP Flow

In AegisComm, automated notifications (e.g., onboarding emails when an Admin provisions a new soldier/commander, password reset tokens) are sent using the **Jakarta Mail API (formerly JavaX Mail)** via Google's SMTP Relay.

### A. Technical Architecture & Component Flow (`MailSender.java`)

```
[ Admin / User Action ]
         │
         ▼
[ AddUserServlet / ForgotPasswordServlet ]
         │
         ▼ calls MailSender.send(recipientEmail, subject, body)
         │
 1. Configure SMTP Properties
    ├── mail.smtp.host = "smtp.gmail.com"
    ├── mail.smtp.port = "587"
    ├── mail.smtp.auth = "true"
    └── mail.smtp.starttls.enable = "true" (TLS Encryption)
         │
         ▼
 2. Initialize Mail Session with Authenticator
    └── Authenticator passes:
        - MAIL_FROM (e.g., ashank1301@gmail.com)
        - MAIL_PASSWORD (16-character Google App Password)
         │
         ▼
 3. Construct MimeMessage
    ├── From: InternetAddress(MAIL_FROM)
    ├── To: InternetAddress.parse(toRecipient)
    ├── Subject: "AegisComm Account Created"
    └── Body: Plaintext credentials / token
         │
         ▼
 4. Transport.send(message)
    ├── Opens TCP Socket to smtp.gmail.com:587
    ├── Issues SMTP EHLO handshake
    ├── Upgrades socket to TLS via STARTTLS (Encryption in Transit)
    ├── Transmits AUTH LOGIN (Base64 encoded credentials)
    ├── Streams MIME packet & disconnects (QUIT)
         │
         ▼
[ Google SMTP Mail Server ] ── relays across internet ──▶ [ Recipient Mailbox (Outlook/Gmail) ]
```

### B. Core Classes in Jakarta Mail (What Each Does)

1. **`java.util.Properties`**:
   - Configures connection rules for the mail client:
     - `mail.smtp.host = smtp.gmail.com`: Google’s outgoing mail server.
     - `mail.smtp.port = 587`: Standard port for STARTTLS submission.
     - `mail.smtp.auth = true`: Tells the library that SMTP authentication is mandatory.
     - `mail.smtp.starttls.enable = true`: Enforces opportunistic TLS so credentials and email bodies are encrypted across the wire.
2. **`jakarta.mail.Session`**:
   - Represents an active mail session holding configuration properties and authentication context.
3. **`jakarta.mail.Authenticator` & `PasswordAuthentication`**:
   - A callback object that supplies username (`MAIL_FROM`) and password (`MAIL_PASSWORD`) when the SMTP server challenges the client during `AUTH LOGIN`.
4. **`jakarta.mail.internet.MimeMessage`**:
   - Formats the RFC 822 / MIME electronic message (From, To, Subject, Headers, Payload body).
5. **`jakarta.mail.Transport`**:
   - The wire-level engine that manages the socket connection, sends SMTP protocol commands (`EHLO`, `STARTTLS`, `AUTH LOGIN`, `MAIL FROM`, `RCPT TO`, `DATA`), and transmits the payload.

### C. Layman's Analogy (The Diplomatic Courier)
* Imagine your AegisComm server needs to send a welcome letter to a soldier stationed abroad.
* **`MimeMessage`** is the envelope, containing the destination address, stamp, and letter inside.
* **`Transport`** is the diplomatic courier who drives to the Google Central Post Office (`smtp.gmail.com`).
* At the post office gate, the guard asks: *"Who is sending this and show your badge"* (`Authenticator` + `STARTTLS`).
* The courier presents the diplomatic ID (`MAIL_FROM` email + 16-character Google App Password).
* Once verified, Google’s post office accepts the envelope and flies it directly to the soldier’s inbox.

### D. Top Interview Questions on Mail Architecture

#### Q1: "Why did you use an App Password instead of a normal Google password?"
> *"Google has deprecated Less Secure Apps (LSA) and standard password authentication for SMTP. Because modern Google accounts enforce Two-Factor Authentication (2FA), an automated service cannot complete an interactive 2FA challenge. A 16-character Google App Password is a cryptographically generated, revocable service token specifically authorized to bypass the interactive 2FA prompt for SMTP protocols."*

#### Q2: "What is STARTTLS and why use port 587 instead of port 465 or 25?"
> - *Port 25 is the legacy relay port, universally blocked by cloud providers (like AWS EC2) to prevent spam.*
> - *Port 465 is implicit SSL (the connection must be SSL from the start).*
> - *Port 587 with **STARTTLS** is the modern IETF standard (RFC 3207). It connects in plain text and immediately negotiates an upgrade to a secure TLS encrypted channel before sensitive credentials or message contents are transmitted.*

#### Q3: "Why did you get `535-5.7.8 BadCredentials` and how did you resolve it?"
> *"The 535 error indicates an SMTP authentication rejection. This happened because the Google App Password was generated on Account A, but `MAIL_FROM` was set to Account B. In Google SMTP, the authenticating account and the originating address must belong to the exact same account. Aligning `MAIL_FROM` and `MAIL_PASSWORD` to the same Google identity in environment variables resolved the issue."*

---

## 6. End-to-End AWS EC2 & Cloud Deployment Architecture

### A. Technical Infrastructure Flow

```
[ User Browser ]
       │ HTTP Request on Port 80
       ▼
[ AWS Internet Gateway ]
       │
       ▼
[ EC2 Security Group Firewall ] (Allows Inbound Port 80 & Port 22 SSH)
       │
       ▼
[ AWS EC2 Instance (Ubuntu 22.04 LTS / t3.micro) ]
   │
   └── Docker Engine
         │
         ├── aegiscomm_default (Internal Bridge Network - 172.18.0.0/16)
         │
         ├── aegiscomm-app Container (Tomcat 9 / Java 11)
         │     ├── Port Binding: Host 80 ──▶ Container 8080
         │     ├── Java Servlets & JSPs (MVC Controller & View)
         │     ├── JDBC connection string: jdbc:mysql://db:3306/aegiscomm_db
         │     └── Communicates externally via Port 587 (smtp.gmail.com)
         │
         └── aegiscomm-db Container (MySQL 8.0)
               ├── Port 3306 (Internal ONLY - Isolated from Public Internet)
               ├── Volume Binding: mysql_data ──▶ /var/lib/mysql (Durable Storage)
               └── Initialized via /docker-entrypoint-initdb.d/schema.sql
```

### B. Layman's Analogy (The Rented Building & Modular Kitchen)
* **AWS EC2:** An office suite you rent from Amazon. You get a public street address (`Public IP / DuckDNS`).
* **AWS Security Group:** The building security desk. They only let visitors into Door 80 (public dining room) and Door 22 (the owner's private keyhole).
* **Docker Containers:** Two self-contained modular kitchen pods inside the office:
  * **Pod 1 (`aegiscomm-app`):** The Chef taking customer orders and preparing dishes.
  * **Pod 2 (`aegiscomm-db`):** The locked pantry keeping all secret inventory and records.
* **Docker Network:** An internal intercom between the kitchen and the pantry (`db:3306`). Outsiders cannot touch the pantry.
* **Docker Volume:** An underground reinforced safe. If the pantry pod is swapped out or upgraded, the inventory remains intact in the safe.

