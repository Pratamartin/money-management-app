sim   # Plano de Validação e Próximas Entregas

*Criado em 2026-06-04*

---

## Auditoria: o que está pronto

### Backend — Fase 1 (gastos diários) ✅ COMPLETO

| Etapa | Status | Observação |
|---|---|---|
| Models + Migrations (`Categoria`, `Periodo`, `GastoDiario`) | ✅ | Seed das 9 categorias default incluído |
| Serializers | ✅ | Leitura nested, escrita por `categoria_id` |
| Views + Router (`CategoriaViewSet`, `PeriodoViewSet`, `GastoDiarioViewSet`) | ✅ | Todos os verbos corretos |
| `services.py` (`calcular_limite_diario`, `calcular_total_gasto_mes`) | ✅ | Fórmula correta com dias restantes |
| Endpoint `GET /periodos/{id}/resumo/` | ✅ | Expõe os 4 campos do design |
| Testes manuais com docker-compose | ⚠️ | Não confirmado explicitamente — validar antes de avançar |

### Mobile — Fase 2 ✅ QUASE COMPLETO

| Etapa | Status | Observação |
|---|---|---|
| Domain models (`Categoria`, `GastoDiario`, `GastoDoDia`, `Periodo`, `Resumo`) | ✅ | |
| DTOs + `FinanceApi` + `FinanceRepositoryImpl` | ✅ | |
| Use cases (todos os 8 de finanças) | ✅ | |
| Tela Controle Diário (`DailyScreen` + `DailyViewModel`) | ✅ | Fiel ao design 05a/05b/05c |
| Tela Categorias (`CategoryScreen` + `CategoryViewModel`) | ✅ | Fiel ao design 06a/06b |
| **Tela Home** | ❌ | Apenas `HomeStub` com campos de texto — placeholder |
| Bottom Navigation Bar | ❌ | Não existe — NavGraph atual não tem `NavigationBar` |

---

## O que falta implementar (por prioridade)

### Prioridade 1 — Tela Home (próxima entrega)

**Por quê primeiro:** é a tela central do app. Sem ela o fluxo real de uso não existe — o usuário precisa digitar IDs manualmente no stub.

**Componentes necessários:**

1. **`HomeViewModel`**
   - Ao iniciar, chama `GET /periodos/` e filtra pelo mês/ano atual
   - Se nenhum período existe: exibe estado "sem período" com botão "Criar período"
   - Se período encontrado: chama `GET /periodos/{id}/resumo/` + `GET /periodos/{id}/gastos-diarios/` (filtrado pelo dia de hoje para exibir o gasto do dia)
   - Expõe: `resumo: Resumo?`, `gastoHoje: Double`, `periodoId: Int?`, `isLoading`, `error`, `semPeriodo: Boolean`

2. **`HomeScreen`** (fiel ao design 03):
   ```
   ┌─────────────────────────────────────────┐
   │  [AR]  < Maio 2026 >              ≡     │  ← Avatar + seletor de mês + menu
   ├─────────────────────────────────────────┤
   │  LIMITE DE HOJE          Qua, 27 mai    │
   │  R$ 55,50                de R$ 87,50   │  ← Card destacado (verde)
   │  ████████░░░░░░░░░░░░░░░░░░░░          │  ← Progress bar
   │  Você gastou R$ 32,00 hoje        37%  │
   ├─────────────────────────────────────────┤
   │  📅 SALDO NA CARTEIRA            >      │
   │     R$ 1.240,00                        │  ← Card secundário
   ├─────────────────────────────────────────┤
   │  RESUMO DO MÊS       R$ 2.220,00 total │
   │  Disponível           Gasto            │
   │  R$ 1.800,00          R$ 420,00        │  ← Dois sub-valores
   │  ███░░░░░░░░░░░░░░░░░░░░░░░░░░░        │
   │  19% usado                81% restante │
   └─────────────────────────────────────────┘
              [+ Registrar gasto]              ← FAB
   ```

3. **Estado "sem período":**
   - Card informativo: "Nenhum período para Junho 2026"
   - Botão "Criar período" → bottom sheet com campos `saldo_carteira` + `saldo_disponivel_mes`
   - Chama `POST /periodos/` e recarrega

4. **FAB "Registrar gasto":**
   - Reutiliza `AddGastoSheet` do `DailyScreen` com a data pré-preenchida para hoje
   - Após salvar, recarrega o resumo

---

### Prioridade 2 — Bottom Navigation Bar

**Por quê junto com a Home:** a Home só faz sentido dentro de uma estrutura de navegação com abas. Sem a bottom nav, não há como navegar entre Início/Diário/Mensal/Perfil.

**Estrutura do `NavGraph.kt`:**
- Substituir `HomeStub` por scaffold com `NavigationBar` (4 abas)
- Abas: Início (`home_icon`), Mensal (`calendar_month`), Diário (`today`), Perfil (`person`)
- Telas Mensal e Perfil entram como stubs por ora (implementação futura)
- `DailyScreen` passa a ser acessada pela aba Diário (sem precisar de `periodoId` na URL — HomeViewModel fornece o período ativo via estado global ou passagem direta)

---

### Prioridade 3 — Controle Mensal (requer backend novo)

**Por quê depois:** depende de novos modelos no backend que ainda não existem.

**Backend necessário** (ver `docs/plans/backend_recebimentos.md`, `backend_despesas_fixas.md`, `backend_parcelamentos.md`):
- Modelo `Recebimento` (data, valor, fonte, tipo: SALÁRIO/FREELANCE/OUTRO)
- Modelo `DespesaFixa` (nome, valor, vencimento, status: PAGO/NÃO_PAGO, cartão: FK opcional)
- Modelo `Parcelamento` (nome, valor_parcela, parcela_atual, total_parcelas)
- Endpoints CRUD para cada um, aninhados sob `/periodos/{id}/`

**Mobile:**
- `MonthlyScreen` + `MonthlyViewModel`
- Três seções expansíveis: Recebimentos, Despesas Fixas, Parcelamentos
- Rodapé fixo: Recebido / A Pagar / Projetado

---

### Prioridade 4 — Perfil

**Por quê por último:** menor impacto funcional para o MVP.

- Tela com nome do usuário, e-mail
- Botão "Sair" (logout) — limpa `TokenStorage` e navega para Login
- Possivelmente edição de nome/senha (avaliar escopo)

---

## Dependência técnica: `GetGastosDoDiaUseCase` para hoje

A Home precisa exibir "Você gastou R$ X hoje". O use case `GetGastosDoDiaUseCase` retorna a lista agrupada por dia — a Home pode:
1. Chamar o use case e filtrar pelo dia de hoje (reutilização sem mudança)
2. Ou calcular `gastoHoje` diretamente no `HomeViewModel` a partir da lista retornada

Opção 1 é preferível (sem duplicação).

---

## Ordem de implementação desta fase

```
[Validação manual do backend]
         ↓
[Bottom Navigation Bar (NavGraph refactor)]
         ↓
[HomeViewModel + HomeScreen]
         ↓
[Integração Home → DailyScreen via bottom nav]
         ↓
[Controle Mensal — backend primeiro]
         ↓
[Controle Mensal — mobile]
         ↓
[Perfil]
```

---

## Checklist da entrega Home

- [ ] Validar backend manualmente (docker-compose up + curl nos endpoints principais)
- [ ] Refatorar `NavGraph.kt` para incluir `NavigationBar` com 4 abas
- [ ] Criar `HomeViewModel` com carregamento automático do período do mês atual
- [ ] Criar `HomeScreen` com os 3 cards (limite hoje, saldo carteira, resumo do mês)
- [ ] Implementar estado "sem período" + bottom sheet de criação de período
- [ ] FAB "Registrar gasto" na Home reutilizando `AddGastoSheet`
- [ ] Aba Diário navega direto para `DailyScreen` com o período ativo (sem digitar ID)
- [ ] Smoke test: login → home carrega resumo → registrar gasto → atualiza limite
