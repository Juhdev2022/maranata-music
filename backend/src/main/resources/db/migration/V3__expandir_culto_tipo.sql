-- V3: Documenta a expansão dos valores aceitos em culto.tipo.
-- culto.tipo é VARCHAR(20) sem CHECK constraint (validação fica no enum Java
-- CultoTipo), então não há alteração de schema ou dados — só atualiza o
-- comentário da coluna para refletir os novos dias aceitos.

COMMENT ON COLUMN culto.tipo IS 'DOMINGO_MANHA | DOMINGO_NOITE | TERCA | QUARTA | QUINTA | SABADO | ESPECIAL';
