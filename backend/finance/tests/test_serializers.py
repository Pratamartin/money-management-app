import datetime
from decimal import Decimal

from django.contrib.auth import get_user_model
from django.test import TestCase
from rest_framework.request import Request
from rest_framework.test import APIRequestFactory, force_authenticate

from finance.models import Categoria, GastoDiario, Periodo
from finance.serializers import (
    CategoriaSerializer,
    GastoDiarioSerializer,
    PeriodoSerializer,
)

User = get_user_model()


class CategoriaSerializerTest(TestCase):

    def test_serializes_expected_fields(self):
        categoria = Categoria(id=1, nome="Lanche", tipo=Categoria.Tipo.DEFAULT)
        data = CategoriaSerializer(categoria).data
        self.assertEqual(set(data.keys()), {"id", "nome", "tipo"})

    def test_tipo_is_read_only(self):
        serializer = CategoriaSerializer(data={"nome": "Teste", "tipo": "DEFAULT"})
        self.assertTrue(serializer.is_valid())
        self.assertNotIn("tipo", serializer.validated_data)

    def test_nome_required(self):
        serializer = CategoriaSerializer(data={})
        self.assertFalse(serializer.is_valid())
        self.assertIn("nome", serializer.errors)


class PeriodoSerializerTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )
        factory = APIRequestFactory()
        raw_request = factory.post("/")
        force_authenticate(raw_request, user=self.user)
        self.request = Request(raw_request)

    def _context(self):
        return {"request": self.request}

    def test_create_assigns_user_from_request_context(self):
        serializer = PeriodoSerializer(
            data={
                "mes": 6,
                "ano": 2026,
                "saldo_carteira": "500.00",
                "saldo_disponivel_mes": "300.00",
            },
            context=self._context(),
        )
        self.assertTrue(serializer.is_valid(), serializer.errors)
        periodo = serializer.save()
        self.assertEqual(periodo.usuario, self.user)

    def test_valid_data_passes(self):
        serializer = PeriodoSerializer(
            data={"mes": 1, "ano": 2026, "saldo_carteira": "0", "saldo_disponivel_mes": "0"},
            context=self._context(),
        )
        self.assertTrue(serializer.is_valid(), serializer.errors)

    def test_mes_required(self):
        serializer = PeriodoSerializer(
            data={"ano": 2026, "saldo_carteira": "0", "saldo_disponivel_mes": "0"},
            context=self._context(),
        )
        self.assertFalse(serializer.is_valid())
        self.assertIn("mes", serializer.errors)

    def test_ano_required(self):
        serializer = PeriodoSerializer(
            data={"mes": 6, "saldo_carteira": "0", "saldo_disponivel_mes": "0"},
            context=self._context(),
        )
        self.assertFalse(serializer.is_valid())
        self.assertIn("ano", serializer.errors)


class GastoDiarioSerializerTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )
        self.categoria = Categoria.objects.create(
            nome="Lanche", tipo=Categoria.Tipo.DEFAULT
        )
        self.periodo = Periodo.objects.create(
            mes=6,
            ano=2026,
            usuario=self.user,
            saldo_disponivel_mes=Decimal("500.00"),
        )

    def _context(self):
        return {"periodo": self.periodo}

    def test_valid_data_passes(self):
        serializer = GastoDiarioSerializer(
            data={
                "data": "2026-06-15",
                "valor": "50.00",
                "categoria_id": self.categoria.pk,
            },
            context=self._context(),
        )
        self.assertTrue(serializer.is_valid(), serializer.errors)

    def test_date_outside_periodo_rejected(self):
        serializer = GastoDiarioSerializer(
            data={
                "data": "2026-07-01",  # julho, mas periodo é junho
                "valor": "50.00",
                "categoria_id": self.categoria.pk,
            },
            context=self._context(),
        )
        self.assertFalse(serializer.is_valid())
        self.assertIn("data", serializer.errors)

    def test_date_from_different_year_rejected(self):
        serializer = GastoDiarioSerializer(
            data={
                "data": "2025-06-01",  # ano errado
                "valor": "50.00",
                "categoria_id": self.categoria.pk,
            },
            context=self._context(),
        )
        self.assertFalse(serializer.is_valid())
        self.assertIn("data", serializer.errors)

    def test_categoria_read_only_in_output(self):
        gasto = GastoDiario.objects.create(
            data=datetime.date(2026, 6, 1),
            valor=Decimal("25.00"),
            categoria=self.categoria,
            periodo=self.periodo,
        )
        data = GastoDiarioSerializer(gasto, context=self._context()).data
        self.assertIn("categoria", data)
        self.assertNotIn("categoria_id", data)
        self.assertEqual(data["categoria"]["nome"], "Lanche")

    def test_descricao_optional(self):
        serializer = GastoDiarioSerializer(
            data={
                "data": "2026-06-10",
                "valor": "30.00",
                "categoria_id": self.categoria.pk,
            },
            context=self._context(),
        )
        self.assertTrue(serializer.is_valid(), serializer.errors)

    def test_valor_required(self):
        serializer = GastoDiarioSerializer(
            data={"data": "2026-06-10", "categoria_id": self.categoria.pk},
            context=self._context(),
        )
        self.assertFalse(serializer.is_valid())
        self.assertIn("valor", serializer.errors)
