package br.com.maranatamusic.presentation.instrumento.dto;

import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;

public record InstrumentoResponse(
        Long id,
        String nome,
        CategoriaInstrumento categoria
) {

    public static InstrumentoResponse from(Instrumento instrumento) {
        return new InstrumentoResponse(
                instrumento.getId(),
                instrumento.getNome(),
                instrumento.getCategoria()
        );
    }
}
