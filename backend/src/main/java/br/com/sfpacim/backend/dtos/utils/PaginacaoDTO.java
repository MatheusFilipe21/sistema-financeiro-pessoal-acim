package br.com.sfpacim.backend.dtos.utils;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) genérico para padronizar e traduzir as respostas
 * paginadas da API.
 *
 * <p>
 * Encapsula o objeto {@link Page} do Spring Data, expondo apenas os metadados
 * essenciais para a navegação do cliente, mantendo o payload limpo.
 *
 * @param <T>            O tipo do DTO contido na lista.
 * @param itens          Lista contendo os itens da página atual.
 * @param paginaAtual    Número da página atual (Zero-based: a primeira página é
 *                       0).
 * @param tamanhoPagina  Quantidade máxima de itens retornados por página.
 * @param totalPaginas   Total de páginas disponíveis baseadas nos filtros.
 * @param totalElementos Total absoluto de elementos na base de dados para o
 *                       filtro aplicado.
 * 
 * @author Matheus F. N. Pereira
 */
@Schema(description = "${utils.paginacao.descricao.schema}")
public record PaginacaoDTO<T>(

        @Schema(description = "${utils.paginacao.descricao.itens}") //
        List<T> itens,

        @Schema(description = "${utils.paginacao.descricao.pagina-atual}", example = "${utils.paginacao.exemplo.pagina-atual}") //
        int paginaAtual,

        @Schema(description = "${utils.paginacao.descricao.tamanho-pagina}", example = "${utils.paginacao.exemplo.tamanho-pagina}") //
        int tamanhoPagina,

        @Schema(description = "${utils.paginacao.descricao.total-paginas}", example = "${utils.paginacao.exemplo.total-paginas}") //
        int totalPaginas,

        @Schema(description = "${utils.paginacao.descricao.total-elementos}", example = "${utils.paginacao.exemplo.total-elementos}") //
        long totalElementos) {

    /**
     * Construtor customizado para mapear automaticamente um {@link Page} do Spring.
     *
     * @param pagina O objeto Page retornado pelo repositório/serviço.
     */
    public PaginacaoDTO(Page<T> pagina) {
        this(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalPages(),
                pagina.getTotalElements());
    }
}
