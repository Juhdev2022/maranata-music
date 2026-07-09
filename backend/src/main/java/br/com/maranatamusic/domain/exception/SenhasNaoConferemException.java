package br.com.maranatamusic.domain.exception;

public class SenhasNaoConferemException extends RuntimeException {

    public SenhasNaoConferemException() {
        super("As senhas não conferem");
    }
}
