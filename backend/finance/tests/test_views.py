import datetime
from decimal import Decimal

from django.contrib.auth import get_user_model
from rest_framework import status
from rest_framework.test import APITestCase

from finance.models import Categoria, GastoDiario, Periodo

User = get_user_model()


class CategoriaViewSetTest(APITestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )
        self.client.force_authenticate(user=self.user)
        self.cat_default = Categoria.objects.create(
            nome="Lanche", tipo=Categoria.Tipo.DEFAULT
        )
        self.cat_custom = Categoria.objects.create(
            nome="Gym", tipo=Categoria.Tipo.CUSTOM, usuario=self.user
        )

    def test_list_includes_default_and_own_categories(self):
        response = self.client.get("/categorias/")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        nomes = [c["nome"] for c in response.data]
        self.assertIn("Lanche", nomes)
        self.assertIn("Gym", nomes)

    def test_list_excludes_other_users_categories(self):
        other = User.objects.create_user(
            username="o", email="o@example.com", nome="Other", password="pass"
        )
        Categoria.objects.create(nome="OtherCat", tipo=Categoria.Tipo.CUSTOM, usuario=other)
        response = self.client.get("/categorias/")
        nomes = [c["nome"] for c in response.data]
        self.assertNotIn("OtherCat", nomes)

    def test_create_custom_category(self):
        response = self.client.post("/categorias/", {"nome": "Viagem"}, format="json")
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data["nome"], "Viagem")

    def test_create_assigns_tipo_custom(self):
        response = self.client.post("/categorias/", {"nome": "Viagem"}, format="json")
        self.assertEqual(response.data["tipo"], Categoria.Tipo.CUSTOM)

    def test_delete_own_custom_category(self):
        response = self.client.delete(f"/categorias/{self.cat_custom.pk}/")
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(Categoria.objects.filter(pk=self.cat_custom.pk).exists())

    def test_delete_default_category_returns_400(self):
        response = self.client.delete(f"/categorias/{self.cat_default.pk}/")
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertTrue(Categoria.objects.filter(pk=self.cat_default.pk).exists())

    def test_delete_other_users_category_returns_404(self):
        other = User.objects.create_user(
            username="o", email="o@example.com", nome="Other", password="pass"
        )
        other_cat = Categoria.objects.create(
            nome="OtherCat", tipo=Categoria.Tipo.CUSTOM, usuario=other
        )
        response = self.client.delete(f"/categorias/{other_cat.pk}/")
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    def test_unauthenticated_returns_401(self):
        self.client.force_authenticate(user=None)
        response = self.client.get("/categorias/")
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class PeriodoViewSetTest(APITestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )
        self.client.force_authenticate(user=self.user)

    def test_create_periodo(self):
        response = self.client.post(
            "/periodos/",
            {"mes": 6, "ano": 2026, "saldo_carteira": "1000.00", "saldo_disponivel_mes": "800.00"},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data["mes"], 6)
        self.assertEqual(response.data["ano"], 2026)

    def test_create_period_belongs_to_authenticated_user(self):
        self.client.post(
            "/periodos/",
            {"mes": 6, "ano": 2026, "saldo_carteira": "0", "saldo_disponivel_mes": "0"},
            format="json",
        )
        self.assertTrue(
            Periodo.objects.filter(mes=6, ano=2026, usuario=self.user).exists()
        )

    def test_list_returns_only_own_periodos(self):
        other = User.objects.create_user(
            username="o", email="o@example.com", nome="Other", password="pass"
        )
        Periodo.objects.create(mes=1, ano=2026, usuario=self.user)
        Periodo.objects.create(mes=2, ano=2026, usuario=other)
        response = self.client.get("/periodos/")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)

    def test_resumo_action_returns_expected_keys(self):
        periodo = Periodo.objects.create(
            mes=6,
            ano=2026,
            usuario=self.user,
            saldo_carteira=Decimal("1000.00"),
            saldo_disponivel_mes=Decimal("800.00"),
        )
        response = self.client.get(f"/periodos/{periodo.pk}/resumo/")
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        for key in ("saldo_carteira", "saldo_disponivel_mes", "total_gasto_mes", "limite_hoje"):
            self.assertIn(key, response.data)

    def test_resumo_total_gasto_zero_when_no_gastos(self):
        periodo = Periodo.objects.create(mes=6, ano=2026, usuario=self.user)
        response = self.client.get(f"/periodos/{periodo.pk}/resumo/")
        self.assertEqual(response.data["total_gasto_mes"], "0")

    def test_unauthenticated_returns_401(self):
        self.client.force_authenticate(user=None)
        response = self.client.get("/periodos/")
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class GastoDiarioViewSetTest(APITestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )
        self.client.force_authenticate(user=self.user)
        self.categoria = Categoria.objects.create(
            nome="Outros", tipo=Categoria.Tipo.DEFAULT
        )
        self.periodo = Periodo.objects.create(
            mes=6, ano=2026, usuario=self.user, saldo_disponivel_mes=Decimal("500.00")
        )
        self.list_url = f"/periodos/{self.periodo.pk}/gastos-diarios/"

    def _create_gasto(self, valor="20.00", data="2026-06-01"):
        return GastoDiario.objects.create(
            data=datetime.date.fromisoformat(data),
            valor=Decimal(valor),
            categoria=self.categoria,
            periodo=self.periodo,
        )

    def test_create_gasto(self):
        response = self.client.post(
            self.list_url,
            {"data": "2026-06-15", "valor": "50.00", "categoria_id": self.categoria.pk},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data["valor"], "50.00")

    def test_create_gasto_returns_categoria_detail(self):
        response = self.client.post(
            self.list_url,
            {"data": "2026-06-15", "valor": "50.00", "categoria_id": self.categoria.pk},
            format="json",
        )
        self.assertIn("categoria", response.data)
        self.assertEqual(response.data["categoria"]["nome"], "Outros")

    def test_list_gastos_of_periodo(self):
        self._create_gasto("10.00")
        self._create_gasto("20.00", "2026-06-02")
        response = self.client.get(self.list_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)

    def test_partial_update_gasto(self):
        gasto = self._create_gasto()
        response = self.client.patch(
            f"{self.list_url}{gasto.pk}/",
            {"valor": "35.00"},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data["valor"], "35.00")

    def test_delete_gasto(self):
        gasto = self._create_gasto()
        response = self.client.delete(f"{self.list_url}{gasto.pk}/")
        self.assertEqual(response.status_code, status.HTTP_204_NO_CONTENT)
        self.assertFalse(GastoDiario.objects.filter(pk=gasto.pk).exists())

    def test_create_gasto_wrong_mes_returns_400(self):
        response = self.client.post(
            self.list_url,
            {"data": "2026-07-01", "valor": "50.00", "categoria_id": self.categoria.pk},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_create_gasto_wrong_ano_returns_400(self):
        response = self.client.post(
            self.list_url,
            {"data": "2025-06-01", "valor": "50.00", "categoria_id": self.categoria.pk},
            format="json",
        )
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_access_other_users_periodo_returns_404(self):
        other = User.objects.create_user(
            username="o", email="o@example.com", nome="Other", password="pass"
        )
        other_periodo = Periodo.objects.create(mes=6, ano=2026, usuario=other)
        response = self.client.get(f"/periodos/{other_periodo.pk}/gastos-diarios/")
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)

    def test_unauthenticated_returns_401(self):
        self.client.force_authenticate(user=None)
        response = self.client.get(self.list_url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)
