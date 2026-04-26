package br.com.sfpacim.backend.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entidade JPA que representa uma movimentação financeira (Receita ou Despesa).
 *
 * <p>
 * Centraliza as operações financeiras do sistema, vinculando valores a
 * categorias, pessoas e contas bancárias.
 *
 * @author Matheus F. N. Pereira
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "lancamentos")
public class Transacao {

    /**
     * Identificador único do lançamento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Descrição curta do lançamento (ex: "Compras no Mercado", "Salário Mensal").
     */
    @Column(nullable = false)
    private String descricao;

    /**
     * Valor monetário da transação.
     * Sempre armazenado positivo. O {@link TipoTransacao} define se é débito ou
     * crédito.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    /**
     * Data de competência (quando o fato gerador ocorreu).
     */
    @Column(nullable = false)
    private LocalDate dataCompetencia;

    /**
     * Data de vencimento (prazo limite para pagamento ou recebimento).
     */
    @Column(nullable = false)
    private LocalDate dataVencimento;

    /**
     * Data da efetivação do pagamento/recebimento.
     * 
     * <p>
     * Se {@code null}, o lançamento é considerado PENDENTE implicitamente,
     * mas o campo {@code status} deve ser a fonte da verdade.
     */
    @Column(nullable = true)
    private LocalDate dataPagamento;

    /**
     * Natureza da operação.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo;

    /**
     * Situação atual do lançamento.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTransacao status;

    /**
     * Observações adicionais ou detalhes da transação.
     */
    @Column(length = 500)
    private String observacao;

    /**
     * Usuário proprietário do lançamento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    /**
     * Categoria da transação (ex: Alimentação, Transporte).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @ToString.Exclude
    private Categoria categoria;

    /**
     * Conta bancária/carteira de onde sai ou entra o dinheiro.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false)
    @ToString.Exclude
    private Conta conta;

    /**
     * Pessoa vinculada a transação.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = true)
    @ToString.Exclude
    private Pessoa pessoa;

    /**
     * Verifica se o lançamento já foi liquidado.
     */
    public boolean isPago() {
        return StatusTransacao.PAGO.equals(this.status);
    }
}
