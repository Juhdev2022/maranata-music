package br.com.maranatamusic.domain.exception;

public class ObservacaoLiderObrigatoriaException extends RuntimeException {

    public ObservacaoLiderObrigatoriaException() {
        super("Observação obrigatória quando a revisão é COM_OBSERVACAO");
    }
}
