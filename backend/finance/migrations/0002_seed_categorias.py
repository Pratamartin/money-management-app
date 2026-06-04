from django.db import migrations

CATEGORIAS_DEFAULT = [
    "Lanche",
    "Almoço",
    "Jantar",
    "Mercado",
    "Remédio",
    "Academia",
    "Transporte",
    "Lazer",
    "Outros",
]


def seed(apps, schema_editor):
    Categoria = apps.get_model("finance", "Categoria")
    Categoria.objects.bulk_create([
        Categoria(nome=nome, tipo="DEFAULT") for nome in CATEGORIAS_DEFAULT
    ])


def reverse_seed(apps, schema_editor):
    Categoria = apps.get_model("finance", "Categoria")
    Categoria.objects.filter(tipo="DEFAULT").delete()


class Migration(migrations.Migration):

    dependencies = [
        ("finance", "0001_initial"),
    ]

    operations = [
        migrations.RunPython(seed, reverse_seed),
    ]
