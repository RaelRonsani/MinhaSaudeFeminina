# Update 2 — Calendário Menstrual Local

Adiciona ao app **Bem-vinda / MinhaSaudeFeminina** o registro local de
ciclos menstruais, com cálculo e previsão do próximo ciclo, alertas de
desvio da faixa normal (FEBRASGO 24-38 dias), e histórico consultável.

## O que muda

**Novos arquivos** (14 no total):

- `composeApp/src/commonMain/sqldelight/com/bemvinda/app/db/Ciclos.sq`
- `composeApp/src/commonMain/kotlin/com/bemvinda/app/ciclo/Banco.kt`
- `composeApp/src/commonMain/kotlin/com/bemvinda/app/ciclo/Ciclo.kt`
- `composeApp/src/commonMain/kotlin/com/bemvinda/app/ciclo/CalculosCiclo.kt`
- `composeApp/src/commonMain/kotlin/com/bemvinda/app/ciclo/CicloRepository.kt`
- `composeApp/src/androidMain/kotlin/com/bemvinda/app/ciclo/DriverFactory.android.kt`
- `composeApp/src/iosMain/kotlin/com/bemvinda/app/ciclo/DriverFactory.ios.kt`
- `composeApp/src/commonMain/kotlin/com/bemvinda/app/ui/HistoricoCiclosScreen.kt`

**Arquivos alterados** (substituem os existentes):

- `composeApp/src/commonMain/kotlin/com/bemvinda/app/ui/CalendarioScreen.kt`
- `composeApp/src/commonMain/kotlin/com/bemvinda/app/ui/PerfilScreen.kt`
- `composeApp/src/commonMain/kotlin/com/bemvinda/app/ui/InicialScreen.kt`
- `composeApp/src/androidMain/kotlin/com/bemvinda/app/MainActivity.kt`
- `composeApp/src/iosMain/kotlin/com/bemvinda/app/MainViewController.kt`

**Arquivos de build alterados** (ver `BUILD_CHANGES.md`):

- `gradle/libs.versions.toml`
- `build.gradle.kts` (raiz)
- `composeApp/build.gradle.kts`

## Passos de instalação

### 1. Alterar arquivos de build

Siga `BUILD_CHANGES.md` — adicionar dependências SQLDelight ao Gradle.

Depois: **File → Sync Project with Gradle Files** no Android Studio.

O primeiro sync vai demorar (baixa SQLDelight). Se der erro, verifica se
copiou tudo do `BUILD_CHANGES.md`.

### 2. Copiar novos arquivos

Todos os arquivos deste ZIP vão nos caminhos correspondentes dentro
do seu projeto. Os que já existem serão sobrescritos.

**Atenção especial ao arquivo `Ciclos.sq`:**

- Precisa estar em `composeApp/src/commonMain/sqldelight/com/bemvinda/app/db/`
- Se essa pasta não existir, **crie-a** manualmente antes de colar o arquivo.
- SQLDelight vai gerar código Kotlin automaticamente na build a partir dele.

### 3. Rebuild

- **Build → Clean Project**
- **Build → Rebuild Project**

Isso força SQLDelight a regenerar as classes. Se algum arquivo `.kt` reclamar
de `import com.bemvinda.app.db.BemVindaDb` não encontrado, é porque a
geração ainda não rodou — o Rebuild resolve.

### 4. Rodar

Run no emulador. Testa nesta ordem para ver todas as features:

1. Login normalmente.
2. Menu → Calendário → toca em um dia → botão "Marcar INÍCIO" → "Aplicar".
3. Toca em outro dia (4-5 dias depois) → "Marcar FIM" → "Aplicar".
4. Botão "Salvar Ciclo" → deve avisar "Ciclo registrado com sucesso".
5. Volta ao Calendário: os dias que você marcou aparecem em **vermelho**.
6. Repete os passos 2-4 para simular mais dois ciclos (uns 28 dias depois cada).
7. Após o 3º ciclo, o Perfil mostra a **previsão do próximo ciclo**.
8. Se registrar ciclos com intervalo fora de 24-38 dias, aparece alerta.
9. Menu → **Meus Ciclos** → tela dedicada com histórico completo.

## Sobre a persistência

- Banco **local** em `bemvinda.db` (SQLite via SQLDelight).
- Android: fica em `/data/data/com.bemvinda.app/databases/`.
- iOS: fica no diretório de dados do app.
- **Não sincroniza** com Supabase. Trocar de dispositivo = perder histórico.
- **Vinculado ao `email_usuario`** — se dois usuários usam o mesmo celular,
  cada um vê só seus próprios ciclos.

## Referências científicas

Todas documentadas em `CalculosCiclo.kt`:

- **FEBRASGO** — intervalo normal 24-38 dias.
- **Manual MSD** — duração do sangramento 4-8 dias.
- **Tua Saúde / MSD** — fase lútea constante ~14 dias (base do cálculo de
  ovulação).

## Bugs conhecidos

- Se você registrar um ciclo hoje mesmo e outro futuramente, a previsão
  pode ficar confusa. Registro de ciclos deve seguir ordem cronológica real.
- Alerta de desvio compara sempre os DOIS últimos ciclos registrados. Não
  detecta desvios acumulados ao longo de vários meses.
- Bug do dark mode do sistema deixando os campos brancos invisíveis continua
  presente (não é escopo deste update).
