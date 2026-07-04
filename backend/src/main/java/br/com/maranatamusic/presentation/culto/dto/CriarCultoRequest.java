package br.com.maranatamusic.presentation.culto.dto;

import br.com.maranatamusic.domain.enums.CultoTipo;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CriarCultoRequest(
        @NotNull @FutureOrPresent LocalDateTime dataHora,
        @NotNull CultoTipo tipo,
        Long ministroId,
        String observacoes
) {}
