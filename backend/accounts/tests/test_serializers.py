from django.contrib.auth import get_user_model
from django.test import TestCase

from accounts.serializers import RegisterSerializer

User = get_user_model()


class RegisterSerializerTest(TestCase):

    def _valid_data(self, **overrides):
        data = {
            "username": "testuser",
            "email": "test@example.com",
            "nome": "Test User",
            "password": "password123",
        }
        data.update(overrides)
        return data

    def test_valid_data_passes(self):
        serializer = RegisterSerializer(data=self._valid_data())
        self.assertTrue(serializer.is_valid(), serializer.errors)

    def test_password_below_min_length_rejected(self):
        serializer = RegisterSerializer(data=self._valid_data(password="short"))
        self.assertFalse(serializer.is_valid())
        self.assertIn("password", serializer.errors)

    def test_missing_email_rejected(self):
        data = self._valid_data()
        del data["email"]
        serializer = RegisterSerializer(data=data)
        self.assertFalse(serializer.is_valid())
        self.assertIn("email", serializer.errors)

    def test_missing_nome_rejected(self):
        data = self._valid_data()
        del data["nome"]
        serializer = RegisterSerializer(data=data)
        self.assertFalse(serializer.is_valid())
        self.assertIn("nome", serializer.errors)

    def test_create_returns_user_instance(self):
        serializer = RegisterSerializer(data=self._valid_data())
        self.assertTrue(serializer.is_valid())
        user = serializer.save()
        self.assertIsInstance(user, User)

    def test_create_hashes_password(self):
        serializer = RegisterSerializer(data=self._valid_data())
        self.assertTrue(serializer.is_valid())
        user = serializer.save()
        self.assertTrue(user.check_password("password123"))

    def test_password_is_write_only(self):
        user = User.objects.create_user(**self._valid_data())
        serializer = RegisterSerializer(user)
        self.assertNotIn("password", serializer.data)

    def test_duplicate_email_rejected(self):
        User.objects.create_user(
            username="existing",
            email="test@example.com",
            nome="Existing",
            password="password123",
        )
        serializer = RegisterSerializer(data=self._valid_data())
        self.assertFalse(serializer.is_valid())
