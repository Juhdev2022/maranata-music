package br.com.maranatamusic.presentation.culto.dto;

import br.com.maranatamusic.domain.enums.CategoriaInstrumento;

public record InstrumentoResumo(Long id, String nome, CategoriaInstrumento categoria) {
}
