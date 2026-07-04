package br.com.maranatamusic.presentation.usuario.dto;

import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.Papel;

import java.util.Set;

public record UsuarioResumoResponse(
        Long id,
        String nome,
        String email,
        Set<Papel> papeis,
        boolean ativo
) {

    public static UsuarioResumoResponse from(Usuario usuario) {
        return new UsuarioResumoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPapeis(),
                usuario.isAtivo()
        );
    }
}
