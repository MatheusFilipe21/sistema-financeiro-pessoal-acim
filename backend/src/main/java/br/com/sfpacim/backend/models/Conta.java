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
 * Possui validação de unicidade para não permitir nomes repetidos para a mesma
 * {@link Pessoa}.
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
        @UniqueConstraint(columnNames = { "nome", "pessoa_id" })
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
     * Nome de identificação da conta (ex: "Nubank", "Cofre").
     * Obrigatório e deve ser único para a pessoa vinculada.
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
        this.nome = nome;
        this.instituicao = instituicao;
        this.saldoInicial = saldoInicial;
        this.saldoAtual = saldoInicial;
        this.pessoa = pessoa;
    }
}