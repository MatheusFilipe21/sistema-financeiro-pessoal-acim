package br.com.sfpacim.backend.controllers;

import java.net.URI;
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

import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.CriarAtualizarTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.FiltroTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.ListagemTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.TransacaoDTO;
import br.com.sfpacim.backend.dtos.utils.PaginacaoDTO;
import br.com.sfpacim.backend.services.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST responsável pelos endpoints de Transações (Receitas e
 * Despesas).
 *
 * <p>
 * Expõe os endpoints para criar, listar (com paginação e filtros), atualizar e
 * excluir transações. Exige autenticação via Token JWT.
 *
 * @author Matheus F. N. Pereira
 */
@Tag(name = "${transacao.nome.plural}", description = "${transacao.controller.descricao}")
@RestController
@RequestMapping(value = "/transacoes", produces = MediaType.APPLICATION_JSON_VALUE)
public class TransacaoController {

    private final TransacaoService transacaoService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param transacaoService O serviço que orquestra a lógica de transações.
     */
    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    /**
     * Endpoint para cadastrar uma nova transação.
     *
     * @param dto Os dados da nova transação (Receita ou Despesa).
     * @return HTTP 201 (Created) com o DTO criado e Header Location.
     */
    @Operation(summary = "${transacao.controller.cadastro.resumo}", description = "${transacao.controller.cadastro.descricao}", responses = {
            @ApiResponse(responseCode = "201", description = "${transacao.controller.cadastro.resposta.201}", content = @Content(schema = @Schema(implementation = TransacaoDTO.class)), headers = @Header(name = "Location", description = "${geral.header.location.descricao}", schema = @Schema(type = "string", example = "${transacao.controller.cadastro.header.location.exemplo}"))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.cadastro.exemplo.404}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = "${transacao.controller.cadastro.exemplo.422.validacao}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.cadastro.exemplo.500}")))
    })
    @PostMapping
    public ResponseEntity<TransacaoDTO> cadastrar(@Valid @RequestBody CriarAtualizarTransacaoDTO dto) {
        TransacaoDTO transacaoCriada = transacaoService.cadastrar(dto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transacaoCriada.id())
                .toUri();

        return ResponseEntity.created(uri).body(transacaoCriada);
    }

    /**
     * Endpoint para listar as transações do usuário autenticado de forma paginada e
     * filtrada.
     *
     * <p>
     * Este endpoint utiliza um objeto de filtro para suportar múltiplas combinações
     * dinâmicas, como busca por descrição, faixa de valores, períodos e categorias.
     * Retorna o DTO otimizado de listagem.
     *
     * @param filtro   Objeto contendo todos os critérios de filtragem opcionais.
     * @param pageable Configurações de paginação injetadas pelo Spring.
     * @return HTTP 200 (OK) com a página de transações envelopada no padrão da API.
     */
    @Operation(summary = "${transacao.controller.listar.resumo}", description = "${transacao.controller.listar.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${geral.resposta.200.listagem-paginada}", content = @Content(schema = @Schema(implementation = PaginacaoDTO.class), examples = @ExampleObject(value = "${transacao.controller.listar.exemplo.200}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.negocio-validacao}", content = @Content(schema = @Schema(oneOf = {
                    ErroValidacaoDTO.class, ErroPadraoDTO.class }), examples = {
                            @ExampleObject(name = "${erro.exemplo.dropdown.validacao}", value = "${transacao.controller.listar.exemplo.422.validacao}"),
                            @ExampleObject(name = "${erro.exemplo.dropdown.negocio}", value = "${transacao.controller.listar.exemplo.422.negocio}")
                    })),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.listar.exemplo.500}")))
    })
    @GetMapping
    public ResponseEntity<PaginacaoDTO<ListagemTransacaoDTO>> listar(
            @ParameterObject @Valid FiltroTransacaoDTO filtro,
            @ParameterObject @PageableDefault(size = 25, sort = "dataVencimento", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(new PaginacaoDTO<>(transacaoService.listar(filtro, pageable)));
    }

    /**
     * Endpoint para buscar uma transação específica pelo ID.
     *
     * @param id O UUID da transação.
     * @return HTTP 200 (OK) com os dados completos da transação.
     */
    @Operation(summary = "${transacao.controller.buscar-por-id.resumo}", description = "${transacao.controller.buscar-por-id.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${transacao.controller.buscar-por-id.resposta.200}", content = @Content(schema = @Schema(implementation = TransacaoDTO.class))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.buscar-por-id.exemplo.404}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.buscar-por-id.exemplo.500}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransacaoDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(transacaoService.buscarPorId(id));
    }

    /**
     * Endpoint para atualizar os dados de uma transação.
     *
     * @param id  O UUID da transação a ser atualizada.
     * @param dto Os novos dados.
     * @return HTTP 200 (OK) com o DTO atualizado.
     */
    @Operation(summary = "${transacao.controller.atualizar.resumo}", description = "${transacao.controller.atualizar.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${transacao.controller.atualizar.resposta.200}", content = @Content(schema = @Schema(implementation = TransacaoDTO.class))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.atualizar.exemplo.404}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = "${transacao.controller.atualizar.exemplo.422.validacao}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.atualizar.exemplo.500}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TransacaoDTO> atualizar(@PathVariable UUID id,
            @Valid @RequestBody CriarAtualizarTransacaoDTO dto) {
        TransacaoDTO transacaoAtualizada = transacaoService.atualizar(id, dto);

        return ResponseEntity.ok(transacaoAtualizada);
    }

    /**
     * Endpoint para excluir uma transação.
     *
     * @param id O UUID da transação a ser excluída.
     * @return HTTP 204 (No Content).
     */
    @Operation(summary = "${transacao.controller.excluir.resumo}", description = "${transacao.controller.excluir.descricao}", responses = {
            @ApiResponse(responseCode = "204", description = "${transacao.controller.excluir.resposta.204}", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "${erro.404.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.excluir.exemplo.404}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${transacao.controller.excluir.exemplo.500}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        transacaoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
