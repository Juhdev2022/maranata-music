package br.com.maranatamusic.domain.exception;

public class InstrumentoJaCadastradoException extends RuntimeException {

    public InstrumentoJaCadastradoException(String nome) {
        super("Instrumento já cadastrado: " + nome);
    }
}
