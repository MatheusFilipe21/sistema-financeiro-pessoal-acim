package br.com.sfpacim.backend.dtos.categoria;

import br.com.sfpacim.backend.models.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para expor os dados resumidos de uma
 * {@link Categoria}.
 *
 * @author Matheus F. N. Pereira
 * 
 * @param nome  Nome da categoria.
 * @param icone Identificador do ícone visual.
 * @param cor   Cor hexadecimal.
 */
@Schema(description = "${categoria.descricao.schema.resumo}")
public record CategoriaResumoDTO(

        @Schema(description = "${categoria.descricao.nome}", example = "${categoria.exemplo.nome}") //
        String nome,

        @Schema(description = "${categoria.descricao.icone}", example = "${categoria.exemplo.icone}") //
        String icone,

        @Schema(description = "${categoria.descricao.cor}", example = "${categoria.exemplo.cor}") //
        String cor) {

    /**
     * Construtor customizado para mapear a entidade {@link Categoria}
     * para este DTO.
     *
     * @param categoria A entidade JPA Categoria a ser convertida.
     */
    public CategoriaResumoDTO(Categoria categoria) {
        this(
                categoria.getNome(),
                categoria.getIcone(),
                categoria.getCor());
    }
}
