package br.com.maranatamusic.presentation.usuario.dto;

import br.com.maranatamusic.domain.Usuario;
import br.com.maranatamusic.domain.enums.Papel;

import java.util.Set;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        boolean ativo,
        Set<Papel> papeis
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.getPapeis()
        );
    }
}
