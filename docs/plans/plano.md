  # Plano — App de Gerenciamento de Dinheiro

## Contexto

Substituir controle manual feito em Notion por um app real com persistência, cálculos automáticos e boa UX.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Mobile | KMP + Compose Multiplatform (Android + iOS) |
| Backend | Django + Django REST Framework |
| Auth | JWT (SimpleJWT) |
| Banco de dados | PostgreSQL |
| Hospedagem | Railway (backend + DB) |

---

## Domínio

### Entidades

**`Usuario`**
- email, senha (hash)
- nome

**`Periodo`** (mês de controle)
- mês/ano
- saldo_carteira (inserido manualmente)
- saldo_disponivel_mes (definido pelo usuário — base do limite diário)
- usuario

**`Recebimento`**
- data
- valor
- descricao (ex: "40% do salário", "restante + 150")
- periodo

**`DespesaFixa`** (fatura de cartão)
- nome (ex: Cartão ML, Cartão NU)
- data_vencimento
- valor
- status: `PAGO` | `NAO_PAGO`
- periodo

**`Parcelamento`**
- descricao (ex: "iPhone 15 — 12x")
- valor_total
- num_parcelas
- parcela_atual
- valor_parcela (`valor_total / num_parcelas`)
- mes_inicio
- cartao (FK → DespesaFixa)
- gera lançamentos automáticos nos meses seguintes

**`GastoDiario`**
- data
- valor
- descricao (opcional)
- categoria (FK → Categoria)
- periodo

**`Categoria`**
- nome
- tipo: `DEFAULT` | `CUSTOM`
- usuario (nulo se DEFAULT)

**Defaults de categoria:** Lanche, Almoço, Jantar, Mercado, Remédio, Academia, Transporte, Lazer, Outros

### Regra central — Limite diário

```
limite_hoje = (saldo_disponivel_mes - Σ gastos até ontem) / dias restantes no mês
```

Recalculado a cada novo gasto registrado. Dias com gasto acima do limite "roubam" dos dias seguintes.

---

## Telas (fluxo de navegação)

```
Login / Cadastro
        │
        ▼
  ┌─────────────────────────────────┐
  │         Home (mês atual)        │
  │  • Saldo da carteira            │
  │  • Limite diário de hoje        │
  │  • Resumo: gastos vs disponível │
  └──────┬──────────────────────────┘
         │
    ┌────┴─────────────────────────┐
    │                              │
    ▼                              ▼
Controle Mensal              Controle Diário
─────────────────            ─────────────────
• Saldo carteira             • Lista dos dias do mês
• Recebimentos               • Gasto(s) de cada dia
  (+ adicionar)              • Limite restante do dia
• Despesas fixas             • Botão: registrar gasto
  (cartões, status)            → modal: valor + categoria
• Parcelamentos              • Total gasto vs disponível
  (+ adicionar)
• Totais do mês

         │
         ▼
   Gerenciar Categorias
   ─────────────────────
   • Lista (defaults + custom)
   • Adicionar nova categoria
   • Remover categoria custom

         │
         ▼
     Perfil / Config
     ─────────────────
     • Dados do usuário
     • Logout
```

### Detalhe das telas principais

**Home**
- Card: saldo atual da carteira
- Card: limite do dia de hoje (valor + barra de progresso)
- Card: total gasto no mês vs saldo disponível
- Botão rápido: "+ Registrar gasto"
- Navegação: mês anterior / próximo

**Controle Mensal**
- Seção Carteira: campo editável de saldo + campo saldo disponível do mês
- Seção Recebimentos: lista + botão adicionar
- Seção Despesas Fixas: lista de cartões com chip pago/não pago + botão adicionar
- Seção Parcelamentos: lista com parcela atual/total + botão adicionar
- Rodapé: total recebido, total a pagar, saldo projetado

**Controle Diário**
- Lista de dias (1 a 31)
- Cada linha: dia, gastos do dia, categoria, limite restante
- Toque no dia → detalhe com lista de gastos + botão adicionar
- Modal de registro: valor, categoria (picker), descrição opcional

---

## Arquitetura de pastas

### KMP (mobile)

```
moneymgtapp/
├── androidApp/
│   └── src/main/
│       └── MainActivity.kt          ← entry point Android
├── iosApp/
│   └── iosApp/
│       └── ContentView.swift        ← entry point iOS
└── shared/
    └── src/
        ├── commonMain/
        │   ├── domain/
        │   │   ├── model/           ← Periodo, Gasto, Categoria…
        │   │   ├── repository/      ← interfaces (contratos)
        │   │   └── usecase/         ← lógica: calcular limite, agrupar por dia…
        │   ├── data/
        │   │   ├── remote/
        │   │   │   ├── api/         ← Ktor HTTP client, endpoints
        │   │   │   └── dto/         ← request/response DTOs
        │   │   └── repository/      ← implementações dos repositórios
        │   └── ui/
        │       ├── navigation/      ← NavHost, rotas
        │       ├── home/            ← HomeScreen + ViewModel
        │       ├── monthly/         ← MonthlyScreen + ViewModel
        │       ├── daily/           ← DailyScreen + ViewModel
        │       ├── category/        ← CategoryScreen + ViewModel
        │       └── auth/            ← LoginScreen, RegisterScreen
        ├── androidMain/             ← expect/actual Android
        └── iosMain/                 ← expect/actual iOS
```

### Django (backend)

```
backend/
├── manage.py
├── config/
│   ├── settings.py
│   ├── urls.py
│   └── wsgi.py
├── accounts/
│   ├── models.py                    ← User (extend AbstractUser)
│   ├── serializers.py
│   ├── views.py                     ← register, login, refresh token
│   └── urls.py
└── finance/
    ├── models.py                    ← Periodo, Recebimento, DespesaFixa,
    │                                   Parcelamento, GastoDiario, Categoria
    ├── serializers.py
    ├── views.py                     ← ViewSets por entidade
    ├── urls.py
    └── services.py                  ← lógica de limite diário, geração de parcelas
```

### Endpoints principais (REST)

```
POST   /auth/register/
POST   /auth/login/
POST   /auth/token/refresh/

GET    /periodos/
POST   /periodos/
GET    /periodos/{id}/resumo/        ← saldo, limite, totais

GET    /periodos/{id}/recebimentos/
POST   /periodos/{id}/recebimentos/

GET    /periodos/{id}/despesas-fixas/
POST   /periodos/{id}/despesas-fixas/
PATCH  /despesas-fixas/{id}/status/  ← marcar pago/não pago

POST   /parcelamentos/               ← cria e propaga parcelas futuras

GET    /periodos/{id}/gastos-diarios/
POST   /periodos/{id}/gastos-diarios/
DELETE /gastos-diarios/{id}/

GET    /categorias/
POST   /categorias/
DELETE /categorias/{id}/             ← só categorias custom
```

---

## MVP — escopo fechado

| # | Feature |
|---|---|
| 1 | Cadastro e login (JWT) |
| 2 | Criar/navegar períodos mensais |
| 3 | Definir saldo da carteira e saldo disponível do mês |
| 4 | Registrar recebimentos |
| 5 | Registrar despesas fixas (cartões) + marcar pago/não pago |
| 6 | Registrar parcelamentos (propaga meses futuros automaticamente) |
| 7 | Registrar gastos diários com categoria |
| 8 | Calcular e exibir limite diário dinâmico |
| 9 | Tela home com resumo do mês |
| 10 | Gerenciar categorias custom |

## Pós-MVP

- Gráficos de gastos por categoria
- Alertas de limite ultrapassado (push notification)
- Projeção de saldo ao fim do mês
- Exportar relatório mensal (PDF/CSV)
- Modo offline + sync
- Integração bancária (Open Finance)

---

## Próximos passos

1. Inicializar projeto KMP com template Compose Multiplatform
2. Configurar projeto Django + DRF + SimpleJWT
3. Criar modelos e migrations do Django
4. Implementar endpoints de auth
5. Implementar endpoints de finance
6. Deploy inicial no Railway
7. Conectar Ktor ao backend
8. Construir telas: Auth → Home → Mensal → Diário

---

*Plano fechado — 2026-05-27*
