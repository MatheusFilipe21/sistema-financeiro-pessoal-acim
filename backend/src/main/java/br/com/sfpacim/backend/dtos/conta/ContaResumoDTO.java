package br.com.sfpacim.backend.dtos.conta;

import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados resumidos de uma
 * {@link Conta}.
 *
 * @author Matheus F. N. Pereira
 * 
 * @param nome        O nome da conta.
 * @param instituicao A instituição financeira.
 * @param pessoaNome  O nome do titular da conta.
 */
@Schema(description = "${conta.descricao.schema.resumo}")
public record ContaResumoDTO(

        @Schema(description = "${conta.descricao.nome}", example = "${conta.exemplo.nome}") //
        String nome,

        @Schema(description = "${conta.descricao.instituicao}", example = "${conta.exemplo.instituicao}") //
        InstituicaoFinanceira instituicao,

        @Schema(description = "${conta.descricao.pessoa-nome}", example = "${pessoa.exemplo.nome}") //
        String pessoaNome) {

    /**
     * Construtor customizado para mapear a entidade {@link Conta}
     * para este DTO.
     *
     * @param conta A entidade JPA Conta a ser convertida.
     */
    public ContaResumoDTO(Conta conta) {
        this(
                conta.getNome(),
                conta.getInstituicao(),
                conta.getPessoa().getNome());
    }
}
