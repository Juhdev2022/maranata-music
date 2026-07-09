package br.com.maranatamusic.application.substituicao;

import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.MusicoInstrumentoId;
import br.com.maranatamusic.domain.SolicitacaoSubstituicao;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import br.com.maranatamusic.domain.enums.EscalaStatus;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.domain.enums.SolicitacaoStatus;
import br.com.maranatamusic.domain.exception.AcessoNaoAutorizadoException;
import br.com.maranatamusic.domain.exception.EscalaNaoEncontradaException;
import br.com.maranatamusic.domain.exception.EstadoEscalaInvalidoException;
import br.com.maranatamusic.domain.exception.EstadoSolicitacaoInvalidoException;
import br.com.maranatamusic.domain.exception.SolicitacaoJaExisteException;
import br.com.maranatamusic.domain.exception.SolicitacaoSubstituicaoNaoEncontradaException;
import br.com.maranatamusic.domain.exception.SubstitutoNaoElegivelException;
import br.com.maranatamusic.domain.exception.SubstitutoObrigatorioException;
import br.com.maranatamusic.domain.exception.UsuarioNaoEncontradoException;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.SolicitacaoSubstituicaoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.substituicao.dto.AprovarSubstituicaoRequest;
import br.com.maranatamusic.presentation.substituicao.dto.SolicitacaoResponse;
import br.com.maranatamusic.presentation.substituicao.dto.SolicitarSubstituicaoRequest;
import br.com.maranatamusic.presentation.substituicao.dto.SubstitutoElegivelResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubstituicaoService {

    private static final long JANELA_HORAS_ANTES = 2;
    private static final long JANELA_HORAS_DEPOIS = 4;

    private final SolicitacaoSubstituicaoRepository solicitacaoRepository;
    private final EscalaRepository escalaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MusicoInstrumentoRepository musicoInstrumentoRepository;

    public SubstituicaoService(SolicitacaoSubstituicaoRepository solicitacaoRepository,
                                EscalaRepository escalaRepository,
                                UsuarioRepository usuarioRepository,
                                MusicoInstrumentoRepository musicoInstrumentoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.escalaRepository = escalaRepository;
        this.usuarioRepository = usuarioRepository;
        this.musicoInstrumentoRepository = musicoInstrumentoRepository;
    }

    @Transactional
    public SolicitacaoResponse solicitar(SolicitarSubstituicaoRequest request, Long solicitanteId) {
        Escala escala = escalaRepository.findById(request.escalaId())
                .orElseThrow(() -> new EscalaNaoEncontradaException(request.escalaId()));

        if (!escala.getUsuario().getId().equals(solicitanteId)) {
            throw new AcessoNaoAutorizadoException("Você não pode solicitar substituição de escala de outro músico");
        }

        if (escala.getStatus() != EscalaStatus.PENDENTE && escala.getStatus() != EscalaStatus.CONFIRMADA) {
            throw new EstadoEscalaInvalidoException();
        }

        if (solicitacaoRepository.existsByEscalaIdAndStatus(escala.getId(), SolicitacaoStatus.ABERTA)) {
            throw new SolicitacaoJaExisteException();
        }

        SolicitacaoSubstituicao solicitacao = new SolicitacaoSubstituicao();
        solicitacao.setEscala(escala);
        solicitacao.setSolicitante(escala.getUsuario());
        solicitacao.setMotivo(request.motivo());
        solicitacao.setObservacao(request.observacao());

        if (request.substitutoSugeridoId() != null) {
            Usuario sugerido = usuarioRepository.findById(request.substitutoSugeridoId())
                    .orElseThrow(() -> new UsuarioNaoEncontradoException(request.substitutoSugeridoId()));
            validarSubstitutoElegivel(escala, sugerido);
            solicitacao.setSubstitutoSugerido(sugerido);
        }

        solicitacaoRepository.save(solicitacao);
        return SolicitacaoResponse.from(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResponse> listarPendentes() {
        return solicitacaoRepository.findByStatusComDetalhes(SolicitacaoStatus.ABERTA).stream()
                .map(SolicitacaoResponse::from)
                .toList();
    }

    @Transactional
    public SolicitacaoResponse aprovar(Long id, AprovarSubstituicaoRequest request, Long liderId) {
        SolicitacaoSubstituicao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new SolicitacaoSubstituicaoNaoEncontradaException(id));

        if (solicitacao.getStatus() != SolicitacaoStatus.ABERTA) {
            throw new EstadoSolicitacaoInvalidoException();
        }

        Long substitutoFinalId = request.substitutoFinalId() != null
                ? request.substitutoFinalId()
                : solicitacao.getSubstitutoSugerido() != null ? solicitacao.getSubstitutoSugerido().getId() : null;

        if (substitutoFinalId == null) {
            throw new SubstitutoObrigatorioException();
        }

        Usuario substituto = usuarioRepository.findById(substitutoFinalId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(substitutoFinalId));

        Escala escalaOriginal = solicitacao.getEscala();
        validarSubstitutoElegivel(escalaOriginal, substituto);

        escalaOriginal.setStatus(EscalaStatus.SUBSTITUIDA);
        escalaRepository.save(escalaOriginal);

        Escala novaEscala = new Escala();
        novaEscala.setCulto(escalaOriginal.getCulto());
        novaEscala.setUsuario(substituto);
        novaEscala.setInstrumento(escalaOriginal.getInstrumento());
        escalaRepository.save(novaEscala);

        solicitacao.setStatus(SolicitacaoStatus.APROVADA);
        solicitacao.setSubstitutoFinal(substituto);
        solicitacao.setAprovadaPor(usuarioRepository.getReferenceById(liderId));
        solicitacao.setResolvidaEm(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        return SolicitacaoResponse.from(solicitacao);
    }

    @Transactional
    public SolicitacaoResponse rejeitar(Long id) {
        SolicitacaoSubstituicao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new SolicitacaoSubstituicaoNaoEncontradaException(id));

        if (solicitacao.getStatus() != SolicitacaoStatus.ABERTA) {
            throw new EstadoSolicitacaoInvalidoException();
        }

        solicitacao.setStatus(SolicitacaoStatus.REJEITADA);
        solicitacao.setResolvidaEm(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        return SolicitacaoResponse.from(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<SubstitutoElegivelResponse> listarSubstitutosElegiveis(Long escalaId, Usuario chamador) {
        Escala escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new EscalaNaoEncontradaException(escalaId));

        boolean ehLider = chamador.getPapeis().contains(Papel.LIDER);
        boolean ehDono = escala.getUsuario().getId().equals(chamador.getId());
        if (!ehLider && !ehDono) {
            throw new AcessoNaoAutorizadoException("Você não pode ver substitutos elegíveis para escala de outro músico");
        }

        return musicoInstrumentoRepository.findByInstrumentoIdComUsuario(escala.getInstrumento().getId()).stream()
                .map(MusicoInstrumento::getUsuario)
                .filter(usuario -> motivoInelegibilidade(escala, usuario) == null)
                .map(usuario -> new SubstitutoElegivelResponse(usuario.getId(), usuario.getNome()))
                .toList();
    }

    private void validarSubstitutoElegivel(Escala escalaOriginal, Usuario candidato) {
        String motivo = motivoInelegibilidade(escalaOriginal, candidato);
        if (motivo != null) {
            throw new SubstitutoNaoElegivelException(motivo);
        }
    }

    private String motivoInelegibilidade(Escala escalaOriginal, Usuario candidato) {
        if (!candidato.isAtivo()) {
            return "Substituto inativo";
        }
        if (candidato.getId().equals(escalaOriginal.getUsuario().getId())) {
            return "Substituto não pode ser o próprio solicitante";
        }

        MusicoInstrumentoId vinculoId = new MusicoInstrumentoId(candidato.getId(), escalaOriginal.getInstrumento().getId());
        if (!musicoInstrumentoRepository.existsById(vinculoId)) {
            return "Substituto não toca o instrumento desta escala";
        }

        LocalDateTime inicioJanela = escalaOriginal.getCulto().getDataHora().minusHours(JANELA_HORAS_ANTES);
        LocalDateTime fimJanela = escalaOriginal.getCulto().getDataHora().plusHours(JANELA_HORAS_DEPOIS);
        if (escalaRepository.existeEscalaAtivaNaJanela(candidato.getId(), inicioJanela, fimJanela)) {
            return "Substituto já escalado em outro culto próximo a este horário";
        }

        if (escalaOriginal.getInstrumento().getCategoria() != CategoriaInstrumento.VOCAL
                && escalaRepository.existsByCultoIdAndInstrumentoIdAndIdNot(
                        escalaOriginal.getCulto().getId(), escalaOriginal.getInstrumento().getId(), escalaOriginal.getId())) {
            return "Já há alguém escalado neste instrumento neste culto";
        }

        return null;
    }
}
