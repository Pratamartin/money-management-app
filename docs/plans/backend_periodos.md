# Plano — Períodos

## Responsabilidade
Representa um mês de controle financeiro. Todas as outras entidades (gastos, recebimentos, despesas) pertencem a um período.

---

## Model

```python
class Periodo(models.Model):
    mes   = models.PositiveSmallIntegerField()        # 1–12
    ano   = models.PositiveSmallIntegerField()        # ex: 2026
    saldo_carteira        = models.DecimalField(...)  # inserido manualmente
    saldo_disponivel_mes  = models.DecimalField(...)  # base do limite diário
    usuario = models.ForeignKey(User, ...)

    class Meta:
        unique_together = ("mes", "ano", "usuario")   # 1 período/mês/usuário
        ordering = ["-ano", "-mes"]
```

---

## Endpoints

| Método | URL | Descrição |
|--------|-----|-----------|
| GET | `/periodos/` | Lista todos os períodos do usuário logado |
| POST | `/periodos/` | Cria um novo período |
| GET | `/periodos/{id}/` | Detalhe de um período |
| PATCH | `/periodos/{id}/` | Atualiza saldo_carteira ou saldo_disponivel_mes |
| GET | `/periodos/{id}/resumo/` | Retorna os totais calculados do mês |

### Payload POST
```json
// request
{ "mes": 6, "ano": 2026, "saldo_carteira": 1500.00, "saldo_disponivel_mes": 2000.00 }
// response 201
{ "id": 3, "mes": 6, "ano": 2026, "saldo_carteira": "1500.00", "saldo_disponivel_mes": "2000.00" }
```

### Resposta GET `/periodos/{id}/resumo/`
```json
{
  "total_recebido": 3000.00,
  "total_despesas_fixas": 800.00,
  "total_gasto_diario": 420.50,
  "saldo_projetado": 1779.50,
  "limite_hoje": 52.30
}
```

---

## Lógica de resumo (services.py)

```
total_recebido        = Σ recebimentos do período
total_despesas_fixas  = Σ despesas fixas do período
total_gasto_diario    = Σ gastos diários do período

limite_hoje =
  (saldo_disponivel_mes - Σ gastos até ontem) / dias_restantes_no_mes
```

---

## Arquivos a criar / alterar

```
finance/
├── models.py       ← class Periodo
├── serializers.py  ← PeriodoSerializer, ResumoSerializer
├── views.py        ← PeriodoViewSet + ResumoView
├── services.py     ← calcular_resumo(periodo)
└── urls.py
```

---

## Pontos de decisão

- [ ] Auto-criar período do mês atual no primeiro login? Ou deixar o app mobile criar manualmente?
