-- V5: Fluxo MVP de solicitação de substituição (Fase 1.1, Pack 4).
-- Sem cascata de notificação e sem ciclo de re-notificação de líder (backlog).
-- Aprovação: escala original -> SUBSTITUIDA, nova escala criada para o substituto.

CREATE TABLE solicitacao_substituicao (
    id BIGSERIAL PRIMARY KEY,
    escala_id BIGINT NOT NULL REFERENCES escala(id),
    solicitante_id BIGINT NOT NULL REFERENCES usuario(id),
    motivo VARCHAR(20) NOT NULL,
    observacao TEXT,
    substituto_sugerido_id BIGINT REFERENCES usuario(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    substituto_final_id BIGINT REFERENCES usuario(id),
    aprovada_por_id BIGINT REFERENCES usuario(id),
    criada_em TIMESTAMP NOT NULL DEFAULT NOW(),
    resolvida_em TIMESTAMP
);

CREATE INDEX idx_solicitacao_status ON solicitacao_substituicao (status);
CREATE INDEX idx_solicitacao_escala_id ON solicitacao_substituicao (escala_id);
