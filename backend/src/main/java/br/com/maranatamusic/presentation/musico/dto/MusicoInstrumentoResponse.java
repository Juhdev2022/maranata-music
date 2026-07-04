package br.com.maranatamusic.presentation.musico.dto;

import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;

public record MusicoInstrumentoResponse(
        Long usuarioId,
        String usuarioNome,
        Long instrumentoId,
        String instrumentoNome,
        CategoriaInstrumento categoria,
        boolean principal
) {

    public static MusicoInstrumentoResponse from(MusicoInstrumento musicoInstrumento) {
        return new MusicoInstrumentoResponse(
                musicoInstrumento.getUsuario().getId(),
                musicoInstrumento.getUsuario().getNome(),
                musicoInstrumento.getInstrumento().getId(),
                musicoInstrumento.getInstrumento().getNome(),
                musicoInstrumento.getInstrumento().getCategoria(),
                musicoInstrumento.isPrincipal()
        );
    }
}
