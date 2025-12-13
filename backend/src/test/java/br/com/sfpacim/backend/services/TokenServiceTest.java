package br.com.sfpacim.backend.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.sfpacim.backend.models.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

/**
 * Testes unitários para a classe {@link TokenService}.
 * 
 * <p>
 * Utiliza Mockito e ReflectionTestUtils para isolar o serviço
 * e injetar as propriedades (secret/expiration) lidas do @Value.
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private static final String SENHA_HASH = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";
    private static final String CHAVE_SECRETA_TESTE = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final Long OITO_HORAS_MS = 28800000L;

    private Usuario usuarioMock;

    /**
     * Configura o TokenService antes de cada teste.
     * Injeta os valores (que viriam do @Value) usando ReflectionTestUtils.
     */
    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "chaveSecreta", CHAVE_SECRETA_TESTE);
        ReflectionTestUtils.setField(tokenService, "tempoExpiracaoMs", OITO_HORAS_MS);

        this.usuarioMock = new Usuario(UUID.randomUUID(), "Matheus Filipe do Nascimento Pereira", EMAIL, SENHA_HASH);
    }

    /**
     * Testa o método {@link TokenService#gerarToken(Usuario)}.
     * Valida o cenário de sucesso da geração do token.
     */
    @Test
    @DisplayName("gerarToken: Quando usuário válido, deve gerar um token JWT")
    void testeGerarToken_QuandoUsuarioValido_DeveGerarToken() {
        String token = tokenService.gerarToken(usuarioMock);

        assertNotNull(token, "O token não deve ser nulo");
        assertFalse(token.isBlank(), "O token não deve estar em branco");
    }

    /**
     * Testa o método {@link TokenService#validarToken(String)}.
     * Valida o cenário de sucesso (ciclo completo: gerar e validar).
     */
    @Test
    @DisplayName("validarToken: Quando token for válido, deve retornar o e-mail (subject)")
    void testeValidarToken_QuandoTokenValido_DeveRetornarSubject() {
        String token = tokenService.gerarToken(usuarioMock);

        String subject = tokenService.validarToken(token);

        assertEquals(EMAIL, subject, "O subject (e-mail) retornado deve ser o mesmo do usuário");
    }

    /**
     * Testa o método {@link TokenService#validarToken(String)}.
     * Valida o cenário de falha (assinatura inválida).
     */
    @Test
    @DisplayName("validarToken: Quando assinatura for inválida, deve lançar SignatureException")
    void testeValidarToken_QuandoAssinaturaInvalida_DeveLancarExcecao() {
        String tokenValido = tokenService.gerarToken(usuarioMock);

        TokenService tokenServiceInvalido = new TokenService();
        ReflectionTestUtils.setField(tokenServiceInvalido, "chaveSecreta",
                "999E635266556A586E3272357538782F413F4428472B4B6250645367566B5999");

        assertThrows(SignatureException.class, () -> {
            tokenServiceInvalido.validarToken(tokenValido);
        }, "Deveria lançar SignatureException (assinatura inválida)");
    }

    /**
     * Testa o método {@link TokenService#validarToken(String)}.
     * Valida o cenário de falha (token expirado).
     */
    @Test
    @DisplayName("validarToken: Quando token estiver expirado, deve lançar ExpiredJwtException")
    void testeValidarToken_QuandoTokenExpirado_DeveLancarExcecao() {
        TokenService tokenServiceExpirado = new TokenService();
        ReflectionTestUtils.setField(tokenServiceExpirado, "chaveSecreta", CHAVE_SECRETA_TESTE);
        ReflectionTestUtils.setField(tokenServiceExpirado, "tempoExpiracaoMs", 1L);

        String tokenExpirado = tokenServiceExpirado.gerarToken(usuarioMock);

        assertThrows(ExpiredJwtException.class, () -> {
            tokenService.validarToken(tokenExpirado);
        }, "Deveria lançar ExpiredJwtException (token expirado)");
    }

    /**
     * Testa o método {@link TokenService#gerarTokenRecuperacao(Usuario)}.
     * Valida se o token de recuperação é gerado corretamente.
     */
    @Test
    @DisplayName("gerarTokenRecuperacao: Deve gerar token com assinatura dinâmica")
    void testeGerarTokenRecuperacao_DeveGerarToken() {
        String token = tokenService.gerarTokenRecuperacao(usuarioMock);

        assertNotNull(token, "O token de recuperação não deve ser nulo");
        assertFalse(token.isBlank(), "O token de recuperação não deve estar em branco");
    }

    /**
     * Testa o método {@link TokenService#validarTokenRecuperacao(String, Usuario)}.
     * Valida o cenário de sucesso (Senha não mudou).
     */
    @Test
    @DisplayName("validarTokenRecuperacao: Quando senha não mudou, deve validar com sucesso")
    void testeValidarTokenRecuperacao_QuandoSenhaIntacta_DeveValidar() {
        String token = tokenService.gerarTokenRecuperacao(usuarioMock);

        String subject = tokenService.validarTokenRecuperacao(token, usuarioMock);

        assertEquals(EMAIL, subject, "Deve retornar o e-mail do usuário");
    }

    /**
     * Testa o método {@link TokenService#validarTokenRecuperacao(String, Usuario)}.
     * Valida o cenário de segurança: A senha mudou após a emissão do token.
     * 
     * <p>
     * O token deve ser considerado inválido (assinatura não bate) pois a chave
     * de validação é derivada da nova senha.
     */
    @Test
    @DisplayName("validarTokenRecuperacao: Quando senha foi alterada, deve lançar SignatureException")
    void testeValidarTokenRecuperacao_QuandoSenhaAlterada_DeveFalhar() {
        String tokenComSenhaAntiga = tokenService.gerarTokenRecuperacao(usuarioMock);

        Usuario usuarioComNovaSenha = new Usuario(
                usuarioMock.getId(),
                usuarioMock.getNome(),
                usuarioMock.getEmail(),
                "Ab1234567");

        assertThrows(SignatureException.class, () -> {
            tokenService.validarTokenRecuperacao(tokenComSenhaAntiga, usuarioComNovaSenha);
        }, "Deveria lançar erro de assinatura pois a senha mudou");
    }

    /**
     * Testa o método {@link TokenService#obterEmailDoToken(String)}.
     * Valida a extração do subject (e-mail) sem validar assinatura.
     */
    @Test
    @DisplayName("obterEmailDoToken: Quando token válido, deve extrair o e-mail")
    void testeObterEmailDoToken_QuandoTokenValido_DeveRetornarEmail() {
        String token = tokenService.gerarToken(usuarioMock);

        String emailExtraido = tokenService.obterEmailDoToken(token);

        assertEquals(EMAIL, emailExtraido);
    }

    /**
     * Testa o método {@link TokenService#obterEmailDoToken(String)}.
     * Valida o cenário onde o token não tem a estrutura mínima (pontos).
     */
    @Test
    @DisplayName("obterEmailDoToken: Quando token não tem pontos (estrutura incorreta), deve retornar null")
    void testeObterEmailDoToken_QuandoTokenSemPontos_DeveRetornarNull() {
        String tokenMalFormado = "TokenSemPontosSeparadores";

        String resultado = tokenService.obterEmailDoToken(tokenMalFormado);

        assertNull(resultado, "Deveria retornar null pois não tem payload separável");
    }

    /**
     * Testa o método {@link TokenService#obterEmailDoToken(String)}.
     * Valida o tratamento de erro para tokens malformados.
     */
    @Test
    @DisplayName("obterEmailDoToken: Quando token malformado, deve retornar null")
    void testeObterEmailDoToken_QuandoTokenInvalido_DeveRetornarNull() {
        String tokenInvalido = "token.sem.formato.valido";

        String resultado = tokenService.obterEmailDoToken(tokenInvalido);

        assertNull(resultado, "Deveria retornar null para token ilegível");
    }
}
