package br.com.sfpacim.backend.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sfpacim.backend.doc.ExemplosDocumentacao;
import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.usuario.UsuarioDTO;
import br.com.sfpacim.backend.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador REST responsável pelos endpoints de Usuários.
 *
 * <p>
 * Expõe os endpoints para recuperação de perfil do usuário logado e
 * atualização de dados cadastrais.
 * Todos os endpoints exigem autenticação via Token JWT.
 *
 * @author Iago Leonam G.
 */
@Tag(name = "Usuários", description = "Gestão de Usuários")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Construtor para Injeção de Dependências.
     *
     * @param usuarioService O serviço que lida com a lógica de usuários.
     */
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Endpoint para recuperar os dados do usuário autenticado.
     *
     * @return HTTP 200 (OK) com o DTO contendo os dados do perfil logado.
     */
    @Operation(summary = "Recupera dados do usuário logado", description = "Retorna as informações de perfil (nome e e-mail) do usuário associado ao token JWT enviado na requisição.", responses = {
            @ApiResponse(responseCode = "200", description = "Dados recuperados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro Interno do Servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroPadraoDTO.class), examples = @ExampleObject(value = ExemplosDocumentacao.ERRO_500)))
    })
    @GetMapping("/eu")
    public ResponseEntity<UsuarioDTO> dadosUsuarioAutenticado() {
        UsuarioDTO usuario = usuarioService.dadosUsuarioAutenticado();

        return ResponseEntity.ok(usuario);
    }
}
