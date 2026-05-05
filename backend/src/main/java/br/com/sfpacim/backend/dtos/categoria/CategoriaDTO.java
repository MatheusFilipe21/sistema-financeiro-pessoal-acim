package br.com.sfpacim.backend.dtos.categoria;

import java.util.UUID;

import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados públicos de uma
 * {@link Categoria}.
 *
 * @author Matheus F. N. Pereira
 *
 * @param id      Identificador único da categoria.
 * @param nome    Nome da categoria.
 * @param tipo    Tipo (Receita, Despesa, Ambos).
 * @param icone   Identificador do ícone visual.
 * @param cor     Cor hexadecimal.
 * @param sistema Indica se é uma categoria padrão do sistema (não editável).
 */
@Schema(description = "${categoria.descricao.schema.leitura}")
public record CategoriaDTO(

        @Schema(description = "${categoria.descricao.id}", example = "${categoria.exemplo.id}") //
        UUID id,

        @Schema(description = "${categoria.descricao.nome}", example = "${categoria.exemplo.nome}") //
        String nome,

        @Schema(description = "${categoria.descricao.tipo}", example = "${categoria.exemplo.tipo}") //
        TipoCategoria tipo,

        @Schema(description = "${categoria.descricao.icone}", example = "${categoria.exemplo.icone}") //
        String icone,

        @Schema(description = "${categoria.descricao.cor}", example = "${categoria.exemplo.cor}") //
        String cor,

        @Schema(description = "${categoria.descricao.sistema}", example = "${categoria.exemplo.sistema}") //
        boolean sistema) {

    /**
     * Construtor customizado para mapear a entidade {@link Categoria}.
     *
     * @param categoria A entidade Categoria.
     */
    public CategoriaDTO(Categoria categoria) {
        this(
                categoria.getId(),
                categoria.getNome(),
                categoria.getTipo(),
                categoria.getIcone(),
                categoria.getCor(),
                categoria.isDoSistema());
    }
}
