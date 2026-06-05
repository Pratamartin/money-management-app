from django.contrib.auth import get_user_model
from rest_framework import status
from rest_framework.test import APITestCase

User = get_user_model()

REGISTER_URL = "/auth/register/"
LOGIN_URL = "/auth/login/"
REFRESH_URL = "/auth/token/refresh/"


class RegisterViewTest(APITestCase):

    def _valid_data(self, **overrides):
        data = {
            "username": "testuser",
            "email": "test@example.com",
            "nome": "Test User",
            "password": "password123",
        }
        data.update(overrides)
        return data

    def test_register_creates_user(self):
        response = self.client.post(REGISTER_URL, self._valid_data(), format="json")
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertTrue(User.objects.filter(email="test@example.com").exists())

    def test_register_response_contains_email(self):
        response = self.client.post(REGISTER_URL, self._valid_data(), format="json")
        self.assertIn("email", response.data)

    def test_register_response_omits_password(self):
        response = self.client.post(REGISTER_URL, self._valid_data(), format="json")
        self.assertNotIn("password", response.data)

    def test_duplicate_email_returns_400(self):
        self.client.post(REGISTER_URL, self._valid_data(), format="json")
        response = self.client.post(REGISTER_URL, self._valid_data(), format="json")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_weak_password_returns_400(self):
        response = self.client.post(
            REGISTER_URL, self._valid_data(password="weak"), format="json"
        )
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)


class LoginViewTest(APITestCase):

    def setUp(self):
        User.objects.create_user(
            username="testuser",
            email="test@example.com",
            nome="Test User",
            password="password123",
        )

    def test_login_returns_access_and_refresh_tokens(self):
        response = self.client.post(
            LOGIN_URL,
            {"email": "test@example.com", "password": "password123"},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("access", response.data)
        self.assertIn("refresh", response.data)

    def test_wrong_password_returns_401(self):
        response = self.client.post(
            LOGIN_URL,
            {"email": "test@example.com", "password": "wrongpassword"},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_nonexistent_user_returns_401(self):
        response = self.client.post(
            LOGIN_URL,
            {"email": "nobody@example.com", "password": "password123"},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_token_refresh(self):
        login_response = self.client.post(
            LOGIN_URL,
            {"email": "test@example.com", "password": "password123"},
            format="json",
        )
        refresh_token = login_response.data["refresh"]
        response = self.client.post(
            REFRESH_URL, {"refresh": refresh_token}, format="json"
        )
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("access", response.data)
