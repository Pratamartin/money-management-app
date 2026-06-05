# Backend — MoneyMgt

Django + Django REST Framework + PostgreSQL, rodando via Docker.

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) e Docker Compose instalados

## Como rodar

### 1. Configurar variáveis de ambiente

```bash
cp .env.example .env
```

Edite o `.env` com valores reais (em desenvolvimento os padrões do exemplo já funcionam):

```env
SECRET_KEY=troque-por-uma-chave-secreta-longa
DEBUG=True
ALLOWED_HOSTS=localhost,127.0.0.1

POSTGRES_DB=moneymgt
POSTGRES_USER=moneymgt
POSTGRES_PASSWORD=moneymgt
```

### 2. Subir os containers

```bash
sudo docker compose up -d
```

O banco de dados sobe primeiro. O servidor Django aguarda o PostgreSQL estar saudável antes de iniciar.

### 3. Rodar as migrations

```bash
sudo docker compose exec web python manage.py migrate
```

### 4. (Opcional) Criar superusuário para o admin

```bash
sudo docker compose exec web python manage.py createsuperuser
```

Acesse o painel em: `http://localhost:8000/admin/`

---

## Testes

A suite cobre modelos, serializers, services (lógica de negócio) e endpoints da API — 82 testes no total.

### Rodar via Docker (recomendado)

Com os containers no ar:

```bash
sudo docker compose exec web python manage.py test --settings=config.test_settings
```

O flag `--settings=config.test_settings` usa SQLite em memória, então **não precisa de banco PostgreSQL rodando**.

### Rodar localmente (sem Docker)

Crie um virtualenv com as dependências e execute:

```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt

python manage.py test --settings=config.test_settings
```

### Rodar um módulo específico

```bash
# Apenas testes de models do finance
python manage.py test finance.tests.test_models --settings=config.test_settings

# Apenas testes de services (lógica pura, sem HTTP)
python manage.py test finance.tests.test_services --settings=config.test_settings

# Apenas testes de um app
python manage.py test accounts --settings=config.test_settings
```

### Ver cobertura (opcional)

```bash
pip install coverage
coverage run manage.py test --settings=config.test_settings
coverage report
```

---

## Endpoints disponíveis

| Método | URL | Auth | Descrição |
|--------|-----|------|-----------|
| POST | `/auth/register/` | ❌ | Criar conta |
| POST | `/auth/login/` | ❌ | Login — retorna `access` + `refresh` token |
| POST | `/auth/token/refresh/` | ❌ | Renovar access token |

### Exemplos

**Criar conta**
```bash
curl -X POST http://localhost:8000/auth/register/ \
  -H "Content-Type: application/json" \
  -d '{"email":"user@mail.com","username":"pratatec_","nome":"Martinho","password":"minhasenha"}'
```

**Login**
```bash
curl -X POST http://localhost:8000/auth/login/ \
  -H "Content-Type: application/json" \
  -d '{"email":"user@mail.com","password":"minhasenha"}'
```

**Renovar token**
```bash
curl -X POST http://localhost:8000/auth/token/refresh/ \
  -H "Content-Type: application/json" \
  -d '{"refresh":"<refresh_token>"}'
```

---

## Comandos úteis

```bash
# Ver logs do servidor
sudo docker compose logs -f web

# Parar os containers
sudo docker compose down

# Parar e apagar o volume do banco (dados perdidos)
sudo docker compose down -v

# Acessar o shell do container
sudo docker compose exec web bash

# Shell do Django
sudo docker compose exec web python manage.py shell
```

---

## Estrutura

```
backend/
├── config/
│   ├── settings.py
│   ├── urls.py
│   └── wsgi.py
├── accounts/          ← autenticação (User, JWT)
│   ├── models.py
│   ├── serializers.py
│   ├── views.py
│   ├── urls.py
│   └── admin.py
├── finance/           ← domínio financeiro (em construção)
├── manage.py
├── requirements.txt
├── Dockerfile
└── docker-compose.yml
```
