package br.com.sfpacim.backend.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.sfpacim.backend.doc.ExemplosDocumentacao;
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
 * Controlador REST responsável pelos endpoints de autenticação
 * Cadastro.
 *
 * @author Matheus F. N. Pereira
 */
@Tag(name = "Autenticação", description = "Endpoints públicos para gestão de acesso (Login, Cadastro e Recuperação de Senha)")
@RestController
@RequestMapping("/autenticacao")
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
     * Endpoint (RF07) para o cadastro de um novo usuário.
     *
     * @param dados Os dados de cadastro (validados pela anotação @Valid).
     * @return HTTP 201 (Created) com o DTO do usuário criado e o Header 'Location'.
     * @throws ViolacaoDadosException Caso o e-mail já esteja cadastrado (RF04).
     */
    @SecurityRequirements({})
    @Operation(summary = "Cadastra um novo usuário", description = "Endpoint público para cadastro de novos usuários. Recebe os dados de registro, cria a conta no sistema e retorna os dados do usuário criado com status 201.", responses = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDTO.class)), headers = @Header(name = "Location", description = "URL do novo recurso criado")),
            @ApiResponse(responseCode = "400", description = "Violação de Dados", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_EMAIL_DUPLICADO))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_VALIDACAO_CADASTRO))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_INTERNO_SERVIDOR)))
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
     * Endpoint (RF08) para autenticar (login) um usuário.
     *
     * @param dados Os dados de autenticação (email e senha) (RF09).
     * @return HTTP 200 (OK) com o Token JWT (RF12).
     *         HTTP 401 (Unauthorized) se as credenciais forem inválidas (RF13).
     */
    @SecurityRequirements({})
    @Operation(summary = "Autentica um usuário", description = "Endpoint público para login. Recebe e-mail e senha e retorna um Token JWT com status 200 se a autenticação for bem-sucedida.", responses = {
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DadosTokenJWTDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não Autorizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_INTERNO_SERVIDOR)))
    })
    @PostMapping("/login")
    public ResponseEntity<DadosTokenJWTDTO> login(@Valid @RequestBody DadosAutenticacaoDTO dados) {
        DadosTokenJWTDTO dadosToken = autenticacaoService.login(dados);

        return ResponseEntity.ok(dadosToken);
    }

    /**
     * Endpoint (RF14) para solicitar a recuperação de senha.
     * 
     * <p>
     * Este endpoint inicia o fluxo de "Esqueci minha senha". Por questões de
     * segurança, ele sempre retornará sucesso, independente de o e-mail existir na
     * base ou não.
     *
     * @param dados O DTO contendo o e-mail do usuário.
     * @return HTTP 204 (No Content).
     */
    @SecurityRequirements({})
    @Operation(summary = "Solicita um link de recuperação de senha", description = "Endpoint público para recuperação de senha. Recebe o e-mail e inicia o envio do link se o usuário existir. Retorna 204 sempre para evitar descoberta de e-mails dos usuários.", responses = {
            @ApiResponse(responseCode = "204", description = "Solicitação recebida com sucesso", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Erro de Validação", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_INTERNO_SERVIDOR)))
    })
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(@Valid @RequestBody DadosRecuperacaoSenhaDTO dados) {
        autenticacaoService.solicitarRecuperacaoSenha(dados);

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint (RF17) para efetivar a redefinição de senha.
     * 
     * <p>
     * Recebe o token enviado por e-mail e a nova senha escolhida pelo usuário.
     * Se o token for válido e a senha segura, a alteração é realizada.
     *
     * @param dados O DTO contendo o token e a nova senha.
     * @return HTTP 204 (No Content) em caso de sucesso.
     */
    @SecurityRequirements({})
    @Operation(summary = "Redefine a senha do usuário", description = "Endpoint público. Recebe o token de recuperação e a nova senha. Se o token for válido (assinatura correta e não expirado), a senha é atualizada.", responses = {
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", description = "Erro de Processamento (Validação de Campos ou Regra de Negócio)", content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                    ErroValidacaoDTO.class, ErroPadraoDTO.class }))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody DadosRedefinicaoSenhaDTO dados) {
        autenticacaoService.redefinirSenha(dados);

        return ResponseEntity.noContent().build();
    }
}
