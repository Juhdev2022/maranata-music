package br.com.maranatamusic.presentation.culto.dto;

import jakarta.validation.constraints.Size;

public record AtualizarObservacoesRequest(
        @Size(max = 2000, message = "Máximo de 2000 caracteres")
        String observacoes
) {}
