package br.com.maranatamusic.domain.exception;

public class EstadoEscalaInvalidoException extends RuntimeException {

    public EstadoEscalaInvalidoException() {
        super("Escala em estado que não permite confirmação");
    }
}
