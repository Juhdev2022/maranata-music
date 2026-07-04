package br.com.maranatamusic.application.usuario;

import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.domain.exception.UltimoLiderException;
import br.com.maranatamusic.domain.exception.UsuarioComEscalaFuturaException;
import br.com.maranatamusic.domain.exception.UsuarioNaoEncontradoException;
import br.com.maranatamusic.infrastructure.persistence.EscalaRepository;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.usuario.dto.UsuarioResponse;
import br.com.maranatamusic.presentation.usuario.dto.UsuarioResumoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EscalaRepository escalaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, EscalaRepository escalaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.escalaRepository = escalaRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumoResponse> listar(boolean ativos, Papel papel) {
        return usuarioRepository.buscarComPapeis(ativos).stream()
                .filter(usuario -> papel == null || usuario.getPapeis().contains(papel))
                .map(UsuarioResumoResponse::from)
                .toList();
    }

    @Transactional
    public UsuarioResponse adicionarPapel(Long usuarioId, Papel papel) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        usuario.getPapeis().add(papel);
        usuarioRepository.save(usuario);
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void removerPapel(Long usuarioId, Papel papel) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        if (papel == Papel.LIDER && usuario.getPapeis().contains(Papel.LIDER)
                && usuarioRepository.countByPapelEAtivo(Papel.LIDER) <= 1) {
            throw new UltimoLiderException();
        }

        usuario.getPapeis().remove(papel);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativar(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        if (usuario.getPapeis().contains(Papel.LIDER) && usuarioRepository.countByPapelEAtivo(Papel.LIDER) <= 1) {
            throw new UltimoLiderException();
        }

        long escalasFuturas = escalaRepository.countEscalasFuturasConfirmadas(usuarioId, LocalDateTime.now());
        if (escalasFuturas > 0) {
            throw new UsuarioComEscalaFuturaException(escalasFuturas);
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponse reativar(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
        usuario.getPapeis().size(); // força carregamento da coleção LAZY antes da sessão fechar (serialização é fora da transação)
        return UsuarioResponse.from(usuario);
    }
}
