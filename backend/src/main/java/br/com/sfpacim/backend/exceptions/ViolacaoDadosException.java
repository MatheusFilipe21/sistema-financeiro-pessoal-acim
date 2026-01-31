package br.com.sfpacim.backend.exceptions;

/**
 * Exceção customizada para erros de regras de negócio que violam
 * a integridade dos dados (ex: e-mail duplicado - RF04).
 *
 * @author Matheus F. N. Pereira
 */
public class ViolacaoDadosException extends RuntimeException {

    /**
     * Construtor que aceita a mensagem de erro.
     *
     * @param mensagem A descrição do erro (ex: "O e-mail já está cadastrado").
     */
    public ViolacaoDadosException(String mensagem) {
        super(mensagem);
    }
}
