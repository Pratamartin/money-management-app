import datetime
from decimal import Decimal

from django.contrib.auth import get_user_model
from django.db import IntegrityError
from django.test import TestCase

from finance.models import Categoria, GastoDiario, Periodo

User = get_user_model()


class CategoriaModelTest(TestCase):

    def test_str(self):
        categoria = Categoria(nome="Lanche", tipo=Categoria.Tipo.DEFAULT)
        self.assertEqual(str(categoria), "Lanche")

    def test_default_tipo_is_custom(self):
        categoria = Categoria(nome="Test")
        self.assertEqual(categoria.tipo, Categoria.Tipo.CUSTOM)

    def test_ordering_by_nome(self):
        user = User.objects.create_user(
            username="u", email="u@u.com", nome="U", password="p"
        )
        Categoria.objects.create(nome="Zumba", tipo=Categoria.Tipo.DEFAULT)
        Categoria.objects.create(nome="Agua", usuario=user)
        nomes = list(Categoria.objects.values_list("nome", flat=True))
        self.assertEqual(nomes, sorted(nomes))

    def test_usuario_nullable(self):
        categoria = Categoria.objects.create(nome="Outros", tipo=Categoria.Tipo.DEFAULT)
        self.assertIsNone(categoria.usuario)


class PeriodoModelTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )

    def test_str_contains_mes_ano(self):
        periodo = Periodo(mes=6, ano=2026, usuario=self.user)
        self.assertIn("06/2026", str(periodo))

    def test_unique_together_constraint(self):
        Periodo.objects.create(mes=1, ano=2026, usuario=self.user)
        with self.assertRaises(IntegrityError):
            Periodo.objects.create(mes=1, ano=2026, usuario=self.user)

    def test_ordering_newest_first(self):
        Periodo.objects.create(mes=1, ano=2025, usuario=self.user)
        Periodo.objects.create(mes=6, ano=2026, usuario=self.user)
        periodos = list(Periodo.objects.filter(usuario=self.user))
        self.assertEqual(periodos[0].ano, 2026)

    def test_different_users_can_share_mes_ano(self):
        other = User.objects.create_user(
            username="o", email="o@example.com", nome="Other", password="pass"
        )
        Periodo.objects.create(mes=6, ano=2026, usuario=self.user)
        # não deve lançar exceção
        Periodo.objects.create(mes=6, ano=2026, usuario=other)

    def test_default_saldos_are_zero(self):
        periodo = Periodo.objects.create(mes=3, ano=2026, usuario=self.user)
        self.assertEqual(periodo.saldo_carteira, Decimal("0"))
        self.assertEqual(periodo.saldo_disponivel_mes, Decimal("0"))


class GastoDiarioModelTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )
        self.categoria = Categoria.objects.create(
            nome="Lanche", tipo=Categoria.Tipo.DEFAULT
        )
        self.periodo = Periodo.objects.create(mes=6, ano=2026, usuario=self.user)

    def test_str_contains_valor_and_categoria(self):
        gasto = GastoDiario(
            data=datetime.date(2026, 6, 1),
            valor=Decimal("25.00"),
            categoria=self.categoria,
            periodo=self.periodo,
        )
        self.assertIn("25.00", str(gasto))
        self.assertIn("Lanche", str(gasto))

    def test_descricao_defaults_to_empty_string(self):
        gasto = GastoDiario.objects.create(
            data=datetime.date(2026, 6, 1),
            valor=Decimal("10.00"),
            categoria=self.categoria,
            periodo=self.periodo,
        )
        self.assertEqual(gasto.descricao, "")

    def test_cascade_delete_with_periodo(self):
        GastoDiario.objects.create(
            data=datetime.date(2026, 6, 1),
            valor=Decimal("10.00"),
            categoria=self.categoria,
            periodo=self.periodo,
        )
        self.periodo.delete()
        self.assertEqual(GastoDiario.objects.count(), 0)

    def test_protect_prevents_categoria_delete(self):
        from django.db.models import ProtectedError

        GastoDiario.objects.create(
            data=datetime.date(2026, 6, 1),
            valor=Decimal("10.00"),
            categoria=self.categoria,
            periodo=self.periodo,
        )
        with self.assertRaises(ProtectedError):
            self.categoria.delete()
