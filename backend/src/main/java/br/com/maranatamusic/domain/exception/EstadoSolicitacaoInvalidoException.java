package br.com.maranatamusic.domain.exception;

public class EstadoSolicitacaoInvalidoException extends RuntimeException {

    public EstadoSolicitacaoInvalidoException() {
        super("Solicitação de substituição já foi resolvida");
    }
}
