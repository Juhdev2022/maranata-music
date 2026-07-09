-- V6: Repertório MVP (Fase 1.2 Pack 6).
-- Biblioteca de músicas auto-alimentada (CONTEXTO.md §9): título novo cria registro em
-- musica; título já existente reaproveita, e um link diferente naquele culto vira
-- override pontual em musica_culto.link_video_override, sem alterar o registro central.
-- Revisão do líder é opcional e por música: APROVADA limpa a observação; COM_OBSERVACAO
-- guarda um texto que só o ministro daquele culto e o líder enxergam.

CREATE TABLE musica (
    id         BIGSERIAL    PRIMARY KEY,
    titulo     VARCHAR(200) NOT NULL,
    link_video VARCHAR(500)
);

CREATE TABLE musica_culto (
    id                       BIGSERIAL    PRIMARY KEY,
    culto_id                 BIGINT       NOT NULL,
    musica_id                BIGINT       NOT NULL,
    ordem                    INT          NOT NULL,
    link_video_override      VARCHAR(500),
    revisao_lider            VARCHAR(20),  -- NULL | APROVADA | COM_OBSERVACAO
    observacao_lider_privada TEXT,
    CONSTRAINT fk_musica_culto_culto
        FOREIGN KEY (culto_id) REFERENCES culto(id),
    CONSTRAINT fk_musica_culto_musica
        FOREIGN KEY (musica_id) REFERENCES musica(id)
);

CREATE INDEX idx_musica_culto_culto_id ON musica_culto (culto_id);
