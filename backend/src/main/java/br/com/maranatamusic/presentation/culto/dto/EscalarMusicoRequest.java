package br.com.maranatamusic.presentation.culto.dto;

import jakarta.validation.constraints.NotNull;

public record EscalarMusicoRequest(
        @NotNull Long usuarioId
) {}
