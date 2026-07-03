-- Convenção: tabelas no singular, snake_case.
-- Bate direto com o nome das entidades JPA sem precisar de @Table(name=...).
-- papeis armazenado como TEXT (CSV de enum) na própria tabela usuario.
-- JPA converterá para Set<Papel> via AttributeConverter no Milestone 2.

CREATE TABLE usuario (
    id         BIGSERIAL    PRIMARY KEY,
    nome       VARCHAR(150) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    telefone   VARCHAR(20),
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    papeis     TEXT         NOT NULL DEFAULT 'MUSICO',
    criado_em  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

CREATE TABLE instrumento (
    id        BIGSERIAL    PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    categoria VARCHAR(20)  NOT NULL  -- VOCAL | CORDA | PERCUSSAO | TECLA | SOPRO
);

CREATE TABLE musico_instrumento (
    usuario_id     BIGINT  NOT NULL,
    instrumento_id BIGINT  NOT NULL,
    principal      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_musico_instrumento
        PRIMARY KEY (usuario_id, instrumento_id),
    CONSTRAINT fk_mi_usuario
        FOREIGN KEY (usuario_id)     REFERENCES usuario(id),
    CONSTRAINT fk_mi_instrumento
        FOREIGN KEY (instrumento_id) REFERENCES instrumento(id)
);

CREATE TABLE culto (
    id                  BIGSERIAL    PRIMARY KEY,
    data_hora           TIMESTAMP    NOT NULL,
    tipo                VARCHAR(20)  NOT NULL,  -- DOMINGO_MANHA | DOMINGO_NOITE | QUARTA | ESPECIAL
    ministro_id         BIGINT,
    repertorio_trancado BOOLEAN      NOT NULL DEFAULT FALSE,
    observacoes         TEXT,
    criado_em           TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_culto_ministro
        FOREIGN KEY (ministro_id) REFERENCES usuario(id)
);

CREATE TABLE escala (
    id             BIGSERIAL    PRIMARY KEY,
    culto_id       BIGINT       NOT NULL,
    usuario_id     BIGINT       NOT NULL,
    instrumento_id BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',  -- PENDENTE | CONFIRMADA | RECUSADA | SUBSTITUIDA
    confirmada_em  TIMESTAMP,
    criado_em      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_escala_culto
        FOREIGN KEY (culto_id)       REFERENCES culto(id),
    CONSTRAINT fk_escala_usuario
        FOREIGN KEY (usuario_id)     REFERENCES usuario(id),
    CONSTRAINT fk_escala_instrumento
        FOREIGN KEY (instrumento_id) REFERENCES instrumento(id),
    CONSTRAINT uq_escala_culto_usuario_instrumento
        UNIQUE (culto_id, usuario_id, instrumento_id)
);

-- Índices para as queries mais frequentes (seção 9 do PROJETO.md)
CREATE INDEX idx_culto_data_hora    ON culto  (data_hora);
CREATE INDEX idx_escala_culto_id    ON escala (culto_id);
CREATE INDEX idx_escala_usuario_id  ON escala (usuario_id);
