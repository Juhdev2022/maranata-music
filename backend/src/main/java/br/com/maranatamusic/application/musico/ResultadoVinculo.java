package br.com.maranatamusic.application.musico;

import br.com.maranatamusic.presentation.musico.dto.MusicoInstrumentoResponse;

public record ResultadoVinculo(MusicoInstrumentoResponse resposta, boolean criado, boolean principalAlterado) {}
