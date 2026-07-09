package br.com.maranatamusic.presentation.substituicao.dto;

import br.com.maranatamusic.domain.enums.MotivoSubstituicao;
import jakarta.validation.constraints.NotNull;

public record SolicitarSubstituicaoRequest(
        @NotNull Long escalaId,
        @NotNull MotivoSubstituicao motivo,
        String observacao,
        Long substitutoSugeridoId
) {}
