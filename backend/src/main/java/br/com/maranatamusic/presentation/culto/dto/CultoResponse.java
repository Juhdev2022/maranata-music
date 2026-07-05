package br.com.maranatamusic.presentation.culto.dto;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.enums.CultoTipo;

import java.time.LocalDateTime;

public record CultoResponse(
        Long id,
        LocalDateTime dataHora,
        CultoTipo tipo,
        MinistroResumo ministro,
        String observacoes,
        boolean repertorioTrancado,
        int totalEscalados
) {

    public static CultoResponse from(Culto culto) {
        return from(culto, 0);
    }

    public static CultoResponse from(Culto culto, int totalEscalados) {
        MinistroResumo ministro = culto.getMinistro() == null
                ? null
                : new MinistroResumo(culto.getMinistro().getId(), culto.getMinistro().getNome());

        return new CultoResponse(
                culto.getId(),
                culto.getDataHora(),
                culto.getTipo(),
                ministro,
                culto.getObservacoes(),
                culto.isRepertorioTrancado(),
                totalEscalados
        );
    }
}
