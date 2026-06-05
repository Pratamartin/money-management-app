import calendar
from datetime import date
from decimal import Decimal

from django.db.models import Sum


def calcular_total_gasto_mes(periodo) -> Decimal:
    return periodo.gastos_diarios.aggregate(
        total=Sum("valor")
    )["total"] or Decimal("0")


def calcular_limite_diario(periodo) -> Decimal:
    """
    limite_hoje = (saldo_disponivel_mes - Σ gastos até ontem) / dias restantes − gasto hoje
    Retorna o saldo restante do dia (negativo se excedeu o limite).
    """
    hoje = date.today()

    total_ate_ontem = periodo.gastos_diarios.filter(
        data__lt=hoje
    ).aggregate(total=Sum("valor"))["total"] or Decimal("0")

    total_hoje = periodo.gastos_diarios.filter(
        data=hoje
    ).aggregate(total=Sum("valor"))["total"] or Decimal("0")

    _, dias_no_mes = calendar.monthrange(periodo.ano, periodo.mes)
    dias_restantes = dias_no_mes - hoje.day + 1

    if dias_restantes <= 0:
        return Decimal("0")

    limite_bruto = (periodo.saldo_disponivel_mes - total_ate_ontem) / dias_restantes
    return limite_bruto - total_hoje
