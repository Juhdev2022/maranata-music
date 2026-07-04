package br.com.maranatamusic.application.culto;

import br.com.maranatamusic.domain.Culto;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.exception.CultoNaoEncontradoException;
import br.com.maranatamusic.domain.exception.UsuarioNaoEncontradoException;
import br.com.maranatamusic.infrastructure.persistence.CultoRepository;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.culto.dto.CriarCultoRequest;
import br.com.maranatamusic.presentation.culto.dto.CultoDetalheResponse;
import br.com.maranatamusic.presentation.culto.dto.CultoResponse;
import br.com.maranatamusic.presentation.culto.dto.EscalaResumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
public class CultoService {

    private final CultoRepository cultoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscalaRepository escalaRepository;

    public CultoService(CultoRepository cultoRepository, UsuarioRepository usuarioRepository,
                         EscalaRepository escalaRepository) {
        this.cultoRepository = cultoRepository;
        this.usuarioRepository = usuarioRepository;
        this.escalaRepository = escalaRepository;
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
                .map(CultoResponse::from)
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
}
