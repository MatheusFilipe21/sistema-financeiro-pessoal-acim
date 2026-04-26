package br.com.sfpacim.backend.models.enums;

import lombok.Getter;

/**
 * Enum que define a natureza da operação financeira (Entrada ou Saída).
 *
 * <p>
 * Fundamental para o cálculo de saldo:
 * <ul>
 * <li>{@code RECEITA}: Incrementa o saldo da conta.</li>
 * <li>{@code DESPESA}: Decrementa o saldo da conta.</li>
 * </ul>
 *
 * @author Matheus F. N. Pereira
 */
@Getter
public enum TipoTransacao {

    RECEITA("Receita"),
    DESPESA("Despesa");

    /**
     * Nome amigável da natureza da operação financeira.
     */
    private final String descricao;

    /**
     * Construtor do enum.
     *
     * @param descricao O nome amigável do tipo para exibição em relatórios e na UI.
     */
    TipoTransacao(String descricao) {
        this.descricao = descricao;
    }
}
