package br.com.maranatamusic.domain.exception;

public class MusicoNaoTocaInstrumentoException extends RuntimeException {

    public MusicoNaoTocaInstrumentoException() {
        super("Este músico não toca este instrumento");
    }
}
