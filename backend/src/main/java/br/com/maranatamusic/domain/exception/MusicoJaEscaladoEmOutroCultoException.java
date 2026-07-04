package br.com.maranatamusic.domain.exception;

public class MusicoJaEscaladoEmOutroCultoException extends RuntimeException {

    public MusicoJaEscaladoEmOutroCultoException() {
        super("Músico já escalado em outro culto próximo a este horário");
    }
}
