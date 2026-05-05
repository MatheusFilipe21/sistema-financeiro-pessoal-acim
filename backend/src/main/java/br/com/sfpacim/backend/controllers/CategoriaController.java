package br.com.sfpacim.backend.controllers;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.FiltroCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.SelecaoCategoriaDTO;
import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.dtos.utils.PaginacaoDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.services.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST responsável pelos endpoints de Categorias de Transação.
 *
 * <p>
 * Expõe os endpoints para criar, listar, atualizar e excluir categorias.
 * Gerencia tanto categorias personalizadas do usuário quanto categorias globais
 * do sistema.
 *
 * @author Matheus F. N. Pereira
 */
@Tag(name = "${categoria.nome.plural}", description = "${categoria.controller.descricao}")
@RestController
@RequestMapping(value = "/categorias", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param categoriaService O serviço que lida com a lógica de categorias.
     */
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * Endpoint para cadastrar uma nova categoria personalizada.
     *
     * @param dto Os dados da categoria.
     * @return HTTP 201 (Created) com o DTO criado e Header Location.
     * @throws ViolacaoDadosException Caso o nome já exista para o usuário.
     */
    @Operation(summary = "${categoria.controller.cadastro.resumo}", description = "${categoria.controller.cadastro.descricao}", responses = {
            @ApiResponse(responseCode = "201", description = "${categoria.controller.cadastro.resposta.201}", content = @Content(schema = @Schema(implementation = CategoriaDTO.class)), headers = @Header(name = "Location", description = "${geral.header.location.descricao}", schema = @Schema(type = "string", example = "${categoria.controller.cadastro.header.location.exemplo}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.duplicado}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.cadastro.exemplo.409}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = "${categoria.controller.cadastro.exemplo.422}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.cadastro.exemplo.500}")))
    })
    @PostMapping
    public ResponseEntity<CategoriaDTO> cadastrar(@Valid @RequestBody CriarAtualizarCategoriaDTO dto)
            throws ViolacaoDadosException {
        CategoriaDTO categoriaCriada = categoriaService.cadastrar(dto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoriaCriada.id())
                .toUri();

        return ResponseEntity.created(uri).body(categoriaCriada);
    }

    /**
     * Endpoint para listar as categorias do usuário e do sistema de forma paginada
     * e filtrada.
     *
     * @param filtro   Objeto contendo os filtros opcionais (nome, tipo e origem).
     * @param pageable Configurações de paginação injetadas pelo Spring.
     * @return HTTP 200 (OK) com a página de categorias envelopada no padrão da API.
     */
    @Operation(summary = "${categoria.controller.listar.resumo}", description = "${categoria.controller.listar.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${geral.resposta.200.listagem-paginada}", content = @Content(schema = @Schema(implementation = PaginacaoDTO.class), examples = @ExampleObject(value = "${categoria.controller.listar.exemplo.200}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.listar.exemplo.500}")))
    })
    @GetMapping
    public ResponseEntity<PaginacaoDTO<CategoriaDTO>> listar(
            @ParameterObject @Valid FiltroCategoriaDTO filtro,
            @ParameterObject @PageableDefault(size = 25, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(new PaginacaoDTO<>(categoriaService.listar(filtro, pageable)));
    }

    /**
     * Endpoint para listar as categorias formatadas para componentes de seleção.
     *
     * <p>
     * Retorna uma lista leve (sem paginação) contendo os dados essenciais e visuais
     * (ID, Nome, Tipo, Ícone e Cor) para preencher Dropdowns/Selects no frontend.
     *
     * @return HTTP 200 (OK) com a lista de opções de categorias.
     */
    @Operation(summary = "${categoria.controller.listar-selecao.resumo}", description = "${categoria.controller.listar-selecao.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${geral.resposta.200.listagem-simples}", content = @Content(examples = @ExampleObject(value = "${categoria.controller.listar-selecao.exemplo.200}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.listar-selecao.exemplo.500}")))
    })
    @GetMapping("/selecao")
    public ResponseEntity<List<SelecaoCategoriaDTO>> listarOpcoesSelecao() {
        return ResponseEntity.ok(categoriaService.listarOpcoes());
    }

    /**
     * Endpoint para buscar uma categoria pelo ID.
     *
     * @param id O UUID da categoria a ser buscada.
     * @return HTTP 200 (OK) com os dados da categoria.
     */
    @Operation(summary = "${categoria.controller.buscar-por-id.resumo}", description = "${categoria.controller.buscar-por-id.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${categoria.controller.buscar-por-id.resposta.200}", content = @Content(schema = @Schema(implementation = CategoriaDTO.class))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.buscar-por-id.exemplo.404}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.buscar-por-id.exemplo.500}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    /**
     * Endpoint para atualizar os dados de uma categoria.
     *
     * @param id  O UUID da categoria a ser atualizada.
     * @param dto Os novos dados.
     * @return HTTP 200 (OK) com o DTO atualizado.
     * @throws RegraDeNegocioException Se tentar alterar uma categoria do sistema.
     */
    @Operation(summary = "${categoria.controller.atualizar.resumo}", description = "${categoria.controller.atualizar.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${categoria.controller.atualizar.resposta.200}", content = @Content(schema = @Schema(implementation = CategoriaDTO.class))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.atualizar.exemplo.404}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.duplicado}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.atualizar.exemplo.409}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.negocio-validacao}", content = @Content(schema = @Schema(oneOf = {
                    ErroValidacaoDTO.class, ErroPadraoDTO.class }), examples = {
                            @ExampleObject(name = "${erro.exemplo.dropdown.validacao}", value = "${categoria.controller.atualizar.exemplo.422.validacao}"),
                            @ExampleObject(name = "${erro.exemplo.dropdown.negocio}", value = "${categoria.controller.atualizar.exemplo.422.negocio}")
                    })),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.atualizar.exemplo.500}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> atualizar(@PathVariable UUID id,
            @Valid @RequestBody CriarAtualizarCategoriaDTO dto) {
        CategoriaDTO categoriaAtualizada = categoriaService.atualizar(id, dto);

        return ResponseEntity.ok(categoriaAtualizada);
    }

    /**
     * Endpoint para excluir uma categoria.
     *
     * @param id O UUID da categoria a ser excluída.
     * @return HTTP 204 (No Content).
     */
    @Operation(summary = "${categoria.controller.excluir.resumo}", description = "${categoria.controller.excluir.descricao}", responses = {
            @ApiResponse(responseCode = "204", description = "${categoria.controller.excluir.resposta.204}", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.excluir.exemplo.404}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.vinculo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.excluir.exemplo.409}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.excluir.exemplo.422}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${categoria.controller.excluir.exemplo.500}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        categoriaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
