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
@Schema(description = "DTO para representar uma categoria de transação.")
public record CategoriaDTO(

        @Schema(description = "Identificador único da categoria.", example = "c8f2a1b3-4d5e-6f7g-8h9i-0j1k2l3m4n5o") //
        UUID id,

        @Schema(description = "Nome da categoria.", example = "Alimentação") //
        String nome,

        @Schema(description = "Tipo da categoria.", example = "DESPESA") //
        TipoCategoria tipo,

        @Schema(description = "Ícone visual (Material Icons).", example = "restaurant") //
        String icone,

        @Schema(description = "Cor hexadecimal para exibição.", example = "#FF5733") //
        String cor,

        @Schema(description = "Indica se a categoria é padrão do sistema (bloqueada para edição).", example = "true") //
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
