# Testando o Money Flow no Galaxy Watch 8

## Opção A — Emulador (sem relógio físico)

### 1. Criar o AVD Wear OS no Android Studio

1. **Tools → Device Manager → + (Add Device)**
2. Selecionar categoria **Wear OS**
3. Escolher **"Wear OS Large Round"** (mais próximo do Watch 8)
4. System image: **Wear OS 4.0 / API 33** — baixar se necessário
5. Finalizar e iniciar o AVD

### 2. Rodar o módulo `:wear`

No Android Studio, selecionar o módulo **`:wear`** no dropdown de configuração de run
(ao lado do botão play) e clicar em Run.

---

## Opção B — Galaxy Watch 8 físico (recomendado para teste real)

### 1. Ativar modo desenvolvedor no relógio

1. No Watch 8: **Configurações → Informações do relógio → Versão do software**
2. Tocar **7 vezes** em "Versão do software" até aparecer "Modo desenvolvedor ativado"
3. Voltar para **Configurações → Opções do desenvolvedor**
4. Ativar **Depuração ADB** e **Depuração por Wi-Fi**

### 2. Conectar via Wi-Fi (sem cabo)

O Watch 8 não tem porta USB, então a conexão é sempre wireless.

```bash
# Descobrir o IP do relógio:

# Configurações → Opções do desenvolvedor → Depuração por Wi-Fi
# Vai mostrar um IP:PORT tipo 192.168.1.X:5555

adb connect 192.168.1.X:5555

# Verificar se apareceu:
adb devices
# Deve listar o relógio como "192.168.1.X:5555  device"
```

### 3. Instalar o APK do `:wear`

```bash
# Na raiz do projeto, gerar o APK debug:
./gradlew :wear:assembleDebug

# Instalar no relógio conectado:
adb -s 192.168.1.X:5555 install wear/build/outputs/apk/debug/wear-debug.apk
```

Ou simplesmente rodar pelo Android Studio com o relógio aparecendo como destino no
dropdown de devices.

---

## Fluxo de teste completo

### Pré-requisito: celular e relógio na mesma rede Wi-Fi e pareados via Galaxy Wearable

```
Celular (app :app)                    Relógio (app :wear)
─────────────────                     ──────────────────
1. Abrir Money Flow
2. Fazer login
   └─ WearTokenSync.push() ────────►  TokenListenerService recebe
                                       e salva o JWT
3.                                    4. Abrir Money Flow no relógio
                                       └─ HomeScreen carrega
                                          período atual + resumo
5.                                    6. Tocar "+ Gasto"
                                          Picker de valor → R$25
                                          Chip → categoria "Alimentação"
                                          Tocar "Salvar"
7. Reabrir app no celular ◄────────── Gasto aparece no histórico
```

### Cenários a verificar

| Cenário | Resultado esperado |
|---|---|
| Login no celular pela primeira vez | Token chega no relógio automaticamente |
| Relógio aberto sem login no celular | Tela "Abra o Money Flow no celular" |
| Mês sem período criado | Tela "Nenhum período para este mês" |
| Adicionar gasto R$50 / Alimentação | Gasto salvo, volta para HomeScreen |
| Swipe da direita para esquerda na tela de gasto | Volta para HomeScreen (padrão Wear OS) |
| Sem internet no relógio | Tela de erro com botão "Tentar novamente" |

---

## Ver logs do relógio

```bash
# Filtrar só logs do app:
adb -s 192.168.1.X:5555 logcat -s "WearFinanceApi" "TokenListenerService" "HomeViewModel"

# Ou tudo do processo:
adb -s 192.168.1.X:5555 logcat --pid=$(adb -s 192.168.1.X:5555 shell pidof com.pratatec.moneymgtapp.wear)
```

---

## Problemas comuns

**`adb connect` não funciona**
- Confirmar que celular e relógio estão na mesma rede Wi-Fi
- Desativar e reativar "Depuração por Wi-Fi" no relógio
- Verificar se o IP mudou (DHCP pode trocar)

**Token não chega no relógio**
- Confirmar que o Galaxy Wearable está conectado e sincronizando
- Fazer logout e login novamente no app do celular
- Verificar nos logs do celular: `adb logcat -s "WearTokenSync"`

**App não aparece no relógio após instalar**
- Pressionar o botão lateral do Watch 8 para abrir a lista de apps
- Rolar até encontrar "Money Flow"

**`INSTALL_FAILED_SDK_VERSION`**
- O relógio precisa estar no Wear OS 3+ (One UI Watch 4+)
- Watch 8 vem com Wear OS 4 de fábrica — não deve ocorrer
