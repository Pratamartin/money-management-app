import django.db.models.deletion
from django.conf import settings
from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        ("accounts", "0001_initial"),
    ]

    operations = [
        migrations.CreateModel(
            name="Categoria",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("nome", models.CharField(max_length=100)),
                ("tipo", models.CharField(choices=[("DEFAULT", "Default"), ("CUSTOM", "Custom")], default="CUSTOM", max_length=10)),
                (
                    "usuario",
                    models.ForeignKey(
                        blank=True,
                        null=True,
                        on_delete=django.db.models.deletion.CASCADE,
                        related_name="categorias",
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
            ],
            options={"ordering": ["nome"]},
        ),
        migrations.CreateModel(
            name="Periodo",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("mes", models.PositiveSmallIntegerField()),
                ("ano", models.PositiveSmallIntegerField()),
                ("saldo_carteira", models.DecimalField(decimal_places=2, default=0, max_digits=12)),
                ("saldo_disponivel_mes", models.DecimalField(decimal_places=2, default=0, max_digits=12)),
                (
                    "usuario",
                    models.ForeignKey(
                        on_delete=django.db.models.deletion.CASCADE,
                        related_name="periodos",
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
            ],
            options={"ordering": ["-ano", "-mes"], "unique_together": {("mes", "ano", "usuario")}},
        ),
        migrations.CreateModel(
            name="GastoDiario",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("data", models.DateField()),
                ("valor", models.DecimalField(decimal_places=2, max_digits=12)),
                ("descricao", models.CharField(blank=True, max_length=255)),
                (
                    "categoria",
                    models.ForeignKey(
                        on_delete=django.db.models.deletion.PROTECT,
                        related_name="gastos",
                        to="finance.categoria",
                    ),
                ),
                (
                    "periodo",
                    models.ForeignKey(
                        on_delete=django.db.models.deletion.CASCADE,
                        related_name="gastos_diarios",
                        to="finance.periodo",
                    ),
                ),
            ],
            options={"ordering": ["data"]},
        ),
    ]
