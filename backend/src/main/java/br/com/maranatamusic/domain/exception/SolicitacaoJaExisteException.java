package br.com.maranatamusic.domain.exception;

public class SolicitacaoJaExisteException extends RuntimeException {

    public SolicitacaoJaExisteException() {
        super("Já existe uma solicitação de substituição aberta para esta escala");
    }
}
