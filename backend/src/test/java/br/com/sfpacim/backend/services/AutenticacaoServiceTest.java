package br.com.sfpacim.backend.services;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.sfpacim.backend.dtos.autenticacao.DadosAutenticacaoDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosRecuperacaoSenhaDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosRedefinicaoSenhaDTO;
import br.com.sfpacim.backend.dtos.autenticacao.DadosTokenJWTDTO;
import br.com.sfpacim.backend.exceptions.RegraDeNegocioException;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.UsuarioRepository;
import br.com.sfpacim.backend.services.interfaces.EmailService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a classe {@link AutenticacaoService}.
 * 
 * <p>
 * Utiliza Mockito para isolar o serviço das dependências externas
 * (AuthenticationManager e TokenService).
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private static final String SENHA = "Ab123456";
    private static final String SENHA_HASH = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";
    private static final String TOKEN_JWT = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTRlAtQUNJTSBBUEkiLCJzdWIiOiJtYXRoZXVzZm5wZXJlaXJhQGdtYWlsLmNvbSIsImlhdCI6MTc2MzMwNjE3NiwiZXhwIjoxNzYzMzM0OTc2fQ.e90EOyfiPFUE4Mu5LgbZEtrYnQIGzueecgm4G-fWIKTtSr7IuxC1X_hBkltJBRxHo9ocTvQFje44r0g84TqaiQ";
    private static final String TOKEN_RECUPERACAO = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJTRlAtQUNJTSBBUEkgUmVjdXBlcmFjYW8iLCJzdWIiOiJtYXRoZXVzZm5wZXJlaXJhQGdtYWlsLmNvbSIsImlhdCI6MTc2NTEwNDM1NywiZXhwIjoxNzY1MTE4NzU3fQ.bXRA5FUJ-7jJZS-7UCbz80PmHTsLGtOH_w0gG5FoR7w8JgJGbXyDC8dax9I_eNwWjgij1VGyapgR1hW5E3ddXQ";
    private static final String URL_FRONTEND = "http://localhost:4200";

    /**
     * Configura o cenário comum e mocks estáticos antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        Answer<String> answerMensagemDinamica = invocation -> {
            String codigo = invocation.getArgument(0);

            if ("erro.autenticacao.token.invalido".equals(codigo)) {
                return "Token inválido ou expirado.";
            }
            if ("email.recuperacao.senha.mensagem".equals(codigo)) {
                Object[] args = invocation.getArgument(1);

                String linkGerado = (args != null && args.length > 1) ? args[1].toString() : "";
                return "Acesse o link de recuperação: " + linkGerado;
            }
            if ("email.recuperacao.senha.assunto".equals(codigo)) {
                return "Assunto de Recuperação";
            }
            return "Mensagem Mockada";
        };

        Mockito.lenient()
                .when(messageSource.getMessage(anyString(), any(), any()))
                .thenAnswer(answerMensagemDinamica);

        Mockito.lenient()
                .when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenAnswer(answerMensagemDinamica);
    }

    /**
     * Testa o método {@link AutenticacaoService#login(DadosAutenticacaoDTO)}.
     * Valida o cenário de sucesso.
     * 
     * <p>
     * Verifica se o serviço chama o AuthenticationManager e o TokenService
     * corretamente e retorna o DTO do token.
     */
    @Test
    @DisplayName("login: Quando credenciais válidas, deve autenticar e retornar o token JWT")
    void testeLogin_QuandoCredenciaisValidas_DeveRetornarToken() {
        DadosAutenticacaoDTO dadosLogin = new DadosAutenticacaoDTO(EMAIL, SENHA);

        Usuario usuarioMock = new Usuario(UUID.randomUUID(), NOME, EMAIL, SENHA_HASH);

        Authentication authenticationMock = new UsernamePasswordAuthenticationToken(usuarioMock, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);

        when(tokenService.gerarToken(usuarioMock)).thenReturn(TOKEN_JWT);

        DadosTokenJWTDTO resultadoDTO = autenticacaoService.login(dadosLogin);

        assertNotNull(resultadoDTO, "O DTO de token não deve ser nulo");
        assertEquals(TOKEN_JWT, resultadoDTO.token(), "O token JWT retornado deve ser o esperado");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, times(1)).gerarToken(usuarioMock);
    }

    /**
     * Testa o método {@link AutenticacaoService#login(DadosAutenticacaoDTO)}.
     * Valida o cenário de falha (credenciais inválidas).
     * 
     * <p>
     * Verifica se o serviço repassa a exceção (AuthenticationException)
     * lançada pelo AuthenticationManager.
     */
    @Test
    @DisplayName("login: Quando credenciais inválidas, deve lançar AuthenticationException")
    void testeLogin_QuandoCredenciaisInvalidas_DeveLancarExcecao() {
        DadosAutenticacaoDTO dadosLogin = new DadosAutenticacaoDTO(EMAIL, "senhaErrada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Credenciais inválidas") {
                });

        assertThrows(AuthenticationException.class, () -> {
            autenticacaoService.login(dadosLogin);
        }, "Deveria lançar AuthenticationException");
    }

    /**
     * Testa o método
     * {@link AutenticacaoService#solicitarRecuperacaoSenha(DadosRecuperacaoSenhaDTO)}.
     * Cenário: Usuário existe na base.
     * 
     * <p>
     * Deve:
     * 1. Buscar o usuário.
     * 2. Gerar o token de recuperação.
     * 3. Enviar o e-mail contendo o link correto.
     */
    @Test
    @DisplayName("solicitarRecuperacaoSenha: Quando e-mail existe, deve gerar token e enviar e-mail")
    void testeSolicitarRecuperacao_QuandoUsuarioExiste_DeveEnviarEmail() {
        DadosRecuperacaoSenhaDTO dados = new DadosRecuperacaoSenhaDTO(EMAIL);
        Usuario usuarioMock = new Usuario(UUID.randomUUID(), NOME, EMAIL, SENHA_HASH);

        ReflectionTestUtils.setField(autenticacaoService, "urlFrontend", URL_FRONTEND);

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioMock));
        when(tokenService.gerarTokenRecuperacao(usuarioMock)).thenReturn(TOKEN_RECUPERACAO);

        autenticacaoService.solicitarRecuperacaoSenha(dados);

        verify(usuarioRepository).findByEmail(EMAIL);
        verify(tokenService).gerarTokenRecuperacao(usuarioMock);

        String linkEsperado = URL_FRONTEND + "/redefinir-senha?token=" + TOKEN_RECUPERACAO;

        verify(emailService).enviar(
                eq(EMAIL),
                anyString(),
                contains(linkEsperado));
    }

    /**
     * Testa o método
     * {@link AutenticacaoService#solicitarRecuperacaoSenha(DadosRecuperacaoSenhaDTO)}.
     * Cenário: Usuário não existe na base.
     * 
     * <p>
     * Deve finalizar silenciosamente (sem erro) e não enviar e-mail (segurança).
     */
    @Test
    @DisplayName("solicitarRecuperacaoSenha: Quando e-mail não existe, deve finalizar silenciosamente")
    void testeSolicitarRecuperacao_QuandoUsuarioNaoExiste_NaoDeveFazerNada() {
        DadosRecuperacaoSenhaDTO dados = new DadosRecuperacaoSenhaDTO("naoexiste@email.com");
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        autenticacaoService.solicitarRecuperacaoSenha(dados);

        verify(usuarioRepository).findByEmail(anyString());
        verify(tokenService, never()).gerarTokenRecuperacao(any());
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
    }

    /**
     * Testa o método
     * {@link AutenticacaoService#redefinirSenha(DadosRedefinicaoSenhaDTO)}.
     * Cenário: Sucesso.
     *
     * <p>
     * O token é válido, o usuário é encontrado e a assinatura confere.
     * Deve chamar o usuarioService para atualizar a senha.
     */
    @Test
    @DisplayName("redefinirSenha: Quando token válido, deve delegar atualização para UsuarioService")
    void testeRedefinirSenha_QuandoSucesso_DeveAtualizarSenha() {
        String novaSenha = "NovaSenha123!";
        DadosRedefinicaoSenhaDTO dados = new DadosRedefinicaoSenhaDTO(TOKEN_RECUPERACAO, novaSenha);
        Usuario usuarioMock = new Usuario(UUID.randomUUID(), NOME, EMAIL, SENHA_HASH);

        when(tokenService.obterEmailDoToken(TOKEN_RECUPERACAO)).thenReturn(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioMock));

        autenticacaoService.redefinirSenha(dados);

        verify(tokenService).validarTokenRecuperacao(TOKEN_RECUPERACAO, usuarioMock);
        verify(usuarioService).atualizarSenha(usuarioMock, novaSenha);
    }

    /**
     * Testa o método
     * {@link AutenticacaoService#redefinirSenha(DadosRedefinicaoSenhaDTO)}.
     * Cenário: Token ilegível (não conseguiu extrair e-mail).
     */
    @Test
    @DisplayName("redefinirSenha: Quando token ilegível (sem e-mail), deve lançar RegraDeNegocioException")
    void testeRedefinirSenha_QuandoTokenIlegivel_DeveLancarExcecao() {
        DadosRedefinicaoSenhaDTO dados = new DadosRedefinicaoSenhaDTO("token.invalido", "Senha123!");

        when(tokenService.obterEmailDoToken(anyString())).thenReturn(null);

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            autenticacaoService.redefinirSenha(dados);
        });

        assertEquals("Token inválido ou expirado.", ex.getMessage());
    }

    /**
     * Testa o método
     * {@link AutenticacaoService#redefinirSenha(DadosRedefinicaoSenhaDTO)}.
     * Cenário: Token contém e-mail, mas usuário não existe no banco.
     */
    @Test
    @DisplayName("redefinirSenha: Quando usuário não encontrado, deve lançar RegraDeNegocioException")
    void testeRedefinirSenha_QuandoUsuarioNaoEncontrado_DeveLancarExcecao() {
        DadosRedefinicaoSenhaDTO dados = new DadosRedefinicaoSenhaDTO(TOKEN_RECUPERACAO, "Senha123!");

        when(tokenService.obterEmailDoToken(TOKEN_RECUPERACAO)).thenReturn(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            autenticacaoService.redefinirSenha(dados);
        });

        assertEquals("Token inválido ou expirado.", ex.getMessage());
    }

    /**
     * Testa o método
     * {@link AutenticacaoService#redefinirSenha(DadosRedefinicaoSenhaDTO)}.
     * Cenário: Falha na validação da assinatura (Token expirado ou senha antiga
     * alterada).
     */
    @Test
    @DisplayName("redefinirSenha: Quando assinatura do token falha, deve lançar RegraDeNegocioException")
    void testeRedefinirSenha_QuandoAssinaturaFalha_DeveLancarExcecao() {
        DadosRedefinicaoSenhaDTO dados = new DadosRedefinicaoSenhaDTO(TOKEN_RECUPERACAO, "Senha123!");
        Usuario usuarioMock = new Usuario(UUID.randomUUID(), NOME, EMAIL, SENHA_HASH);

        when(tokenService.obterEmailDoToken(TOKEN_RECUPERACAO)).thenReturn(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioMock));

        doThrow(new RuntimeException("Assinatura inválida"))
                .when(tokenService).validarTokenRecuperacao(TOKEN_RECUPERACAO, usuarioMock);

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            autenticacaoService.redefinirSenha(dados);
        });

        assertEquals("Token inválido ou expirado.", ex.getMessage());
        verify(usuarioService, never()).atualizarSenha(any(), anyString());
    }
}
