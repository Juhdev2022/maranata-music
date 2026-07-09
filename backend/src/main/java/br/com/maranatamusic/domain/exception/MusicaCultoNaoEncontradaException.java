package br.com.maranatamusic.domain.exception;

public class MusicaCultoNaoEncontradaException extends RuntimeException {

    public MusicaCultoNaoEncontradaException(Long id) {
        super("Música não encontrada no repertório: " + id);
    }
}
