package br.com.maranatamusic.application.usuario;

import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.Papel;
import br.com.maranatamusic.domain.exception.UltimoLiderException;
import br.com.maranatamusic.domain.exception.UsuarioNaoEncontradoException;
import br.com.maranatamusic.infrastructure.persistence.UsuarioRepository;
import br.com.maranatamusic.presentation.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
}
