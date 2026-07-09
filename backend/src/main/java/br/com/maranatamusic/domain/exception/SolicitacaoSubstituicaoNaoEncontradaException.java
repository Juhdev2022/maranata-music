package br.com.maranatamusic.domain.exception;

public class SolicitacaoSubstituicaoNaoEncontradaException extends RuntimeException {

    public SolicitacaoSubstituicaoNaoEncontradaException(Long id) {
        super("Solicitação de substituição não encontrada: " + id);
    }
}
