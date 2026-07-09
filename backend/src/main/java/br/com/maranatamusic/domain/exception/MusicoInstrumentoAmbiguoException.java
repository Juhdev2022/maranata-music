package br.com.maranatamusic.domain.exception;

public class MusicoInstrumentoAmbiguoException extends RuntimeException {

    public MusicoInstrumentoAmbiguoException() {
        super("Músico tem mais de um instrumento — defina um principal no cadastro");
    }
}
