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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.dtos.pessoa.CriarAtualizarPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.FiltroPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.SelecaoPessoaDTO;
import br.com.sfpacim.backend.dtos.utils.PaginacaoDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.services.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST responsável pelos endpoints de Pessoas.
 *
 * <p>
 * Expõe os endpoints para criar, listar, atualizar e excluir pessoas.
 * Todos os endpoints exigem autenticação via Token JWT.
 *
 * @author Matheus F. N. Pereira
 */
@Tag(name = "${pessoa.nome.plural}", description = "${pessoa.controller.descricao}")
@RestController
@RequestMapping(value = "/pessoas", produces = MediaType.APPLICATION_JSON_VALUE)
public class PessoaController {

    private final PessoaService pessoaService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param pessoaService O serviço que lida com a lógica de pessoas.
     */
    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    /**
     * Endpoint para cadastrar uma nova pessoa.
     *
     * @param dto Os dados da pessoa (Nome).
     * @return HTTP 201 (Created) com o DTO criado e Header Location.
     * @throws ViolacaoDadosException Caso o nome já exista para o usuário.
     */
    @Operation(summary = "${pessoa.controller.cadastro.resumo}", description = "${pessoa.controller.cadastro.descricao}", responses = {
            @ApiResponse(responseCode = "201", description = "${pessoa.controller.cadastro.resposta.201}", content = @Content(schema = @Schema(implementation = PessoaDTO.class)), headers = @Header(name = "Location", description = "${geral.header.location.descricao}", schema = @Schema(type = "string", example = "${pessoa.controller.cadastro.header.location.exemplo}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.duplicado}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.cadastro.exemplo.409}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.cadastro.exemplo.422}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.cadastro.exemplo.500}")))
    })
    @PostMapping
    public ResponseEntity<PessoaDTO> cadastrar(@Valid @RequestBody CriarAtualizarPessoaDTO dto)
            throws ViolacaoDadosException {
        PessoaDTO pessoaSalva = pessoaService.cadastrar(dto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pessoaSalva.id())
                .toUri();

        return ResponseEntity.created(uri).body(pessoaSalva);
    }

    /**
     * Endpoint para listar as pessoas do usuário autenticado de forma paginada e
     * filtrada.
     *
     * @param filtro   Filtro opcional contendo os parâmetros de busca (nome,
     *                 titularidade).
     * @param pageable Configurações de paginação injetadas pelo Spring.
     * @return HTTP 200 (OK) com a página de pessoas envelopada no padrão da API.
     */
    @Operation(summary = "${pessoa.controller.listar.resumo}", description = "${pessoa.controller.listar.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${geral.resposta.200.listagem-paginada}", content = @Content(schema = @Schema(implementation = PaginacaoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.listar.exemplo.200}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.listar.exemplo.500}")))
    })
    @GetMapping
    public ResponseEntity<PaginacaoDTO<PessoaDTO>> listar(
            @ParameterObject @Valid FiltroPessoaDTO filtro,
            @ParameterObject @PageableDefault(size = 25, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(new PaginacaoDTO<>(pessoaService.listar(filtro, pageable)));
    }

    /**
     * Endpoint para listar as pessoas formatadas para componentes de seleção.
     *
     * <p>
     * Retorna uma lista leve (sem paginação) contendo apenas os dados essenciais
     * (ID e Nome) para preencher Dropdowns/Selects no frontend.
     *
     * @param titular Filtro opcional para buscar exclusivamente titulares (true) ou
     *                dependentes (false).
     * @return HTTP 200 (OK) com a lista de opções de pessoas.
     */
    @Operation(summary = "${pessoa.controller.listar-selecao.resumo}", description = "${pessoa.controller.listar-selecao.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${geral.resposta.200.listagem-simples}", content = @Content(examples = @ExampleObject(value = "${pessoa.controller.listar-selecao.exemplo.200}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.listar-selecao.exemplo.500}")))
    })
    @GetMapping("/selecao")
    public ResponseEntity<List<SelecaoPessoaDTO>> listarOpcoesSelecao(
            @Parameter(description = "${pessoa.controller.listar-selecao.parametro.titular}") @RequestParam(required = false) Boolean titular) {
        return ResponseEntity.ok(pessoaService.listarOpcoes(titular));
    }

    /**
     * Endpoint para buscar uma pessoa pelo ID.
     *
     * @param id O UUID da pessoa a ser buscada.
     * @return HTTP 200 (OK) com os dados da pessoa.
     */
    @Operation(summary = "${pessoa.controller.buscar-por-id.resumo}", description = "${pessoa.controller.buscar-por-id.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${pessoa.controller.buscar-por-id.resposta.200}", content = @Content(schema = @Schema(implementation = PessoaDTO.class))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.buscar-por-id.exemplo.404}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.buscar-por-id.exemplo.500}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PessoaDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pessoaService.buscarPorId(id));
    }

    /**
     * Endpoint para atualizar os dados de uma pessoa.
     *
     * @param id  O UUID da pessoa a ser atualizada.
     * @param dto Os novos dados (Nome).
     * @return HTTP 200 (OK) com o DTO atualizado.
     */
    @Operation(summary = "${pessoa.controller.atualizar.resumo}", description = "${pessoa.controller.atualizar.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${pessoa.controller.atualizar.resposta.200}", content = @Content(schema = @Schema(implementation = PessoaDTO.class))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.atualizar.exemplo.404}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.duplicado}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.atualizar.exemplo.409}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.atualizar.exemplo.422}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.atualizar.exemplo.500}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PessoaDTO> atualizar(@PathVariable UUID id, @Valid @RequestBody CriarAtualizarPessoaDTO dto)
            throws ViolacaoDadosException {
        PessoaDTO pessoaAtualizada = pessoaService.atualizar(id, dto);

        return ResponseEntity.ok(pessoaAtualizada);
    }

    /**
     * Endpoint para excluir uma pessoa.
     *
     * @param id O UUID da pessoa a ser excluída.
     * @return HTTP 204 (No Content).
     */
    @Operation(summary = "${pessoa.controller.excluir.resumo}", description = "${pessoa.controller.excluir.descricao}", responses = {
            @ApiResponse(responseCode = "204", description = "${pessoa.controller.excluir.resposta.204}", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.excluir.exemplo.404}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.vinculo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.excluir.exemplo.409}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${pessoa.controller.excluir.exemplo.500}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        pessoaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
