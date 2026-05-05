package br.com.sfpacim.backend.dtos.conta;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados otimizados de uma
 * {@link Conta} em tabelas e listagens.
 *
 * <p>
 * Este record é projetado para retornar apenas as colunas essenciais
 * necessárias para a visualização em grade/tabelas no frontend.
 *
 * @author Matheus F. N. Pereira
 * 
 * @param id          O identificador único da conta.
 * @param nome        O nome da conta.
 * @param instituicao A instituição financeira.
 * @param saldoAtual  O saldo atualizado.
 * @param pessoaNome  O nome do titular da conta.
 */
@Schema(description = "${conta.descricao.schema.listagem}")
public record ListagemContaDTO(

        @Schema(description = "${conta.descricao.id}", example = "${conta.exemplo.id}") //
        UUID id,

        @Schema(description = "${conta.descricao.nome}", example = "${conta.exemplo.nome}") //
        String nome,

        @Schema(description = "${conta.descricao.instituicao}", example = "${conta.exemplo.instituicao}") //
        InstituicaoFinanceira instituicao,

        @Schema(description = "${conta.descricao.saldo-atual}", example = "${conta.exemplo.saldo-atual}") //
        BigDecimal saldoAtual,

        @Schema(description = "${conta.descricao.pessoa-nome}", example = "${pessoa.exemplo.nome}") //
        String pessoaNome) {

    /**
     * Construtor para converter a entidade {@link Conta} em dados de listagem.
     *
     * @param conta A entidade JPA Conta.
     */
    public ListagemContaDTO(Conta conta) {
        this(
                conta.getId(),
                conta.getNome(),
                conta.getInstituicao(),
                conta.getSaldoAtual(),
                conta.getPessoa().getNome());
    }
}
