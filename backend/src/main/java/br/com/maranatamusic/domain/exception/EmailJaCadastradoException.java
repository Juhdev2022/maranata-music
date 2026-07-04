package br.com.maranatamusic.domain.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("Email já cadastrado: " + email);
    }
}
