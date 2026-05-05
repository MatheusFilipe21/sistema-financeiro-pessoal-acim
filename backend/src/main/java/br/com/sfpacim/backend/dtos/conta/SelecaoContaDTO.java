package br.com.sfpacim.backend.dtos.conta;

import java.util.UUID;

import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) simplificado para preenchimento de componentes
 * de seleção (Selects).
 *
 * <p>
 * Este record é projetado para ser leve, retornando apenas o essencial para
 * identificação e preenchimento de componentes de seleção (Selects) no
 * frontend.
 *
 * @author Matheus F. N. Pereira
 *
 * @param id          O identificador único da conta.
 * @param nome        O nome da conta.
 * @param instituicao A instituição financeira.
 * @param pessoaNome  O nome do titular da conta.
 */
@Schema(description = "${conta.descricao.schema.selecao}")
public record SelecaoContaDTO(

        @Schema(description = "${conta.descricao.id}", example = "${conta.exemplo.id}") //
        UUID id,

        @Schema(description = "${conta.descricao.nome}", example = "${conta.exemplo.nome.selecao}") //
        String nome,

        @Schema(description = "${conta.descricao.instituicao}", example = "${conta.exemplo.instituicao}") //
        InstituicaoFinanceira instituicao,

        @Schema(description = "${conta.descricao.pessoa-nome}", example = "${pessoa.exemplo.nome}") //
        String pessoaNome) {
}
