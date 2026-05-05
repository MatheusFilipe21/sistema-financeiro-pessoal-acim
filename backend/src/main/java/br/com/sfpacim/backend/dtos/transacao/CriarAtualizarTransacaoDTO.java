package br.com.sfpacim.backend.dtos.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para encapsular os dados de entrada para a
 * criação ou atualização de uma transação.
 *
 * Este record aplica as validações de negócio usando o Spring Validation.
 *
 * @author Matheus F. N. Pereira
 *
 * @param descricao       A descrição da transação.
 * @param valor           O valor da transação (deve ser positivo).
 * @param dataCompetencia A data de competência (fato gerador).
 * @param dataVencimento  A data de vencimento.
 * @param dataPagamento   A data do efetivo pagamento (opcional).
 * @param tipo            O tipo da transação (RECEITA ou DESPESA).
 * @param status          O status da transação (PENDENTE ou PAGO).
 * @param observacao      Observações adicionais (opcional).
 * @param categoriaId     O identificador único da categoria vinculada.
 * @param contaId         O identificador único da conta vinculada.
 * @param pessoaId        O identificador único da pessoa vinculada (opcional).
 */
@Schema(description = "${transacao.descricao.schema.criacao-atualizacao}")
public record CriarAtualizarTransacaoDTO(

        @Schema(description = "${transacao.descricao.descricao}", example = "${transacao.exemplo.descricao}") //
        @NotBlank(message = "{transacao.validacao.descricao.obrigatoria}") //
        @Size(max = 255, message = "{transacao.validacao.descricao.tamanho}") //
        String descricao,

        @Schema(description = "${transacao.descricao.valor.criacao}", example = "${transacao.exemplo.valor}") //
        @NotNull(message = "{transacao.validacao.valor.obrigatorio}") //
        @Positive(message = "{transacao.validacao.valor.positivo}") //
        BigDecimal valor,

        @Schema(description = "${transacao.descricao.data-competencia}", example = "${transacao.exemplo.data-competencia}") //
        @NotNull(message = "{transacao.validacao.data-competencia.obrigatoria}") //
        LocalDate dataCompetencia,

        @Schema(description = "${transacao.descricao.data-vencimento}", example = "${transacao.exemplo.data-vencimento}") //
        @NotNull(message = "{transacao.validacao.data-vencimento.obrigatoria}") //
        LocalDate dataVencimento,

        @Schema(description = "${transacao.descricao.data-pagamento}", example = "${transacao.exemplo.data-pagamento}") //
        LocalDate dataPagamento,

        @Schema(description = "${transacao.descricao.tipo}", example = "${transacao.exemplo.tipo}") //
        @NotNull(message = "{geral.validacao.tipo.obrigatorio}") //
        TipoTransacao tipo,

        @Schema(description = "${transacao.descricao.status}", example = "${transacao.exemplo.status}") //
        @NotNull(message = "{transacao.validacao.status.obrigatorio}") //
        StatusTransacao status,

        @Schema(description = "${transacao.descricao.observacao}", example = "${transacao.exemplo.observacao}") //
        @Size(max = 500, message = "{transacao.validacao.observacao.tamanho}") //
        String observacao,

        @Schema(description = "${transacao.descricao.categoria-id}", example = "${categoria.exemplo.id}") //
        @NotNull(message = "{transacao.validacao.categoria.obrigatoria}") //
        UUID categoriaId,

        @Schema(description = "${transacao.descricao.conta-id}", example = "${conta.exemplo.id}") //
        @NotNull(message = "{transacao.validacao.conta.obrigatoria}") //
        UUID contaId,

        @Schema(description = "${transacao.descricao.pessoa-id}", example = "${pessoa.exemplo.id}") //
        UUID pessoaId) {
}
