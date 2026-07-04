package br.com.maranatamusic.domain.exception;

public class MusicoInstrumentoNaoEncontradoException extends RuntimeException {

    public MusicoInstrumentoNaoEncontradoException(Long usuarioId, Long instrumentoId) {
        super("Vínculo não encontrado entre usuário " + usuarioId + " e instrumento " + instrumentoId);
    }
}
