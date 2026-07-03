# Maranata Music — App de Escalas Musicais

> Documento vivo. Atualize sempre que uma decisão arquitetural mudar.
> Este arquivo existe pra dar contexto imediato a qualquer sessão nova do Claude Code — leia primeiro, codifique depois.

---

## 1. Visão geral

App mobile (iOS + Android) para o ministério de louvor **Maranata Music** gerenciar escalas mensais de músicos e cantores, substituir uma planilha atual que centraliza escalas mas não suporta interação, confirmação nem substituição fluida.

**Nome de trabalho:** Maranata Music
**Status:** MVP em definição
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
| **Ministro do dia** | Tudo que músico faz + define repertório e tonalidades do culto que vai ministrar + tranca repertório após prazo. |
| **Líder / Administrador** | Cria escalas mensais, aprova substituições fora da janela automática, gerencia cadastro de músicos e instrumentos. |

Um mesmo usuário acumula papéis (a maior parte dos ministros também toca em outros cultos).

---

## 4. Escopo do MVP (3 fases)

### Fase 1 — Fundação
- Login (email + senha, JWT).
- Cadastro básico de músicos (nome, instrumentos, função vocal).
- Visualizar escalas do mês (calendário + lista).
- Confirmar presença em culto escalado.
- Notificação push básica (escalação, lembrete 24h antes).

### Fase 2 — Ministro do dia
- Definir repertório do culto (adicionar músicas).
- Definir tonalidade por música.
- Marcar função de cada música (abertura, adoração, oferta, ministração).
- Lembrete configurável (ensaio, chegada antecipada).
- Trancar repertório (após esse momento, alterações precisam de aprovação).

### Fase 3 — Substituição
- Solicitar substituição com motivo (viagem, saúde, trabalho, outro).
- Sugerir substituto específico OU deixar aberto para todos aptos.
- Cascata automática: se sugerido recusa, próximos disponíveis são notificados.
- Aprovação do líder para solicitações dentro da janela crítica (< 24h).
- Ministro do dia sempre recebe cópia da solicitação e do desfecho.

**Fora do MVP (backlog):** integração com Spotify/YouTube pra playlist do repertório, transposição automática de tonalidade, cifras anexadas, histórico de músicas mais tocadas, estatísticas por músico.

---

## 5. Stack técnica

### Backend
- **Java 17 + Spring Boot 3.x**
- **PostgreSQL** (produção) / H2 (testes)
- **Spring Security + JWT** para autenticação
- **Flyway** para migrations
- **JUnit 5 + Mockito** para testes
- **Maven** como build tool
- **Firebase Cloud Messaging (FCM)** para push notifications (unificado iOS/Android)

### Mobile
- **Decisão pendente:** React Native (Expo) OU PWA (React + Vite)
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
│   │   ├── domain/         ← entidades JPA
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
- `id`, `nome`, `email`, `senha_hash`, `telefone`, `ativo`
- `papeis` (SET: MUSICO, MINISTRO, LIDER)
- `fcm_token` (para push notifications)

### Instrumento
- `id`, `nome` (ex: Vocal Feminino, Violão, Bateria, Baixo, Teclado)
- `categoria` (VOCAL, CORDA, PERCUSSAO, TECLA, SOPRO)

### MusicoInstrumento (N-N)
- `usuario_id`, `instrumento_id`, `principal` (boolean)

### Culto
- `id`, `data_hora`, `tipo` (DOMINGO_MANHA, DOMINGO_NOITE, QUARTA, ESPECIAL)
- `ministro_id` (FK → Usuario)
- `repertorio_trancado` (boolean)
- `observacoes`

### Escala
- `id`, `culto_id`, `usuario_id`, `instrumento_id`
- `status` (PENDENTE, CONFIRMADA, RECUSADA, SUBSTITUIDA)
- `confirmada_em`

### Musica
- `id`, `titulo`, `artista`, `tonalidade_base`, `bpm`, `letra_url`, `cifra_url`
- (biblioteca reutilizável do ministério)

### MusicaCulto (repertório do culto)
- `id`, `culto_id`, `musica_id`, `tonalidade_execucao`, `ordem`, `funcao` (ABERTURA, ADORACAO, OFERTA, MINISTRACAO, FINAL)

### SolicitacaoSubstituicao
- `id`, `escala_id`, `solicitante_id`, `motivo` (VIAGEM, SAUDE, TRABALHO, OUTRO), `observacao`
- `substituto_sugerido_id` (nullable — se null, é aberta)
- `status` (ABERTA, ACEITA, RECUSADA, CANCELADA, EXPIRADA)
- `criada_em`, `resolvida_em`, `substituto_final_id`
- `requer_aprovacao_lider` (boolean)

### NotificacaoSubstituicao (cascata)
- `id`, `solicitacao_id`, `notificado_id`, `enviada_em`, `resposta` (ACEITOU, RECUSOU, SEM_RESPOSTA), `respondida_em`
- `ordem_cascata` (INT)

### Lembrete
- `id`, `culto_id`, `titulo`, `mensagem`, `disparar_em`

---

## 8. Regras de negócio (críticas)

### Janela de substituição
| Antecedência | Comportamento |
|---|---|
| > 72h | Substituição aprovada automaticamente após aceite do substituto |
| 24h – 72h | Requer aprovação do ministro do dia |
| < 24h | Requer aprovação do líder + marcada como emergência |

### Cascata de notificação
1. Se solicitante sugeriu substituto específico → só ele é notificado (janela: 12h).
2. Se recusar ou não responder em 12h → sistema busca todos os músicos com o mesmo instrumento, sem escala no dia, ordenados por "mais tempo sem tocar".
3. Notifica em lotes de 3 a cada 6h até alguém aceitar ou esgotar a lista.
4. Se esgotar sem aceite → líder é notificado para intervenção manual.

### Trancar repertório
- Ministro do dia pode trancar o repertório a qualquer momento.
- Após trancado, alterações exigem que ministro destranque (com aviso automático pra equipe).
- Auto-trancamento configurável (padrão: 24h antes do culto).

### Confirmação de presença
- Escalação envia notificação imediata.
- Lembrete de confirmação: 7 dias antes, 3 dias antes, 24h antes.
- Não confirmar até 48h antes escala automaticamente marca como "pendente crítico" e avisa o líder.

### Filtro de substituto elegível
Um usuário só é sugerido como substituto se:
- Toca o mesmo instrumento da escala original (E é sua função `principal` OU secundária).
- Não está escalado em outro culto que conflita horário.
- Está com `ativo = true`.
- Não recusou mais de 3 substituições consecutivas nos últimos 60 dias (sinaliza pro líder revisar engajamento).

---

## 9. Endpoints REST (esboço)

```
POST   /api/auth/login
POST   /api/auth/registro

GET    /api/cultos?mes=2026-07
GET    /api/cultos/{id}
PATCH  /api/cultos/{id}/trancar-repertorio
PATCH  /api/cultos/{id}/destrancar-repertorio

GET    /api/cultos/{id}/repertorio
POST   /api/cultos/{id}/repertorio           ← ministro adiciona música
PATCH  /api/cultos/{id}/repertorio/{musicaCultoId}   ← alterar tonalidade/ordem
DELETE /api/cultos/{id}/repertorio/{musicaCultoId}

GET    /api/escalas/minhas
POST   /api/escalas/{id}/confirmar
POST   /api/escalas/{id}/recusar

POST   /api/substituicoes                    ← solicitar
GET    /api/substituicoes/pendentes         ← recebidas pra responder
POST   /api/substituicoes/{id}/aceitar
POST   /api/substituicoes/{id}/recusar
POST   /api/substituicoes/{id}/aprovar      ← líder/ministro

GET    /api/musicos/disponiveis?cultoId=X&instrumentoId=Y

GET    /api/musicas                          ← biblioteca do ministério
POST   /api/musicas
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

## 11. Decisões arquiteturais (ADRs resumidos)

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

> A preencher conforme cada camada for implementada.

### Backend
```bash
cd backend
./mvnw spring-boot:run
# API sobe em http://localhost:8080
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
- **Sem hardcode de credenciais.** Nunca. Sempre `application-{env}.properties` fora do controle de versão + `.env.example` no repo.
- **Convenção de commits:** conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).

---

## 14. Glossário

- **Culto:** evento litúrgico onde a equipe de louvor toca (domingo manhã, domingo noite, quarta, especiais).
- **Escala:** designação de um músico específico para tocar um instrumento específico em um culto específico.
- **Ministro do dia:** músico responsável por liderar o culto — define repertório, tonalidades e ordem.
- **Repertório:** conjunto de músicas do culto, na ordem prevista, com tonalidade de execução.
- **Cascata:** processo automático de notificar substitutos em sequência até alguém aceitar.
- **Janela crítica:** menos de 24h antes do culto — substituições nesse período viram emergência e exigem líder.