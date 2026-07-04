package br.com.maranatamusic.presentation.instrumento.dto;

import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarInstrumentoRequest(
        @NotBlank @Size(min = 2, max = 50) String nome,
        @NotNull CategoriaInstrumento categoria
) {}
