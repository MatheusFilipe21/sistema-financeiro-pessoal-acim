package br.com.sfpacim.backend.services;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para a classe de serviço {@link ContextoUsuarioService}.
 *
 * <p>
 * Verifica a lógica de recuperação do usuário autenticado a partir do
 * contexto de segurança do Spring (SecurityContextHolder).
 *
 * @author Matheus F. N. Pereira
 */
@ExtendWith(MockitoExtension.class)
class ContextoUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ContextoUsuarioService contextoUsuarioService;

    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private Usuario usuario;

    /**
     * Configura o ambiente de teste antes de cada execução.
     *
     * <p>
     * Inicializa o objeto {@link Usuario}, configura o mock do
     * SecurityContextHolder e padroniza as respostas do MessageSource.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario("Matheus Filipe do Nascimento Pereira",
                "matheusfnpereira@gmail.com", "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK");

        SecurityContextHolder.setContext(securityContext);

        Answer<String> answerMensagemDinamica = invocation -> {
            String codigo = invocation.getArgument(0);
            if ("erro.seguranca.contexto.vazio".equals(codigo)) {
                return "Não há usuário autenticado no contexto de segurança.";
            }
            if ("erro.seguranca.usuario.nao-encontrado".equals(codigo)) {
                return "Usuário autenticado não encontrado na base de dados.";
            }
            return "Mensagem Mockada";
        };

        Mockito.lenient()
                .when(messageSource.getMessage(anyString(), any(), any()))
                .thenAnswer(answerMensagemDinamica);
    }

    /**
     * Limpa o contexto de segurança após cada teste.
     *
     * <p>
     * Garante que o estado de um teste não interfira nos demais (isolamento).
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Testa o método {@link ContextoUsuarioService#getUsuarioAutenticado()}.
     *
     * <p>
     * Cenário de Sucesso: O contexto possui uma autenticação válida e o
     * e-mail extraído do token existe na base de dados.
     */
    @Test
    @DisplayName("getUsuarioAutenticado quando token válido e usuário existir, deve retornar entidade")
    void testGetUsuarioAutenticado_QuandoTudoValido_DeveRetornarUsuario() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        Usuario resultado = contextoUsuarioService.getUsuarioAutenticado();

        assertNotNull(resultado, "O usuário retornado não deve ser nulo");
        assertEquals(EMAIL, resultado.getEmail(), "O e-mail do usuário deve corresponder ao do token");
        verify(usuarioRepository).findByEmail(EMAIL);
    }

    /**
     * Testa o método {@link ContextoUsuarioService#getUsuarioAutenticado()}.
     *
     * <p>
     * Cenário de Erro: O objeto de autenticação no contexto é nulo (ex: falha
     * nos filtros de segurança ou endpoint público).
     */
    @Test
    @DisplayName("getUsuarioAutenticado quando sem autenticação no contexto, deve lançar exceção")
    void testGetUsuarioAutenticado_QuandoContextoNulo_DeveLancarExcecao() {

        when(securityContext.getAuthentication()).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            contextoUsuarioService.getUsuarioAutenticado();
        }, "Deveria lançar UsernameNotFoundException quando não há autenticação");

        assertEquals("Não há usuário autenticado no contexto de segurança.", exception.getMessage());
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    /**
     * Testa o método {@link ContextoUsuarioService#getUsuarioAutenticado()}.
     *
     * <p>
     * Cenário de Erro: Existe um objeto de autenticação, mas o método
     * isAuthenticated() retorna false (token inválido ou expirado).
     */
    @Test
    @DisplayName("getUsuarioAutenticado quando isAuthenticated for false, deve lançar exceção")
    void testGetUsuarioAutenticado_QuandoNaoAutenticado_DeveLancarExcecao() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            contextoUsuarioService.getUsuarioAutenticado();
        }, "Deveria lançar UsernameNotFoundException quando isAuthenticated() é false");

        assertEquals("Não há usuário autenticado no contexto de segurança.", exception.getMessage());
        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    /**
     * Testa o método {@link ContextoUsuarioService#getUsuarioAutenticado()}.
     *
     * <p>
     * Cenário de Erro (Inconsistência de Dados): O token é válido e contém um
     * e-mail, mas esse usuário foi removido do banco de dados recentemente.
     */
    @Test
    @DisplayName("getUsuarioAutenticado quando usuário não encontrado no banco, deve lançar exceção")
    void testGetUsuarioAutenticado_QuandoUsuarioInexistenteNoBanco_DeveLancarExcecao() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(EMAIL);

        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            contextoUsuarioService.getUsuarioAutenticado();
        }, "Deveria lançar UsernameNotFoundException quando o banco não encontrar o e-mail");

        assertEquals("Usuário autenticado não encontrado na base de dados.", exception.getMessage());
    }
}
