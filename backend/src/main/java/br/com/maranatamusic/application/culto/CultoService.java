package br.com.maranatamusic.application.culto;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Escala;
import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.CategoriaInstrumento;
import br.com.maranatamusic.domain.enums.EscalaStatus;
import br.com.maranatamusic.domain.exception.CultoNaoEncontradoException;
import br.com.maranatamusic.domain.exception.EscalaNaoEncontradaException;
import br.com.maranatamusic.domain.exception.EstadoEscalaInvalidoException;
import br.com.maranatamusic.domain.exception.InstrumentoJaEscaladoException;
import br.com.maranatamusic.domain.exception.MusicoInstrumentoAmbiguoException;
import br.com.maranatamusic.domain.exception.MusicoJaEscaladoEmOutroCultoException;
import br.com.maranatamusic.domain.exception.MusicoNaoTocaInstrumentoException;
import br.com.maranatamusic.domain.exception.UsuarioInativoException;
import br.com.maranatamusic.domain.exception.UsuarioNaoEncontradoException;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.culto.dto.AtualizarObservacoesRequest;
import br.com.maranatamusic.presentation.culto.dto.CriarCultoRequest;
import br.com.maranatamusic.presentation.culto.dto.CultoDetalheResponse;
import br.com.maranatamusic.presentation.culto.dto.CultoResponse;
import br.com.maranatamusic.presentation.culto.dto.EscalaResumo;
import br.com.maranatamusic.presentation.culto.dto.EscalarMusicoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CultoService {

    private static final long JANELA_HORAS_ANTES = 2;
    private static final long JANELA_HORAS_DEPOIS = 4;

    private final CultoRepository cultoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscalaRepository escalaRepository;
    private final MusicoInstrumentoRepository musicoInstrumentoRepository;

    public CultoService(CultoRepository cultoRepository, UsuarioRepository usuarioRepository,
                         EscalaRepository escalaRepository,
                         MusicoInstrumentoRepository musicoInstrumentoRepository) {
        this.cultoRepository = cultoRepository;
        this.usuarioRepository = usuarioRepository;
        this.escalaRepository = escalaRepository;
        this.musicoInstrumentoRepository = musicoInstrumentoRepository;
    }

    @Transactional
    public CultoResponse criar(CriarCultoRequest request, Long criadoPorId) {
        Culto culto = new Culto();
        culto.setDataHora(request.dataHora());
        culto.setTipo(request.tipo());
        culto.setObservacoes(request.observacoes());
        culto.setRepertorioTrancado(false);

        if (request.ministroId() != null) {
            Usuario ministro = usuarioRepository.findById(request.ministroId())
                    .orElseThrow(() -> new UsuarioNaoEncontradoException(request.ministroId()));
            culto.setMinistro(ministro);
        }

        cultoRepository.save(culto);
        return CultoResponse.from(culto);
    }

    @Transactional(readOnly = true)
    public List<CultoResponse> listarPorMes(YearMonth mes) {
        LocalDateTime inicio = mes.atDay(1).atStartOfDay();
        LocalDateTime fim = mes.atEndOfMonth().atTime(23, 59, 59);

        return cultoRepository.findByDataHoraBetween(inicio, fim).stream()
                .sorted(Comparator.comparing(Culto::getDataHora))
                .map(culto -> CultoResponse.from(culto, (int) escalaRepository.countByCultoId(culto.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CultoDetalheResponse buscarDetalhe(Long cultoId) {
        Culto culto = cultoRepository.findById(cultoId)
                .orElseThrow(() -> new CultoNaoEncontradoException(cultoId));

        List<EscalaResumo> equipe = escalaRepository.findByCultoIdComUsuarioEInstrumento(cultoId).stream()
                .map(EscalaResumo::from)
                .toList();

        return CultoDetalheResponse.from(culto, equipe);
    }

    @Transactional
    public EscalaResumo escalar(Long cultoId, EscalarMusicoRequest request) {
        Culto culto = cultoRepository.findById(cultoId)
                .orElseThrow(() -> new CultoNaoEncontradoException(cultoId));

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(request.usuarioId()));
        if (!usuario.isAtivo()) {
            throw new UsuarioInativoException(usuario.getId());
        }

        Instrumento instrumento = resolverInstrumento(usuario.getId());

        // Vocais aceitam quantidade ilimitada por culto; demais categorias, só 1 pessoa por instrumento.
        if (instrumento.getCategoria() != CategoriaInstrumento.VOCAL
                && escalaRepository.existsByCultoIdAndInstrumentoId(cultoId, instrumento.getId())) {
            throw new InstrumentoJaEscaladoException();
        }

        LocalDateTime inicioJanela = culto.getDataHora().minusHours(JANELA_HORAS_ANTES);
        LocalDateTime fimJanela = culto.getDataHora().plusHours(JANELA_HORAS_DEPOIS);
        if (escalaRepository.existeEscalaAtivaNaJanela(usuario.getId(), inicioJanela, fimJanela)) {
            throw new MusicoJaEscaladoEmOutroCultoException();
        }

        Escala escala = new Escala();
        escala.setCulto(culto);
        escala.setUsuario(usuario);
        escala.setInstrumento(instrumento);

        escalaRepository.save(escala);
        return EscalaResumo.from(escala);
    }

    private Instrumento resolverInstrumento(Long usuarioId) {
        List<MusicoInstrumento> vinculos = musicoInstrumentoRepository.findByUsuarioIdComDetalhes(usuarioId);
        if (vinculos.isEmpty()) {
            throw new MusicoNaoTocaInstrumentoException();
        }

        Optional<MusicoInstrumento> principal = musicoInstrumentoRepository.findPrincipalPorUsuario(usuarioId);
        if (principal.isPresent()) {
            return principal.get().getInstrumento();
        }
        if (vinculos.size() == 1) {
            return vinculos.get(0).getInstrumento();
        }
        throw new MusicoInstrumentoAmbiguoException();
    }

    @Transactional
    public CultoResponse atualizarObservacoes(Long cultoId, AtualizarObservacoesRequest request) {
        Culto culto = cultoRepository.findById(cultoId)
                .orElseThrow(() -> new CultoNaoEncontradoException(cultoId));
        culto.setObservacoes(request.observacoes());
        cultoRepository.save(culto);
        return CultoResponse.from(culto);
    }

    @Transactional
    public void removerEscala(Long cultoId, Long escalaId) {
        Escala escala = escalaRepository.findById(escalaId)
                .orElseThrow(() -> new EscalaNaoEncontradaException(escalaId));
        if (!escala.getCulto().getId().equals(cultoId)) {
            throw new EscalaNaoEncontradaException(escalaId);
        }
        if (escala.getStatus() == EscalaStatus.RECUSADA || escala.getStatus() == EscalaStatus.SUBSTITUIDA) {
            throw new EstadoEscalaInvalidoException();
        }
        escalaRepository.delete(escala);
    }
}
