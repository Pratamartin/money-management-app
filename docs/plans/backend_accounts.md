# Plano — Accounts (Auth)

## Responsabilidade
Cadastro, login e renovação de token JWT. Nenhuma lógica financeira aqui.

---

## Model

```python
# estende AbstractUser do Django
class User(AbstractUser):
    email = models.EmailField(unique=True)
    nome = models.CharField(max_length=150)   # nome real: "Martinho"
    # username herdado do AbstractUser — handle único: "pratatec_"
    USERNAME_FIELD = "email"
    REQUIRED_FIELDS = ["username", "nome"]
```

> `username` = handle público (ex: `pratatec_`), `nome` = nome real de exibição (ex: `Martinho`).

---

## Endpoints

| Método | URL | Auth | Descrição |
|--------|-----|------|-----------|
| POST | `/auth/register/` | ❌ | Cria conta (email, username, senha) |
| POST | `/auth/login/` | ❌ | Retorna access + refresh token |
| POST | `/auth/token/refresh/` | ❌ (refresh token) | Renova o access token |

### Payloads

**Register**
```json
// request
{ "email": "user@mail.com", "username": "pratatec_", "nome": "Martinho", "password": "min8chars" }
// response 201
{ "id": 1, "email": "user@mail.com", "username": "pratatec_", "nome": "Martinho" }
```

**Login**
```json
// request
{ "email": "user@mail.com", "password": "min8chars" }
// response 200
{ "access": "<jwt>", "refresh": "<jwt>" }
```

**Refresh**
```json
// request
{ "refresh": "<jwt>" }
// response 200
{ "access": "<jwt>" }
```

---

## Configurações JWT (SimpleJWT)

- `ACCESS_TOKEN_LIFETIME` → 60 minutos
- `REFRESH_TOKEN_LIFETIME` → 30 dias
- `ROTATE_REFRESH_TOKENS` → True (novo refresh a cada renovação)

---

## Arquivos a criar

```
accounts/
├── __init__.py
├── models.py
├── serializers.py
├── views.py
└── urls.py
```

---

## Dependências

- `djangorestframework-simplejwt`
- `AUTH_USER_MODEL = "accounts.User"` no settings

---

## Pontos de decisão

- [ ] Usar `username` como campo extra ou remover completamente e só ter `email` + `nome`?
