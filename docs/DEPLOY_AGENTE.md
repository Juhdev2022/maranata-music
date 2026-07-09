# Deploy — playbook para agentes (Claude Code / Cursor)

> Procedimento operacional para **agentes de IA**. Humanos: ver também `docs/OPERACAO.md` §3.
>
> Objetivo: após push ou bug em produção, o agente **não para no "faça redeploy manual"** — executa verificação, redeploy e smoke test sozinho quando possível.

---

## 1. Arquitetura de deploy

| Camada | Provedor | URL | Trigger automático |
|--------|----------|-----|-------------------|
| Backend | Render Web Service | `https://maranata-music-api.onrender.com` | Push em `main` |
| Banco | Render Postgres | (interno) | — |
| Frontend PWA | Vercel | `https://maranata-music.vercel.app` | Push em `main` |

Push em `main` dispara os dois deploys. Mesmo assim, Render free pode falhar, ficar em versão antiga ou demorar (cold start 2–5 min). Vercel costuma ser rápido, mas o **PWA cacheia** bundle antigo no dispositivo.

---

## 2. Quando o agente deve intervir

| Situação | Ação |
|----------|------|
| Push acabou de ser feito | Poll health + smoke login + abrir app |
| Health UP mas login 500 | Redeploy Render + aguardar Live |
| UI nova, API retorna "Dados inválidos" em rotas novas | Backend desatualizado → redeploy Render |
| PATCH/DELETE retorna 404 | Backend desatualizado → redeploy Render |
| API OK, app com comportamento antigo | Redeploy Vercel ou teste em contexto sem cache |
| Usuária reportou erro pós-deploy | Fluxo completo §4 |

---

## 3. Pré-push (obrigatório)

```bash
cd backend && ./mvnw test
cd ../mobile && npm run build
git status   # working tree limpa
```

Push **somente** com autorização explícita da Julliana.

---

## 4. Fluxo pós-push (agente executa na ordem)

### 4.1 Aguardar e verificar backend

```bash
# Repetir até 200 ou timeout ~5 min (intervalo 30s)
curl.exe -s https://maranata-music-api.onrender.com/actuator/health
# Esperado: {"status":"UP",...}
```

### 4.2 Smoke API (curl)

```bash
# Login demo líder — esperado 200 + token
curl.exe -s -X POST "https://maranata-music-api.onrender.com/api/auth/login" \
  -H "Content-Type: application/json" \
  --data-binary "{\"email\":\"demo-lider@teste.com\",\"senha\":\"demo123456\"}"
```

Com token, testar rota relevante do pack atual (ex.: PATCH observações, POST escalar só com `usuarioId`).

### 4.3 Se backend falhar → redeploy Render

**Opção A — API (preferida se credenciais no ambiente)**

Variáveis (não commitar; configurar no Cursor/Render):
- `RENDER_API_KEY` — Render Dashboard → Account Settings → API Keys
- `RENDER_SERVICE_ID` — URL do serviço no dashboard (`srv-...`)

```bash
curl.exe -s -X POST "https://api.render.com/v1/services/$RENDER_SERVICE_ID/deploys" \
  -H "Authorization: Bearer $RENDER_API_KEY" \
  -H "Content-Type: application/json" \
  -d "{\"clearCache\":\"clear\"}"
```

Aguardar status **Live** no dashboard ou repetir health até UP.

**Opção B — Playwright MCP (sessão logada no GitHub/Google)**

1. `browser_navigate` → `https://dashboard.render.com`
2. Abrir serviço **maranata-music-api** (ou nome equivalente)
3. Clicar **Manual Deploy** → **Deploy latest commit** (ou Clear build cache & deploy se deploy falhou)
4. Aguardar badge **Live** (poll a página ou health a cada 30s)

### 4.4 Verificar frontend

**Opção A — Playwright / browser MCP (contexto limpo)**

1. `browser_navigate` → `https://maranata-music.vercel.app` (novo contexto / sem cache)
2. Login `demo-lider@teste.com` / `demo123456`
3. Smoke do fluxo alterado (ex.: culto → observações, escalar, remover)

**Opção B — Redeploy Vercel se bundle claramente antigo**

Variáveis opcionais:
- `VERCEL_TOKEN` — Vercel → Settings → Tokens
- `VERCEL_PROJECT_ID` — Project Settings

```bash
curl.exe -s -X POST "https://api.vercel.com/v13/deployments" \
  -H "Authorization: Bearer $VERCEL_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"maranata-music\",\"project\":\"$VERCEL_PROJECT_ID\",\"target\":\"production\",\"gitSource\":{\"type\":\"github\",\"ref\":\"main\"}}"
```

**Opção C — Playwright no dashboard Vercel**

1. `https://vercel.com` → projeto **maranata-music**
2. Aba **Deployments** → último deploy → menu **⋯** → **Redeploy**
3. Abrir app em nova aba anônima e validar

> "F5 no Vercel" = garantir que produção serve o commit atual (redeploy ou hard refresh). Push em `main` já dispara deploy; redeploy manual resolve cache/CDN atrasado.

### 4.5 Reportar à usuária

Resumo em 3–5 linhas:
- Render: Live / redeploy feito / ainda falhando (+ log se houver)
- Vercel: deploy OK / redeploy feito
- Smokes: login OK/FAIL, fluxo X OK/FAIL
- Ação manual restante (só se API e Playwright falharam)

---

## 5. Sintomas comuns × causa

| Sintoma | Causa provável | Fix |
|---------|----------------|-----|
| "Dados inválidos" ao escalar sem instrumento | Backend antigo exige `instrumentoId` | Redeploy Render |
| PATCH observações / DELETE escala → erro genérico | Endpoints não existem no backend antigo | Redeploy Render |
| Login 500 com health UP | Postgres hibernando ou erro runtime | Redeploy Render; checar logs |
| UI correta, API correta, celular errado | PWA cache | Guia anônima ou redeploy Vercel |

---

## 6. O que o agente NÃO faz

- Push sem autorização explícita
- Alterar secrets no git
- `flyway:repair` em produção sem diagnóstico e aviso
- Assumir deploy OK só porque o push terminou

---

## 7. Referência rápida

- Regra Cursor: `.cursor/rules/deploy-producao.mdc`
- Operação humana: `docs/OPERACAO.md`
- Credenciais demo: `demo-lider@teste.com` / `demo123456`
