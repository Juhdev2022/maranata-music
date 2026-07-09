package br.com.maranatamusic.presentation.substituicao.dto;

import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.SolicitacaoSubstituicao;
import br.com.maranatamusic.domain.enums.MotivoSubstituicao;
import br.com.maranatamusic.domain.enums.SolicitacaoStatus;
import br.com.maranatamusic.presentation.culto.dto.InstrumentoResumo;
import br.com.maranatamusic.presentation.culto.dto.MusicoResumo;
import br.com.maranatamusic.presentation.escala.dto.CultoResumo;

public record SolicitacaoResponse(
        Long id,
        Long escalaId,
        MusicoResumo solicitante,
        CultoResumo culto,
        InstrumentoResumo instrumento,
        MotivoSubstituicao motivo,
        String observacao,
        MusicoResumo substitutoSugerido,
        SolicitacaoStatus status,
        MusicoResumo substitutoFinal
) {

    public static SolicitacaoResponse from(SolicitacaoSubstituicao solicitacao) {
        Escala escala = solicitacao.getEscala();
        return new SolicitacaoResponse(
                solicitacao.getId(),
                escala.getId(),
                new MusicoResumo(solicitacao.getSolicitante().getId(), solicitacao.getSolicitante().getNome()),
                new CultoResumo(escala.getCulto().getId(), escala.getCulto().getDataHora(), escala.getCulto().getTipo()),
                new InstrumentoResumo(
                        escala.getInstrumento().getId(),
                        escala.getInstrumento().getNome(),
                        escala.getInstrumento().getCategoria()),
                solicitacao.getMotivo(),
                solicitacao.getObservacao(),
                solicitacao.getSubstitutoSugerido() == null
                        ? null
                        : new MusicoResumo(solicitacao.getSubstitutoSugerido().getId(), solicitacao.getSubstitutoSugerido().getNome()),
                solicitacao.getStatus(),
                solicitacao.getSubstitutoFinal() == null
                        ? null
                        : new MusicoResumo(solicitacao.getSubstitutoFinal().getId(), solicitacao.getSubstitutoFinal().getNome())
        );
    }
}
