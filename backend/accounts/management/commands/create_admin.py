import os
from django.core.management.base import BaseCommand
from django.contrib.auth import get_user_model


class Command(BaseCommand):
    help = "Cria superusuário a partir de variáveis de ambiente"

    def handle(self, *args, **kwargs):
        User = get_user_model()

        email = os.environ.get("ADMIN_EMAIL")
        password = os.environ.get("ADMIN_PASSWORD")
        nome = os.environ.get("ADMIN_NOME", "Admin")
        username = os.environ.get("ADMIN_USERNAME", email)

        if not email or not password:
            self.stderr.write("ADMIN_EMAIL e ADMIN_PASSWORD são obrigatórios.")
            return

        if User.objects.filter(email=email).exists():
            self.stdout.write(f"Admin '{email}' já existe. Nada foi alterado.")
            return

        User.objects.create_superuser(
            email=email,
            username=username,
            password=password,
            nome=nome,
        )
        self.stdout.write(self.style.SUCCESS(f"Superusuário '{email}' criado com sucesso."))
