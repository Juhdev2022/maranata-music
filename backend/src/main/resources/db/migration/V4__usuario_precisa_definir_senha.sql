-- V4: Suporte a pré-cadastro de usuário pelo líder (Fase 1.1, Pack 1).
-- Líder cria o registro só com nome/email; usuário ainda não tem senha própria
-- até completar o primeiro acesso (Pack 2). Usuários já existentes já
-- definiram senha no próprio registro, então ficam FALSE.

ALTER TABLE usuario ADD COLUMN precisa_definir_senha BOOLEAN NOT NULL DEFAULT FALSE;
