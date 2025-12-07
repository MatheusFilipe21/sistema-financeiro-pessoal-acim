package br.com.sfpacim.backend.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sfpacim.backend.config.SegurancaConfig;
import br.com.sfpacim.backend.dtos.autenticacao.DadosAutenticacaoDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosRecuperacaoSenhaDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosRedefinicaoSenhaDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosTokenJWTDTO;
import br.com.sfpacim.backend.dtos.usuario.DadosCadastroUsuarioDTO;
import br.com.sfpacim.backend.dtos.usuario.UsuarioDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.exceptions.TratadorDeErrosGlobal;
import br.com.sfpacim.backend.services.AutenticacaoService;
import br.com.sfpacim.backend.services.TokenService;
import br.com.sfpacim.backend.services.UsuarioService;

/**
 * Testes unitários para a classe {@link AutenticacaoController}.
 * 
 * <p>
 * Esta classe utiliza {@link WebMvcTest} para carregar apenas a camada web
 * (MVC) e testa o controlador de forma isolada, simulando (mockando) o
 * UsuarioService.
 *
 * @author Matheus F. N. Pereira
 */
@WebMvcTest(AutenticacaoController.class)
@Import({ SegurancaConfig.class, TratadorDeErrosGlobal.class })
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private static final String SENHA = "Ab123456";
    private static final String NOME_INVALIDO_BRANCO = " ";
    private static final String EMAIL_INVALIDO_FORMATO = "email.invalido.com";
    private static final String SENHA_INVALIDA_REGEX = "123456";
    private static final String TOKEN_JWT = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTRlAtQUNJTSBBUEkiLCJzdWIiOiJtYXRoZXVzZm5wZXJlaXJhQGdtYWlsLmNvbSIsImlhdCI6MTc2MzMwNjE3NiwiZXhwIjoxNzYzMzM0OTc2fQ.e90EOyfiPFUE4Mu5LgbZEtrYnQIGzueecgm4G-fWIKTtSr7IuxC1X_hBkltJBRxHo9ocTvQFje44r0g84TqaiQ";

    /**
     * Testa o endpoint POST /autenticacao/cadastro (RF07).
     * Valida o cenário de sucesso.
     * 
     * <p>
     * Verifica se, ao enviar dados válidos, o controlador retorna HTTP 201
     * (Created),
     * o DTO do usuário no corpo e o cabeçalho 'Location'.
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("cadastrar: Quando dados válidos, deve retornar HTTP 201 Created e o UsuarioDTO")
    void testeCadastrar_QuandoDadosValidos_DeveRetornar201() throws Exception {
        DadosCadastroUsuarioDTO dadosCadastro = new DadosCadastroUsuarioDTO(NOME, EMAIL, SENHA);
        UsuarioDTO usuarioCriado = new UsuarioDTO(UUID.randomUUID(), NOME, EMAIL);

        when(usuarioService.registrar(any(DadosCadastroUsuarioDTO.class))).thenReturn(usuarioCriado);

        String jsonRequisicao = objectMapper.writeValueAsString(dadosCadastro);

        mockMvc.perform(post("/autenticacao/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(usuarioCriado.id().toString()))
                .andExpect(jsonPath("$.nome").value(NOME))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(header().exists("Location"));
    }

    /**
     * Testa a validação do DTO (RF04 - Campo em Branco).
     * 
     * <p>
     * Verifica se, ao enviar um nome que não atende ao @NotBlank,
     * o controlador (via @Valid e TratadorDeErros) retorna HTTP 422.
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @Test
    @DisplayName("cadastrar: Quando nome estiver em branco (DTO Validation), deve retornar HTTP 422")
    @SuppressWarnings("null")
    void testeCadastrar_QuandoNomeEmBranco_DeveRetornarUnprocessableEntity() throws Exception {
        DadosCadastroUsuarioDTO dadosInvalidos = new DadosCadastroUsuarioDTO(NOME_INVALIDO_BRANCO, EMAIL, SENHA);
        String jsonRequisicao = objectMapper.writeValueAsString(dadosInvalidos);

        mockMvc.perform(post("/autenticacao/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Testa a validação do DTO (RF02 - E-mail Inválido).
     * 
     * <p>
     * Verifica se, ao enviar um e-mail que não atende ao @Email,
     * o controlador (via @Valid e TratadorDeErros) retorna HTTP 422.
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @Test
    @DisplayName("cadastrar: Quando e-mail for inválido (DTO Validation), deve retornar HTTP 422")
    @SuppressWarnings("null")
    void testeCadastrar_QuandoEmailInvalido_DeveRetornarUnprocessableEntity() throws Exception {
        DadosCadastroUsuarioDTO dadosInvalidos = new DadosCadastroUsuarioDTO(NOME, EMAIL_INVALIDO_FORMATO,
                SENHA);
        String jsonRequisicao = objectMapper.writeValueAsString(dadosInvalidos);

        mockMvc.perform(post("/autenticacao/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Testa a validação do DTO (RF03 - Senha Fraca).
     * 
     * <p>
     * Verifica se, ao enviar uma senha que não atende ao @Pattern,
     * o controlador (via @Valid e TratadorDeErros) retorna HTTP 422.
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @Test
    @DisplayName("cadastrar: Quando senha for inválida (DTO Validation), deve retornar HTTP 422")
    @SuppressWarnings("null")
    void testeCadastrar_QuandoSenhaInvalida_DeveRetornarUnprocessableEntity() throws Exception {
        DadosCadastroUsuarioDTO dadosInvalidos = new DadosCadastroUsuarioDTO(NOME, EMAIL, SENHA_INVALIDA_REGEX);
        String jsonRequisicao = objectMapper.writeValueAsString(dadosInvalidos);

        mockMvc.perform(post("/autenticacao/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Testa o endpoint POST /autenticacao/login (RF08).
     * Valida o cenário de sucesso (HTTP 200 OK).
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("login: Quando credenciais válidas, deve retornar HTTP 200 OK e o Token JWT")
    void testeLogin_QuandoCredenciaisValidas_DeveRetornar200() throws Exception {
        DadosAutenticacaoDTO dadosLogin = new DadosAutenticacaoDTO(EMAIL, SENHA);
        DadosTokenJWTDTO dadosToken = new DadosTokenJWTDTO(TOKEN_JWT);
        String jsonRequisicao = objectMapper.writeValueAsString(dadosLogin);

        when(autenticacaoService.login(any(DadosAutenticacaoDTO.class))).thenReturn(dadosToken);

        mockMvc.perform(post("/autenticacao/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN_JWT));
    }

    /**
     * Testa o endpoint POST /autenticacao/login (RF13 - Falha de Autenticação).
     * Valida o cenário de credenciais inválidas (HTTP 401 Unauthorized).
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("login: Quando credenciais inválidas (auth failure), deve retornar HTTP 401 Unauthorized")
    void testeLogin_QuandoCredenciaisInvalidas_DeveRetornar401() throws Exception {
        DadosAutenticacaoDTO dadosLogin = new DadosAutenticacaoDTO(EMAIL, "senhaErrada");
        String jsonRequisicao = objectMapper.writeValueAsString(dadosLogin);

        when(autenticacaoService.login(any(DadosAutenticacaoDTO.class)))
                .thenThrow(new BadCredentialsException("E-mail ou senha inválidos."));

        mockMvc.perform(post("/autenticacao/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnauthorized()); // Espera HTTP 401 (capturado pelo TratadorDeErros)
    }

    /**
     * Testa o endpoint POST /autenticacao/login (Validação).
     * Valida o cenário de DTO inválido (ex: e-mail em branco).
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("login: Quando dados de entrada inválidos (DTO Validation), deve retornar HTTP 422")
    void testeLogin_QuandoEmailEmBranco_DeveRetornar422() throws Exception {
        DadosAutenticacaoDTO dadosInvalidos = new DadosAutenticacaoDTO(" ", SENHA);
        String jsonRequisicao = objectMapper.writeValueAsString(dadosInvalidos);

        mockMvc.perform(post("/autenticacao/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Testa o endpoint POST /autenticacao/esqueci-senha (RF14).
     * Valida o cenário de sucesso (Solicitação aceita).
     *
     * <p>
     * Verifica se, ao enviar um e-mail válido, o controlador retorna HTTP 204
     * (No Content) e corpo vazio, indicando que o processo iniciou.
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("esqueciSenha: Quando e-mail válido, deve retornar HTTP 204 No Content")
    void testeEsqueciSenha_QuandoEmailValido_DeveRetornar204() throws Exception {
        DadosRecuperacaoSenhaDTO dadosRecuperacao = new DadosRecuperacaoSenhaDTO(EMAIL);
        String jsonRequisicao = objectMapper.writeValueAsString(dadosRecuperacao);

        mockMvc.perform(post("/autenticacao/esqueci-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    /**
     * Testa a validação do endpoint (RF14 - E-mail Inválido).
     *
     * <p>
     * Verifica se, ao enviar um e-mail com formato inválido no DTO,
     * o controlador retorna HTTP 422 (Unprocessable Entity).
     *
     * @throws Exception se ocorrer um erro durante a execução do MockMvc.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("esqueciSenha: Quando e-mail inválido (DTO Validation), deve retornar HTTP 422")
    void testeEsqueciSenha_QuandoEmailInvalido_DeveRetornar422() throws Exception {
        DadosRecuperacaoSenhaDTO dadosInvalidos = new DadosRecuperacaoSenhaDTO(EMAIL_INVALIDO_FORMATO);
        String jsonRequisicao = objectMapper.writeValueAsString(dadosInvalidos);

        mockMvc.perform(post("/autenticacao/esqueci-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Testa o endpoint POST /autenticacao/redefinir-senha (RF17).
     * Valida o cenário de sucesso.
     *
     * <p>
     * Verifica se, ao enviar um token e uma nova senha válida, o controlador
     * chama o serviço e retorna HTTP 204 (No Content).
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("redefinirSenha: Quando dados válidos, deve retornar HTTP 204 No Content")
    void testeRedefinirSenha_QuandoDadosValidos_DeveRetornar204() throws Exception {
        DadosRedefinicaoSenhaDTO dados = new DadosRedefinicaoSenhaDTO(TOKEN_JWT, SENHA);
        String jsonRequisicao = objectMapper.writeValueAsString(dados);

        mockMvc.perform(post("/autenticacao/redefinir-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    /**
     * Testa a validação do endpoint (Senha Fraca).
     *
     * <p>
     * Verifica se o @Valid barra uma senha que não atende aos requisitos de
     * complexidade.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("redefinirSenha: Quando senha fraca (DTO Validation), deve retornar HTTP 422")
    void testeRedefinirSenha_QuandoSenhaFraca_DeveRetornar422() throws Exception {
        DadosRedefinicaoSenhaDTO dadosInvalidos = new DadosRedefinicaoSenhaDTO(TOKEN_JWT, "123");
        String jsonRequisicao = objectMapper.writeValueAsString(dadosInvalidos);

        mockMvc.perform(post("/autenticacao/redefinir-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Testa a validação do endpoint (Token em branco).
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("redefinirSenha: Quando token em branco (DTO Validation), deve retornar HTTP 422")
    void testeRedefinirSenha_QuandoTokenBranco_DeveRetornar422() throws Exception {
        DadosRedefinicaoSenhaDTO dadosInvalidos = new DadosRedefinicaoSenhaDTO("", SENHA);
        String jsonRequisicao = objectMapper.writeValueAsString(dadosInvalidos);

        mockMvc.perform(post("/autenticacao/redefinir-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Testa o erro de Regra de Negócio (Token Inválido/Expirado).
     *
     * <p>
     * Simula o serviço lançando RegraDeNegocioException e verifica se o
     * TratadorDeErrosGlobal converte corretamente para 422 (Unprocessable Entity),
     * conforme definimos na arquitetura.
     */
    @SuppressWarnings("null")
    @Test
    @DisplayName("redefinirSenha: Quando serviço lança RegraDeNegocioException, deve retornar HTTP 422")
    void testeRedefinirSenha_QuandoTokenInvalidoNoService_DeveRetornar422() throws Exception {
        DadosRedefinicaoSenhaDTO dados = new DadosRedefinicaoSenhaDTO(TOKEN_JWT, SENHA);
        String jsonRequisicao = objectMapper.writeValueAsString(dados);

        doThrow(new RegraDeNegocioException("Token inválido ou expirado."))
                .when(autenticacaoService).redefinirSenha(any(DadosRedefinicaoSenhaDTO.class));

        mockMvc.perform(post("/autenticacao/redefinir-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequisicao))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.titulo").value("Regra de Negócio"));
    }
}
