package br.com.maranatamusic.presentation.repertorio.dto;

import br.com.maranatamusic.domain.MusicaCulto;
import br.com.maranatamusic.domain.enums.RevisaoLider;

public record MusicaCultoResponse(
        Long id,
        Long musicaId,
        String titulo,
        String linkVideo,
        int ordem,
        RevisaoLider revisaoLider,
        String observacaoLiderPrivada
) {

    public static MusicaCultoResponse from(MusicaCulto musicaCulto, boolean podeVerObservacao) {
        String linkEfetivo = musicaCulto.getLinkVideoOverride() != null
                ? musicaCulto.getLinkVideoOverride()
                : musicaCulto.getMusica().getLinkVideo();

        return new MusicaCultoResponse(
                musicaCulto.getId(),
                musicaCulto.getMusica().getId(),
                musicaCulto.getMusica().getTitulo(),
                linkEfetivo,
                musicaCulto.getOrdem(),
                musicaCulto.getRevisaoLider(),
                podeVerObservacao ? musicaCulto.getObservacaoLiderPrivada() : null
        );
    }
}
