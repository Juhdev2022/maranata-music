package br.com.maranatamusic.application.instrumento;

import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.exception.InstrumentoEmUsoException;
import br.com.maranatamusic.domain.exception.InstrumentoJaCadastradoException;
import br.com.maranatamusic.domain.exception.InstrumentoNaoEncontradoException;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.presentation.instrumento.dto.CriarInstrumentoRequest;
import br.com.maranatamusic.presentation.instrumento.dto.InstrumentoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InstrumentoService {

    private final InstrumentoRepository instrumentoRepository;
    private final EscalaRepository escalaRepository;
    private final MusicoInstrumentoRepository musicoInstrumentoRepository;

    public InstrumentoService(InstrumentoRepository instrumentoRepository, EscalaRepository escalaRepository,
                               MusicoInstrumentoRepository musicoInstrumentoRepository) {
        this.instrumentoRepository = instrumentoRepository;
        this.escalaRepository = escalaRepository;
        this.musicoInstrumentoRepository = musicoInstrumentoRepository;
    }

    @Transactional
    public InstrumentoResponse criar(CriarInstrumentoRequest request) {
        if (instrumentoRepository.existsByNomeIgnoreCase(request.nome())) {
            throw new InstrumentoJaCadastradoException(request.nome());
        }

        Instrumento instrumento = new Instrumento();
        instrumento.setNome(request.nome());
        instrumento.setCategoria(request.categoria());

        instrumentoRepository.save(instrumento);
        return InstrumentoResponse.from(instrumento);
    }

    @Transactional(readOnly = true)
    public List<InstrumentoResponse> listar() {
        return instrumentoRepository.findAllByOrderByCategoriaAscNomeAsc().stream()
                .map(InstrumentoResponse::from)
                .toList();
    }

    @Transactional
    public void remover(Long id) {
        Instrumento instrumento = instrumentoRepository.findById(id)
                .orElseThrow(() -> new InstrumentoNaoEncontradoException(id));

        if (escalaRepository.existsByInstrumentoId(id) || musicoInstrumentoRepository.existsByInstrumentoId(id)) {
            throw new InstrumentoEmUsoException();
        }

        instrumentoRepository.delete(instrumento);
    }
}
