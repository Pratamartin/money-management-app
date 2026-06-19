# Plano — Parcelamentos

## Responsabilidade
Registrar uma compra parcelada vinculada a um cartão. Ao criar, propaga automaticamente lançamentos nos meses seguintes.

---

## Model

```python
class Parcelamento(models.Model):
    descricao    = models.CharField(max_length=255)    # "iPhone 15 — 12x"
    valor_total  = models.DecimalField(...)
    num_parcelas = models.PositiveSmallIntegerField()
    parcela_atual = models.PositiveSmallIntegerField() # qual parcela cai neste período
    mes_inicio   = models.DateField()                  # mês da 1ª parcela
    cartao       = models.ForeignKey(DespesaFixa, ...)

    @property
    def valor_parcela(self):
        return self.valor_total / self.num_parcelas
```

---

## Endpoints

| Método | URL | Descrição |
|--------|-----|-----------|
| POST | `/parcelamentos/` | Cria e propaga parcelas nos meses seguintes |
| GET | `/periodos/{id}/parcelamentos/` | Lista parcelamentos ativos no período |
| DELETE | `/parcelamentos/{pid}/` | Remove todas as parcelas futuras desta compra |

### Payload POST
```json
// request
{
  "descricao": "iPhone 15 — 12x",
  "valor_total": 6000.00,
  "num_parcelas": 12,
  "mes_inicio": "2026-06-01",
  "cartao_id": 2
}
// response 201
{
  "id": 10,
  "descricao": "iPhone 15 — 12x",
  "valor_parcela": "500.00",
  "num_parcelas": 12,
  "parcelas_criadas": 12   // quantas foram geradas
}
```

---

## Lógica de propagação (services.py)

```
Para i in range(num_parcelas):
    mes_ref = mes_inicio + i meses
    periodo = Periodo.objects.get_or_create(mes=mes_ref.month, ano=mes_ref.year, usuario=...)
    Parcelamento.objects.create(
        descricao=descricao,
        valor_total=valor_total,
        num_parcelas=num_parcelas,
        parcela_atual=i + 1,
        mes_inicio=mes_inicio,
        cartao=cartao (do período equivalente ou o mesmo?)
    )
```

> **Ponto crítico:** cada mês tem sua própria `DespesaFixa` (cartão) ou o parcelamento referencia o cartão do mês de início?

---

## Arquivos a alterar

```
finance/
├── models.py       ← class Parcelamento
├── serializers.py  ← ParcelamentoSerializer
├── views.py        ← ParcelamentoViewSet
└── services.py     ← propagar_parcelamento(dados, usuario)
```

---

## Pontos de decisão

- [ ] `get_or_create` dos períodos futuros ao propagar — criar períodos automaticamente ou exigir que existam?
- [ ] O cartão do parcelamento é sempre o mesmo objeto `DespesaFixa` ou cada mês tem o seu? (Impacta FK)
- [ ] Ao deletar: remove só as parcelas futuras (a partir de hoje) ou todas?
