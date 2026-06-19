# Plano: Money Flow para Wear OS

## Decisão: mesmo repo, novo módulo `:wear`

Não precisa de um repositório novo. O padrão do Android é criar um módulo separado dentro
do mesmo projeto Gradle. Isso permite:
- Compartilhar código de domínio (models, use cases) eventualmente
- Um único CI/CD e versionamento
- Deploy independente: o `:wear` gera um APK/AAB próprio para a Play Store

---

## Estrutura do projeto após a mudança

```
moneymgtapp/
├── app/          # app do celular (existente, sem alteração)
├── wear/         # app do relógio (novo módulo)
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/pratatec/moneymgtapp/wear/
│           ├── WearMainActivity.kt
│           ├── data/
│           │   └── WearApiClient.kt    # Ktor igual ao app, reutiliza lógica
│           ├── presentation/
│           │   ├── HomeScreen.kt       # resumo do período
│           │   └── AddGastoScreen.kt   # gasto rápido
│           └── tile/
│               └── SummaryTileService.kt  # tile na tela do relógio (opcional)
├── backend/
├── gradle/
│   └── libs.versions.toml
└── settings.gradle.kts
```

---

## Funcionalidades (manter simples)

| Feature | Descrição |
|---|---|
| Resumo do período | Saldo disponível + total gasto hoje |
| Adicionar gasto rápido | Valor (girar coroa) + categoria (lista) + confirmar |
| Tile (opcional) | Widget na tela inicial do relógio com saldo |

Fora do escopo inicial: login no relógio (usa o token salvo no celular via Data Layer),
gráficos, gestão de períodos/categorias.

---

## Autenticação: login exclusivo no celular (decisão definida)

**O relógio nunca exibe tela de login.** O usuário faz login normalmente no app do
celular, e o JWT é enviado automaticamente para o relógio via **Wearable Data Layer**.
Se o token ainda não chegou, o relógio exibe apenas uma mensagem pedindo para abrir o
app no celular.

```kotlin
// No app do celular (app/), após login bem-sucedido:
val client = Wearable.getDataClient(context)
val request = PutDataMapRequest.create("/auth_token").apply {
    dataMap.putString("token", jwtToken)
}.asPutDataRequest().setUrgent()
client.putDataItem(request)

// No wear/, ouvir a chegada do token:
class TokenListenerService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        for (event in events) {
            if (event.dataItem.uri.path == "/auth_token") {
                val token = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap.getString("token")
                WearTokenStorage.save(context, token)
            }
        }
    }
}

// Tela exibida no relógio quando não há token:
@Composable
fun NotAuthenticatedScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Abra o Money Flow no celular para continuar",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2
        )
    }
}
```

---

## Código: wear/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pratatec.moneymgtapp.wear"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pratatec.moneymgtapp.wear"
        minSdk = 30          // Wear OS 3+, cobre Galaxy Watch 4/5/6/7/8
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            buildConfigField("String", "BASE_URL",
                "\"https://money-management-app-production.up.railway.app/\"")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
        }
    }

    buildFeatures { compose = true; buildConfig = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Wear Compose (substitui o Material3 normal)
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)

    // Tiles (opcional)
    implementation(libs.wear.tiles)
    implementation(libs.wear.tiles.material)
    implementation(libs.horologist.tiles)   // helper do Google

    // Data Layer (sync do token com o celular)
    implementation(libs.play.services.wearable)

    // Ktor igual ao app
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
}
```

---

## Código: HomeScreen (resumo)

Wear Compose usa `ScalingLazyColumn` e componentes circulares:

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel, onAddGasto: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Disponível",
                style = MaterialTheme.typography.caption1
            )
        }
        item {
            Text(
                text = "R$ ${state.saldoDisponivel}",
                style = MaterialTheme.typography.display1,
                color = if (state.saldoDisponivel >= 0) Color.Green else Color.Red
            )
        }
        item {
            Text(
                text = "Hoje: R$ ${state.gastoHoje}",
                style = MaterialTheme.typography.body1
            )
        }
        item {
            Button(onClick = onAddGasto) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar gasto")
            }
        }
    }
}
```

---

## Código: AddGastoScreen (input simples)

```kotlin
@Composable
fun AddGastoScreen(
    categorias: List<String>,
    onConfirm: (valor: Double, categoria: String) -> Unit
) {
    var valor by remember { mutableDoubleStateOf(0.0) }
    var categoriaSelecionada by remember { mutableStateOf(categorias.first()) }

    ScalingLazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            // Picker de valor via coroa física (RotaryScrollState)
            Picker(
                state = rememberPickerState(50),
                modifier = Modifier.rotaryScrollable(
                    rememberRotaryScrollableState(),
                    focusRequester = remember { FocusRequester() }
                )
            ) { index ->
                Text("R$ ${index * 5}")  // incrementos de R$5
            }
        }
        item {
            // Lista de categorias
            categorias.forEach { cat ->
                Chip(
                    label = { Text(cat) },
                    onClick = { categoriaSelecionada = cat },
                    colors = if (cat == categoriaSelecionada)
                        ChipDefaults.primaryChipColors()
                    else ChipDefaults.secondaryChipColors()
                )
            }
        }
        item {
            Button(onClick = { onConfirm(valor, categoriaSelecionada) }) {
                Icon(Icons.Default.Check, contentDescription = "Confirmar")
            }
        }
    }
}
```

---

## Libs a adicionar no libs.versions.toml

```toml
[versions]
# ... existentes ...
wearCompose = "1.4.0"
wearTiles  = "1.4.0"
horologist  = "0.6.19"
playServicesWearable = "18.2.0"

[libraries]
# ... existentes ...
wear-compose-material   = { group = "androidx.wear.compose", name = "compose-material",   version.ref = "wearCompose" }
wear-compose-foundation = { group = "androidx.wear.compose", name = "compose-foundation", version.ref = "wearCompose" }
wear-compose-navigation = { group = "androidx.wear.compose", name = "compose-navigation", version.ref = "wearCompose" }
wear-tiles              = { group = "androidx.wear.tiles",   name = "tiles",              version.ref = "wearTiles" }
wear-tiles-material     = { group = "androidx.wear.tiles",   name = "tiles-material",     version.ref = "wearTiles" }
horologist-tiles        = { group = "com.google.android.horologist", name = "horologist-tiles", version.ref = "horologist" }
play-services-wearable  = { group = "com.google.android.gms", name = "play-services-wearable", version.ref = "playServicesWearable" }

[plugins]
# ... existentes (nenhum novo necessário) ...
```

---

## settings.gradle.kts: adicionar o módulo

```kotlin
// linha a adicionar:
include(":app", ":wear")
```

---

## Passos de implementação (ordem sugerida)

1. **Adicionar as libs** no `libs.versions.toml`
2. **Criar o módulo `:wear`** no Android Studio (File → New Module → Wear OS)
3. **Configurar `wear/build.gradle.kts`** conforme acima
4. **Implementar `WearTokenStorage`** + `TokenListenerService` (sync do login)
5. **Reutilizar `WearApiClient`** — copiar `FinanceRepositoryImpl` e `AuthRepositoryImpl`
   do `:app` (por enquanto duplicar está ok; refatorar para `:core` é próximo passo)
6. **`HomeScreen`** com resumo
7. **`AddGastoScreen`** com input de valor + categoria
8. **Tile** com saldo (opcional, mas agrega muito valor)

---

## Sobre a refatoração para `:core` (futuro, não agora)

Quando o wear estiver funcionando e houver duplicação evidente, extrair:

```
core/
├── domain/   → modelos (Gasto, Periodo, Categoria) + interfaces de Repository + UseCases
└── data/     → ktor client + implementações de Repository
```

Tanto `:app` quanto `:wear` dependem de `:core`. Mas isso é refatoração — **não bloqueia**
a entrega do wear.

---

## Novo repo vs. mesmo repo — conclusão

| | Mesmo repo (recomendado) | Repo separado |
|---|---|---|
| Compartilhar código | Fácil, Gradle modules | Publicar como biblioteca |
| CI/CD | Um pipeline só | Dois pipelines |
| Versionamento | Sincronizado | Manual |
| Complexidade inicial | Baixa | Alta |

**Use o mesmo repo.** Criar um novo repo só faria sentido se o wear fosse mantido
por um time diferente ou tivesse ciclo de release totalmente independente.
