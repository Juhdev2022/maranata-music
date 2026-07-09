package br.com.maranatamusic.application.escala;

import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.enums.EscalaStatus;
import br.com.maranatamusic.domain.enums.SolicitacaoStatus;
import br.com.maranatamusic.domain.exception.AcessoNaoAutorizadoException;
import br.com.maranatamusic.domain.exception.EscalaNaoEncontradaException;
import br.com.maranatamusic.domain.exception.EstadoEscalaInvalidoException;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.SolicitacaoSubstituicaoRepository;
import br.com.maranatamusic.presentation.escala.dto.EscalaMinhaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
public class EscalaService {

    private final EscalaRepository escalaRepository;
    private final SolicitacaoSubstituicaoRepository solicitacaoSubstituicaoRepository;

    public EscalaService(EscalaRepository escalaRepository,
                          SolicitacaoSubstituicaoRepository solicitacaoSubstituicaoRepository) {
        this.escalaRepository = escalaRepository;
        this.solicitacaoSubstituicaoRepository = solicitacaoSubstituicaoRepository;
    }

    @Transactional(readOnly = true)
    public List<EscalaMinhaResponse> minhasEscalas(Long usuarioId, YearMonth mes) {
        LocalDateTime inicio = mes.atDay(1).atStartOfDay();
        LocalDateTime fim = mes.atEndOfMonth().atTime(23, 59, 59);

        return escalaRepository.findByUsuarioIdAndCultoDataHoraBetween(usuarioId, inicio, fim).stream()
                .sorted(Comparator.comparing(escala -> escala.getCulto().getDataHora()))
                .map(escala -> EscalaMinhaResponse.from(escala, temSolicitacaoAberta(escala.getId())))
                .toList();
    }

    @Transactional
    public EscalaMinhaResponse confirmar(Long escalaId, Long usuarioId) {
        Escala escala = buscarEscalaDoUsuario(escalaId, usuarioId);

        if (escala.getStatus() == EscalaStatus.CONFIRMADA) {
            // Idempotente: não reatualiza confirmadaEm, preserva o momento da confirmação original.
            return EscalaMinhaResponse.from(escala, temSolicitacaoAberta(escala.getId()));
        }

        if (escala.getStatus() == EscalaStatus.SUBSTITUIDA || escala.getStatus() == EscalaStatus.RECUSADA) {
            throw new EstadoEscalaInvalidoException();
        }

        escala.setStatus(EscalaStatus.CONFIRMADA);
        escala.setConfirmadaEm(LocalDateTime.now());
        escalaRepository.save(escala);
        return EscalaMinhaResponse.from(escala, temSolicitacaoAberta(escala.getId()));
    }

    @Transactional
    public EscalaMinhaResponse recusar(Long escalaId, Long usuarioId) {
        Escala escala = buscarEscalaDoUsuario(escalaId, usuarioId);

        if (escala.getStatus() == EscalaStatus.SUBSTITUIDA) {
            throw new EstadoEscalaInvalidoException();
        }

        escala.setStatus(EscalaStatus.RECUSADA);
        escalaRepository.save(escala);
        return EscalaMinhaResponse.from(escala, temSolicitacaoAberta(escala.getId()));
    }

    private boolean temSolicitacaoAberta(Long escalaId) {
        return solicitacaoSubstituicaoRepository.existsByEscalaIdAndStatus(escalaId, SolicitacaoStatus.ABERTA);
    }

    private Escala buscarEscalaDoUsuario(Long escalaId, Long usuarioId) {
        Escala escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new EscalaNaoEncontradaException(escalaId));

        if (!escala.getUsuario().getId().equals(usuarioId)) {
            throw new AcessoNaoAutorizadoException();
        }

        return escala;
    }
}
