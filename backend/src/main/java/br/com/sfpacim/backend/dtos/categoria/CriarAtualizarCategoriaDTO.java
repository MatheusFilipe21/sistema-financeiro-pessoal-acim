package br.com.sfpacim.backend.dtos.categoria;

import br.com.sfpacim.backend.models.enums.TipoCategoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO (Data Transfer Object) para encapsular os dados de entrada para a
 * criação ou atualização de uma categoria.
 * 
 * <p>
 * Este record aplica as validações de negócio usando o Spring Validation.
 *
 * @author Matheus F. N. Pereira
 *
 * @param nome  O nome da categoria.
 * @param tipo  O tipo da categoria (RECEITA, DESPESA ou AMBOS).
 * @param icone O identificador do ícone visual (Material Icons).
 * @param cor   A cor hexadecimal para exibição (formato #RRGGBB ou #RGB).
 */
@Schema(description = "${categoria.descricao.schema.criacao-atualizacao}")
public record CriarAtualizarCategoriaDTO(

        @Schema(description = "${categoria.descricao.nome}", example = "${categoria.exemplo.nome.novo}") //
        @NotBlank(message = "{geral.validacao.nome.obrigatorio}") //
        String nome,

        @Schema(description = "${categoria.descricao.tipo}", example = "${categoria.exemplo.tipo}") //
        @NotNull(message = "{geral.validacao.tipo.obrigatorio}") //
        TipoCategoria tipo,

        @Schema(description = "${categoria.descricao.icone}", example = "${categoria.exemplo.icone.novo}") //
        @NotBlank(message = "{categoria.validacao.icone.obrigatorio}") //
        String icone,

        @Schema(description = "${categoria.descricao.cor}", example = "${categoria.exemplo.cor.nova}") //
        @NotBlank(message = "{categoria.validacao.cor.obrigatoria}") //
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "{categoria.validacao.cor.invalida}") //
        String cor) {
}
