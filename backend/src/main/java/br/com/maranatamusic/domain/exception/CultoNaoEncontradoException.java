package br.com.maranatamusic.domain.exception;

public class CultoNaoEncontradoException extends RuntimeException {

    public CultoNaoEncontradoException(Long id) {
        super("Culto não encontrado: " + id);
    }
}
