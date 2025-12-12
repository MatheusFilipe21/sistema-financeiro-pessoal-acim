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
import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.dtos.pessoa.CriarAtualizarPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.PessoaDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.services.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST responsável pelos endpoints de Pessoas.
 *
 * <p>
 * Expõe os endpoints para criar, listar, atualizar e excluir pessoas.
 * Todos os endpoints exigem autenticação via Token JWT.
 *
 * @author Matheus F. N. Pereira
 */
@Tag(name = "Pessoas", description = "Gestão de Pessoas")
@RestController
@RequestMapping("/pessoas")
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
     * Endpoint (RF46) para cadastrar uma nova pessoa.
     *
     * @param dto Os dados da pessoa (Nome).
     * @return HTTP 201 (Created) com o DTO criado e Header Location.
     * @throws ViolacaoDadosException Caso o nome já exista para o usuário.
     */
    @Operation(summary = "Cadastra uma nova pessoa", description = "Cria um novo registro de pessoa vinculado ao usuário logado. O nome deve ser único para o usuário.", responses = {
            @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PessoaDTO.class)), headers = @Header(name = "Location", description = "URL do novo recurso criado")),
            @ApiResponse(responseCode = "409", description = "Conflito (Nome Duplicado)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
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
     * Endpoint (RF47) para listar todas as pessoas do usuário autenticado.
     *
     * @return HTTP 200 (OK) com a lista de pessoas.
     */
    @Operation(summary = "Lista as pessoas do usuário", description = "Retorna todas as pessoas cadastradas pelo usuário logado. Garante que o usuário veja apenas os seus próprios registros.", responses = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PessoaDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @GetMapping
    public ResponseEntity<List<PessoaDTO>> listar() {
        List<PessoaDTO> pessoas = pessoaService.listar();

        return ResponseEntity.ok(pessoas);
    }

    /**
     * Endpoint para atualizar os dados de uma pessoa.
     *
     * @param id  O UUID da pessoa a ser atualizada.
     * @param dto Os novos dados (Nome).
     * @return HTTP 200 (OK) com o DTO atualizado.
     */
    @Operation(summary = "Atualiza uma pessoa", description = "Atualiza o nome de uma pessoa existente. O usuário só pode alterar pessoas que ele mesmo cadastrou.", responses = {
            @ApiResponse(responseCode = "200", description = "Pessoa atualizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PessoaDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada ou acesso negado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito (Nome Duplicado)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_422))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PessoaDTO> atualizar(@PathVariable UUID id, @Valid @RequestBody CriarAtualizarPessoaDTO dto)
            throws ViolacaoDadosException {
        PessoaDTO pessoaAtualizada = pessoaService.atualizar(id, dto);

        return ResponseEntity.ok(pessoaAtualizada);
    }

    /**
     * Endpoint (RF49) para excluir uma pessoa.
     *
     * @param id O UUID da pessoa a ser excluída.
     * @return HTTP 204 (No Content).
     */
    @Operation(summary = "Exclui uma pessoa", description = "Remove uma pessoa do sistema. A exclusão só é permitida se a pessoa não tiver vínculos com Contas ou Cartões.", responses = {
            @ApiResponse(responseCode = "204", description = "Pessoa excluída com sucesso", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada ou acesso negado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Conflito (Vínculos existentes)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_409))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        pessoaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}
