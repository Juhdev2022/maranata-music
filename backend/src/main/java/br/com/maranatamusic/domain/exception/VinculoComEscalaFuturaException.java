package br.com.maranatamusic.domain.exception;

public class VinculoComEscalaFuturaException extends RuntimeException {

    public VinculoComEscalaFuturaException() {
        super("Músico tem escala futura confirmada com este instrumento — não pode ser desvinculado");
    }
}
