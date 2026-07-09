package br.com.maranatamusic.application.substituicao;

import br.com.maranatamusic.domain.Escala;
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
import br.com.maranatamusic.infrastructure.persistence.SolicitacaoSubstituicaoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.substituicao.dto.RejeitarSubstituicaoRequest;
import br.com.maranatamusic.presentation.substituicao.dto.SolicitacaoResponse;
import br.com.maranatamusic.presentation.substituicao.dto.SolicitarSubstituicaoRequest;
import br.com.maranatamusic.presentation.substituicao.dto.SubstitutoElegivelResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubstituicaoService {

    private final SolicitacaoSubstituicaoRepository solicitacaoRepository;
    private final EscalaRepository escalaRepository;
    private final UsuarioRepository usuarioRepository;

    public SubstituicaoService(SolicitacaoSubstituicaoRepository solicitacaoRepository,
                                EscalaRepository escalaRepository,
                                UsuarioRepository usuarioRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.escalaRepository = escalaRepository;
        this.usuarioRepository = usuarioRepository;
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

        Usuario sugerido = usuarioRepository.findById(request.substitutoSugeridoId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(request.substitutoSugeridoId()));
        validarSubstitutoBasico(escala, sugerido);

        SolicitacaoSubstituicao solicitacao = new SolicitacaoSubstituicao();
        solicitacao.setEscala(escala);
        solicitacao.setSolicitante(escala.getUsuario());
        solicitacao.setMotivo(request.motivo());
        solicitacao.setObservacao(request.observacao());
        solicitacao.setSubstitutoSugerido(sugerido);

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
    public SolicitacaoResponse aprovar(Long id, Long liderId) {
        SolicitacaoSubstituicao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new SolicitacaoSubstituicaoNaoEncontradaException(id));

        if (solicitacao.getStatus() != SolicitacaoStatus.ABERTA) {
            throw new EstadoSolicitacaoInvalidoException();
        }

        Usuario substituto = solicitacao.getSubstitutoSugerido();
        Escala escalaOriginal = solicitacao.getEscala();

        validarSubstitutoBasico(escalaOriginal, substituto);
        validarSlotDisponivel(escalaOriginal);
        trocarEscala(escalaOriginal, substituto);

        solicitacao.setStatus(SolicitacaoStatus.APROVADA);
        solicitacao.setSubstitutoFinal(substituto);
        solicitacao.setAprovadaPor(usuarioRepository.getReferenceById(liderId));
        solicitacao.setResolvidaEm(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        return SolicitacaoResponse.from(solicitacao);
    }

    @Transactional
    public SolicitacaoResponse rejeitar(Long id, RejeitarSubstituicaoRequest request, Long liderId) {
        SolicitacaoSubstituicao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new SolicitacaoSubstituicaoNaoEncontradaException(id));

        if (solicitacao.getStatus() != SolicitacaoStatus.ABERTA) {
            throw new EstadoSolicitacaoInvalidoException();
        }

        if (request == null || request.substitutoFinalId() == null) {
            throw new SubstitutoObrigatorioException("Informe quem vai substituir");
        }

        Usuario substituto = usuarioRepository.findById(request.substitutoFinalId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(request.substitutoFinalId()));

        Escala escalaOriginal = solicitacao.getEscala();
        validarSubstitutoBasico(escalaOriginal, substituto);
        validarSlotDisponivel(escalaOriginal);
        trocarEscala(escalaOriginal, substituto);

        // Status permanece REJEITADA — a sugestão do músico foi recusada; substitutoFinal
        // registra quem o líder escolheu no lugar (ver PROJETO.md / CONTEXTO.md sobre a troca).
        solicitacao.setStatus(SolicitacaoStatus.REJEITADA);
        solicitacao.setSubstitutoFinal(substituto);
        solicitacao.setAprovadaPor(usuarioRepository.getReferenceById(liderId));
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

        // Escolha livre: qualquer usuário ativo serve, exceto o próprio dono da escala.
        return usuarioRepository.buscarComPapeis(true).stream()
                .filter(usuario -> !usuario.getId().equals(escala.getUsuario().getId()))
                .map(usuario -> new SubstitutoElegivelResponse(usuario.getId(), usuario.getNome()))
                .toList();
    }

    private void trocarEscala(Escala escalaOriginal, Usuario substituto) {
        escalaOriginal.setStatus(EscalaStatus.SUBSTITUIDA);
        escalaRepository.save(escalaOriginal);

        Escala novaEscala = new Escala();
        novaEscala.setCulto(escalaOriginal.getCulto());
        novaEscala.setUsuario(substituto);
        novaEscala.setInstrumento(escalaOriginal.getInstrumento());
        escalaRepository.save(novaEscala);
    }

    private void validarSubstitutoBasico(Escala escalaOriginal, Usuario candidato) {
        if (!candidato.isAtivo()) {
            throw new SubstitutoNaoElegivelException("Substituto inativo");
        }
        if (candidato.getId().equals(escalaOriginal.getUsuario().getId())) {
            throw new SubstitutoNaoElegivelException("Substituto não pode ser o próprio solicitante");
        }
    }

    private void validarSlotDisponivel(Escala escalaOriginal) {
        if (escalaOriginal.getInstrumento().getCategoria() != CategoriaInstrumento.VOCAL
                && escalaRepository.existsByCultoIdAndInstrumentoIdAndIdNot(
                        escalaOriginal.getCulto().getId(), escalaOriginal.getInstrumento().getId(), escalaOriginal.getId())) {
            throw new SubstitutoNaoElegivelException("Já há alguém escalado neste instrumento neste culto");
        }
    }
}
