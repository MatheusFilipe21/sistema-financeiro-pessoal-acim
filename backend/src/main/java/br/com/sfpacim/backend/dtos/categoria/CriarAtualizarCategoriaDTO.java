package br.com.sfpacim.backend.dtos.categoria;

import br.com.sfpacim.backend.models.enums.TipoCategoria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO (Data Transfer Object) para recebimento de dados de criação e
 * atualização de categorias.
 *
 * <p>
 * Contém validações de formato para cor hexadecimal e obrigatoriedade
 * dos campos visuais.
 *
 * @author Matheus F. N. Pereira
 *
 * @param nome  Nome da categoria (Obrigatório).
 * @param tipo  Tipo da categoria (Enum, Obrigatório).
 * @param icone Ícone visual (Obrigatório).
 * @param cor   Cor hexadecimal (Obrigatório, formato #RRGGBB).
 */
@Schema(description = "DTO utilizado para cadastrar ou atualizar uma categoria.")
public record CriarAtualizarCategoriaDTO(

        @Schema(description = "Nome da categoria.", example = "Lazer") //
        @NotBlank(message = "O nome é obrigatório.") //
        String nome,

        @Schema(description = "Tipo da categoria.", example = "DESPESA") //
        @NotNull(message = "O tipo é obrigatório.") //
        TipoCategoria tipo,

        @Schema(description = "Ícone visual (Material Icons).", example = "sports_soccer") //
        @NotBlank(message = "O ícone é obrigatório.") //
        String icone,

        @Schema(description = "Cor hexadecimal para exibição.", example = "#00FF00") //
        @NotBlank(message = "A cor é obrigatória.") //
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Formato de cor inválido.") //
        String cor) {
}
