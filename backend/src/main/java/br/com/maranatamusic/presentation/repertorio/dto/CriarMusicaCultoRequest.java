package br.com.maranatamusic.presentation.repertorio.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarMusicaCultoRequest(
        @NotBlank String titulo,
        String linkVideo,
        Integer ordem
) {}
