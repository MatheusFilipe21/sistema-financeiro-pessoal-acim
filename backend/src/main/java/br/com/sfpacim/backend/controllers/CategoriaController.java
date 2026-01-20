package br.com.sfpacim.backend.controllers;

import java.net.URI;
import java.util.List;
import java.util.UUID;

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

import br.com.sfpacim.backend.doc.ExemplosDocumentacao;
import br.com.sfpacim.backend.dtos.categoria.CategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.CriarAtualizarCategoriaDTO;
import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
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
import jakarta.validation.Valid;

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
@Tag(name = "Categorias", description = "Gestão de Categorias")
@RestController
@RequestMapping("/categorias")
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
    @Operation(summary = "Cadastra uma nova categoria", description = "Cria uma nova categoria personalizada para o usuário. O nome deve ser único.", responses = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDTO.class)), headers = @Header(name = "Location", description = "URL do novo recurso criado")),
            @ApiResponse(responseCode = "409", description = "Conflito (Nome Duplicado)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_422))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
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
     * Endpoint para listar todas as categorias visíveis para o usuário.
     *
     * @return HTTP 200 (OK) com a lista de categorias.
     */
    @Operation(summary = "Lista as categorias", description = "Retorna todas as categorias visíveis para o usuário, incluindo as personalizadas e as globais do sistema. Ordenado por nome.", responses = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    /**
     * Endpoint para atualizar os dados de uma categoria.
     *
     * @param id  O UUID da categoria a ser atualizada.
     * @param dto Os novos dados.
     * @return HTTP 200 (OK) com o DTO atualizado.
     * @throws RegraDeNegocioException Se tentar alterar uma categoria do sistema.
     */
    @Operation(summary = "Atualiza uma categoria", description = "Atualiza os dados de uma categoria personalizada. Categorias do sistema não podem ser alteradas.", responses = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoriaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou acesso negado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito (Nome Duplicado ou Categoria do Sistema)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_422))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
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
    @Operation(summary = "Exclui uma categoria", description = "Remove uma categoria personalizada. Categorias do sistema não podem ser excluídas.", responses = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou acesso negado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito (Categoria do Sistema ou Vínculos)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        categoriaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
