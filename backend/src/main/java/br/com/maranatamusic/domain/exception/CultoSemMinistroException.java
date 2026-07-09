package br.com.maranatamusic.domain.exception;

public class CultoSemMinistroException extends RuntimeException {

    public CultoSemMinistroException() {
        super("Culto sem ministro designado");
    }
}
