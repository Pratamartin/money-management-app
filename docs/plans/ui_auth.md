# Plano — UI Auth (Login + Cadastro)

## Contexto visual (design)

Tema escuro, minimalista. Logo centralizado na tela de login. Cadastro com back button.
Cor de destaque: verde `#22C55E` (botões, links, ícone do logo).
Fundo: `#111` / `#1A1A1A`. Campos com borda sutil arredondada.

---

## Telas

### 00 · SplashScreen

**Elementos:**
1. Logo centralizado (`MoneyMgtLogo` — ícone + nome)
2. Fundo escuro, sem distrações

**Lógica:**
- Ao iniciar, lê o token salvo no `TokenStorage`
- Se token existe → navega para `appGraph` (Home)
- Se não existe → navega para `authGraph` (Login)
- Nenhum botão — transição automática

---

### 01 · LoginScreen

**Elementos (de cima pra baixo):**
1. Logo — ícone `$` em card verde arredondado, centralizado
2. Título `MoneyMgt` (bold, branco, ~24sp)
3. Subtítulo `Suas finanças, organizadas.` (cinza, ~14sp)
4. Campo **Email** (outlined, label flutuante)
5. Campo **Senha** (outlined, label flutuante, toggle visibilidade 👁)
6. Link `Esqueceu a senha?` alinhado à direita (verde, desativado no MVP)
7. Botão `Entrar` (verde cheio, largura total, arredondado)
8. Rodapé `Não tem conta? Cadastre-se` (cinza + link verde) — navega para RegisterScreen

**Estados:**
- Idle
- Loading (botão com CircularProgressIndicator, campos desabilitados)
- Erro de credenciais (snackbar ou texto vermelho abaixo dos campos)

---

### 02 · RegisterScreen

**Elementos (de cima pra baixo):**
1. Back button `<` no topo esquerdo
2. Logo — ícone `$` pequeno (mesmo componente, menor)
3. Título `Criar conta` (bold, branco, ~24sp)
4. Subtítulo `Leva menos de um minuto.` (cinza, ~14sp)
5. Campo **Nome** (outlined, label flutuante) — nome real, ex: `Martinho`
6. Campo **Usuário** (outlined, label flutuante) — handle único, ex: `pratatec_`
7. Campo **Email** (outlined, label flutuante)
8. Campo **Senha** (outlined, label flutuante, toggle visibilidade 👁)
9. Campo **Confirmar senha** (outlined, label flutuante, toggle visibilidade 👁)
10. Texto legal `Ao se cadastrar, você concorda com nossos Termos e Política de Privacidade.` (cinza, ~12sp)
11. Botão `Criar conta` (verde cheio, largura total, arredondado)
12. Rodapé `Já tem conta? Entrar` (cinza + link verde) — volta para LoginScreen

**Estados:**
- Idle
- Loading (botão com CircularProgressIndicator)
- Erros de validação inline (abaixo de cada campo)
- Erro de servidor (snackbar)

---

## Arquitetura — shared/commonMain

### Estrutura de arquivos

```
shared/src/commonMain/
├── domain/
│   ├── model/
│   │   └── AuthUser.kt            ← data class (id, email, username, nome)
│   ├── repository/
│   │   └── AuthRepository.kt      ← interface
│   └── usecase/
│       ├── LoginUseCase.kt
│       └── RegisterUseCase.kt
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AuthApi.kt         ← funções suspend: login(), register(), refresh()
│   │   │   └── KtorClient.kt      ← cliente Ktor + interceptor de refresh (401)
│   │   └── dto/
│   │       ├── LoginRequest.kt
│   │       ├── LoginResponse.kt
│   │       ├── RegisterRequest.kt
│   │       └── RegisterResponse.kt
│   └── repository/
│       └── AuthRepositoryImpl.kt
└── ui/
    └── auth/
        ├── AuthViewModel.kt        ← LoginUiState + RegisterUiState
        ├── SplashScreen.kt         ← lê token → redireciona
        ├── LoginScreen.kt
        └── RegisterScreen.kt
```

---

## Contratos

### AuthRepository.kt

```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Tokens>
    suspend fun register(email: String, username: String, password: String): Result<AuthUser>
    suspend fun saveTokens(tokens: Tokens)
    suspend fun getAccessToken(): String?
    suspend fun clearTokens()
}
```

### AuthViewModel — UiState

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class RegisterUiState(
    val nome: String = "",           // nome real: "Martinho"
    val username: String = "",       // handle: "pratatec_"
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val error: String? = null,
)

sealed class AuthEvent {
    object NavigateToHome : AuthEvent()
    object NavigateToLogin : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}
```

---

## Validações (client-side)

| Campo | Regra |
|---|---|
| Email | não vazio + formato válido |
| Senha (login) | não vazio |
| Nome (cadastro) | não vazio, mín 2 chars |
| Usuário/handle (cadastro) | não vazio, mín 3 chars, sem espaços |
| Senha (cadastro) | mín 8 chars |
| Confirmar senha | deve ser igual à senha |

Erros exibidos inline (abaixo do campo) ao sair do foco ou ao tentar submeter.

---

## Tokens — persistência

Usar `expect/actual` para armazenamento seguro:
- Android: `EncryptedSharedPreferences`
- iOS: `Keychain`

Interface comum: `TokenStorage` em commonMain.

```kotlin
// commonMain
interface TokenStorage {
    suspend fun save(access: String, refresh: String)
    suspend fun getAccess(): String?
    suspend fun getRefresh(): String?
    suspend fun clear()
}
```

---

## Navegação

```
NavGraph raiz (startDestination = "splash"):
  ├── splash                                  ← lê token, redireciona
  ├── authGraph (startDestination = "login")
  │     ├── login
  │     └── register
  └── appGraph (startDestination = "home")   ← acessado após login ou token válido
```

`SplashScreen` é sempre o ponto de entrada. Ela decide sozinha para onde ir com base no `TokenStorage`.

---

## DTOs ↔ Backend

| DTO | Campo | Backend |
|---|---|---|
| `LoginRequest` | `email`, `password` | `POST /auth/login/` |
| `LoginResponse` | `access`, `refresh` | — |
| `RegisterRequest` | `email`, `username`, `nome`, `password` | `POST /auth/register/` |
| `RegisterResponse` | `id`, `email`, `username`, `nome` | — |

> `username` = handle (`pratatec_`), `nome` = nome real (`Martinho`). São campos distintos no modelo e na tela.

---

## Componentes reutilizáveis a criar

| Componente | Descrição |
|---|---|
| `MoneyMgtLogo` | Ícone `$` + título + subtítulo opcional |
| `EmailField` | OutlinedTextField com validação de email |
| `PasswordField` | OutlinedTextField com toggle visibilidade |
| `PrimaryButton` | Botão verde padrão com suporte a loading state |
| `AuthFooterLink` | Texto cinza + link verde clicável |

Todos em `shared/src/commonMain/ui/components/`.

---

## Ordem de implementação

1. DTOs (`LoginRequest`, `LoginResponse`, `RegisterRequest`, `RegisterResponse`)
2. `KtorClient.kt` — cliente Ktor + interceptor de refresh automático (401)
3. `AuthApi.kt` — funções suspend usando o cliente
4. `TokenStorage` expect/actual (Android: EncryptedSharedPreferences / iOS: Keychain)
5. `AuthRepository` interface + `AuthRepositoryImpl`
6. `LoginUseCase` + `RegisterUseCase`
7. `AuthViewModel`
8. Componentes reutilizáveis (`MoneyMgtLogo`, `PasswordField`, `PrimaryButton`, `AuthFooterLink`)
9. `SplashScreen`
10. `LoginScreen`
11. `RegisterScreen`
12. Conectar ao NavGraph (splash como startDestination)

---

## Pontos de decisão

- [x] ~~`username` ou `nome`?~~ Resolvido: `username` = handle único (ex: `pratatec_`), `nome` = nome real (ex: `Martinho`). Ambos existem no modelo.
- [x] Token refresh automático: **interceptor no Ktor** — captura 401 globalmente, renova o token e repete a requisição sem o resto do app saber.
- [x] Verificação de token: **Splash Screen** — exibe o logo enquanto lê o token salvo, depois navega para Home ou Login.

---

*Plano fechado — 2026-05-29*
