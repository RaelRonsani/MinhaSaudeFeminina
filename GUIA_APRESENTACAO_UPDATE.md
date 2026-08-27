# Guia da Apresentação — Update Calendário Menstrual

Documento de apoio pra defender o update de ciclo menstrual na frente do
professor. Complementa o `guia_apresentacao.md` da entrega anterior.

---

## Requisitos vs implementação (checklist do enunciado)

| Requisito | Implementação |
|---|---|
| Registro do início e término do ciclo menstrual | Card "Registrar Menstruação" no Calendário — usuário marca INÍCIO e FIM |
| Armazenamento local | SQLite via SQLDelight (banco `bemvinda.db` no dispositivo) |
| Visualização em calendário | Grid do Calendário pinta dias em vermelho (menstruação), verde (fértil), amarelo (ovulação) |
| Identificação dos períodos menstruais | Dias em vermelho no calendário + Histórico completo em tela dedicada |
| Cálculo e previsão do próximo ciclo | `CalculosCiclo.preverProximoCiclo()` — após 3 ciclos, com base na média pessoal |
| Consulta dos registros já realizados | Card com setas no Perfil + Tela "Meus Ciclos" com detalhes completos |

## Referências científicas usadas nos cálculos

Todas as constantes vieram de fontes reconhecidas — não são chutes. Estão
documentadas em `CalculosCiclo.kt` no topo do arquivo:

**FEBRASGO** (Federação Brasileira das Associações de Ginecologia e Obstetrícia):
> "Ciclo menstrual normal apresenta intervalo que varia entre **24 a 38 dias**;
> duração de até 8 dias."

**Manual MSD (versão saúde para a família):**
> "Ciclos menstruais costumam variar entre 24 e 38 dias. A maioria das mulheres
> não tem ciclos que duram exatamente 28 dias."
> "Sangramento menstrual costuma durar entre 4 a 8 dias."

**Fase lútea** (base do cálculo de ovulação):
> "A fase lútea é a mais constante entre mulheres, com duração de cerca de
> 14 dias, sem grande variação."

**Ovulação:**
> "Em um ciclo médio de 28 dias, a ovulação ocorre em média 14 dias antes
> do início da próxima menstruação."

**Janela fértil:** 5 dias antes da ovulação + o dia da ovulação (espermatozoides
sobrevivem até 5 dias no trato reprodutor; óvulo vive ~24h).

## Fluxo do usuário (o que demonstrar no vídeo/apresentação)

Um roteiro de 4 minutos:

**Minuto 1 — Login e visão geral**
- Abre o app, login com usuário existente
- Passa pelas telas: Notícias, Calendário (vazio ainda), Perfil (sem ciclo)
- Mostra menu "Meus Ciclos" — vazio

**Minuto 2 — Registrar 3 ciclos**
- Vai em Calendário
- Registra Ciclo 1: início 01/09, fim 05/09 → "Ciclo registrado"
- Volta pro calendário, avança pra Outubro
- Registra Ciclo 2: início 29/09, fim 03/10 (intervalo 28 dias, normal)
- Volta ao Perfil — ainda sem previsão
- Registra Ciclo 3: início 27/10, fim 01/11 (intervalo 28 dias)

**Minuto 3 — Ver os resultados**
- Vai ao Perfil
- Mostra "Informações do Ciclo" preenchido:
  - Duração média: 28 dias
  - Duração do período: 5 dias
  - Última menstruação: 27/10
  - **Próximo ciclo previsto: 24/11** ← agora aparece
- Mostra card "Meus Ciclos Registrados" — usa setas pra navegar entre os 3
- Volta ao Calendário → agora dias ficam pintados:
  - Vermelho nos dias marcados
  - Verde e amarelo aparecem calculados pro próximo ciclo (fértil e ovulação)

**Minuto 4 — Alertas e histórico**
- Registra um 4º ciclo com intervalo anormal (ex: 45 dias de intervalo)
- Alerta aparece: "Você menstruou 7 dias mais tarde que o padrão"
- Vai em Menu → Meus Ciclos → mostra tela dedicada:
  - Lista completa com datas
  - Intervalo entre cada ciclo (marcado em vermelho quando fora da faixa)
  - Referência FEBRASGO no rodapé
  - Botão excluir por ciclo

## Perguntas prováveis do professor + respostas prontas

### "Por que armazenar local em vez do Supabase?"
> O enunciado exigiu armazenamento local nesta etapa. Além disso, dados de
> ciclo são altamente sensíveis (saúde íntima) — mantê-los apenas no dispositivo
> reduz superfície de ataque. Um trade-off é que a usuária perde o histórico
> se trocar de celular; sincronização opcional com nuvem está no roadmap.

### "Como faz para calcular a previsão?"
> Uso a média pessoal dos intervalos entre ciclos registrados. Ex: se ela
> registrou 3 ciclos com intervalos de 28 e 30 dias, a média é 29 dias.
> A previsão do próximo é: data do último ciclo + 29 dias. Só ativo com 3
> ciclos registrados porque com menos a média é estatisticamente pobre.

### "E se não tiver ciclos suficientes?"
> Mostro mensagem explícita: "Registre 3+ ciclos" no card do Perfil e uma
> contagem regressiva no Histórico ("2 restantes"). Não invento previsão
> com dados insuficientes.

### "Como calcula a fase fértil?"
> Uso a regra clássica: a fase lútea é constante em ~14 dias, então a
> ovulação sempre ocorre 14 dias antes do próximo ciclo. A janela fértil
> são os 5 dias antes da ovulação (sobrevivência do espermatozoide) + o
> dia da ovulação em si.

### "O que aciona o alerta?"
> Comparo o intervalo entre os DOIS últimos ciclos registrados. Se estiver
> fora da faixa 24-38 dias definida pela FEBRASGO, mostro alerta com o
> desvio em dias e a recomendação de procurar médico se persistir.

### "Por que SQLDelight e não Room?"
> Room só existe no Android. Como o app é multiplataforma (Compose
> Multiplatform), preciso de um ORM que funcione em Android E iOS com o
> mesmo código. SQLDelight compila SQL puro em Kotlin comum, gerando
> drivers específicos para cada plataforma (AndroidSqliteDriver e
> NativeSqliteDriver).

### "Como funciona a sobreposição de ciclos?"
> Antes de inserir um ciclo novo, chamo `encontrarSobreposicao()` que
> verifica se as datas conflitam com algum ciclo existente do mesmo
> usuário. Se conflita, mostro dialog: "Já existe ciclo registrado nessas
> datas. Deseja substituir?" Se confirmar, uso uma transaction do
> SQLDelight — delete + insert atômicos, sem risco de deixar o banco
> inconsistente.

### "Isso substitui uma consulta médica?"
> Não, e o próprio app é explícito sobre isso: as notícias já tinham
> disclaimer "não substitui avaliação médica". Os alertas de desvio
> recomendam procurar médico. É uma ferramenta de acompanhamento pessoal,
> não diagnóstica.

## Arquitetura desta feature (para desenhar num slide)

```
[UI Layer]
├── CalendarioScreen.kt     → marca dias, adiciona ciclo
├── PerfilScreen.kt         → mostra estatísticas + navegador de ciclos
├── HistoricoCiclosScreen.kt → tela dedicada de consulta detalhada
└── InicialScreen.kt        → menu com "Meus Ciclos"
             ↓
[Domain Layer]
├── CalculosCiclo.kt        → regras de negócio (média, previsão, fase, alerta)
└── Ciclo.kt / Fluxo / FaseCiclo → modelos
             ↓
[Data Layer]
├── CicloRepository.kt      → CRUD (suspend, roda em Dispatchers.Default)
├── Banco.kt                → singleton do BemVindaDb (gerado por SQLDelight)
└── DriverFactory (expect/actual) → driver nativo por plataforma
             ↓
        SQLite local (bemvinda.db)
```

**Ponto forte:** camada de UI não conhece SQLDelight. Se um dia migrar pra
Room ou Realm, muda só o `CicloRepository`, a UI continua igual.
