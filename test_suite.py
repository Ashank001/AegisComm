import unittest
import requests
import random
import string
import os

# Configuration: Point this to your local Docker or EC2 IP
BASE_URL = os.getenv("AEGIS_TEST_URL", "http://localhost:8080")

class AegisCommIntegrationTest(unittest.TestCase):
    """
    Enterprise End-to-End Blackbox Test Suite for AegisComm.
    Validates authentication, sessions, role-based dashboards, and the core AES-encrypted messaging flow.
    """

    def setUp(self):
        # Create a persistent session for cookies (JSESSIONID)
        self.session = requests.Session()
        
        # Test accounts seeded in the schema
        self.admin_email = "admin@aegiscomm.mil"
        self.admin_password = "admin123"
        
        # Random data for testing
        self.test_msg = f"TEST_ENCRYPTED_MESSAGE_{random.randint(1000, 9999)}"

    def test_01_invalid_login(self):
        """Test that invalid credentials fail safely"""
        response = self.session.post(f"{BASE_URL}/LoginServlet", data={
            "email": "hacker@evil.com",
            "password": "wrongpassword"
        }, allow_redirects=False)
        
        # Should respond with a 302 redirect back to login with an error
        self.assertEqual(response.status_code, 302)
        self.assertIn("login.jsp?error=", response.headers.get('Location', ''))

    def test_02_valid_admin_login(self):
        """Test successful login and role-based routing for Admin"""
        response = self.session.post(f"{BASE_URL}/LoginServlet", data={
            "email": self.admin_email,
            "password": self.admin_password
        }, allow_redirects=False)
        
        # Admin should be redirected to the admin dashboard
        self.assertEqual(response.status_code, 302)
        self.assertIn("dashboard.jsp", response.headers.get('Location', ''))

    def test_03_create_new_soldier_and_login(self):
        """Test admin capability to add a user, and that user can login"""
        # 1. Login as Admin
        self.session.post(f"{BASE_URL}/LoginServlet", data={
            "email": self.admin_email,
            "password": self.admin_password
        })

        # 2. Create a Soldier
        new_email = f"soldier_{random.randint(1000,9999)}@aegiscomm.mil"
        response = self.session.post(f"{BASE_URL}/AddUserServlet", data={
            "name": "Test Soldier",
            "email": new_email,
            "role": "Soldier"
            # "sendMail" is unchecked so it defaults to "123" password
        }, allow_redirects=False)
        
        self.assertEqual(response.status_code, 302)
        self.assertIn("success", response.headers.get('Location', ''))

        # 3. Logout (Clear session)
        self.session.cookies.clear()

        # 4. Login as new Soldier
        soldier_login = self.session.post(f"{BASE_URL}/LoginServlet", data={
            "email": new_email,
            "password": "123"
        }, allow_redirects=False)
        
        self.assertEqual(soldier_login.status_code, 302)
        # Soldiers are routed to dashboard_soldier.jsp
        self.assertIn("dashboard_soldier.jsp", soldier_login.headers.get('Location', ''))

    def test_04_message_encryption_flow(self):
        """Test sending a message (AES Encrypted) and verifying it appears in the inbox"""
        # 1. Login as Admin
        self.session.post(f"{BASE_URL}/LoginServlet", data={
            "email": self.admin_email,
            "password": self.admin_password
        })

        # 2. Admin sends a message to themselves (Admin is ID 1 in the schema)
        response = self.session.post(f"{BASE_URL}/SendMessageServlet", data={
            "recipientIds": ["1"],
            "message": self.test_msg
        }, allow_redirects=False)

        self.assertEqual(response.status_code, 302)
        self.assertIn("success", response.headers.get('Location', ''))

        # 3. Check Inbox and verify the decrypted message is present
        # The InboxServlet handles retrieving, unwrapping the AES key, and decrypting the content
        inbox_response = self.session.get(f"{BASE_URL}/InboxServlet", allow_redirects=True)
        
        self.assertEqual(inbox_response.status_code, 200)
        
        # If the decryption workflow is functioning properly, the original test message
        # string will be present in the HTML of the inbox.
        self.assertIn(self.test_msg, inbox_response.text, 
            "The sent message was not found or failed to decrypt in the inbox.")


if __name__ == "__main__":
    print(f"Starting Enterprise Test Suite against {BASE_URL}...")
    print("Ensure the database and Tomcat server are running.")
    unittest.main(verbosity=2)
