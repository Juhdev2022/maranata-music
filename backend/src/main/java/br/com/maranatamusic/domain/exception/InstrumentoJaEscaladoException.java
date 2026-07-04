package br.com.maranatamusic.domain.exception;

public class InstrumentoJaEscaladoException extends RuntimeException {

    public InstrumentoJaEscaladoException() {
        super("Já há alguém escalado neste instrumento neste culto");
    }
}
