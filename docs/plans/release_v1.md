# Plano de Release v1.0 — MoneyMgtApp Android

## Objetivo
Entregar a versão 1.0 do app Android, conectada ao backend de produção (Railway),
com APK assinado disponível no GitHub Releases e pipeline de entregas contínuas via GitHub Actions.

---

## 1. Pré-requisitos

| # | Tarefa | Status |
|---|--------|--------|
| 1 | Backend Django deployado no Railway com banco Postgres | [x] |
| 2 | `BASE_URL` de produção configurado via `BuildConfig` (remover IP hardcoded) | [x] |
| 3 | Keystore de assinatura criada e armazenada com segurança | [ ] |
| 4 | Segredos do GitHub configurados (keystore, senhas, URL de produção) | [ ] |
| 5 | Tela Mensal implementada (ou aceitar stub "em breve" na v1.0) | [ ] |
| 6 | Profile exibindo nome e e-mail real do usuário autenticado | [ ] |
| 7 | `README.md` atualizado com instruções de instalação | [ ] |

---

## 2. Padronização de Versão

### Convenção: Semantic Versioning (semver)

```
MAJOR.MINOR.PATCH
```

| Campo | Quando incrementar |
|-------|-------------------|
| MAJOR | Quebra de compatibilidade com API ou mudança de fluxo principal |
| MINOR | Nova funcionalidade sem quebrar fluxo existente |
| PATCH | Bugfix, ajuste visual, melhoria de performance |

### Exemplos
- `1.0.0` — primeira release pública
- `1.1.0` — tela Mensal implementada
- `1.1.1` — correção de bug no fluxo de login
- `2.0.0` — migração para KMP (iOS incluído)

### `versionCode`
Incrementar manualmente a cada release no `app/build.gradle.kts`.
`versionCode` deve ser sempre crescente — nunca decrementar.

```
v1.0.0 → versionCode = 1
v1.1.0 → versionCode = 2
v1.1.1 → versionCode = 3
```

---

## 3. Configuração do BASE_URL por Ambiente

**Problema atual:** `KtorClient.kt` tem IP local hardcoded (`192.168.0.104:8000`).

**Solução:** `BuildConfig` por `buildType` no `app/build.gradle.kts`.

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"https://SEU-APP.up.railway.app/\"")
    }
    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://SEU-APP.up.railway.app/\"")
            isMinifyEnabled = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true  // habilitar BuildConfig explicitamente
    }
}
```

```kotlin
// KtorClient.kt — após a mudança
const val BASE_URL = BuildConfig.BASE_URL
```

---

## 4. Assinatura do APK

### Criar keystore (executar uma única vez)
```bash
base64 -d /tmp/keystore.b64 > /tmp/test.jks 

JAVA_HOME=/home/pratatec/.local/share/JetBrains/Toolbox/apps/android-studio/jbr/bin/keytool -list -keystore /tmp/test.jks -storepass 'Clara1604*!@' -alias moneymgtapp


```

Guardar o `.jks` fora do repositório. Nunca commitar.

### Configurar assinatura no build.gradle.kts
```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEYSTORE_PATH") ?: "moneymgtapp-release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = true
    }
}
```

---

## 5. GitHub Releases — Disponibilizar APK para Download

### Fluxo de uma release
1. Merge na branch `main` com todas as tarefas da versão concluídas
2. Criar tag: `git tag v1.0.0 && git push origin v1.0.0`
3. GitHub Actions detecta a tag, builda o APK release assinado e publica no GitHub Releases

### Estrutura de branches
```
main          ← sempre estável, reflete produção
feature/*     ← desenvolvimento de funcionalidades
fix/*         ← correções de bugs
release/vX.Y  ← (opcional) branch de estabilização antes de grandes releases
```

---

## 6. Pipeline CI/CD — GitHub Actions

### Arquivo: `.github/workflows/release.yml`

```yaml
name: Release Android APK

on:
  push:
    tags:
      - 'v*.*.*'

jobs:
  build-and-release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > app/moneymgtapp-release.jks

      - name: Build release APK
        env:
          KEYSTORE_PATH: moneymgtapp-release.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          BASE_URL: ${{ secrets.PROD_BASE_URL }}
        run: ./gradlew assembleRelease

      - name: Get tag name
        id: tag
        run: echo "TAG=${GITHUB_REF#refs/tags/}" >> $GITHUB_OUTPUT

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ steps.tag.outputs.TAG }}
          name: "MoneyMgtApp ${{ steps.tag.outputs.TAG }}"
          body: |
            ## O que há de novo
            _Preencher antes de publicar (edite o release no GitHub)_

            ## Instalação
            1. Baixe o arquivo `moneymgtapp-${{ steps.tag.outputs.TAG }}.apk`
            2. No Android, ative "Fontes desconhecidas" nas configurações
            3. Abra o APK e instale
          files: app/build/outputs/apk/release/app-release.apk
          draft: true
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

> O release é criado como **draft** para que você revise e publique manualmente.

### Arquivo: `.github/workflows/ci.yml`

```yaml
name: CI — Build & Lint

on:
  push:
    branches: [ main, 'feature/*', 'fix/*' ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build debug
        run: ./gradlew assembleDebug
      - name: Run unit tests
        run: ./gradlew test
```

---

## 7. Segredos a Configurar no GitHub

Acessar: `Settings → Secrets and variables → Actions → New repository secret`

| Secret | Valor |
|--------|-------|
| `KEYSTORE_BASE64` | `base64 -w0 moneymgtapp-release.jks` |
| `KEYSTORE_PASSWORD` | senha do keystore |
| `KEY_ALIAS` | alias da chave (ex: `moneymgtapp`) |
| `KEY_PASSWORD` | senha da chave |
| `PROD_BASE_URL` | URL do Railway (ex: `https://moneymgt.up.railway.app/`) |

---

## 8. Backend em Produção — Railway

### Variáveis de ambiente a configurar no Railway

```
SECRET_KEY=<chave-aleatória-longa>
DEBUG=False
ALLOWED_HOSTS=moneymgt.up.railway.app
CORS_ALLOWED_ORIGINS=https://moneymgt.up.railway.app
DATABASE_URL=<injetado automaticamente pelo plugin Postgres>
```

### Checklist de deploy
- [ ] `railway up` na pasta `backend/`
- [ ] Adicionar plugin Postgres no Railway
- [ ] Rodar `python manage.py migrate`
- [ ] Criar superusuário de admin
- [ ] Testar endpoint `/auth/token/` com Postman/curl
- [ ] Confirmar que `CORS_ALLOWED_ORIGINS` aceita requisições do app

---

## 9. Checklist Final da v1.0.0

- [ ] `versionCode = 1`, `versionName = "1.0.0"` no `build.gradle.kts`
- [ ] `BASE_URL` usando `BuildConfig` (sem IP hardcoded)
- [ ] Build release local testado e instalado em dispositivo físico
- [ ] Backend Railway respondendo em produção
- [ ] Login, cadastro e fluxo principal testados contra backend de prod
- [ ] Keystore criada e segredos configurados no GitHub
- [ ] Workflows `.github/workflows/release.yml` e `ci.yml` adicionados
- [ ] Tag `v1.0.0` criada e push feito
- [ ] Draft release no GitHub revisado e publicado
- [ ] `README.md` com link de download e instruções

---

## 10. Regras de Entregas Contínuas

### Regra 1 — Nada vai pra `main` quebrado
Todo PR para `main` deve passar no CI (`assembleDebug` + `test`).
Merges diretos sem PR são proibidos exceto hotfix urgente documentado.

### Regra 2 — Versão sempre incrementa em release
Antes de criar uma tag de release, atualizar `versionCode` e `versionName`
no `app/build.gradle.kts`. Nunca reusar uma tag.

### Regra 3 — Release = tag semver
Toda release pública começa com a criação de uma tag `vX.Y.Z`.
O pipeline é disparado automaticamente. Não buildar APK manualmente para distribuição.

### Regra 4 — Draft antes de publicar
O GitHub Release é criado como draft. Revisar o changelog e testar o APK antes
de clicar em "Publish release".

### Regra 5 — Feature flags para trabalho em progresso
Funcionalidades incompletas ficam ocultas via flag ou stub (ex: tela Mensal atual),
não em branch separada bloqueando o merge.

### Regra 6 — Hotfix vai para `main` diretamente
Tag como `v1.0.1`, pipeline sobe o patch. Sem branch `fix/` longa.

---

## Ordem de Execução para Chegar na v1.0.0

```
1. Deploy backend Railway
2. Configurar BuildConfig com URL de prod
3. Criar keystore e configurar secrets no GitHub
4. Criar .github/workflows/release.yml e ci.yml
5. Testar build release local
6. Resolver pontos abertos (profile real, stub mensal decidido)
7. git tag v1.0.0 && git push origin v1.0.0
8. Revisar draft release → Publish
```
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