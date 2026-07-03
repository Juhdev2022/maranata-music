-- V2: Normalização do campo papeis de usuario.
-- V1 armazenava papeis como coluna TEXT (ex: 'MUSICO'). Substituído por tabela
-- usuario_papel para garantir integridade referencial, queries eficientes por papel
-- e mapeamento correto via @ElementCollection no JPA. Descoberto antes de qualquer
-- dado de produção existir — migração sem risco.

CREATE TABLE usuario_papel (
    usuario_id BIGINT      NOT NULL,
    papel      VARCHAR(20) NOT NULL,
    CONSTRAINT pk_usuario_papel
        PRIMARY KEY (usuario_id, papel),
    CONSTRAINT fk_usuario_papel_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE INDEX idx_usuario_papel_usuario_id ON usuario_papel (usuario_id);

-- Preserva papel padrão de qualquer registro existente em dev
INSERT INTO usuario_papel (usuario_id, papel)
SELECT id, 'MUSICO' FROM usuario WHERE papeis IS NOT NULL;

ALTER TABLE usuario DROP COLUMN papeis;
