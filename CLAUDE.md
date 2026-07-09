# Maranata Music — instruções para Claude Code

Leia antes de implementar qualquer feature:

- `PROJETO.md` — escopo, entidades, endpoints
- `CONTEXTO.md` — princípios e restrições
- `docs/OPERACAO.md` — bootstrap e operação humana

## Deploy em produção (sua responsabilidade)

**Quem automatiza deploy:** Claude Code (esta sessão). **Não** delegar redeploy manual à Julliana se você puder executar.

Playbook completo: **`docs/DEPLOY_AGENTE.md`**

Após push autorizado em `main` ou quando ela reportar erro pós-deploy:

1. `./mvnw test` + `npm run build` antes do push
2. Poll `GET /actuator/health` até UP
3. Smoke `POST /api/auth/login` (demo-lider@teste.com)
4. Se API falhar → **redeploy Render** (API ou Playwright no dashboard)
5. Se frontend antigo → **redeploy Vercel** + validar em contexto sem cache
6. Reportar status Render, Vercel e smokes

Push só com autorização explícita dela.

## Convenções de código

- Sem Lombok; records para DTOs; getters/setters explícitos em entidades JPA
- Edições cirúrgicas; testes verdes antes de commit
- Conventional Commits em português
- Commits backend + mobile separados quando o diff compartilhar arquivos estruturais

## URLs

- API: https://maranata-music-api.onrender.com
- App: https://maranata-music.vercel.app
- Demo líder: demo-lider@teste.com / demo123456
