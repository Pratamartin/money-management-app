# UI Design Prompt — MoneyMgtApp

## Objetivo

Gerar telas de alta fidelidade para um app de gerenciamento financeiro pessoal (Android + iOS).
O design deve transmitir controle, clareza e confiança financeira.

---

## Identidade Visual

### Paleta de Cores

| Role | Hex | Uso |
|---|---|---|
| Primary | `#00C853` | Ações principais, CTAs, valores positivos |
| Primary Dark | `#00952A` | Hover, pressed states, appbar |
| Primary Light | `#5EFC82` | Highlights, badges de limite OK |
| Background | `#0D0D0D` | Fundo geral (dark mode) |
| Surface | `#1A1A1A` | Cards, modais, bottom sheets |
| Surface Variant | `#242424` | Campos de input, seções |
| On Primary | `#FFFFFF` | Texto sobre verde |
| On Background | `#F0F0F0` | Texto principal |
| On Surface Muted | `#888888` | Labels secundários, placeholders |
| Danger | `#FF4444` | Valores negativos, limite ultrapassado |
| Warning | `#FFA726` | Limite próximo do esgotamento |
| Success | `#00C853` | Pago, dentro do limite |

**Tom geral:** dark mode com acentos em verde vivo — transmite dinheiro, modernidade e foco.

### Tipografia

- **Fonte principal:** Inter ou Roboto
- **Valores monetários:** peso Bold/ExtraBold, tamanhos grandes (28–40sp)
- **Labels:** Regular 12–14sp, cor muted
- **Títulos de seção:** SemiBold 16–18sp

### Elevação e Bordas

- Cards: `border-radius: 16dp`, sem sombra pesada — usar borda sutil `1dp #2A2A2A`
- Bottom sheets: `border-radius: 24dp` no topo
- Chips: `border-radius: 100dp` (pill shape)
- Campos de input: `border-radius: 12dp`

---

## Telas a Gerar

---

### 1. Login / Cadastro

**Contexto:** primeira tela do app. Simples, elegante, sem distrações.

**Elementos:**
- Fundo escuro `#0D0D0D`
- Logo/ícone centralizado no topo (símbolo de cifrão estilizado em verde `#00C853`)
- Título "MoneyMgt" ou similar
- Campos: Email, Senha — estilo outlined com borda verde ao focar
- Botão primário: "Entrar" — fundo `#00C853`, texto branco, largura total, `border-radius: 12dp`
- Link: "Não tem conta? Cadastre-se"
- Tela de cadastro: mesmos elementos + campo Nome + confirmação de senha

---

### 2. Home (Mês Atual)

**Contexto:** tela principal. Deve dar uma visão rápida e poderosa do mês.

**Layout — top to bottom:**

1. **AppBar**
   - Ícone menu ou avatar à esquerda
   - Título "Maio 2026" centralizado
   - Setas `<` `>` para navegar entre meses

2. **Card de Limite Diário** (destaque máximo)
   - Fundo `#1A1A1A`, borda-radius 20dp
   - Label pequeno: "LIMITE DE HOJE"
   - Valor grande em verde: "R$ 87,50"
   - Barra de progresso horizontal: verde → amarelo → vermelho conforme uso do dia
   - Subtexto: "Você gastou R$ 32,00 hoje"

3. **Card de Saldo da Carteira**
   - Ícone de carteira
   - Valor: "R$ 1.240,00"
   - Label: "Saldo na carteira"

4. **Card de Resumo do Mês**
   - Dois valores lado a lado:
     - Esquerda: "Disponível" em verde — R$ 1.800,00
     - Direita: "Gasto" em branco/vermelho — R$ 420,00
   - Mini barra de progresso geral do mês

5. **Botão FAB ou Banner**
   - Botão "+ Registrar Gasto" — cor `#00C853`, posicionado no centro-baixo ou como FAB
   - Ícone de `+` à esquerda do texto

6. **Bottom Navigation Bar**
   - 4 ícones: Home (ativo), Mensal, Diário, Perfil
   - Item ativo: ícone + label em verde, demais em cinza

---

### 3. Controle Mensal

**Contexto:** visão completa do mês — recebimentos, despesas fixas e parcelamentos.

**Seções (ScrollView vertical):**

1. **Carteira**
   - Campo editável: saldo atual da carteira
   - Campo editável: saldo disponível para o mês
   - Estilo: dois campos side-by-side em cards pequenos

2. **Recebimentos**
   - Header da seção: "Recebimentos" + botão `+` à direita (ícone verde)
   - Lista: cada item = data | descrição | valor em verde
   - Ex: "25/05 · 40% do salário · R$ 1.200,00"

3. **Despesas Fixas (Cartões)**
   - Header: "Despesas Fixas" + botão `+`
   - Lista: cada item = nome do cartão | vencimento | valor | chip de status
   - Chip "PAGO" → fundo verde claro, texto verde escuro
   - Chip "NÃO PAGO" → fundo vermelho translúcido, texto vermelho

4. **Parcelamentos**
   - Header: "Parcelamentos" + botão `+`
   - Lista: cada item = descrição | parcela X/Y | valor da parcela
   - Ex: "iPhone 15 — 12x · 3/12 · R$ 458,33"

5. **Rodapé fixo (sticky)**
   - Fundo `#1A1A1A`, borda superior sutil
   - "Recebido: R$ 2.400 · A pagar: R$ 890 · Projetado: R$ 1.510"
   - Valores coloridos: recebido em verde, a pagar em vermelho, projetado em branco

---

### 4. Controle Diário

**Contexto:** lista de todos os dias do mês com gastos e limite.

**Layout:**

1. **Header do mês** com total gasto e disponível

2. **Lista de dias** (estilo LazyColumn)
   - Cada linha:
     - Esquerda: número do dia + dia da semana (ex: "15 · Qui")
     - Centro: chips de categorias dos gastos (ícone + valor) ou "—" se vazio
     - Direita: limite restante do dia (verde se positivo, vermelho se estourado)
   - Dia atual: linha destacada com borda verde sutil

3. **Toque em um dia → Bottom Sheet**
   - Título: "Gastos do dia 15"
   - Lista de gastos: categoria | descrição | valor
   - Botão "+ Adicionar gasto" no rodapé do sheet

4. **Modal de Registro de Gasto**
   - Campo: valor (teclado numérico, valor grande centralizado)
   - Picker de categoria: grid de chips com ícone + nome
     - Ícones sugeridos: 🍔 Lanche, 🍽 Almoço, 🌙 Jantar, 🛒 Mercado, 💊 Remédio, 🏋 Academia, 🚌 Transporte, 🎮 Lazer, 📦 Outros
     - Chip selecionado: fundo verde, texto branco
   - Campo opcional: descrição (texto livre)
   - Botão: "Salvar" — verde, largura total

---

### 5. Gerenciar Categorias

**Layout:**

- Lista de categorias com ícone + nome
- Categorias DEFAULT: chip cinza "padrão", sem botão de excluir
- Categorias CUSTOM: chip verde "custom" + ícone de lixeira à direita
- Botão "+ Nova Categoria" no rodapé
- Dialog/Sheet para criação: campo nome + picker de ícone

---

### 6. Perfil / Config

**Layout simples:**

- Avatar com inicial do nome, fundo verde
- Nome e email do usuário
- Opção: "Sair" com ícone de logout, texto em vermelho

---

## Componentes Recorrentes

| Componente | Descrição |
|---|---|
| `MoneyCard` | Card escuro com label + valor monetário destacado |
| `LimitBar` | Barra de progresso colorida (verde→amarelo→vermelho) |
| `StatusChip` | Pill com cor dinâmica: PAGO / NÃO PAGO / dentro/fora do limite |
| `CategoryChip` | Pill com ícone + nome, selecionável |
| `SectionHeader` | Título de seção + botão `+` alinhados |
| `MoneyInput` | Campo grande centralizado para digitação de valor |

---

## Diretrizes Gerais

- Todos os valores negativos ou estouro de limite: `#FF4444`
- Todos os valores positivos ou dentro do limite: `#00C853`
- Sem gradientes chamativos — o verde vivo já é o destaque
- Ícones: estilo outline, peso fino (ex: Material Symbols Outlined)
- Espaçamento generoso: mínimo 16dp de padding horizontal nas telas
- Transições: fade suave entre telas, slide-up para bottom sheets

---

## Formato de Saída Esperado

Gere as telas como **mockups de alta fidelidade** com:
- Especificações de cor em hex
- Medidas em dp
- Variantes: estado vazio, estado carregado, estado de erro
- Versão Android (Material You guidelines) como prioridade
