package br.com.sfpacim.backend.controllers;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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

import br.com.sfpacim.backend.doc.ExemplosDocumentacao;
import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.CriarAtualizarTransacaoDTO;
import br.com.sfpacim.backend.dtos.transacao.TransacaoDTO;
import br.com.sfpacim.backend.dtos.utils.PaginacaoDTO;
import br.com.sfpacim.backend.services.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
@Tag(name = "Transações", description = "Gestão de Receitas e Despesas")
@RestController
@RequestMapping("/transacoes")
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
    @Operation(summary = "Cadastra uma nova transação", description = "Cria uma nova receita ou despesa. Se lançada como PAGA, já efetiva o saldo na conta vinculada.", responses = {
            @ApiResponse(responseCode = "201", description = "Transação criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransacaoDTO.class)), headers = @Header(name = "Location", description = "URL do novo recurso criado")),
            @ApiResponse(responseCode = "400", description = "Regra de Negócio Violada (IDs inválidos)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_422))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
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
     * Endpoint para listar as transações filtradas por data de vencimento e
     * paginadas.
     *
     * <p>
     * Nota: O período máximo permitido entre a data de início e fim é de 90 dias.
     *
     * @param inicio   Data inicial da busca.
     * @param fim      Data final da busca.
     * @param pageable Configurações de paginação do Spring.
     * @return HTTP 200 (OK) com a página de transações.
     */
    @Operation(summary = "Lista transações por período", description = "Retorna uma página de transações baseada no período de vencimento selecionado. O intervalo entre a data inicial e final não pode ser superior a 90 dias.", responses = {
            @ApiResponse(responseCode = "200", description = "Página retornada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (Ex: Período superior a 90 dias ou data inicial maior que final)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_422))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @GetMapping
    public ResponseEntity<PaginacaoDTO<TransacaoDTO>> listarPorPeriodo(
            @Parameter(description = "Data inicial do vencimento (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data final do vencimento (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @PageableDefault(size = 25, sort = "dataVencimento", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(new PaginacaoDTO<>(transacaoService.listarPorPeriodo(inicio, fim, pageable)));
    }

    /**
     * Endpoint para atualizar os dados de uma transação.
     *
     * @param id  O UUID da transação a ser atualizada.
     * @param dto Os novos dados.
     * @return HTTP 200 (OK) com o DTO atualizado.
     */
    @Operation(summary = "Atualiza uma transação", description = "Atualiza os dados e recalcula saldos automaticamente (estorno e efetivação) se houver mudança de status, valor ou conta.", responses = {
            @ApiResponse(responseCode = "200", description = "Transação atualizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransacaoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Regra de Negócio Violada (IDs inválidos)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada ou acesso negado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_404))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_422))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
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
    @Operation(summary = "Exclui uma transação", description = "Remove uma transação do sistema. Se ela estiver PAGA, realiza o estorno no saldo da conta antes da exclusão.", responses = {
            @ApiResponse(responseCode = "204", description = "Transação excluída com sucesso", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada ou acesso negado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_404))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        transacaoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
