package br.com.sfpacim.backend.dtos.categoria;

import br.com.sfpacim.backend.models.enums.TipoCategoria;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * DTO (Data Transfer Object) para encapsular os parâmetros de busca e filtragem
 * de categorias.
 *
 * <p>
 * Este record é utilizado em conjunto com o @ParameterObject para expor os
 * filtros dinâmicos via Query Parameters nas rotas de listagem.
 *
 * @author Matheus F. N. Pereira
 *
 * @param nome    Filtro opcional para busca textual parcial.
 * @param tipo    Filtro opcional pelo tipo de movimentação financeira.
 * @param sistema Filtro opcional pela origem (padrão do sistema ou
 *                customizada).
 */
public record FiltroCategoriaDTO(

        @Parameter(description = "${categoria.descricao.filtro.nome}", example = "${categoria.exemplo.nome}") //
        String nome,

        @Parameter(description = "${categoria.descricao.filtro.tipo}", example = "${categoria.exemplo.tipo}") //
        TipoCategoria tipo,

        @Parameter(description = "${categoria.descricao.filtro.sistema}", example = "${categoria.exemplo.filtro.sistema}") //
        Boolean sistema) {
}
