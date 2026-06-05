from django.contrib.auth import get_user_model
from django.db import IntegrityError
from django.test import TestCase

User = get_user_model()


class UserModelTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="testuser",
            email="test@example.com",
            nome="Test User",
            password="password123",
        )

    def test_username_field_is_email(self):
        self.assertEqual(User.USERNAME_FIELD, "email")

    def test_str_returns_email(self):
        self.assertEqual(str(self.user), "test@example.com")

    def test_email_must_be_unique(self):
        with self.assertRaises(IntegrityError):
            User.objects.create_user(
                username="other",
                email="test@example.com",
                nome="Other User",
                password="password123",
            )

    def test_password_is_hashed(self):
        self.assertNotEqual(self.user.password, "password123")
        self.assertTrue(self.user.check_password("password123"))

    def test_nome_field_saved(self):
        self.assertEqual(self.user.nome, "Test User")

    def test_required_fields(self):
        self.assertIn("username", User.REQUIRED_FIELDS)
        self.assertIn("nome", User.REQUIRED_FIELDS)
