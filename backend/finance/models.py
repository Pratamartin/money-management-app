from django.conf import settings
from django.db import models


class Categoria(models.Model):
    class Tipo(models.TextChoices):
        DEFAULT = "DEFAULT", "Default"
        CUSTOM = "CUSTOM", "Custom"

    nome = models.CharField(max_length=100)
    tipo = models.CharField(max_length=10, choices=Tipo.choices, default=Tipo.CUSTOM)
    usuario = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        null=True,
        blank=True,
        related_name="categorias",
    )

    class Meta:
        ordering = ["nome"]

    def __str__(self):
        return self.nome


class Periodo(models.Model):
    mes = models.PositiveSmallIntegerField()
    ano = models.PositiveSmallIntegerField()
    saldo_carteira = models.DecimalField(max_digits=12, decimal_places=2, default=0)
    saldo_disponivel_mes = models.DecimalField(max_digits=12, decimal_places=2, default=0)
    usuario = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="periodos",
    )

    class Meta:
        ordering = ["-ano", "-mes"]
        unique_together = [("mes", "ano", "usuario")]

    def __str__(self):
        return f"{self.mes:02d}/{self.ano} — {self.usuario}"


class GastoDiario(models.Model):
    data = models.DateField()
    valor = models.DecimalField(max_digits=12, decimal_places=2)
    descricao = models.CharField(max_length=255, blank=True)
    categoria = models.ForeignKey(
        Categoria,
        on_delete=models.PROTECT,
        related_name="gastos",
    )
    periodo = models.ForeignKey(
        Periodo,
        on_delete=models.CASCADE,
        related_name="gastos_diarios",
    )

    class Meta:
        ordering = ["data"]

    def __str__(self):
        return f"{self.data} — R$ {self.valor} ({self.categoria})"
