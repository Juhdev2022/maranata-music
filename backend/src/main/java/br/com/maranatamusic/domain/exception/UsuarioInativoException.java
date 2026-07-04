package br.com.maranatamusic.domain.exception;

public class UsuarioInativoException extends RuntimeException {

    public UsuarioInativoException(Long id) {
        super("Usuário inativo: " + id);
    }
}
