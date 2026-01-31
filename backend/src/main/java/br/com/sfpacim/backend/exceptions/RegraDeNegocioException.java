package br.com.sfpacim.backend.exceptions;

/**
 * Exceção genérica para violações de regras de negócio da aplicação.
 *
 * <p>
 * Esta exceção deve ser lançada sempre que uma operação não puder ser concluída
 * devido a uma inconsistência lógica ou regra de domínio não atendida
 * (ex: Token expirado).
 *
 * @author Matheus F. N. Pereira
 */
public class RegraDeNegocioException extends RuntimeException {

    /**
     * Construtor que aceita a mensagem de erro.
     *
     * @param mensagem A descrição clara do motivo da falha (será retornada ao
     *                 frontend).
     */
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
