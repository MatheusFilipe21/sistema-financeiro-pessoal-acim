package br.com.sfpacim.backend.dtos.pessoa;

import java.util.UUID;

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
 * @param id   O identificador único da pessoa.
 * @param nome O nome da pessoa.
 */
@Schema(description = "${pessoa.descricao.schema.selecao}")
public record SelecaoPessoaDTO(

        @Schema(description = "${pessoa.descricao.id}", example = "${pessoa.exemplo.id}") //
        UUID id,

        @Schema(description = "${pessoa.descricao.nome}", example = "${pessoa.exemplo.nome}") //
        String nome) {
}
