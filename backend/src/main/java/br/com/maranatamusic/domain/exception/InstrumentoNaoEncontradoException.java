package br.com.maranatamusic.domain.exception;

public class InstrumentoNaoEncontradoException extends RuntimeException {

    public InstrumentoNaoEncontradoException(Long id) {
        super("Instrumento não encontrado: " + id);
    }
}
