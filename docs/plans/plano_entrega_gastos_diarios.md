# Plano de Entrega — Gastos Diários

## Contexto

`GastoDiario` depende de `Periodo` e `Categoria`, então o backend precisa construir as três entidades. O cálculo de limite diário fica em `services.py` e é exposto pelo endpoint `/periodos/{id}/resumo/`.

---

## Fase 1 — Backend

### Etapa 1 — Modelos + Migrations

Criar os três modelos em `finance/models.py` na ordem de dependência:

1. **`Categoria`** — `nome`, `tipo` (DEFAULT/CUSTOM), `usuario` (nullable FK)
2. **`Periodo`** — `mes`, `ano`, `saldo_carteira`, `saldo_disponivel_mes`, `usuario`
3. **`GastoDiario`** — `data`, `valor`, `descricao`, `categoria` (FK), `periodo` (FK)

Incluir migration para popular as categorias default: Lanche, Almoço, Jantar, Mercado, Remédio, Academia, Transporte, Lazer, Outros.

### Etapa 2 — Serializers

`finance/serializers.py`:
- `CategoriaSerializer`
- `PeriodoSerializer`
- `GastoDiarioSerializer` — leitura com `categoria` nested, escrita com `categoria_id`

### Etapa 3 — Views + Router

`finance/views.py` + `finance/urls.py` com `DefaultRouter`:

| ViewSet | Endpoints |
|---|---|
| `CategoriaViewSet` | `GET /categorias/`, `POST /categorias/`, `DELETE /categorias/{id}/` |
| `PeriodoViewSet` | `GET /periodos/`, `POST /periodos/` |
| `GastoDiarioViewSet` (nested) | `GET /periodos/{id}/gastos-diarios/`, `POST`, `PATCH`, `DELETE` |

Decisões:
- GET retorna **lista plana** (mobile agrupa por dia)
- Validar que `data` pertence ao mês/ano do `periodo`

### Etapa 4 — services.py (limite diário)

```python
def calcular_limite_diario(periodo) -> Decimal:
    # limite_hoje = (saldo_disponivel_mes - Σ gastos até ontem) / dias restantes no mês
```

### Etapa 5 — Endpoint de resumo

`GET /periodos/{id}/resumo/` retorna:

```json
{
  "saldo_carteira": "5000.00",
  "saldo_disponivel_mes": "2000.00",
  "total_gasto_mes": "430.00",
  "limite_hoje": "54.28"
}
```

### Etapa 6 — Testes manuais

Subir via `docker-compose up`, testar todos os endpoints com autenticação JWT.

---

## Fase 2 — Mobile

### Etapa 7 — Domain/Data layer (shared)

- `GastoDiario`, `Categoria`, `Periodo` em `domain/model/`
- DTOs em `data/remote/dto/`
- Endpoints Ktor em `data/remote/api/`
- `GastoDiarioRepository` interface + implementação

### Etapa 8 — Use cases

- `CalcularLimiteDiarioUseCase` (delega ao `/periodos/{id}/resumo/`)
- `GetGastosDoDiaUseCase` (agrupa lista plana por data)

### Etapa 9 — Tela Controle Diário

- `DailyViewModel` + `DailyScreen`
- Lista de dias do mês, gastos por dia, limite restante
- Modal de registro: valor + categoria picker + descrição opcional

### Etapa 10 — Tela Categorias

- `CategoryViewModel` + `CategoryScreen`
- Lista de categorias (default + custom)
- Adicionar/remover custom

---

## Ordem de implementação

```
Etapa 1 (models)
    ↓
Etapa 2 (serializers)
    ↓
Etapas 3+4 (views + services)  ← paralelas
    ↓
Etapa 5 (resumo endpoint)
    ↓
Etapa 6 (testes manuais)
    ↓
Etapas 7+8 (KMP domain/data)   ← paralelas
    ↓
Etapas 9+10 (telas)            ← paralelas
```

---

*Plano criado — 2026-06-02*
