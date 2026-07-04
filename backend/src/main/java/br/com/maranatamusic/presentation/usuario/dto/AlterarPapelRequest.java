package br.com.maranatamusic.presentation.usuario.dto;

import br.com.maranatamusic.domain.enums.Papel;
import jakarta.validation.constraints.NotNull;

public record AlterarPapelRequest(
        @NotNull Papel papel
) {}
