package br.com.sfpacim.backend.dtos.categoria;

import java.util.UUID;

import br.com.sfpacim.backend.models.enums.TipoCategoria;
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
 * @param id    O identificador único da categoria.
 * @param nome  O nome da categoria.
 * @param tipo  O tipo da categoria.
 * @param icone O identificador do ícone.
 * @param cor   A cor hexadecimal.
 */
@Schema(description = "${categoria.descricao.schema.selecao}")
public record SelecaoCategoriaDTO(

        @Schema(description = "${categoria.descricao.id}", example = "${categoria.exemplo.id.selecao}") //
        UUID id,

        @Schema(description = "${categoria.descricao.nome}", example = "${categoria.exemplo.nome}") //
        String nome,

        @Schema(description = "${categoria.descricao.tipo}", example = "${categoria.exemplo.tipo}") //
        TipoCategoria tipo,

        @Schema(description = "${categoria.descricao.icone.dropdown}", example = "${categoria.exemplo.icone}") //
        String icone,

        @Schema(description = "${categoria.descricao.cor.dropdown}", example = "${categoria.exemplo.cor}") //
        String cor) {
}
