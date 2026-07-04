package br.com.maranatamusic.application.musico;

import br.com.maranatamusic.domain.Instrumento;
import br.com.maranatamusic.domain.MusicoInstrumento;
import br.com.maranatamusic.domain.MusicoInstrumentoId;
import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.exception.InstrumentoNaoEncontradoException;
import br.com.maranatamusic.domain.exception.MusicoInstrumentoNaoEncontradoException;
import br.com.maranatamusic.domain.exception.UsuarioNaoEncontradoException;
import br.com.maranatamusic.domain.exception.VinculoComEscalaFuturaException;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.InstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.MusicoInstrumentoRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.musico.dto.MusicoInstrumentoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MusicoInstrumentoService {

    private final MusicoInstrumentoRepository musicoInstrumentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final EscalaRepository escalaRepository;

    public MusicoInstrumentoService(MusicoInstrumentoRepository musicoInstrumentoRepository,
                                     UsuarioRepository usuarioRepository,
                                     InstrumentoRepository instrumentoRepository,
                                     EscalaRepository escalaRepository) {
        this.musicoInstrumentoRepository = musicoInstrumentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.instrumentoRepository = instrumentoRepository;
        this.escalaRepository = escalaRepository;
    }

    @Transactional
    public ResultadoVinculo vincular(Long usuarioId, Long instrumentoId, boolean principal) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
        Instrumento instrumento = instrumentoRepository.findById(instrumentoId)
                .orElseThrow(() -> new InstrumentoNaoEncontradoException(instrumentoId));

        MusicoInstrumentoId id = new MusicoInstrumentoId(usuarioId, instrumentoId);
        Optional<MusicoInstrumento> existente = musicoInstrumentoRepository.findById(id);

        if (principal) {
            musicoInstrumentoRepository.findPrincipalPorUsuario(usuarioId)
                    .filter(outro -> !outro.getId().equals(id))
                    .ifPresent(outro -> {
                        outro.setPrincipal(false);
                        musicoInstrumentoRepository.save(outro);
                    });
        }

        if (existente.isPresent()) {
            MusicoInstrumento vinculo = existente.get();
            boolean alterado = vinculo.isPrincipal() != principal;
            if (alterado) {
                vinculo.setPrincipal(principal);
                musicoInstrumentoRepository.save(vinculo);
            }
            return new ResultadoVinculo(MusicoInstrumentoResponse.from(vinculo), false, alterado);
        }

        MusicoInstrumento novo = new MusicoInstrumento();
        novo.setId(id);
        novo.setUsuario(usuario);
        novo.setInstrumento(instrumento);
        novo.setPrincipal(principal);
        musicoInstrumentoRepository.save(novo);

        return new ResultadoVinculo(MusicoInstrumentoResponse.from(novo), true, false);
    }

    @Transactional
    public void desvincular(Long usuarioId, Long instrumentoId) {
        MusicoInstrumentoId id = new MusicoInstrumentoId(usuarioId, instrumentoId);
        MusicoInstrumento vinculo = musicoInstrumentoRepository.findById(id)
                .orElseThrow(() -> new MusicoInstrumentoNaoEncontradoException(usuarioId, instrumentoId));

        long escalasFuturas = escalaRepository.countEscalasFuturasConfirmadasPorInstrumento(
                usuarioId, instrumentoId, LocalDateTime.now());
        if (escalasFuturas > 0) {
            throw new VinculoComEscalaFuturaException();
        }

        musicoInstrumentoRepository.delete(vinculo);
    }

    @Transactional(readOnly = true)
    public List<MusicoInstrumentoResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new UsuarioNaoEncontradoException(usuarioId);
        }

        return musicoInstrumentoRepository.findByUsuarioIdComDetalhes(usuarioId).stream()
                .map(MusicoInstrumentoResponse::from)
                .toList();
    }
}
