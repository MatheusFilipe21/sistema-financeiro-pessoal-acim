package br.com.sfpacim.backend.models;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidade JPA que representa uma conta bancária ou carteira digital no
 * sistema.
 *
 * <p>
 * Armazena o saldo financeiro e serve como origem/destino de transações.
 * Possui validação de unicidade composta, garantindo que uma {@link Pessoa} não
 * possua contas com o mesmo nome dentro da mesma instituição financeira.
 *
 * @author Matheus F. N. Pereira
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "contas", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "nome", "pessoa_id",
                "instituicao" })
})
public class Conta {

    /**
     * Identificador único da conta.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Nome de identificação da conta (ex: "Conta Corrente", "Poupança").
     * Obrigatório. Deve ser único para a combinação de pessoa titular e
     * instituição.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * Instituição financeira vinculada.
     * Define o ícone e identidade visual no frontend.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstituicaoFinanceira instituicao;

    /**
     * Saldo inicial informado no momento do cadastro.
     * Serve como ponto de partida para o histórico.
     */
    @Setter(AccessLevel.NONE)
    @Column(name = "saldo_inicial", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoInicial;

    /**
     * Saldo atualizado em tempo real.
     * 
     * <p>
     * Armazenado fisicamente para evitar somas pesadas a cada leitura
     * (Performance).
     * Fórmula: Saldo Inicial + Receitas - Despesas.
     */
    @Setter(AccessLevel.NONE)
    @Column(name = "saldo_atual", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoAtual;

    /**
     * Pessoa titular desta conta.
     * Obrigatório.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    @ToString.Exclude
    private Pessoa pessoa;

    /**
     * Construtor para o cadastro de novas contas.
     * 
     * <p>
     * Inicializa o {@code saldoAtual} com o mesmo valor do {@code saldoInicial}.
     *
     * @param nome         Nome da conta.
     * @param instituicao  Instituição financeira.
     * @param saldoInicial Valor inicial da conta.
     * @param pessoa       Pessoa titular.
     */
    public Conta(String nome, InstituicaoFinanceira instituicao, BigDecimal saldoInicial, Pessoa pessoa) {
        if (saldoInicial == null) {
            throw new IllegalArgumentException(
                    "O saldo inicial não pode ser nulo. Use zero caso a conta esteja vazia.");
        }

        this.nome = nome;
        this.instituicao = instituicao;
        this.saldoInicial = saldoInicial;
        this.saldoAtual = saldoInicial;
        this.pessoa = pessoa;
    }

    /**
     * Altera o saldo inicial e reajusta o saldo atual automaticamente
     * com base na diferença.
     *
     * @param novoSaldoInicial O novo valor de saldo inicial informado.
     */
    public void alterarSaldoInicial(BigDecimal novoSaldoInicial) {
        if (novoSaldoInicial == null) {
            throw new IllegalArgumentException("O novo saldo inicial não pode ser nulo.");
        }

        BigDecimal diferenca = novoSaldoInicial.subtract(this.saldoInicial);
        this.saldoAtual = this.saldoAtual.add(diferenca);
        this.saldoInicial = novoSaldoInicial;
    }

    /**
     * Adiciona um valor ao saldo atual da conta.
     *
     * @param valor O valor a ser creditado. Deve ser maior que zero.
     */
    public void creditar(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor para crédito deve ser maior que zero.");
        }

        this.saldoAtual = this.saldoAtual.add(valor);
    }

    /**
     * Subtrai um valor do saldo atual da conta.
     *
     * @param valor O valor a ser debitado. Deve ser maior que zero.
     */
    public void debitar(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor para débito deve ser maior que zero.");
        }

        this.saldoAtual = this.saldoAtual.subtract(valor);
    }
}
