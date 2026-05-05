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

import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.dtos.conta.FiltroContaDTO;
import br.com.sfpacim.backend.dtos.conta.ListagemContaDTO;
import br.com.sfpacim.backend.dtos.conta.SelecaoContaDTO;
import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.dtos.utils.PaginacaoDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.services.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST responsável pelos endpoints de Contas Bancárias.
 *
 * <p>
 * Expõe os endpoints para criar, listar, atualizar e excluir contas.
 * Todos os endpoints exigem autenticação via Token JWT.
 *
 * @author Matheus F. N. Pereira
 */
@Tag(name = "${conta.nome.plural}", description = "${conta.controller.descricao}")
@RestController
@RequestMapping(value = "/contas", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContaController {

    private final ContaService contaService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param contaService O serviço que lida com a lógica de contas.
     */
    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    /**
     * Endpoint para cadastrar uma nova conta.
     *
     * @param dto Os dados da conta (Nome, Instituição, Saldo, Pessoa).
     * @return HTTP 201 (Created) com o DTO criado e Header Location.
     * @throws ViolacaoDadosException Caso o nome já exista para a pessoa ou pessoa
     *                                não seja titular.
     */
    @Operation(summary = "${conta.controller.cadastro.resumo}", description = "${conta.controller.cadastro.descricao}", responses = {
            @ApiResponse(responseCode = "201", description = "${conta.controller.cadastro.resposta.201}", content = @Content(schema = @Schema(implementation = ContaDTO.class)), headers = @Header(name = "Location", description = "${geral.header.location.descricao}", schema = @Schema(type = "string", example = "${conta.controller.cadastro.header.location.exemplo}"))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.cadastro.exemplo.404}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.duplicado}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.cadastro.exemplo.409}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.negocio-validacao}", content = @Content(schema = @Schema(oneOf = {
                    ErroValidacaoDTO.class, ErroPadraoDTO.class }), examples = {
                            @ExampleObject(name = "${erro.exemplo.dropdown.validacao}", value = "${conta.controller.cadastro.exemplo.422.validacao}"),
                            @ExampleObject(name = "${conta.controller.dropdown.conflito.titular}", value = "${conta.controller.cadastro.exemplo.422.titular}")
                    })),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.cadastro.exemplo.500}")))
    })
    @PostMapping
    public ResponseEntity<ContaDTO> cadastrar(@Valid @RequestBody CriarAtualizarContaDTO dto)
            throws ViolacaoDadosException {
        ContaDTO contaCriada = contaService.cadastrar(dto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(contaCriada.id())
                .toUri();

        return ResponseEntity.created(uri).body(contaCriada);
    }

    /**
     * Endpoint para listar as contas do usuário autenticado de forma paginada e
     * filtrada.
     *
     * @param filtro   Objeto contendo os filtros dinâmicos (nome, instituições,
     *                 pessoas).
     * @param pageable Configurações de paginação injetadas pelo Spring.
     * @return HTTP 200 (OK) com a página de contas otimizada para tabelas.
     */
    @Operation(summary = "${conta.controller.listar.resumo}", description = "${conta.controller.listar.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${geral.resposta.200.listagem-paginada}", content = @Content(schema = @Schema(implementation = PaginacaoDTO.class), examples = @ExampleObject(value = "${conta.controller.listar.exemplo.200}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.listar.exemplo.500}")))
    })
    @GetMapping
    public ResponseEntity<PaginacaoDTO<ListagemContaDTO>> listar(
            @ParameterObject @Valid FiltroContaDTO filtro,
            @ParameterObject @PageableDefault(size = 25, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(new PaginacaoDTO<>(contaService.listar(filtro, pageable)));
    }

    /**
     * Endpoint para listar opções de contas formatadas para componentes de seleção.
     *
     * @return HTTP 200 (OK) com a lista leve de contas.
     */
    @Operation(summary = "${conta.controller.listar-selecao.resumo}", description = "${conta.controller.listar-selecao.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${geral.resposta.200.listagem-simples}", content = @Content(examples = @ExampleObject(value = "${conta.controller.listar-selecao.exemplo.200}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.listar-selecao.exemplo.500}")))
    })
    @GetMapping("/selecao")
    public ResponseEntity<List<SelecaoContaDTO>> listarOpcoesSelecao() {
        return ResponseEntity.ok(contaService.listarOpcoes());
    }

    /**
     * Endpoint para buscar os dados completos de uma conta pelo ID.
     *
     * @param id O UUID da conta a ser buscada.
     * @return HTTP 200 (OK) com os dados completos da conta.
     */
    @Operation(summary = "${conta.controller.buscar-por-id.resumo}", description = "${conta.controller.buscar-por-id.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${conta.controller.buscar-por-id.resposta.200}", content = @Content(schema = @Schema(implementation = ContaDTO.class))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.buscar-por-id.exemplo.404}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.buscar-por-id.exemplo.500}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContaDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    /**
     * Endpoint para atualizar os dados de uma conta.
     *
     * @param id  O UUID da conta a ser atualizada.
     * @param dto Os novos dados.
     * @return HTTP 200 (OK) com o DTO atualizado.
     */
    @Operation(summary = "${conta.controller.atualizar.resumo}", description = "${conta.controller.atualizar.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${conta.controller.atualizar.resposta.200}", content = @Content(schema = @Schema(implementation = ContaDTO.class))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.atualizar.exemplo.404}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.duplicado}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.atualizar.exemplo.409}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.negocio-validacao}", content = @Content(schema = @Schema(oneOf = {
                    ErroValidacaoDTO.class, ErroPadraoDTO.class }), examples = {
                            @ExampleObject(name = "${erro.exemplo.dropdown.validacao}", value = "${conta.controller.atualizar.exemplo.422.validacao}"),
                            @ExampleObject(name = "${conta.controller.dropdown.conflito.titular}", value = "${conta.controller.atualizar.exemplo.422.titular}")
                    })),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.atualizar.exemplo.500}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContaDTO> atualizar(@PathVariable UUID id, @Valid @RequestBody CriarAtualizarContaDTO dto)
            throws ViolacaoDadosException {
        ContaDTO contaAtualizada = contaService.atualizar(id, dto);

        return ResponseEntity.ok(contaAtualizada);
    }

    /**
     * Endpoint para excluir uma conta.
     *
     * @param id O UUID da conta a ser excluída.
     * @return HTTP 204 (No Content).
     */
    @Operation(summary = "${conta.controller.excluir.resumo}", description = "${conta.controller.excluir.descricao}", responses = {
            @ApiResponse(responseCode = "204", description = "${conta.controller.excluir.resposta.204}", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.excluir.exemplo.404}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.vinculo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.excluir.exemplo.409}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${conta.controller.excluir.exemplo.500}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        contaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
