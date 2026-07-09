package br.com.maranatamusic.domain.exception;

public class SubstitutoObrigatorioException extends RuntimeException {

    public SubstitutoObrigatorioException() {
        super("substitutoFinalId é obrigatório quando a solicitação não tem substituto sugerido");
    }
}
