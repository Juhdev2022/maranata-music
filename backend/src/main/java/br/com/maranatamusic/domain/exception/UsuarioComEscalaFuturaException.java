package br.com.maranatamusic.domain.exception;

public class UsuarioComEscalaFuturaException extends RuntimeException {

    public UsuarioComEscalaFuturaException(long quantidade) {
        super("Usuário possui " + quantidade + " escala(s) futura(s) confirmada(s) — não pode ser desativado");
    }
}
