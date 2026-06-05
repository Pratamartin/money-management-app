import datetime
from decimal import Decimal
from unittest.mock import patch

from django.contrib.auth import get_user_model
from django.test import TestCase

from finance.models import Categoria, GastoDiario, Periodo
from finance.services import calcular_limite_diario, calcular_total_gasto_mes

User = get_user_model()


class CalcularTotalGastoMesTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )
        self.categoria = Categoria.objects.create(
            nome="Outros", tipo=Categoria.Tipo.DEFAULT
        )
        self.periodo = Periodo.objects.create(mes=6, ano=2026, usuario=self.user)

    def _create_gasto(self, valor, data=datetime.date(2026, 6, 1), periodo=None):
        return GastoDiario.objects.create(
            data=data,
            valor=Decimal(str(valor)),
            categoria=self.categoria,
            periodo=periodo or self.periodo,
        )

    def test_sem_gastos_retorna_zero(self):
        resultado = calcular_total_gasto_mes(self.periodo)
        self.assertEqual(resultado, Decimal("0"))

    def test_um_gasto(self):
        self._create_gasto("25.50")
        resultado = calcular_total_gasto_mes(self.periodo)
        self.assertEqual(resultado, Decimal("25.50"))

    def test_soma_multiplos_gastos(self):
        self._create_gasto("10.00", datetime.date(2026, 6, 1))
        self._create_gasto("25.50", datetime.date(2026, 6, 2))
        self._create_gasto("14.50", datetime.date(2026, 6, 3))
        resultado = calcular_total_gasto_mes(self.periodo)
        self.assertEqual(resultado, Decimal("50.00"))

    def test_ignora_gastos_de_outro_periodo(self):
        outro_periodo = Periodo.objects.create(mes=5, ano=2026, usuario=self.user)
        self._create_gasto("99.00", datetime.date(2026, 5, 1), periodo=outro_periodo)
        resultado = calcular_total_gasto_mes(self.periodo)
        self.assertEqual(resultado, Decimal("0"))


class CalcularLimiteDiarioTest(TestCase):

    def setUp(self):
        self.user = User.objects.create_user(
            username="u", email="u@example.com", nome="User", password="pass"
        )
        self.categoria = Categoria.objects.create(
            nome="Outros", tipo=Categoria.Tipo.DEFAULT
        )
        self.periodo = Periodo.objects.create(
            mes=6,
            ano=2026,
            usuario=self.user,
            saldo_disponivel_mes=Decimal("300.00"),
        )

    def _create_gasto(self, valor, data):
        return GastoDiario.objects.create(
            data=data,
            valor=Decimal(str(valor)),
            categoria=self.categoria,
            periodo=self.periodo,
        )

    @patch("finance.services.date")
    def test_sem_gastos_retorna_limite_proporcional(self, mock_date):
        # Dia 1 de junho: limite = 300 / 30 dias = 10.00
        mock_date.today.return_value = datetime.date(2026, 6, 1)
        resultado = calcular_limite_diario(self.periodo)
        self.assertEqual(resultado, Decimal("300") / 30)

    @patch("finance.services.date")
    def test_desconta_gasto_de_hoje(self, mock_date):
        mock_date.today.return_value = datetime.date(2026, 6, 15)
        self._create_gasto("5.00", datetime.date(2026, 6, 15))
        resultado = calcular_limite_diario(self.periodo)
        # dias_restantes = 30 - 15 + 1 = 16
        # limite_bruto = 300 / 16
        # limite = 300/16 - 5
        esperado = Decimal("300") / 16 - Decimal("5")
        self.assertAlmostEqual(float(resultado), float(esperado), places=6)

    @patch("finance.services.date")
    def test_desconta_gasto_de_ontem_do_saldo(self, mock_date):
        mock_date.today.return_value = datetime.date(2026, 6, 10)
        self._create_gasto("60.00", datetime.date(2026, 6, 9))
        resultado = calcular_limite_diario(self.periodo)
        # total_ate_ontem = 60, dias_restantes = 30 - 10 + 1 = 21
        # limite_bruto = (300 - 60) / 21 = 240 / 21
        esperado = Decimal("240") / 21
        self.assertAlmostEqual(float(resultado), float(esperado), places=6)

    @patch("finance.services.date")
    def test_limite_negativo_quando_excedeu_hoje(self, mock_date):
        mock_date.today.return_value = datetime.date(2026, 6, 1)
        # Limite do dia = 300/30 = 10.00, mas gastou 50.00
        self._create_gasto("50.00", datetime.date(2026, 6, 1))
        resultado = calcular_limite_diario(self.periodo)
        # limite_bruto = 300 / 30 = 10, limite = 10 - 50 = -40
        self.assertLess(resultado, Decimal("0"))

    @patch("finance.services.date")
    def test_dias_restantes_zero_retorna_zero(self, mock_date):
        # Simula dia 31 com período de junho (30 dias): dias_restantes = 30 - 31 + 1 = 0
        mock_date.today.return_value = datetime.date(2026, 7, 31)
        resultado = calcular_limite_diario(self.periodo)
        self.assertEqual(resultado, Decimal("0"))

    @patch("finance.services.date")
    def test_ultimo_dia_do_mes(self, mock_date):
        mock_date.today.return_value = datetime.date(2026, 6, 30)
        resultado = calcular_limite_diario(self.periodo)
        # dias_restantes = 30 - 30 + 1 = 1
        # limite = 300 / 1 = 300
        self.assertEqual(resultado, Decimal("300"))
