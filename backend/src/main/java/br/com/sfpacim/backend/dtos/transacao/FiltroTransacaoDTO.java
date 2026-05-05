package br.com.sfpacim.backend.dtos.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO (Data Transfer Object) para encapsular os parâmetros de busca e filtragem
 * de transações.
 *
 * Este record é utilizado em conjunto com o @ParameterObject para expor os
 * filtros dinâmicos via Query Parameters nas rotas de listagem.
 *
 * @author Matheus F. N. Pereira
 *
 * @param descricao            Filtro opcional para busca textual parcial pela
 *                             descrição.
 * @param valorMin             Filtro opcional pelo valor mínimo.
 * @param valorMax             Filtro opcional pelo valor máximo.
 * @param dataVencimentoInicio Filtro opcional pela data de vencimento inicial
 *                             do período.
 * @param dataVencimentoFim    Filtro opcional pela data de vencimento final do
 *                             período.
 * @param tipos                Filtro opcional por lista de tipos de transação.
 * @param statusList           Filtro opcional por lista de status de transação.
 * @param categorias           Filtro opcional por lista de IDs de categorias.
 * @param contas               Filtro opcional por lista de IDs de contas.
 * @param pessoas              Filtro opcional por lista de IDs de pessoas.
 */
public record FiltroTransacaoDTO(

        @Parameter(description = "${transacao.descricao.filtro.descricao}", example = "${transacao.exemplo.descricao.filtro}") //
        String descricao,

        @Parameter(description = "${transacao.descricao.filtro.valor-min}", example = "${transacao.exemplo.valor-min}") //
        @PositiveOrZero(message = "{transacao.validacao.valor.filtro.negativo}") //
        BigDecimal valorMin,

        @Parameter(description = "${transacao.descricao.filtro.valor-max}", example = "${transacao.exemplo.valor-max}") //
        @PositiveOrZero(message = "{transacao.validacao.valor.filtro.negativo}") //
        BigDecimal valorMax,

        @Parameter(description = "${transacao.descricao.filtro.data-vencimento-inicio}", example = "${transacao.exemplo.data.filtro-inicio}") //
        LocalDate dataVencimentoInicio,

        @Parameter(description = "${transacao.descricao.filtro.data-vencimento-fim}", example = "${transacao.exemplo.data.filtro-fim}") //
        LocalDate dataVencimentoFim,

        @Parameter(description = "${transacao.descricao.filtro.tipos}") //
        List<TipoTransacao> tipos,

        @Parameter(description = "${transacao.descricao.filtro.status}") //
        List<StatusTransacao> statusList,

        @Parameter(description = "${transacao.descricao.filtro.categorias}") //
        List<UUID> categorias,

        @Parameter(description = "${transacao.descricao.filtro.contas}") //
        List<UUID> contas,

        @Parameter(description = "${transacao.descricao.filtro.pessoas}") //
        List<UUID> pessoas) {
}
