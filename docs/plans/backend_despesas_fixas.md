# Plano — Despesas Fixas

## Responsabilidade
Faturas de cartão e outras despesas mensais recorrentes com controle de status pago/não pago.

---

## Model

```python
class DespesaFixa(models.Model):
    class Status(TextChoices):
        PAGO    = "PAGO"
        NAO_PAGO = "NAO_PAGO"

    nome             = models.CharField(max_length=100)       # "Cartão Nubank"
    data_vencimento  = models.PositiveSmallIntegerField()     # dia do mês (ex: 15)
    valor            = models.DecimalField(max_digits=12, decimal_places=2)
    status           = models.CharField(..., default=NAO_PAGO)
    periodo          = models.ForeignKey(Periodo, ...)
```

---

## Endpoints

| Método | URL | Descrição |
|--------|-----|-----------|
| GET | `/periodos/{id}/despesas-fixas/` | Lista despesas do período |
| POST | `/periodos/{id}/despesas-fixas/` | Adiciona despesa fixa |
| PATCH | `/periodos/{id}/despesas-fixas/{did}/` | Edita valor, nome, vencimento |
| PATCH | `/despesas-fixas/{did}/status/` | Alterna pago / não pago |
| DELETE | `/periodos/{id}/despesas-fixas/{did}/` | Remove despesa |

### Payload POST
```json
// request
{ "nome": "Cartão Nubank", "data_vencimento": 15, "valor": 450.00 }
// response 201
{ "id": 2, "nome": "Cartão Nubank", "data_vencimento": 15, "valor": "450.00", "status": "NAO_PAGO" }
```

### Payload PATCH status
```json
// request
{ "status": "PAGO" }
// response 200
{ "id": 2, "status": "PAGO" }
```

---

## Relação com Parcelamentos

`DespesaFixa` é o cartão — `Parcelamento` referencia o cartão via FK. O valor da fatura exibida na tela virá da soma do campo `valor` da `DespesaFixa` mais os parcelamentos ativos naquele mês (lógica no resumo do período).

---

## Arquivos a alterar

```
finance/
├── models.py       ← class DespesaFixa
├── serializers.py  ← DespesaFixaSerializer, StatusSerializer
├── views.py        ← DespesaFixaViewSet + StatusUpdateView
└── urls.py
```

---

## Pontos de decisão

- [ ] O `valor` da `DespesaFixa` representa a fatura base (sem parcelamentos) ou a fatura total? Clarificar antes de implementar.
- [ ] O endpoint de status é separado (`/despesas-fixas/{id}/status/`) ou é só um PATCH no próprio recurso?
