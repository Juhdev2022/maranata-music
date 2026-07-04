package br.com.maranatamusic.domain.exception;

public class AcessoNaoAutorizadoException extends RuntimeException {

    public AcessoNaoAutorizadoException() {
        super("Você não pode confirmar escala de outro músico");
    }
}
