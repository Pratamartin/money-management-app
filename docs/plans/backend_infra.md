# Plano — Infra (Docker + Settings)

## Responsabilidade
Configuração do ambiente de desenvolvimento com Docker e preparação para deploy no Railway.

---

## Serviços (docker-compose.yml)

```yaml
services:
  db:
    image: postgres:16-alpine
    volumes: [postgres_data:/var/lib/postgresql/data]
    env_file: .env
    healthcheck: pg_isready

  web:
    build: .
    command: python manage.py runserver 0.0.0.0:8000
    volumes: [.:/app]          # hot-reload em dev
    ports: ["8000:8000"]
    depends_on: db (healthy)
```

---

## Variáveis de ambiente (.env)

```
SECRET_KEY=...
DEBUG=True
ALLOWED_HOSTS=localhost,127.0.0.1

POSTGRES_DB=moneymgt
POSTGRES_USER=moneymgt
POSTGRES_PASSWORD=moneymgt
POSTGRES_HOST=db             # nome do serviço no compose
POSTGRES_PORT=5432

CORS_ALLOWED_ORIGINS=http://localhost:8000
```

---

## Dockerfile

```dockerfile
FROM python:3.12-slim
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
WORKDIR /app
RUN apt-get install libpq-dev gcc   # necessário para psycopg2
COPY requirements.txt . && pip install -r requirements.txt
COPY . .
```

---

## requirements.txt

```
django==5.1
djangorestframework==3.15.2
djangorestframework-simplejwt==5.3.1
psycopg2-binary==2.9.9
python-decouple==3.8
django-cors-headers==4.4.0
```

---

## Comandos do dia a dia

```bash
# subir tudo
docker compose up

# rodar migrations
docker compose exec web python manage.py migrate

# criar superuser
docker compose exec web python manage.py createsuperuser

# abrir shell Django
docker compose exec web python manage.py shell
```

---

## Railway (deploy)

- Root directory: `backend/`
- Start command: `python manage.py migrate && gunicorn config.wsgi`
- Adicionar `gunicorn` ao requirements antes do deploy
- Variáveis de ambiente configuradas no painel do Railway

---

## Arquivos a criar

```
backend/
├── Dockerfile
├── docker-compose.yml
├── .env.example        ← commitado no repo
├── .env                ← NÃO commitado (.gitignore)
├── .gitignore
└── requirements.txt
```

---

## Pontos de decisão

- [ ] Usar `psycopg2-binary` (mais fácil) ou `psycopg2` compilado (mais correto para prod)?
- [ ] Adicionar `gunicorn` agora no requirements ou só antes do deploy?
