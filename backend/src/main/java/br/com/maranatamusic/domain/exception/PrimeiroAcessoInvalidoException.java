package br.com.maranatamusic.domain.exception;

/**
 * Lançada para qualquer motivo que impeça o primeiro acesso (email não encontrado,
 * usuário inativo, ou usuário que já definiu senha). Mensagem propositalmente
 * genérica — não deve revelar qual dessas condições ocorreu.
 */
public class PrimeiroAcessoInvalidoException extends RuntimeException {

    public PrimeiroAcessoInvalidoException() {
        super("Não foi possível completar o primeiro acesso");
    }
}
