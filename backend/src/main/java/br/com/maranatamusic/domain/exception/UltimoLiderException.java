package br.com.maranatamusic.domain.exception;

public class UltimoLiderException extends RuntimeException {

    public UltimoLiderException() {
        super("Não é possível remover o último líder do sistema");
    }
}
