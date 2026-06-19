# Testes Manuais — Backend Gastos Diários

Base URL: `http://localhost:8000`

---

## 1. Preparar ambiente

```bash
# Copiar .env
cd backend
cp .env.example .env

# Subir containers
docker-compose up -d --build

# Aplicar migrations (aguarde o healthcheck do db passar)
docker-compose exec web python manage.py migrate
```

Resultado esperado das migrations:
```
Applying finance.0001_initial... OK
Applying finance.0002_seed_categorias... OK
```

---

## 2. Auth — Cadastro e Login

### 2.1 Cadastrar usuário

```bash
curl -s -X POST http://localhost:8000/auth/register/ \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@email.com","nome":"Teste","username":"teste","password":"senha123!"}' \
  | python3 -m json.tool
```

Esperado: `201 Created` com dados do usuário.

### 2.2 Login (obter tokens)

```bash
curl -s -X POST http://localhost:8000/auth/login/ \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@email.com","password":"senha123!"}' \
  | python3 -m json.tool
```

Esperado: `200 OK` com `access` e `refresh`.

```bash
# Salvar o token para reusar nos próximos passos
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzgwNDUwOTA0LCJpYXQiOjE3ODA0NDczMDQsImp0aSI6ImI0N2FmODE5Y2E4OTRkNzVhNTQwN2JjM2ZmZGMxNzExIiwidXNlcl9pZCI6MX0.gpQlJzrDRB2O_EZcJa47PfXfc7VUWgAQuOq4XFhOpDM"
```

---

## 3. Categorias

### 3.1 Listar categorias (deve retornar as 9 defaults)

```bash
curl -s http://localhost:8000/categorias/ \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool
```

Esperado: lista com Lanche, Almoço, Jantar, Mercado, Remédio, Academia, Transporte, Lazer, Outros.

### 3.2 Criar categoria custom

```bash
curl -s -X POST http://localhost:8000/categorias/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Streaming"}' \
  | python3 -m json.tool
```

Esperado: `201 Created` com `"tipo": "CUSTOM"`.

### 3.3 Deletar categoria custom

```bash
# Use o id retornado no passo anterior
curl -s -X DELETE http://localhost:8000/categorias/<id>/ \
  -H "Authorization: Bearer $TOKEN"
```

Esperado: `204 No Content`.

### 3.4 Tentar deletar categoria default (deve falhar)

```bash
curl -s -X DELETE http://localhost:8000/categorias/1/ \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool
```

Esperado: `400 Bad Request` com mensagem de erro.

---

## 4. Períodos

### 4.1 Criar período

```bash
curl -s -X POST http://localhost:8000/periodos/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"mes":6,"ano":2026,"saldo_carteira":"5000.00","saldo_disponivel_mes":"2000.00"}' \
  | python3 -m json.tool
```

Esperado: `201 Created`. Guarde o `id` do período.

```bash
PERIODO_ID=<id do período>
```

### 4.2 Listar períodos

```bash
curl -s http://localhost:8000/periodos/ \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool
```

Esperado: lista com o período criado.

### 4.3 Tentar criar período duplicado (deve falhar)

```bash
curl -s -X POST http://localhost:8000/periodos/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"mes":6,"ano":2026,"saldo_carteira":"100.00","saldo_disponivel_mes":"100.00"}' \
  | python3 -m json.tool
```

Esperado: `400 Bad Request` (unique_together).

---

## 5. Gastos Diários

### 5.1 Registrar gasto

```bash
curl -s -X POST http://localhost:8000/periodos/$PERIODO_ID/gastos-diarios/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"data":"2026-06-02","valor":"32.50","categoria_id":1,"descricao":"Almoço no trabalho"}' \
  | python3 -m json.tool
```

Esperado: `201 Created` com `categoria` nested (`id` + `nome`).

```bash
GASTO_ID=<id do gasto>
```

### 5.2 Registrar segundo gasto (sem descrição)

```bash
curl -s -X POST http://localhost:8000/periodos/$PERIODO_ID/gastos-diarios/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"data":"2026-06-02","valor":"8.00","categoria_id":2}' \
  | python3 -m json.tool
```

Esperado: `201 Created` com `"descricao": ""`.

### 5.3 Listar gastos do período

```bash
curl -s http://localhost:8000/periodos/$PERIODO_ID/gastos-diarios/ \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool
```

Esperado: lista com os 2 gastos, ordenados por `data`.

### 5.4 Editar gasto (PATCH)

```bash
curl -s -X PATCH http://localhost:8000/periodos/$PERIODO_ID/gastos-diarios/$GASTO_ID/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"valor":"35.00"}' \
  | python3 -m json.tool
```

Esperado: `200 OK` com `"valor": "35.00"`.

### 5.5 Tentar registrar gasto com data fora do período (deve falhar)

```bash
curl -s -X POST http://localhost:8000/periodos/$PERIODO_ID/gastos-diarios/ \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"data":"2026-07-01","valor":"10.00","categoria_id":1}' \
  | python3 -m json.tool
```

Esperado: `400 Bad Request` — "A data não pertence ao mês/ano do período."

### 5.6 Deletar gasto

```bash
curl -s -X DELETE http://localhost:8000/periodos/$PERIODO_ID/gastos-diarios/$GASTO_ID/ \
  -H "Authorization: Bearer $TOKEN"
```

Esperado: `204 No Content`.

---

## 6. Resumo do período

```bash
curl -s http://localhost:8000/periodos/$PERIODO_ID/resumo/ \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool
```

Esperado:
```json
{
  "saldo_carteira": "5000.00",
  "saldo_disponivel_mes": "2000.00",
  "total_gasto_mes": "8.00",
  "limite_hoje": "<valor calculado>"
}
```

> `limite_hoje` varia conforme a data de hoje e os gastos registrados.

---

## 7. Segurança — acesso cruzado entre usuários

### 7.1 Cadastrar segundo usuário e tentar acessar período do primeiro

```bash
curl -s -X POST http://localhost:8000/auth/register/ \
  -H "Content-Type: application/json" \
  -d '{"email":"outro@email.com","nome":"Outro","username":"outro","password":"senha123!"}' \
  | python3 -m json.tool

curl -s -X POST http://localhost:8000/auth/login/ \
  -H "Content-Type: application/json" \
  -d '{"email":"outro@email.com","password":"senha123!"}' \
  | python3 -m json.tool

TOKEN2="<access do segundo usuário>"

curl -s http://localhost:8000/periodos/$PERIODO_ID/gastos-diarios/ \
  -H "Authorization: Bearer $TOKEN2" \
  | python3 -m json.tool
```

Esperado: `404 Not Found` — o período não existe para este usuário.

---

## 8. Derrubar ambiente

```bash
docker-compose down
# Para apagar os dados do banco também:
docker-compose down -v
```
