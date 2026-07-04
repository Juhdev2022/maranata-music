package br.com.maranatamusic.presentation.escala.dto;

import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.enums.EscalaStatus;
import br.com.maranatamusic.presentation.culto.dto.InstrumentoResumo;

import java.time.LocalDateTime;

public record EscalaMinhaResponse(
        Long id,
        CultoResumo culto,
        InstrumentoResumo instrumento,
        EscalaStatus status,
        LocalDateTime confirmadaEm
) {

    public static EscalaMinhaResponse from(Escala escala) {
        return new EscalaMinhaResponse(
                escala.getId(),
                new CultoResumo(
                        escala.getCulto().getId(),
                        escala.getCulto().getDataHora(),
                        escala.getCulto().getTipo()),
                new InstrumentoResumo(
                        escala.getInstrumento().getId(),
                        escala.getInstrumento().getNome(),
                        escala.getInstrumento().getCategoria()),
                escala.getStatus(),
                escala.getConfirmadaEm()
        );
    }
}
