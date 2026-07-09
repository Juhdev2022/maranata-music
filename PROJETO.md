# Maranata Music — App de Escalas Musicais

> Documento vivo. Atualize sempre que uma decisão arquitetural mudar.
> Este arquivo existe pra dar contexto imediato a qualquer sessão nova do Claude Code — leia primeiro, codifique depois.

---

## 1. Visão geral

App mobile (iOS + Android) para o ministério de louvor **Maranata Music** gerenciar escalas mensais de músicos e cantores, substituir uma planilha atual que centraliza escalas mas não suporta interação, confirmação nem substituição fluida.

**Nome de trabalho:** Maranata Music
**Status:** MVP em definição — Fase 1 em execução (Milestones 1 e 2 concluídos)
**Proprietária:** Julliana (@girl.code2026)

---

## 2. Problema atual

A planilha mensal cumpre o papel de registro, mas gera três atritos:

1. **Comunicação fragmentada.** Confirmações, avisos e trocas acontecem em grupos de WhatsApp separados — informação perdida, mensagens repetidas.
2. **Repertório disperso.** Músicas e tonalidades definidas pelo ministro chegam de última hora, sem histórico consultável.
3. **Substituições manuais e opacas.** Quem não pode tocar precisa mandar mensagem individual buscando substituto, sem visibilidade de quem está disponível.

---

## 3. Usuários e papéis

| Papel | O que faz no app |
|---|---|
| **Músico / Cantor** | Vê escalas, confirma presença, solicita substituição, aceita/recusa convites de substituição. |
| **Ministro do dia** | Tudo que músico faz + define repertório, tonalidades, links de vídeo, paleta de cores, observações do culto + tranca/destranca repertório. **Não aprova substituição.** |
| **Líder / Administrador** | Tudo que ministro do dia faz + aprova solicitações de substituição + posta avisos no quadro geral + gerencia cadastro de músicos e instrumentos. |

Um mesmo usuário acumula papéis (a maior parte dos ministros também toca em outros cultos, e líderes frequentemente são ministros também).

---

## 4. Escopo do MVP (4 fases)

### Fase 1 — Fundação
- Login (email + senha, JWT).
- Cadastro básico de músicos (nome, instrumentos, função vocal).
- Visualizar escalas do mês (calendário + lista).
- Confirmar presença em culto escalado.
- Notificação push básica (escalação, lembrete 24h antes).

### Fase 2 — Ministro do dia
- Definir repertório do culto (adicionar músicas).
- Definir tonalidade por música.
- Adicionar 1 link de vídeo por música (YouTube/Spotify).
- Marcar função de cada música (abertura, adoração, oferta, ministração).
- **Biblioteca de músicas auto-alimentada** — músicas novas entram automaticamente na biblioteca central; ao adicionar música já existente, link e tonalidade base são sugeridos automaticamente da biblioteca, podendo ser sobrescritos pontualmente naquele culto sem alterar o registro central.
- **Paleta de cores do culto** — quadro cromático fixo, ministro seleciona de 1 a 3 cores.
- **Campo de observações do culto** — texto livre, visível para equipe escalada + líderes.
- Trancar/destrancar repertório.

### Fase 3 — Substituição
- Solicitar substituição com motivo (viagem, saúde, trabalho, outro).
- Sugerir substituto específico OU deixar aberto para todos aptos.
- Cascata automática: se sugerido recusa, próximos disponíveis são notificados.
- **Aprovação obrigatória de qualquer líder** — sem janela automática, toda substituição passa por líder (ver seção 8).
- Ministro do dia recebe cópia informativa da solicitação e do desfecho (não aprova).

### Fase 4 — Quadro de avisos
- Líderes postam avisos gerais para todo o ministério.
- Autor define data de expiração ao postar; avisos expirados somem da tela.
- Aviso novo dispara notificação push para todos os usuários ativos.
- Aparece na tela inicial do app (topo da lista de escalas).

**Fora do MVP (backlog):** integração com Spotify/YouTube pra playlist automática, transposição automática de tonalidade, cifras anexadas, histórico de músicas mais tocadas, estatísticas por músico, comentários em avisos.

---

## 5. Stack técnica

### Backend
- **Java 17 + Spring Boot 3.x**
- **PostgreSQL** (produção) / H2 (testes e dev local)
- **Spring Security + JWT** para autenticação
- **Flyway** para migrations
- **JUnit 5 + Mockito** para testes
- **Maven** como build tool
- **Firebase Cloud Messaging (FCM)** para push notifications (unificado iOS/Android)

### Mobile
- **Decisão pendente (ADR-001):** React Native (Expo) OU PWA (React + Vite)
  - **PWA** ganha se: foco é validar rápido com o ministério, evitar custo de Apple Developer Account (US$ 99/ano), atualização instantânea sem review.
  - **React Native** ganha se: precisar de recursos nativos pesados (câmera, biometria, background tasks), publicação nas lojas é importante como marco de portfólio.
- **Recomendação inicial:** começar como PWA. Se validar, migrar core pra React Native reaproveitando componentes React.

### Scripts auxiliares
- **Python 3.11+** para migração da planilha atual (`openpyxl` + `psycopg2`)
- Um script único de import que roda uma vez para popular o histórico.

### Deploy
- **Backend:** Render.com (já dominado — ver TODO app anterior)
- **Banco:** Render Postgres ou Supabase (free tier)
- **Mobile (PWA):** Vercel ou Netlify
- **Mobile (React Native):** Expo EAS Build

---

## 6. Estrutura do repositório

```
maranata-music/
├── PROJETO.md              ← este arquivo (contexto pra Claude Code)
├── README.md               ← apresentação pública do portfólio
├── docs/
│   ├── arquitetura.md
│   ├── modelo-dados.md
│   ├── decisoes/           ← ADRs (Architecture Decision Records)
│   └── screenshots/
├── backend/
│   ├── src/main/java/br/com/maranatamusic/
│   │   ├── domain/         ← entidades JPA, enums
│   │   ├── application/    ← services, casos de uso
│   │   ├── infrastructure/ ← repositories, adapters (FCM, email)
│   │   └── presentation/   ← controllers REST, DTOs
│   ├── src/main/resources/
│   │   └── db/migration/   ← Flyway
│   ├── src/test/
│   └── pom.xml
├── mobile/
│   ├── src/
│   │   ├── screens/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── services/       ← chamadas API
│   │   └── stores/         ← estado global (Zustand)
│   └── package.json
└── scripts/
    ├── importar_planilha.py
    ├── seed_dev.py
    └── requirements.txt
```

---

## 7. Modelo de dados (entidades principais)

### Usuario
- `id`, `nome`, `email` (unique), `senha_hash`, `telefone`, `ativo`
- `papeis` — `Set<Papel>` via tabela `usuario_papel` (SET: MUSICO, MINISTRO, LIDER)
- `fcm_token` (para push notifications — Fase 1 tardia)

### Instrumento
- `id`, `nome` (unique), `categoria` (VOCAL, CORDA, PERCUSSAO, TECLA, SOPRO)

### MusicoInstrumento (N-N com atributo)
- Chave composta: `usuario_id` + `instrumento_id`
- `principal` (boolean) — se é o instrumento principal do músico

### Culto
- `id`, `data_hora`, `tipo` (DOMINGO_MANHA, DOMINGO_NOITE, QUARTA, ESPECIAL)
- `ministro_id` (FK → Usuario, nullable)
- `repertorio_trancado` (boolean, default false)
- `observacoes` (TEXT, nullable) — texto livre do ministro do dia
- `paleta_id` (FK → PaletaCulto, nullable)

### Escala
- `id`, `culto_id`, `usuario_id`, `instrumento_id`
- `status` (PENDENTE, CONFIRMADA, RECUSADA, SUBSTITUIDA)
- `confirmada_em` (nullable)

### Musica (biblioteca central do ministério — Fase 2)
- `id`, `titulo`, `artista`, `tonalidade_base`, `bpm`, `letra_url`, `cifra_url`
- `link_video` (VARCHAR, nullable) — link oficial de referência (YouTube/Spotify)
- `criada_em`, `atualizada_em` (audit fields)

### MusicaCulto (repertório do culto — Fase 2)
- `id`, `culto_id`, `musica_id`, `tonalidade_execucao`, `ordem`, `funcao` (ABERTURA, ADORACAO, OFERTA, MINISTRACAO, FINAL)
- `link_video_override` (VARCHAR, nullable) — se preenchido, sobrepõe o link da biblioteca **apenas neste culto**; não altera o registro central em `Musica`.

### PaletaCulto (Fase 2)
- `id`, `culto_id` (FK único)
- `cor_1`, `cor_2`, `cor_3` (VARCHAR — código da paleta pré-definida; `cor_2` e `cor_3` nullable)

### CorPaleta (Fase 2 — tabela de referência estática)
- `id`, `codigo` (VARCHAR único — ex: "PRETO", "BORDO", "OFF_WHITE")
- `nome_exibicao` (VARCHAR — "Preto", "Bordô", "Off-white")
- `hex` (VARCHAR — "#000000")
- `familia` (ENUM: NEUTRO, TERROSO, PASTEL, VIBRANTE, ESCURO, METALICO)
- `ordem_exibicao` (INT — pra grade visual)
- Populada via migration com ~24 cores pré-definidas (grade 6×4 no app).

### SolicitacaoSubstituicao (Fase 3)
- `id`, `escala_id`, `solicitante_id`, `motivo` (VIAGEM, SAUDE, TRABALHO, OUTRO), `observacao`
- `substituto_sugerido_id` (nullable — se null, é aberta)
- `status` (ABERTA, ACEITA, RECUSADA, CANCELADA, EXPIRADA)
- `criada_em`, `resolvida_em`, `substituto_final_id`
- `notificacao_lider_ultima_em` (TIMESTAMP) — controla ciclo de re-notificação de 1h
- `aprovada_por_id` (FK → Usuario, nullable) — qual líder aprovou
- `aprovada_em` (TIMESTAMP, nullable)

### NotificacaoSubstituicao (Fase 3 — cascata de substituto)
- `id`, `solicitacao_id`, `notificado_id`, `enviada_em`, `resposta` (ACEITOU, RECUSOU, SEM_RESPOSTA), `respondida_em`
- `ordem_cascata` (INT)

### NotificacaoLider (Fase 3 — ciclo de aprovação)
- `id`, `solicitacao_id` (FK), `lider_id` (FK → Usuario)
- `enviada_em` (TIMESTAMP)
- `ciclo` (INT — 1, 2, 3... conforme ciclos de re-notificação de 1h)

### Lembrete
- `id`, `culto_id`, `titulo`, `mensagem`, `disparar_em`

### Aviso (Fase 4)
- `id`, `autor_id` (FK → Usuario com papel LIDER)
- `titulo` (VARCHAR — obrigatório, curto)
- `conteudo` (TEXT — obrigatório)
- `criado_em` (TIMESTAMP)
- `expira_em` (TIMESTAMP — obrigatório, definido pelo autor)
- `ativo` (BOOLEAN — computed: `expira_em > NOW()`, ou soft delete)

---

## 8. Regras de negócio (críticas)

### Substituição de músico — aprovação de líder

**Toda solicitação de substituição exige aprovação de um líder.** Não existe aprovação automática por antecedência.

- Qualquer usuário com papel `LIDER` pode aprovar. Basta o primeiro a agir.
- Ao criar a solicitação, todos os líderes ativos recebem notificação push imediata.
- Se nenhum líder aprovar em **1 hora**, sistema re-notifica todos os líderes ativos novamente (ciclo 2). Repete a cada hora até alguém aprovar ou recusar, ou até o culto acontecer.
- Cada notificação enviada é registrada em `NotificacaoLider` com o número do ciclo — para rastreabilidade e para evitar spam duplicado no mesmo ciclo.
- Ministro do dia **não aprova substituição**. Recebe cópia informativa da solicitação e do desfecho.

### Cascata de notificação de substituto

Este fluxo é sobre encontrar QUEM cobrirá a escala (paralelo à aprovação do líder):

1. Se solicitante sugeriu substituto específico → só ele é notificado (janela: 12h).
2. Se recusar ou não responder em 12h → sistema busca todos os músicos com o mesmo instrumento, sem escala no dia, ordenados por "mais tempo sem tocar".
3. Notifica em lotes de 3 a cada 6h até alguém aceitar ou esgotar a lista.
4. Se esgotar sem aceite → líder é notificado para intervenção manual.

### Trancamento e alteração de repertório

Responsabilidades separadas por papel:

**Ministro do dia pode:**
- Adicionar/remover músicas do repertório
- Definir tonalidade de execução
- Adicionar/alterar link de vídeo (override do link da biblioteca)
- Definir paleta de cores do culto (1 a 3 cores do quadro cromático)
- Escrever observações do culto
- Trancar e destrancar repertório

**Líder pode:**
- Aprovar solicitações de substituição
- Postar avisos no quadro geral (Fase 4)
- Tudo que ministro do dia pode (líder acumula papéis)

**Ministro do dia NÃO pode:**
- Aprovar substituição (nem no seu próprio culto)
- Postar avisos gerais (só líder)

Após trancado, alterações no repertório exigem que o ministro destranque primeiro (com aviso automático pra equipe). Auto-trancamento configurável (padrão: 24h antes do culto).

### Biblioteca de músicas — comportamento

Ao adicionar música ao repertório de um culto:

1. Ministro digita título e artista.
2. Sistema busca match exato (case-insensitive) na tabela `Musica`.
3. **Se encontrou:** carrega `link_video` e `tonalidade_base` como sugestão. Ministro pode aceitar ou sobrescrever pontualmente (`link_video_override` e `tonalidade_execucao` na `MusicaCulto`). A biblioteca central **não é alterada**.
4. **Se não encontrou:** ministro preenche todos os campos manualmente. Ao salvar, novo registro é criado em `Musica` (a biblioteca "aprende").

Atualização do registro central em `Musica` só acontece via ação explícita ("salvar essa versão como padrão da biblioteca") — nunca automaticamente ao editar num culto.

### Confirmação de presença

- Escalação envia notificação imediata.
- Lembrete de confirmação: 7 dias antes, 3 dias antes, 24h antes.
- Não confirmar até 48h antes escala automaticamente marca como "pendente crítico" e avisa o líder.

### Filtro de substituto elegível

Um usuário só é sugerido como substituto se:
- Toca o mesmo instrumento da escala original (E é sua função `principal` OU secundária).
- Não está escalado em outro culto que conflita horário.
- Está com `ativo = true`.
- **Nota:** Não bloqueia automaticamente por recusa. A regra "não pode ter recusado mais de 3 substituições consecutivas em 60 dias" serve como flag para o líder revisar engajamento — o músico ainda aparece como opção, só é destacado visualmente na cascata.

### Quadro de avisos (Fase 4)

- Só usuários com papel `LIDER` podem criar avisos.
- Autor define `expira_em` no momento da criação (data + hora).
- Avisos com `expira_em <= NOW()` não aparecem mais na tela inicial mas continuam no banco (soft archive) — permite auditoria.
- Todo novo aviso dispara notificação push para todos os usuários ativos.
- Avisos aparecem ordenados por `criado_em DESC` (mais recente primeiro).
- Sem edição após publicação (imutável) — pra evitar mudança silenciosa de conteúdo já visto pela equipe. Correção = deletar e postar de novo.

---

## 9. Endpoints REST (esboço)

### Fase 1
```
POST   /api/auth/login
POST   /api/auth/registro

GET    /api/cultos?mes=2026-07
GET    /api/cultos/{id}
POST   /api/cultos                           ← LIDER cria culto
POST   /api/cultos/{id}/escalas              ← LIDER escala músico

GET    /api/escalas/minhas
POST   /api/escalas/{id}/confirmar
POST   /api/escalas/{id}/recusar
```

### Fase 2
```
PATCH  /api/cultos/{id}/trancar-repertorio
PATCH  /api/cultos/{id}/destrancar-repertorio

GET    /api/cultos/{id}/repertorio
POST   /api/cultos/{id}/repertorio           ← ministro adiciona música
PATCH  /api/cultos/{id}/repertorio/{musicaCultoId}   ← alterar tonalidade/ordem/link
DELETE /api/cultos/{id}/repertorio/{musicaCultoId}

PATCH  /api/cultos/{id}/paleta               ← ministro define paleta
PATCH  /api/cultos/{id}/observacoes          ← ministro atualiza observações

GET    /api/musicas                          ← biblioteca do ministério
GET    /api/musicas/buscar?titulo=X&artista=Y   ← autocomplete
POST   /api/musicas
POST   /api/musicas/{id}/atualizar-padrao    ← salvar override como padrão (só ministro do culto)

GET    /api/paletas/cores                    ← lista completa do quadro cromático
```

### Fase 3
```
POST   /api/substituicoes                    ← solicitar
GET    /api/substituicoes/pendentes          ← recebidas pra responder
POST   /api/substituicoes/{id}/aceitar
POST   /api/substituicoes/{id}/recusar
POST   /api/substituicoes/{id}/aprovar       ← APENAS LIDER (403 pra outros)
POST   /api/substituicoes/{id}/rejeitar      ← APENAS LIDER

GET    /api/musicos/disponiveis?cultoId=X&instrumentoId=Y
```

### Fase 4
```
GET    /api/avisos                           ← avisos ativos (não expirados)
GET    /api/avisos/todos                     ← inclui expirados (LIDER)
POST   /api/avisos                           ← criar (só LIDER)
DELETE /api/avisos/{id}                      ← só o autor ou outro LIDER
```

---

## 10. Princípios de design (mobile)

- **Mobile-first, mão única.** Uso principal é no celular, uma mão só, muitas vezes em movimento (a caminho do culto).
- **Ações críticas com dois toques no máximo.** Confirmar presença, ver próxima escala, solicitar substituição.
- **Confirmações não bloqueantes.** Sucessos aparecem como toast discreto ("Presença confirmada"), não como modal.
- **Cores sem cair no clichê "app de igreja".** Paleta neutra com um acento (sugestão: azul escuro ou um bordô suave). Nada de ícones religiosos genéricos.
- **Sentence case, texto direto.** "Confirmar presença", não "Confirmar Minha Presença!".
- **Nomes reais, não avatares abstratos.** Foto ou iniciais em círculo colorido.

---

## 11. Decisões arquiteturais (ADRs)

Documentar em `docs/decisoes/` conforme decisões forem tomadas. Template:

```
# ADR-001: [Título]
Data: YYYY-MM-DD
Status: [Proposto | Aceito | Superado por ADR-XXX]

## Contexto
## Decisão
## Consequências
```

Decisões já em pauta a documentar:
- **ADR-001:** PWA vs React Native como MVP mobile
- **ADR-002:** JWT stateless vs sessão com Redis
- **ADR-003:** FCM único vs FCM + APNs separados
- **ADR-004:** Postgres no Render vs Supabase

---

## 12. Como rodar localmente

### Backend
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# API sobe em http://localhost:8080
# Console H2 em http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:maranatadev, user: sa)
```

### Testes
```bash
cd backend
./mvnw test
```

### Mobile (PWA)
```bash
cd mobile
npm install
npm run dev
# App sobe em http://localhost:5173
```

### Script de importação
```bash
cd scripts
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
python importar_planilha.py --arquivo escalas_2026.xlsx
```

---

## 13. Contexto adicional (pra Claude Code entender o "porquê")

- **Portfólio.** Esse projeto é peça central de portfólio pra vagas Junior Java Developer remote (alvo imediato: BairesDev). Priorizar código limpo, testes, README com screenshots e deploy funcionando > features avançadas.
- **Estilo de código preferido:** edições cirúrgicas, sem reescrever o que já funciona. Comentários só quando o "porquê" não é óbvio pelo código.
- **Aprendizado ativo:** Spring Boot, Collections, algoritmos e OOP — o projeto deve exercitar isso naturalmente, não forçar. Se aparecer oportunidade de usar Streams, Optional, records, ou padrões (Strategy pra cascata de notificação, por exemplo), aproveitar sem exagerar.
- **Sem hardcode de credenciais.** Nunca. Sempre `application-{env}.yml` fora do controle de versão + `.env.example` no repo.
- **Convenção de commits:** conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).
- **Sem Lombok.** Getters/setters explícitos nas entidades JPA, records para DTOs (Java 17). É escolha consciente para portfólio — evita "magia" de anotação e demonstra Java moderno.
- **Nomes de tabela em singular** (`usuario`, `culto`, `escala`) para bater com nome da entidade JPA. `@Table(name="...")` explícito em toda entidade, sem confiar em convenção default do Hibernate.

---

## 14. Glossário

- **Culto:** evento litúrgico onde a equipe de louvor toca (domingo manhã, domingo noite, quarta, especiais).
- **Escala:** designação de um músico específico para tocar um instrumento específico em um culto específico.
- **Ministro do dia:** músico responsável por liderar o culto — define repertório, tonalidades, links, paleta e observações.
- **Líder:** usuário com papel `LIDER` — pode aprovar substituições e postar avisos gerais. Não é sinônimo de ministro do dia.
- **Repertório:** conjunto de músicas do culto, na ordem prevista, com tonalidade de execução.
- **Repertório trancado:** estado onde o ministro finalizou as escolhas; alterações exigem destrancar primeiro.
- **Biblioteca de músicas:** tabela central `Musica` que armazena o catálogo do ministério; auto-alimentada conforme ministros adicionam novas músicas.
- **Cascata de substituto:** processo automático de notificar candidatos a substituto em sequência até alguém aceitar.
- **Ciclo de aprovação:** re-notificação de líderes a cada 1 hora enquanto ninguém aprova uma solicitação de substituição.
- **Paleta cromática:** grade fixa de ~24 cores pré-definidas que o ministro escolhe para orientar figurino/produção do culto.
- **Quadro de avisos:** mural onde líderes postam comunicados gerais para todo o ministério (Fase 4).

---

## 15. Histórico de decisões

### 2026-07-03 — Consolidação após início da Fase 1

Refinamentos identificados durante execução dos Milestones 1 e 2:

- **Regra de aprovação de substituição alterada.** Removida janela automática de 72h; toda substituição passa por líder.
- **Aprovação por qualquer líder** (não requer unanimidade nem quórum). Basta o primeiro a agir.
- **Ciclo de re-notificação de líderes definido em 1 hora** — repete indefinidamente até aprovação ou culto acontecer.
- **Ministro do dia perde poder de aprovar substituição.** Recebe apenas cópia informativa. Papel dele agora é estritamente sobre repertório, tonalidades, paleta e observações.
- **Adicionada paleta de cores do culto** — quadro cromático fixo, 1-3 seleções, tabela `CorPaleta` como referência estática.
- **Adicionado campo de observações do culto** — texto livre, visível para equipe escalada + líderes.
- **Biblioteca de músicas auto-alimentada** — músicas novas entram automaticamente na biblioteca central; overrides pontuais por culto via `link_video_override` na `MusicaCulto`.
- **Cada música da biblioteca ganha campo `link_video`** — 1 link oficial (YouTube/Spotify), pode ser sobrescrito por culto sem alterar registro central.
- **Criada Fase 4: Quadro de avisos.** Só líderes postam, autor define data de expiração, notificação push automática.

Decisões técnicas do M1/M2:

- **PostgreSQL como banco de produção; H2 com `MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE` como banco de teste/dev.** Permite testar migrations Flyway com `BIGSERIAL` sem exigir Postgres local.
- **Sem Lombok.** Getters/setters explícitos, records apenas para DTOs futuros.
- **Nomes de tabelas em singular** (`usuario`, `culto`) — bate com nome de entidade JPA sem `@Table(name=...)` verboso, mas anotação está explícita mesmo assim.
- **Perfis `test` e `dev` separados.** Ambos usam H2, mas o `test` é isolado dos dados de trabalho.
- **`@ElementCollection` para `Set<Papel>` com fetch LAZY** — evita N+1 em queries que retornam lista de `Usuario`. Autenticação (M3) fará query customizada com `JOIN FETCH` quando precisar dos papéis.
- **Chave composta em `MusicoInstrumento` via `@EmbeddedId` + `@MapsId`** — padrão mais idiomático para N-N com atributo extra.
- **V2 (`normalizar_usuario_papel`) criada em desvio de escopo do Milestone 2** — necessária porque a V1 modelou `papeis` como coluna TEXT, e usar `@AttributeConverter` seria gambiarra sem integridade referencial. Como schema estava vazio, a migração de dados foi gratuita.
- **`@AutoConfigureTestDatabase(replace = NONE)` nos testes** — sem isso, `@DataJpaTest` substitui o DataSource pelo H2 default sem `MODE=PostgreSQL`, e `BIGSERIAL` da V1 falha na migração.
- **`equals`/`hashCode` das entidades JPA baseados APENAS em `id` com null-safety via `Objects.equals`** — evita bugs clássicos ao adicionar entidades em `HashSet` antes de persistir.
- **Investigação fechada: `POST /api/instrumentos` com nome acentuado (`"Cajón"`) parecia retornar 500 em produção em 2026-07-07.** Suspeita inicial era bug de encoding/charset no Spring. Investigação de 2026-07-08 (com teste de integração em `InstrumentoControllerIT` cobrindo `Cajón`/`Percussão`/`Órgão`, todos verdes, e reprodução isolada do request real com `--data-binary @arquivo`) mostrou que **não há bug na API** — Jackson lê o corpo em UTF-8 direto dos bytes, independente do `file.encoding` da JVM. O 500 original era falso positivo causado por `curl -d "<texto com acento>"` no Git Bash do Windows, que corrompe o cálculo de `Content-Length`/bytes de argumentos multi-byte passados inline na linha de comando (stack trace real: `HttpMessageNotReadableException: Invalid UTF-8 middle byte`). Usar `curl --data-binary @arquivo.json` (ou qualquer cliente real como o navegador) não reproduz o problema. `"Violão"` (id 1) segue existindo por ter sido inserido via SQL no bootstrap, mas isso não indica limitação da API.

Pendências não resolvidas:

- **Lista final das ~24 cores do quadro cromático.** Definir antes da migration da Fase 2.
- **Match fuzzy na biblioteca de músicas.** Atualmente planejado como match exato case-insensitive; considerar Levenshtein na Fase 2 se aparecerem duplicatas.
- **Ordem entre aprovação de líder e cascata de substituto.** Fluxos rodam em paralelo? Aprovação vem primeiro? Definir antes da Fase 3.