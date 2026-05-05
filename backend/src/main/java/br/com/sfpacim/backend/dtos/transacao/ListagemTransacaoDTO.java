package br.com.sfpacim.backend.dtos.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.sfpacim.backend.dtos.categoria.CategoriaResumoDTO;
import br.com.sfpacim.backend.dtos.conta.ContaResumoDTO;
import br.com.sfpacim.backend.models.Transacao;
import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados otimizados de uma
 * {@link Transacao} em tabelas e listagens de extrato.
 *
 * <p>
 * Este record é projetado para retornar apenas as colunas essenciais
 * necessárias para a visualização em grade/tabelas no frontend.
 *
 * @author Matheus F. N. Pereira
 *
 * @param id             O identificador único da transação.
 * @param descricao      A descrição da transação.
 * @param valor          O valor monetário.
 * @param dataVencimento A data de vencimento da transação.
 * @param tipo           O tipo (RECEITA ou DESPESA).
 * @param status         O status atual (PENDENTE ou PAGO).
 * @param categoria      Objeto simplificado contendo os dados visuais da
 *                       categoria.
 * @param conta          Objeto simplificado contendo os dados visuais da conta.
 * @param pessoaNome     O nome da pessoa vinculada (Opcional).
 */
@Schema(description = "${transacao.descricao.schema.listagem}")
public record ListagemTransacaoDTO(

        @Schema(description = "${transacao.descricao.id}", example = "${transacao.exemplo.id}") //
        UUID id,

        @Schema(description = "${transacao.descricao.descricao.curta}", example = "${transacao.exemplo.descricao.extrato}") //
        String descricao,

        @Schema(description = "${transacao.descricao.valor}", example = "${transacao.exemplo.valor.extrato}") //
        BigDecimal valor,

        @Schema(description = "${transacao.descricao.data-vencimento}", example = "${transacao.exemplo.data-vencimento}") //
        LocalDate dataVencimento,

        @Schema(description = "${transacao.descricao.tipo}", example = "${transacao.exemplo.tipo}") //
        TipoTransacao tipo,

        @Schema(description = "${transacao.descricao.status}", example = "${transacao.exemplo.status}") //
        StatusTransacao status,

        @Schema(description = "${transacao.descricao.categoria-resumo}") //
        CategoriaResumoDTO categoria,

        @Schema(description = "${transacao.descricao.conta-resumo}") //
        ContaResumoDTO conta,

        @Schema(description = "${transacao.descricao.pessoa-nome}", example = "${pessoa.exemplo.nome}") //
        String pessoaNome) {

    /**
     * Construtor customizado para converter a entidade {@link Transacao} em dados
     * de listagem.
     *
     * @param transacao A entidade JPA Transacao a ser convertida.
     */
    public ListagemTransacaoDTO(Transacao transacao) {
        this(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getDataVencimento(),
                transacao.getTipo(),
                transacao.getStatus(),
                new CategoriaResumoDTO(transacao.getCategoria()),
                new ContaResumoDTO(transacao.getConta()),
                transacao.getPessoa() != null ? transacao.getPessoa().getNome() : null);
    }
}
