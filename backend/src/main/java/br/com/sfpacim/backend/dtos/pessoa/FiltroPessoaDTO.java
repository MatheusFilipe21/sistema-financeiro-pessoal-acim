package br.com.sfpacim.backend.dtos.pessoa;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * DTO (Data Transfer Object) para encapsular os parâmetros de busca e filtragem
 * de pessoas.
 *
 * <p>
 * Este record é utilizado em conjunto com o @ParameterObject para expor os
 * filtros dinâmicos via Query Parameters nas rotas de listagem.
 *
 * @author Matheus F. N. Pereira
 *
 * @param nome    Filtro opcional para busca textual parcial.
 * @param titular Filtro opcional por situação de titularidade.
 */
public record FiltroPessoaDTO(

        @Parameter(description = "${pessoa.descricao.filtro.nome}", example = "${pessoa.exemplo.filtro.nome}") //
        String nome,

        @Parameter(description = "${pessoa.descricao.filtro.titular}", example = "${pessoa.exemplo.titular}") //
        Boolean titular) {
}
