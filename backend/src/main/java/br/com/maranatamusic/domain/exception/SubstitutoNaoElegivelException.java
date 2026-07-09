package br.com.maranatamusic.domain.exception;

public class SubstitutoNaoElegivelException extends RuntimeException {

    public SubstitutoNaoElegivelException(String motivo) {
        super(motivo);
    }
}
