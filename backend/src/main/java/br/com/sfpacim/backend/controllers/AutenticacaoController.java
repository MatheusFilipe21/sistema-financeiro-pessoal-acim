package br.com.sfpacim.backend.controllers;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.sfpacim.backend.dtos.autenticacao.DadosAutenticacaoDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosRecuperacaoSenhaDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosRedefinicaoSenhaDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosTokenJWTDTO;
import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.dtos.usuario.DadosCadastroUsuarioDTO;
import br.com.sfpacim.backend.dtos.usuario.UsuarioDTO;
import br.com.sfpacim.backend.exceptions.ViolacaoDadosException;
import br.com.sfpacim.backend.services.AutenticacaoService;
import br.com.sfpacim.backend.services.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST responsável pelos endpoints de autenticação.
 *
 * @author Matheus F. N. Pereira
 */
@Tag(name = "${autenticacao.controller.nome}", description = "${autenticacao.controller.descricao}")
@RestController
@RequestMapping(value = "/autenticacao", produces = MediaType.APPLICATION_JSON_VALUE)
public class AutenticacaoController {

    private final UsuarioService usuarioService;
    private final AutenticacaoService autenticacaoService;

    /**
     * Construtor para Injeção de Dependências.
     * 
     * <p>
     * O Spring injeta automaticamente a instância de UsuarioService
     * quando esta classe é criada.
     *
     * @param usuarioService      O serviço que lida com a lógica de usuários.
     * @param autenticacaoService O serviço que lida com a lógica de login.
     */
    public AutenticacaoController(UsuarioService usuarioService,
            AutenticacaoService autenticacaoService) {
        this.usuarioService = usuarioService;
        this.autenticacaoService = autenticacaoService;
    }

    /**
     * Endpoint para o cadastro de um novo usuário.
     *
     * @param dados Os dados de cadastro (validados pela anotação @Valid).
     * @return HTTP 201 (Created) com o DTO do usuário criado e o Header 'Location'.
     * @throws ViolacaoDadosException Caso o e-mail já esteja cadastrado.
     */
    @SecurityRequirements({})
    @Operation(summary = "${autenticacao.controller.cadastro.resumo}", description = "${autenticacao.controller.cadastro.descricao}", responses = {
            @ApiResponse(responseCode = "201", description = "${autenticacao.controller.cadastro.resposta.201}", content = @Content(schema = @Schema(implementation = UsuarioDTO.class)), headers = @Header(name = "Location", description = "${geral.header.location.descricao}", schema = @Schema(type = "string", example = "${autenticacao.controller.cadastro.header.location.exemplo}"))),
            @ApiResponse(responseCode = "409", description = "${geral.resposta.409.duplicado}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.cadastro.exemplo.409}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.cadastro.exemplo.422}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.cadastro.exemplo.500}")))
    })
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioDTO> cadastrar(@Valid @RequestBody DadosCadastroUsuarioDTO dados)
            throws ViolacaoDadosException {
        UsuarioDTO usuario = usuarioService.registrar(dados);

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/usuarios/{id}")
                .buildAndExpand(usuario.id())
                .toUri();

        return ResponseEntity.created(uri).body(usuario);
    }

    /**
     * Endpoint para autenticar (login) um usuário.
     *
     * @param dados Os dados de autenticação (email e senha).
     * @return HTTP 200 (OK) com o Token JWT.
     *         HTTP 401 (Unauthorized) se as credenciais forem inválidas.
     */
    @SecurityRequirements({})
    @Operation(summary = "${autenticacao.controller.login.resumo}", description = "${autenticacao.controller.login.descricao}", responses = {
            @ApiResponse(responseCode = "200", description = "${autenticacao.controller.login.resposta.200}", content = @Content(schema = @Schema(implementation = DadosTokenJWTDTO.class))),
            @ApiResponse(responseCode = "401", description = "${erro.401.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.login.exemplo.401}"))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.login.exemplo.422}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.login.exemplo.500}")))
    })
    @PostMapping("/login")
    public ResponseEntity<DadosTokenJWTDTO> login(@Valid @RequestBody DadosAutenticacaoDTO dados) {
        DadosTokenJWTDTO dadosToken = autenticacaoService.login(dados);

        return ResponseEntity.ok(dadosToken);
    }

    /**
     * Endpoint para solicitar a recuperação de senha.
     * 
     * <p>
     * Este endpoint inicia o fluxo de "Recuperação de senha". Por questões de
     * segurança, ele sempre retornará sucesso, independente de o e-mail existir na
     * base ou não.
     *
     * @param dados O DTO contendo o e-mail do usuário.
     * @return HTTP 204 (No Content).
     */
    @SecurityRequirements({})
    @Operation(summary = "${autenticacao.controller.recuperar-senha.resumo}", description = "${autenticacao.controller.recuperar-senha.descricao}", responses = {
            @ApiResponse(responseCode = "204", description = "${autenticacao.controller.recuperar-senha.resposta.204}", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.validacao}", content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.recuperar-senha.exemplo.422}"))),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.recuperar-senha.exemplo.500}")))
    })
    @PostMapping("/recuperar-senha")
    public ResponseEntity<Void> recuperarSenha(@Valid @RequestBody DadosRecuperacaoSenhaDTO dados) {
        autenticacaoService.solicitarRecuperacaoSenha(dados);

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para efetivar a redefinição de senha.
     * 
     * <p>
     * Recebe o token enviado por e-mail e a nova senha escolhida pelo usuário.
     * Se o token for válido e a senha segura, a alteração é realizada.
     *
     * @param dados O DTO contendo o token e a nova senha.
     * @return HTTP 204 (No Content) em caso de sucesso.
     */
    @SecurityRequirements({})
    @Operation(summary = "${autenticacao.controller.redefinir-senha.resumo}", description = "${autenticacao.controller.redefinir-senha.descricao}", responses = {
            @ApiResponse(responseCode = "204", description = "${autenticacao.controller.redefinir-senha.resposta.204}", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "${geral.resposta.422.negocio-validacao}", content = @Content(schema = @Schema(oneOf = {
                    ErroValidacaoDTO.class, ErroPadraoDTO.class }), examples = {
                            @ExampleObject(name = "${erro.exemplo.dropdown.validacao}", value = "${autenticacao.controller.redefinir-senha.exemplo.422.campos}"),
                            @ExampleObject(name = "${erro.exemplo.dropdown.negocio}", value = "${autenticacao.controller.redefinir-senha.exemplo.422.token}")
                    })),
            @ApiResponse(responseCode = "500", description = "${erro.500.titulo}", content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = "${autenticacao.controller.redefinir-senha.exemplo.500}")))
    })
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody DadosRedefinicaoSenhaDTO dados) {
        autenticacaoService.redefinirSenha(dados);

        return ResponseEntity.noContent().build();
    }
}
