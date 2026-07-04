package br.com.maranatamusic.presentation.culto.dto;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.enums.CultoTipo;

import java.time.LocalDateTime;
import java.util.List;

public record CultoDetalheResponse(
        Long id,
        LocalDateTime dataHora,
        CultoTipo tipo,
        MinistroResumo ministro,
        String observacoes,
        boolean repertorioTrancado,
        List<EscalaResumo> equipe
) {

    public static CultoDetalheResponse from(Culto culto, List<EscalaResumo> equipe) {
        MinistroResumo ministro = culto.getMinistro() == null
                ? null
                : new MinistroResumo(culto.getMinistro().getId(), culto.getMinistro().getNome());

        return new CultoDetalheResponse(
                culto.getId(),
                culto.getDataHora(),
                culto.getTipo(),
                ministro,
                culto.getObservacoes(),
                culto.isRepertorioTrancado(),
                equipe
        );
    }
}
