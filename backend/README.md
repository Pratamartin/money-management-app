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
