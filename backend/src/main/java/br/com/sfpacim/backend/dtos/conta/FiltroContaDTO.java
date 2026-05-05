package br.com.sfpacim.backend.dtos.conta;

import java.util.List;
import java.util.UUID;

import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * DTO (Data Transfer Object) para encapsular os parâmetros de busca e filtragem
 * de contas.
 *
 * Este record é utilizado em conjunto com o @ParameterObject para expor os
 * filtros dinâmicos via Query Parameters nas rotas de listagem.
 *
 * @author Matheus F. N. Pereira
 *
 * @param nome         Filtro opcional para busca textual parcial.
 * @param instituicoes Filtro opcional pela instituição financeira.
 * @param pessoaIds    Filtro opcional pelos IDs das pessoas (titulares).
 */
public record FiltroContaDTO(

        @Parameter(description = "${conta.descricao.filtro.nome}", example = "${conta.exemplo.nome.filtro}") //
        String nome,

        @Parameter(description = "${conta.descricao.filtro.instituicoes}") //
        List<InstituicaoFinanceira> instituicoes,

        @Parameter(description = "${conta.descricao.filtro.pessoa-ids}") //
        List<UUID> pessoaIds) {
}
