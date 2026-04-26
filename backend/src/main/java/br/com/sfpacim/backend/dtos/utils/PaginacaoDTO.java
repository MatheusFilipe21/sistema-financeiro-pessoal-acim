package br.com.sfpacim.backend.dtos.utils;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO genérico para padronizar e traduzir as respostas paginadas da API.
 *
 * <p>
 * Encapsula o objeto {@link Page} do Spring Data, expondo apenas os metadados
 * essenciais para a navegação do cliente, mantendo o payload limpo.
 *
 * @param <T> O tipo do DTO contido na lista.
 * 
 * @author Matheus F. N. Pereira
 */
@Schema(description = "Envelopamento padrão para retornos paginados da API.")
public record PaginacaoDTO<T>(

        @Schema(description = "Lista contendo os itens da página atual.") List<T> itens,

        @Schema(description = "Número da página atual (Zero-based: a primeira página é 0).", example = "0") int paginaAtual,

        @Schema(description = "Quantidade máxima de itens retornados por página.", example = "25") int tamanhoPagina,

        @Schema(description = "Total de páginas disponíveis baseadas nos filtros.", example = "5") int totalPaginas,

        @Schema(description = "Total absoluto de elementos na base de dados para o filtro aplicado.", example = "100") long totalElementos) {

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
