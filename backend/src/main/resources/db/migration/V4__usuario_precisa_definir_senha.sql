-- V4: Suporte a pré-cadastro de usuário pelo líder (Fase 1.1, Pack 1).
-- Líder cria o registro só com nome/email; usuário ainda não tem senha própria
-- até completar o primeiro acesso (Pack 2). Usuários já existentes já
-- definiram senha no próprio registro, então ficam FALSE.
--
-- IF NOT EXISTS: em 2026-07-08 a coluna precisou ser criada manualmente em
-- produção pra desbloquear o 500 do POST /api/usuarios (ver docs/OPERACAO.md,
-- seção 7). Idempotente evita que um próximo deploy falhe tentando recriar
-- coluna que já existe.

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS precisa_definir_senha BOOLEAN NOT NULL DEFAULT FALSE;
