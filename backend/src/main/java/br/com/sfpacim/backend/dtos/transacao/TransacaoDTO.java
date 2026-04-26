package br.com.sfpacim.backend.dtos.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Positive;

import br.com.sfpacim.backend.models.Transacao;
import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados completos de uma
 * {@link Transacao}.
 *
 * @author Matheus F. N. Pereira
 */
@Schema(description = "DTO para representar uma transação financeira (Receita ou Despesa).")
public record TransacaoDTO(

        @Schema(description = "Identificador único da transação.", example = "a1b2c3d4-e5f6-7890-1234-56789abcdef0") //
        UUID id,

        @Schema(description = "Descrição curta do lançamento.", example = "Compras no Supermercado") //
        String descricao,

        @Schema(description = "Valor monetário da transação.", example = "450.50") //
        @Positive(message = "O valor da transação deve ser maior que zero.") //
        BigDecimal valor,

        @Schema(description = "Data de competência (Fato gerador).", example = "2026-02-10") //
        LocalDate dataCompetencia,

        @Schema(description = "Data de vencimento.", example = "2026-02-15") //
        LocalDate dataVencimento,

        @Schema(description = "Data do efetivo pagamento (pode ser nulo).", example = "2026-02-14") //
        LocalDate dataPagamento,

        @Schema(description = "Tipo da transação (RECEITA ou DESPESA).", example = "DESPESA") //
        TipoTransacao tipo,

        @Schema(description = "Status atual (PENDENTE ou PAGO).", example = "PAGO") //
        StatusTransacao status,

        @Schema(description = "Observações adicionais.", example = "Compra do mês incluindo itens de limpeza.") //
        String observacao,

        @Schema(description = "ID da categoria vinculada.") //
        UUID categoriaId,

        @Schema(description = "ID da conta vinculada.", example = "b2c3d4e5-f6a7-8901-2345-67890abcdef1") //
        UUID contaId,

        @Schema(description = "ID da pessoa vinculada (Opcional).", example = "c3d4e5f6-a7b8-9012-3456-7890abcdef2") //
        UUID pessoaId) {

    /**
     * Construtor customizado para mapear a entidade {@link Transacao}.
     *
     * @param transacao A entidade Transacao.
     */
    public TransacaoDTO(Transacao transacao) {
        this(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getDataCompetencia(),
                transacao.getDataVencimento(),
                transacao.getDataPagamento(),
                transacao.getTipo(),
                transacao.getStatus(),
                transacao.getObservacao(),
                transacao.getCategoria().getId(),
                transacao.getConta().getId(),
                transacao.getPessoa() != null ? transacao.getPessoa().getId() : null);
    }
}
