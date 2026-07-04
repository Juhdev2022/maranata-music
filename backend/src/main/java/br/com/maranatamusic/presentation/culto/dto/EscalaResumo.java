package br.com.maranatamusic.presentation.culto.dto;

import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.enums.EscalaStatus;

import java.time.LocalDateTime;

public record EscalaResumo(
        Long id,
        MusicoResumo usuario,
        InstrumentoResumo instrumento,
        EscalaStatus status,
        LocalDateTime confirmadaEm
) {

    public static EscalaResumo from(Escala escala) {
        return new EscalaResumo(
                escala.getId(),
                new MusicoResumo(escala.getUsuario().getId(), escala.getUsuario().getNome()),
                new InstrumentoResumo(
                        escala.getInstrumento().getId(),
                        escala.getInstrumento().getNome(),
                        escala.getInstrumento().getCategoria()),
                escala.getStatus(),
                escala.getConfirmadaEm()
        );
    }
}
