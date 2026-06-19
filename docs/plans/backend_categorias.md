# Plano — Categorias

## Responsabilidade
Classificar gastos diários. Existem categorias padrão (compartilhadas por todos) e categorias custom (por usuário).

---

## Model

```python
class Categoria(models.Model):
    class Tipo(TextChoices):
        DEFAULT = "DEFAULT"
        CUSTOM  = "CUSTOM"

    nome    = models.CharField(max_length=100)
    tipo    = models.CharField(..., default=DEFAULT)
    usuario = models.ForeignKey(User, null=True, blank=True, ...)
    # usuario=null → categoria DEFAULT (global)

    class Meta:
        unique_together = ("nome", "usuario")
```

---

## Defaults (seed via migration ou management command)

```
Lanche, Almoço, Jantar, Mercado, Remédio, Academia, Transporte, Lazer, Outros
```

---

## Endpoints

| Método | URL | Auth | Descrição |
|--------|-----|------|-----------|
| GET | `/categorias/` | ✅ | Lista defaults + custom do usuário |
| POST | `/categorias/` | ✅ | Cria categoria custom |
| DELETE | `/categorias/{id}/` | ✅ | Remove categoria custom (defaults bloqueados) |

### Payload POST
```json
// request
{ "nome": "Pet" }
// response 201
{ "id": 12, "nome": "Pet", "tipo": "CUSTOM" }
```

### GET — resposta combinada
```json
[
  { "id": 1, "nome": "Almoço", "tipo": "DEFAULT" },
  { "id": 2, "nome": "Lanche", "tipo": "DEFAULT" },
  ...
  { "id": 12, "nome": "Pet", "tipo": "CUSTOM" }
]
```

---

## Regras

- DELETE só permitido em categorias com `tipo=CUSTOM` e `usuario=request.user`
- Se uma categoria custom já tem gastos vinculados, bloquear deleção (ou proteger via `on_delete=PROTECT`)
- Na listagem, retornar sempre defaults primeiro, depois custom

---

## Arquivos a alterar

```
finance/
├── models.py       ← class Categoria
├── serializers.py  ← CategoriaSerializer
├── views.py        ← CategoriaViewSet
└── urls.py
```

---

## Pontos de decisão

- [ ] Seed das categorias default: usar `data migration` (mais robusto) ou `management command`?
- [ ] Bloquear delete se categoria tem gastos vinculados (erro 400) ou deixar o `PROTECT` do Django levantar 500?
