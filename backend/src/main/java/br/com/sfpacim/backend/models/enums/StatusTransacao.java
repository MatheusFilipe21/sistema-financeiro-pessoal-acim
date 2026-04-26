package br.com.sfpacim.backend.models.enums;

import lombok.Getter;

/**
 * Enum que define o estágio de liquidação de um lançamento financeiro.
 *
 * <p>
 * Este status é o gatilho para a atualização do saldo bancário:
 * <ul>
 * <li>{@code PENDENTE}: Apenas uma previsão. Não afeta o saldo atual da
 * conta.</li>
 * <li>{@code PAGO}: A transação foi efetivada. O valor já foi debitado ou
 * creditado na conta.</li>
 * </ul>
 *
 * @author Matheus F. N. Pereira
 */
@Getter
public enum StatusTransacao {

    PENDENTE("Pendente"),
    PAGO("Pago");

    /**
     * Descrição textual e legível do status atual do lançamento.
     */
    private final String descricao;

    /**
     * Construtor do enum.
     *
     * @param descricao O nome amigável do status para exibição.
     */
    StatusTransacao(String descricao) {
        this.descricao = descricao;
    }
}
