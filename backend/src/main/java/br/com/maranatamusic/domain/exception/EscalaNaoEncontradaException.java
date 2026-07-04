package br.com.maranatamusic.domain.exception;

public class EscalaNaoEncontradaException extends RuntimeException {

    public EscalaNaoEncontradaException(Long id) {
        super("Escala não encontrada: " + id);
    }
}
