package br.com.maranatamusic.presentation.exception;

import java.util.Map;

public record ErroResponse(String erro, Map<String, String> campos) {

    public static ErroResponse simples(String mensagem) {
        return new ErroResponse(mensagem, null);
    }
}
