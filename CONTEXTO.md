# CONTEXTO — Princípios e Justificativas

> Este arquivo complementa o `PROJETO.md`.
>
> - `PROJETO.md` responde **o quê** e **como** (escopo, entidades, endpoints, stack).
> - `CONTEXTO.md` responde **por quê** (motivações, princípios, restrições, decisões deliberadas).
>
> **Para o Claude Code:** leia este arquivo antes de propor mudanças arquiteturais, refatorações ou "melhorias" que fujam do escopo pedido. Muita coisa aqui está definida deliberadamente — o que parece limitação frequentemente é escolha consciente.

---

## 1. Por que este app existe

### O problema real que estamos resolvendo

O ministério Maranata Music opera com:
- **Uma planilha** compartilhada com as escalas mensais (fonte da verdade).
- **Grupos de WhatsApp** separados pra confirmação de presença, alteração de repertório e busca por substituto.

Isso gera três dores concretas:

**Dor 1 — Informação fragmentada.** Ninguém consegue ter certeza do estado atual só olhando um lugar. A planilha pode estar desatualizada, a confirmação pode ter ficado num grupo que ninguém lê, o pedido de substituição pode ter sido esquecido no meio de 200 mensagens.

**Dor 2 — Substituições opacas e trabalhosas.** Quando alguém não pode tocar, precisa mandar mensagem individual pra vários músicos até alguém aceitar. Sem visibilidade de quem já respondeu, quem tá disponível, quem já foi consultado. O líder muitas vezes só descobre a troca no dia.

**Dor 3 — Repertório de última hora.** Ministro define músicas e tonalidades no dia ou véspera, sem tempo pra equipe estudar. Links de vídeo ficam espalhados. Cifras chegam em foto do WhatsApp.

### O que o app tem que fazer

Substituir a planilha + os grupos de WhatsApp por **um lugar só, sempre atualizado, onde cada pessoa vê exatamente o que precisa fazer**. Nada mais, nada menos.

### O que o app NÃO deve virar

- **Não é rede social.** Sem timeline, sem curtidas, sem comentários (nem em avisos).
- **Não é ferramenta genérica de igreja.** Não vai virar app pra secretaria, cadastro de membros, controle financeiro, transmissão de culto. Escopo é ministério de louvor.
- **Não é sistema de gravação/estúdio.** Sem upload de MP3, sem edição de partitura, sem gravador embutido.
- **Não é plataforma multi-tenant.** Um ministério só, não estamos criando SaaS pra igrejas em geral.

Se surgir tentação de "e se a gente aproveitasse pra também..." — a resposta padrão é **não**. Escopo estreito é feature, não limitação.

---

## 2. Por que login individual é obrigatório

Login por usuário é **fundação de tudo**. Não é opcional, não é "vamos ver depois". Sem identidade autenticada, nenhuma das funcionalidades principais funciona:

- **Confirmar presença** precisa saber quem confirmou.
- **Aprovar substituição** precisa verificar se o usuário tem papel `LIDER`.
- **Editar repertório** precisa verificar se o usuário é ministro daquele culto.
- **Solicitar substituição** precisa registrar `solicitante_id`.
- **Notificação push** precisa de destinatário (`usuario.fcm_token`).
- **"Minhas escalas"** precisa saber quem é "eu".

Login não é fricção evitável — é a fundação da personalização. O que **pode** ser suavizado:

- Fluxo "criar senha na primeira vez" em vez de "criar conta do zero" (líder pré-cadastra emails via importação da planilha).
- Login com Google/Apple no futuro (menos senha pra lembrar).
- Link mágico por email como backup (fica no backlog).

Mas eliminar login por completo transformaria o app em outra coisa — provavelmente uma versão pior da planilha.

---

## 3. Papéis: por que três?

O modelo tem exatamente três papéis: **MUSICO**, **MINISTRO**, **LIDER**. Foi tentador ter mais (secretária, tesoureiro, cantor vs instrumentista), mas cada papel adicional é mais lógica de autorização, mais telas condicionais, mais teste. Três cobre o essencial:

- **MUSICO** — todo mundo tem. Vê escala, confirma presença, pede substituição.
- **MINISTRO** — quem lidera o culto naquele dia. **Não é cargo permanente** — é situacional. Uma pessoa é ministro do culto de domingo à noite; na quarta ela toca vocal comum. Marcamos via `ministro_id` na tabela `culto`, não como papel no usuário.
- **LIDER** — administração real do ministério. Pequeno grupo (2-5 pessoas). Aprova substituição, posta aviso geral, gerencia cadastro.

**Ministro do dia não aprova substituição.** Isso foi decisão explícita: aprovação de substituição é ato administrativo, não litúrgico. O ministro do dia está preocupado com o culto acontecer bem, não com processo interno. Líder é quem cuida da estrutura.

---

## 4. Por que Java + Spring Boot no backend

A escolha técnica não foi só técnica — foi **estratégica**.

**Contexto pessoal.** A Julliana está em transição de carreira: analista de sistemas rumo a Junior Java Developer remoto. Meta imediata: vaga na BairesDev. Todo o esforço técnico do projeto deve exercitar exatamente o que vai ser cobrado em entrevista técnica:

- Spring Boot, Spring Data JPA, Spring Security, Flyway
- OOP moderno com Java 17 (records, sealed classes, switch expressions quando fizerem sentido)
- Collections, Streams, Optional, exception handling
- Testes com JUnit 5 + Mockito
- Padrões arquiteturais limpos (camadas, DTOs, separação de domínio)

Portanto: **o backend não é só backend, é peça de portfólio**. Isso muda o critério de decisão em vários momentos:

- Preferir Java 17 records a Lombok — demonstra conhecimento de Java moderno, evita "magia".
- Escrever `equals`/`hashCode` explícito, não gerado por biblioteca — mostra que entende as armadilhas.
- Usar `@ElementCollection` corretamente em vez de gambiarra com `@AttributeConverter` — decisão de modelagem relacional real.
- Testar as queries customizadas com casos positivos E negativos — cobertura defensiva.
- Documentar decisões em ADRs — sinaliza maturidade de senioridade.

Se aparecer uma dúvida do tipo "usar biblioteca X que faz mágica" vs "escrever mais código explícito", **prefira código explícito**. É peça de portfólio, não startup em pressão de time-to-market.

---

## 5. Por que PWA antes de React Native

O `PROJETO.md` menciona que essa é decisão pendente (ADR-001), com recomendação inicial PWA. O motivo por trás:

**Tempo pra validar.** O ministério vai começar a usar o app pra valer. Se ele não resolver a dor real, precisa iterar rápido — trocar tela, ajustar fluxo, corrigir bug. PWA atualiza instantâneo: você faz o deploy no Vercel e todo mundo tem a nova versão no próximo refresh. React Native precisa passar por review da Apple (2-7 dias) e Google Play (horas a dias).

**Custo.** Apple Developer Account custa US$ 99/ano. Pra um projeto de portfólio em fase inicial, esse dinheiro faz falta em outros lugares (domínio, hospedagem paga, cursos).

**Aprendizado incremental.** PWA usa React. Se depois migrar pra React Native, a maior parte do código de componentes e lógica de negócio reaproveita. A curva de aprendizado é gradual, não uma segunda stack completamente nova.

**Ressalva.** Push notification em PWA no iOS só funciona a partir do iOS 16.4 (março 2023). Se o ministério tiver muita gente em iPhone antigo, isso é limitação real. Nesse caso, migrar pra React Native fica prioritário.

---

## 6. Por que sem Lombok

Decisão consciente, não "esquecimento".

**Razão de portfólio.** Em entrevista técnica pra Junior Java, é comum o entrevistador pedir "me mostra você escrevendo um getter/setter" ou "explica equals/hashCode". Quem depende de `@Data` da Lombok gagueja. Escrever manualmente demonstra domínio.

**Razão técnica.** Lombok manipula bytecode em tempo de compilação. Isso funciona bem 99% do tempo, mas quando dá problema o erro é obscuro ("`getName()` not found" em código que aparentemente tem `@Getter`). Debug de anotação de terceiros custa mais tempo que o código economizado.

**Razão pedagógica.** Records (Java 17) já resolvem 80% do caso de uso de Lombok pra DTOs — imutabilidade, equals/hashCode/toString gerados. Usar records nos DTOs e getter/setter explícito nas entidades JPA demonstra Java moderno de verdade.

**Consequência prática.** Entidades JPA vão ter ~20 linhas de getter/setter cada. É repetitivo, mas é código óbvio que ninguém precisa ler duas vezes. Aceitável.

---

## 7. Por que Flyway com nomes de tabela em singular

**Flyway.** Migration versionada é padrão em qualquer projeto Spring sério. Alternativas (Liquibase, `ddl-auto: update` do Hibernate) foram descartadas: Liquibase é mais verboso, `ddl-auto: update` em produção é receita pra desastre.

**Nomes singulares.** Duas escolas: singular (`usuario`, `culto`) ou plural (`usuarios`, `cultos`). Escolhemos singular porque:
- Bate 1:1 com o nome da entidade JPA. `@Entity Usuario` → tabela `usuario`.
- Evita ambiguidade em tabelas de junção. `usuario_papel` é claro; `usuarios_papeis` fica esquisito.
- Convenção Oracle/PostgreSQL tradicional é singular (Rails popularizou o plural, mas fora do ecossistema Ruby é minoria).

**Consequência prática.** Toda entidade tem `@Table(name="...")` explícito, mesmo quando o default do Hibernate daria o mesmo resultado. Não confiar em convenção implícita.

---

## 8. Por que aprovação de substituição é sempre por líder

O modelo original tinha janela de 72h: substituições feitas com antecedência maior que 72h eram aprovadas automaticamente. Isso foi **descartado** durante o refinamento.

Motivos:

**Controle real.** O ministério quer visibilidade de todas as trocas, não só as de última hora. Janela automática cria pontos cegos.

**Simplicidade de modelo.** Duas regras (automática vs líder) exigem branching lógico em vários lugares: notificação, tela, timeline, auditoria. Uma regra só (sempre líder) elimina toda essa complexidade.

**Cadeia de responsabilidade clara.** Se algo dá errado (músico não aparece no culto), o líder que aprovou a substituição tem responsabilidade. Aprovação automática dilui isso.

**Contrapartida da simplicidade.** Sistema precisa notificar líderes de forma efetiva (ciclo de 1 hora até alguém agir). Sem isso, substituição fica pendurada e vira pior que o WhatsApp.

---

## 9. Por que biblioteca de músicas auto-alimentada

Poderia ter uma tela "gerenciar biblioteca" onde alguém cadastra músicas manualmente. **Descartado.** Ninguém quer trabalho administrativo repetitivo. A biblioteca **aprende** com o uso:

- Primeira vez que ministro adiciona "Grande é o Senhor" a um culto, o registro entra em `Musica` automaticamente.
- Da segunda vez em diante, autocomplete sugere e ele só confirma.
- Se ele quiser alterar link só naquele culto (versão ao vivo específica, por exemplo), sobrepõe via `link_video_override` — a biblioteca central não muda.
- Se quiser atualizar de fato o link padrão, precisa ação explícita ("salvar essa versão como padrão").

**Princípio geral por trás disso:** funcionalidade administrativa nasce como efeito colateral de uso. Se um dado precisa existir, ele emerge naturalmente do fluxo normal — não da adição de uma tela de cadastro.

---

## 10. Por que quadro cromático fixo e não picker livre

Ministros vão escolher paleta de cores pra orientar figurino ou produção visual. Duas opções:

- **Picker livre** (RGB, HEX, o que quiser) — máxima flexibilidade
- **Quadro cromático fixo** (24 cores curadas em grade 6×4) — restrição consciente

Escolhemos **quadro fixo**. Motivos:

- **Evita "azul-piscina-neon com laranja-flúor".** Curadoria protege coerência estética.
- **Escolha rápida.** Picker livre exige decisão fina; grade fixa é dois toques.
- **Nome amigável.** "Bordô", "Off-white" fala mais alto que "#7a1e2e", "#f5f0e8" pra maioria da equipe.
- **Consistência entre cultos.** A paleta do ministério tem identidade previsível.

**Trade-off aceito.** Alguém pode querer uma cor específica que não está na grade. Aceitável — é preço de manter coerência.

---

## 11. Estilo de trabalho da Julliana

Quem vai codar com o Claude Code (via VS Code) precisa saber isso pra alinhar propostas:

**Edições cirúrgicas.** Se um arquivo já funciona, não reescrever ele inteiro — alterar só o necessário. Refatoração ampla é decisão à parte, não efeito colateral de um pedido pequeno.

**Sem over-engineering.** Não criar interface "só por criar", não abstrair camada que tem uma implementação só, não introduzir padrão que não resolve problema real ainda.

**Explicar decisão, não o óbvio.** Comentário em código é pra explicar "por quê", não "o quê". `// incrementa contador` é ruído. `// ordem importa: precisa validar antes de persistir pra evitar registro órfão` é útil.

**Aprender fazendo, não copiando.** Se aparecer conceito novo (padrão de projeto, algoritmo, recurso de linguagem), preferir explicação curta + código do que "aplica isso aqui" sem contexto.

**Pausa antes de escolha grande.** Se o Claude Code identifica ambiguidade ou dilema arquitetural, **para e pergunta**. Não decide sozinho e reporta depois. Foi assim no M2 com o conflito de modelagem `papeis`, e funcionou muito bem.

**Testes verdes antes de commit.** Nada é considerado "pronto" sem `./mvnw test` verde. Se um teste do M1/M2 quebrar por causa de mudança do M3, isso é bloqueio, não "resolvo depois".

**Convenção de commits.** Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`). Mensagens em português. Uma linha resumo, sem descrição extensa a não ser em commits arquiteturais.

---

## 12. Restrições que parecem arbitrárias mas não são

Este é o "leia antes de sugerir mudança" para o Claude Code:

- **Sem Lombok** → seção 6 explica.
- **Sem microservices, sem event sourcing, sem CQRS** → escopo não pede, complexidade acidental sem retorno.
- **Sem GraphQL** → REST simples resolve. GraphQL brilha em telas com dados heterogêneos; aqui as views são previsíveis.
- **Sem NoSQL** → dados são fortemente relacionais (usuário-instrumento, culto-escala-músico). Postgres é escolha óbvia.
- **Sem Docker de aplicação em dev** → `./mvnw spring-boot:run` é suficiente. Docker entra pra Postgres em dev e pra deploy.
- **Sem CI/CD elaborado ainda** → GitHub Actions com `mvn test` no push basta. Sem Sonar, sem Codecov, sem quality gate por enquanto.
- **Sem monitoring/APM** → não temos escala pra justificar. Actuator + log estruturado é suficiente. New Relic/Datadog entra quando/se o app crescer.
- **Sem migração pra Kotlin** → Java está na jogada por razão de portfólio (seção 4). Kotlin é ótimo, mas não é o que a Julliana quer estudar agora.

Se aparecer proposta pra adicionar qualquer coisa acima, a resposta padrão é **avisar antes de aplicar**. Todas essas restrições podem ser revistas com discussão; nenhuma deve ser removida silenciosamente.

---

## 13. Sobre este documento

Este arquivo tende a envelhecer mais devagar que o `PROJETO.md`. Princípios raramente mudam; escopo, entidades e endpoints mudam com frequência.

**Quando atualizar CONTEXTO.md:**
- Uma restrição foi revista com discussão (ex: decidimos usar Lombok afinal).
- Uma decisão de princípio foi tomada e vale documentar o porquê.
- Um novo princípio surgiu de aprendizado durante a implementação.

**Quando NÃO atualizar CONTEXTO.md:**
- Adicionar/remover entidade → PROJETO.md.
- Ajustar regra de negócio pontual → PROJETO.md.
- Registrar milestone concluído → PROJETO.md (seção 15).

Se estiver em dúvida entre atualizar aqui ou lá: se a mudança responde **por quê**, é aqui. Se responde **o quê** ou **como**, é lá.