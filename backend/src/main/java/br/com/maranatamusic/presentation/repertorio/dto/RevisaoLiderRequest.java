package br.com.maranatamusic.presentation.repertorio.dto;

import br.com.maranatamusic.domain.enums.RevisaoLider;
import jakarta.validation.constraints.NotNull;

public record RevisaoLiderRequest(
        @NotNull RevisaoLider revisaoLider,
        String observacaoLiderPrivada
) {}
