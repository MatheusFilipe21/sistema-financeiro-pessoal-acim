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
 *
 * @param id              Identificador único da transação.
 * @param descricao       Descrição curta do lançamento.
 * @param valor           Valor monetário da transação.
 * @param dataCompetencia Data de competência (Fato gerador).
 * @param dataVencimento  Data de vencimento.
 * @param dataPagamento   Data do efetivo pagamento (pode ser nulo).
 * @param tipo            Tipo da transação (RECEITA ou DESPESA).
 * @param status          Status atual (PENDENTE ou PAGO).
 * @param observacao      Observações adicionais.
 * @param categoriaId     ID da categoria vinculada.
 * @param contaId         ID da conta vinculada.
 * @param pessoaId        ID da pessoa vinculada (Opcional).
 */
@Schema(description = "${transacao.descricao.schema.leitura}")
public record TransacaoDTO(

        @Schema(description = "${transacao.descricao.id}", example = "${transacao.exemplo.id}") //
        UUID id,

        @Schema(description = "${transacao.descricao.descricao.curta}", example = "${transacao.exemplo.descricao.extrato}") //
        String descricao,

        @Schema(description = "${transacao.descricao.valor}", example = "${transacao.exemplo.valor.extrato}") //
        @Positive(message = "{transacao.validacao.valor.positivo}") //
        BigDecimal valor,

        @Schema(description = "${transacao.descricao.data-competencia}", example = "${transacao.exemplo.data-competencia}") //
        LocalDate dataCompetencia,

        @Schema(description = "${transacao.descricao.data-vencimento}", example = "${transacao.exemplo.data-vencimento}") //
        LocalDate dataVencimento,

        @Schema(description = "${transacao.descricao.data-pagamento}", example = "${transacao.exemplo.data-pagamento}") //
        LocalDate dataPagamento,

        @Schema(description = "${transacao.descricao.tipo}", example = "${transacao.exemplo.tipo}") //
        TipoTransacao tipo,

        @Schema(description = "${transacao.descricao.status}", example = "${transacao.exemplo.status}") //
        StatusTransacao status,

        @Schema(description = "${transacao.descricao.observacao}", example = "${transacao.exemplo.observacao}") //
        String observacao,

        @Schema(description = "${transacao.descricao.categoria-id}", example = "${categoria.exemplo.id}") //
        UUID categoriaId,

        @Schema(description = "${transacao.descricao.conta-id}", example = "${conta.exemplo.id}") //
        UUID contaId,

        @Schema(description = "${transacao.descricao.pessoa-id}", example = "${pessoa.exemplo.id}") //
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
