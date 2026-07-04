package br.com.maranatamusic.domain.exception;

public class InstrumentoEmUsoException extends RuntimeException {

    public InstrumentoEmUsoException() {
        super("Instrumento em uso em uma ou mais escalas — não pode ser removido");
    }
}
