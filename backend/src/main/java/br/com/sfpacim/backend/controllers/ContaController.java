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
import br.com.sfpacim.backend.dtos.conta.ContaDTO;
import br.com.sfpacim.backend.dtos.conta.CriarAtualizarContaDTO;
import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.services.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST responsável pelos endpoints de Contas Bancárias.
 *
 * <p>
 * Expõe os endpoints para criar, listar, atualizar e excluir contas.
 * Todos os endpoints exigem autenticação via Token JWT.
 *
 * @author Matheus F. N. Pereira
 */
@Tag(name = "Contas", description = "Gestão de Contas")
@RestController
@RequestMapping("/contas")
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
    @Operation(summary = "Cadastra uma nova conta", description = "Cria uma nova conta bancária para uma pessoa titular. O nome deve ser único para a pessoa selecionada.", responses = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaDTO.class)), headers = @Header(name = "Location", description = "URL do novo recurso criado")),
            @ApiResponse(responseCode = "409", description = "Conflito (Nome Duplicado ou Regra de Titularidade)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_422))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
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
     * Endpoint para listar todas as contas do usuário autenticado.
     *
     * @return HTTP 200 (OK) com a lista de contas.
     */
    @Operation(summary = "Lista as contas do usuário", description = "Retorna todas as contas vinculadas a todas as pessoas do usuário logado. Ordenado pelo nome da conta.", responses = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @GetMapping
    public ResponseEntity<List<ContaDTO>> listar() {
        return ResponseEntity.ok(contaService.listar());
    }

    /**
     * Endpoint para atualizar os dados de uma conta.
     *
     * @param id  O UUID da conta a ser atualizada.
     * @param dto Os novos dados.
     * @return HTTP 200 (OK) com o DTO atualizado.
     */
    @Operation(summary = "Atualiza uma conta", description = "Atualiza os dados de uma conta existente. Recalcula o saldo atual automaticamente se o saldo inicial for alterado.", responses = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou acesso negado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito (Nome Duplicado)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_422))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
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
    @Operation(summary = "Exclui uma conta", description = "Remove uma conta do sistema. A exclusão só é permitida se a conta não tiver transações vinculadas (futuro).", responses = {
            @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou acesso negado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito (Vínculos existentes)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        contaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
