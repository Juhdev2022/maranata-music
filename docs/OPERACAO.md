# Operação — Maranata Music

> Este arquivo documenta procedimentos operacionais críticos: bootstrap inicial, deploy, backup, recuperação, e tudo que envolve intervenção manual fora do fluxo normal do app.
>
> **Público-alvo:** administradores do sistema (hoje: Julliana).
> **Fora de escopo:** desenvolvimento (isso está em `PROJETO.md` e `CONTEXTO.md`).

---

## 1. Bootstrap do primeiro líder

O endpoint `/api/auth/registro` sempre atribui papel `MUSICO` por decisão de segurança (evita auto-atribuição de LIDER). Isso significa que em cada ambiente novo (dev, staging, produção), o primeiro líder precisa ser promovido manualmente uma única vez.

### Procedimento

**Passo 1 — Deploy do backend rodando**

O ambiente precisa estar no ar e acessível (backend + banco de dados operacional).

**Passo 2 — Criar sua conta pelo app (via API)**

Faça um POST no endpoint público de registro. Use uma senha forte real, pois a partir daqui é a mesma senha que você usará no dia a dia:

```bash
curl -X POST https://<seu-dominio>/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{
    "nome":"Julliana Leão",
    "email":"julliana@maranata.com.br",
    "senha":"<sua-senha-forte>",
    "telefone":"61999999999"
  }'
```

Guarde o `id` do usuário retornado — você vai precisar no próximo passo.

Se não tiver o `id` na resposta, consulte no banco:

```sql
SELECT id, nome, email FROM usuario WHERE email = 'julliana@maranata.com.br';
```

**Passo 3 — Promover a LIDER via SQL**

Acesse o banco de produção pelo console do provedor (Render.com, Supabase, etc) ou via `psql` direto. Execute:

```sql
INSERT INTO usuario_papel (usuario_id, papel) VALUES (<seu-id>, 'LIDER');
```

Confirme:

```sql
SELECT u.nome, u.email, up.papel
FROM usuario u
JOIN usuario_papel up ON up.usuario_id = u.id
WHERE u.email = 'julliana@maranata.com.br';
```

Deve retornar `MUSICO` e `LIDER` — o usuário acumula os dois papéis.

**Passo 4 — Confirmar via login**

Faça login pelo app. O token JWT retornado agora contém o papel LIDER:

```bash
curl -X POST https://<seu-dominio>/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"julliana@maranata.com.br","senha":"<sua-senha-forte>"}'
```

A partir daqui, você consegue usar todos os endpoints protegidos com `hasRole('LIDER')`.

### Por que é assim

- **Segurança em primeiro lugar.** Se o endpoint público permitisse escolher o papel, qualquer atacante viraria líder. Como o registro só cria MUSICO, mesmo se alguém quebrar a autenticação, o dano é limitado.
- **Uma vez só na vida.** Bootstrap acontece uma vez por ambiente. Depois disso, você pode criar outros líderes pelo app (endpoint futuro na Fase 4) ou via SQL da mesma forma se preferir.
- **Sua senha nunca vai pro git.** Ela é definida por você no registro, com hash bcrypt gerado pelo próprio backend. Zero risco de vazamento por commit.

---

## 2. Bootstrap dos instrumentos

Enquanto o CRUD de instrumentos ainda não existe (chegará no Milestone 5), o catálogo inicial precisa ser inserido via SQL.

Instrumentos sugeridos para o ministério:

```sql
INSERT INTO instrumento (nome, categoria) VALUES
  ('Vocal Feminino', 'VOCAL'),
  ('Vocal Masculino', 'VOCAL'),
  ('Vocal Ministro', 'VOCAL'),
  ('Violão', 'CORDA'),
  ('Guitarra', 'CORDA'),
  ('Baixo', 'CORDA'),
  ('Teclado', 'TECLA'),
  ('Piano', 'TECLA'),
  ('Bateria', 'PERCUSSAO'),
  ('Cajón', 'PERCUSSAO'),
  ('Percussão Auxiliar', 'PERCUSSAO'),
  ('Saxofone', 'SOPRO'),
  ('Trompete', 'SOPRO');
```

Ajuste a lista conforme a realidade do ministério antes de rodar. Após o M5, esse processo vira endpoint no app.

**Vínculo músico-instrumento:** desde o Milestone 5.6, ligar um músico a um instrumento que ele toca tem endpoint próprio — `POST /api/musicos/{usuarioId}/instrumentos/{instrumentoId}` (só `LIDER`, body opcional `{"principal": true|false}`; idempotente se o vínculo já existir). Inserir direto na tabela `musico_instrumento` via SQL fica como fallback só para carga inicial em lote ou emergência, não é mais o caminho padrão.

---

## 3. Deploy em produção

### URLs

| Serviço | URL |
|---------|-----|
| API (Render) | https://maranata-music-api.onrender.com |
| Health | https://maranata-music-api.onrender.com/actuator/health |
| App (Vercel) | https://maranata-music.vercel.app |

### Deploy automático

Push na branch `main` dispara redeploy no **Render** (backend) e no **Vercel** (frontend).

### Confirmação rápida (humano ou agente)

```bash
curl https://maranata-music-api.onrender.com/actuator/health
# Esperado: {"status":"UP",...}
```

Testar login demo após deploy: `demo-lider@teste.com` / `demo123456`.

Validar o app em **guia anônima** (PWA cacheia versão antiga).

### Redeploy manual

**Render:** Dashboard → serviço da API → **Manual Deploy** → Deploy latest commit.  
Se deploy falhou ou comportamento estranho: **Clear build cache & deploy**.

**Vercel:** Dashboard → projeto → **Deployments** → último → **Redeploy**.

### Playbook para Claude Code

Procedimento completo (curl, Render API, Playwright, smoke tests): **`docs/DEPLOY_AGENTE.md`**.  
Instruções por sessão: **`CLAUDE.md`** na raiz do repo.  
Deploy automatizado é responsabilidade do **Claude Code**, não do Cursor.

### Variáveis de ambiente (Render)

- `JWT_SECRET`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- Perfil Spring: `prod`

### Cold start (plano free)

Backend e Postgres podem hibernar após ~15 min sem tráfego. Primeira requisição após idle pode levar 1–3 min. Health UP não garante que login já responde — aguardar ou redeploy se 500 persistir.

---

## 4. Backup do banco de produção

*A ser preenchido quando o deploy real acontecer.*

Prioridades:

- Frequência de backup automático do Render Postgres
- Como baixar backup manual
- Como restaurar um dump em ambiente local para debug
- Retenção mínima recomendada: 7 dias diários + 4 semanais

---

## 5. Recuperação de conta (esqueci minha senha)

Enquanto não existir fluxo de recuperação de senha por email (backlog pós-MVP), a única forma é redefinir manualmente:

```sql
-- Gerar hash bcrypt de uma nova senha temporária
-- (usar o próprio Java/Spring, ou lib externa como htpasswd)
UPDATE usuario
SET senha_hash = '$2a$10$<hash-bcrypt-gerado>'
WHERE email = 'usuario@que-esqueceu.com';
```

O usuário faz login com a senha temporária e (idealmente) altera na primeira oportunidade — endpoint de "alterar senha" também é backlog.

**Gerando hash bcrypt manualmente:**

Roda essa classe uma vez em local, imprime o hash, cola no SQL:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarHash {
    public static void main(String[] args) {
        String senha = "senhaTemporaria123";
        String hash = new BCryptPasswordEncoder().encode(senha);
        System.out.println(hash);
    }
}
```

Nunca escreva senhas em texto puro em SQL. Sempre gere hash bcrypt antes.

---

## 6. Ambiente local para debug

Quando algo estranho acontece em produção e você precisa reproduzir:

```bash
# 1. Baixar dump do banco de produção
# (comando específico do provedor — a ser preenchido)

# 2. Restaurar em H2 local ou Postgres local
# (comando específico — a ser preenchido)

# 3. Rodar backend apontando pra esse banco temporário
export DB_URL="jdbc:postgresql://localhost:5432/maranata_debug"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
export JWT_SECRET="<qualquer coisa em base64>"
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

**IMPORTANTE:** Nunca teste em produção direto. Sempre reproduza local com dump.

---

## 7. Histórico de operações críticas

Registrar aqui toda operação manual feita em produção. Serve como auditoria e memória institucional.

Formato sugerido:

```
YYYY-MM-DD HH:MM — Descrição da operação — Responsável — Motivo
```

Exemplos futuros:

```
2026-08-15 14:30 — Bootstrap do primeiro LIDER (julliana@maranata.com.br) — Julliana — Setup inicial de produção
2026-08-15 14:45 — Seed dos 13 instrumentos iniciais — Julliana — Setup inicial de produção
```

Manter esse log atualizado a cada intervenção manual. Se algo der errado no futuro, esse histórico ajuda a entender o que aconteceu.

### Log real

```
2026-07-07 — Registro de demo-musico@teste.com e demo-lider@teste.com via /api/auth/registro — Julliana — M7.4.1, portfólio
2026-07-07 — Promoção de demo-lider@teste.com a LIDER via INSERT em usuario_papel — Julliana — M7.4.1, portfólio
2026-07-07 — Criação de 7 instrumentos via API (Baixo, Guitarra, Bateria, Teclado, Vocal Feminino, Vocal Masculino) + INSERT direto de "Cajón" via SQL (workaround desnecessário, ver correção abaixo) — Julliana — M7.4.2, dados demo
2026-07-07 — Suspeita de bug: POST /api/instrumentos com nome acentuado parecia retornar 500. Contornado com INSERT SQL direto para "Cajón". Registrado como pendência em PROJETO.md §15 — Julliana — M7.4.2
2026-07-08 — Investigação encerrada: NÃO havia bug na API. O 500 era falso positivo de `curl -d "<texto com acento>"` no Git Bash do Windows (corrompe Content-Length de argumentos multi-byte na linha de comando). Confirmado com `--data-binary @arquivo` (byte-exato) funcionando normalmente, e com teste de integração cobrindo Cajón/Percussão/Órgão. O INSERT SQL do Cajón foi desnecessário — a API já aceitava o nome corretamente. Pendência removida de PROJETO.md §15 — Julliana, Claude Code — M7.5
2026-07-08 — Bug real (separado, confirmado): digitar acento direto no prompt interativo do psql no Windows corrompe o dado antes de gravar no banco (code page 850 do console). Corrigido com escape Unicode ASCII-safe (`UPDATE ... SET nome = E'Cajón'`) ou passando o SQL via arquivo/`-c` não-interativo — nunca digitar acento direto no prompt psql do Windows — Julliana — M7.4.2
2026-07-08 — Bug: POST /api/usuarios retornava 500 em produção ao líder pré-cadastrar um usuário (Fase 1.1 Pack 1). Coluna `usuario.precisa_definir_senha` (migration V4) foi criada manualmente via SQL direto em produção pra desbloquear o endpoint enquanto a causa raiz era investigada — Julliana — Fase 1.1
2026-07-08 — Causa raiz do 500 do POST /api/usuarios: `UsuarioService.criar()` retornava `UsuarioResponse.from(usuario)` sem forçar carregamento da coleção LAZY `papeis` antes da sessão/transação fechar — mesma classe de problema já resolvida em `reativar()`. Corrigido adicionando `usuario.getPapeis().size()` antes da serialização. `V4` também foi alterada para `ADD COLUMN IF NOT EXISTS`, idempotente — já que a coluna existe em produção desde a correção manual acima, um próximo deploy não deve falhar tentando recriá-la — Julliana, Claude Code — Fase 1.1

**Atenção pra próximo deploy:** se o Flyway já tiver `V4` registrada em `flyway_schema_history` de uma execução anterior (checksum do arquivo antigo, sem `IF NOT EXISTS`), o deploy vai falhar por *checksum mismatch* — o conteúdo do arquivo mudou. Se isso acontecer, rodar `./mvnw flyway:repair` (ou o equivalente do Render) contra o banco de produção antes do deploy, ou confirmar via `SELECT * FROM flyway_schema_history WHERE version = '4';` se a migração já foi marcada como aplicada e, se sim, ajustar o checksum manualmente ou deletar a linha pra ela ser reaplicada (segura, pois agora é idempotente).
```

---

## 8. Contatos e responsabilidades

- **Owner do sistema:** Julliana Leão (@girl.code2026)
- **Provedor do backend:** *(a definir — provavelmente Render.com)*
- **Provedor do banco:** *(a definir — provavelmente Render Postgres)*
- **Domínio:** *(a definir)*
- **Emergência:** *(canal de contato — WhatsApp pessoal ou email dedicado)*