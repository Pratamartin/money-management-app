# Plano — Gastos Diários

## Responsabilidade
Registrar cada gasto do dia com categoria. É o dado central para calcular o limite diário.

---

## Model

```python
class GastoDiario(models.Model):
    data      = models.DateField()
    valor     = models.DecimalField(max_digits=12, decimal_places=2)
    descricao = models.CharField(max_length=255, blank=True)
    categoria = models.ForeignKey(Categoria, on_delete=PROTECT, ...)
    periodo   = models.ForeignKey(Periodo, on_delete=CASCADE, ...)

    class Meta:
        ordering = ["data"]
```

---

## Endpoints

| Método | URL | Descrição |
|--------|-----|-----------|
| GET | `/periodos/{id}/gastos-diarios/` | Lista todos os gastos do período |
| POST | `/periodos/{id}/gastos-diarios/` | Registra um gasto |
| PATCH | `/periodos/{id}/gastos-diarios/{gid}/` | Edita gasto |
| DELETE | `/periodos/{id}/gastos-diarios/{gid}/` | Remove gasto |

### Payload POST
```json
// request
{ "data": "2026-06-10", "valor": 32.50, "categoria_id": 1, "descricao": "Almoço" }
// response 201
{ "id": 55, "data": "2026-06-10", "valor": "32.50", "descricao": "Almoço", "categoria": { "id": 1, "nome": "Almoço" } }
```

### GET — agrupado por dia (opcional)
Retornar lista plana e deixar o mobile agrupar, ou retornar já agrupado:
```json
[
  {
    "data": "2026-06-10",
    "total_dia": 62.50,
    "gastos": [ { "id": 55, "valor": "32.50", ... }, { "id": 56, "valor": "30.00", ... } ]
  }
]
```

---

## Impacto no limite diário

A cada gasto criado/editado/deletado, o endpoint `/periodos/{id}/resumo/` recalcula `limite_hoje` automaticamente (sem cache por ora).

---

## Arquivos a alterar

```
finance/
├── models.py       ← class GastoDiario
├── serializers.py  ← GastoDiarioSerializer
├── views.py        ← GastoDiarioViewSet
└── urls.py
```

---

## Pontos de decisão

- [ ] GET retorna lista plana ou agrupada por dia? (lista plana é mais simples, mobile agrupa)
- [ ] Validar que `data` está dentro do `periodo` (mês/ano batem)?
