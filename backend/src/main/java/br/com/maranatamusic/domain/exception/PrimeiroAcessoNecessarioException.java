package br.com.maranatamusic.domain.exception;

public class PrimeiroAcessoNecessarioException extends RuntimeException {

    public PrimeiroAcessoNecessarioException() {
        super("Defina sua senha em Primeiro acesso");
    }
}
