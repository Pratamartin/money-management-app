# Plano — Recebimentos

## Responsabilidade
Registrar entradas de dinheiro dentro de um período (ex: parcelas de salário, freelances).

---

## Model

```python
class Recebimento(models.Model):
    data      = models.DateField()
    valor     = models.DecimalField(max_digits=12, decimal_places=2)
    descricao = models.CharField(max_length=255, blank=True)   # "40% do salário"
    periodo   = models.ForeignKey(Periodo, on_delete=CASCADE, related_name="recebimentos")
```

---

## Endpoints

| Método | URL | Descrição |
|--------|-----|-----------|
| GET | `/periodos/{id}/recebimentos/` | Lista recebimentos do período |
| POST | `/periodos/{id}/recebimentos/` | Registra um recebimento |
| PATCH | `/periodos/{id}/recebimentos/{rid}/` | Edita valor ou descrição |
| DELETE | `/periodos/{id}/recebimentos/{rid}/` | Remove recebimento |

### Payload POST
```json
// request
{ "data": "2026-06-05", "valor": 1200.00, "descricao": "40% do salário" }
// response 201
{ "id": 7, "data": "2026-06-05", "valor": "1200.00", "descricao": "40% do salário" }
```

---

## Regras

- Pertence sempre a um período — a URL já carrega `{periodo_id}`
- Usuário só acessa recebimentos de seus próprios períodos (verificado na view)
- Sem lógica automática: é inserção simples; o resumo do período soma tudo

---

## Arquivos a alterar

```
finance/
├── models.py       ← class Recebimento
├── serializers.py  ← RecebimentoSerializer
├── views.py        ← RecebimentoViewSet (nested no período)
└── urls.py
```

---

## Pontos de decisão

- [ ] Permitir edição (`PATCH`) ou só criar/deletar?
