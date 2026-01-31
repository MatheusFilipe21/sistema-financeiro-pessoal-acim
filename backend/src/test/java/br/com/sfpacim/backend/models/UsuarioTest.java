package br.com.sfpacim.backend.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Testes unitários para a entidade {@link Usuario}.
 * 
 * @author Matheus F. N. Pereira
 */
class UsuarioTest {

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private static final String SENHA = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";

    private Usuario usuario;

    /**
     * Configura um objeto {@link Usuario} padrão antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME, EMAIL, SENHA);
    }

    /**
     * Testa o construtor customizado (sem Id) da classe
     * {@link Usuario#Usuario(String, String, String)}.
     * 
     * <p>
     * Verifica se os atributos são inicializados corretamente.
     */
    @Test
    @DisplayName("Construtor sem id deve inicializar atributos corretamente")
    void testConstrutorSemId_QuandoChamado_DeveInicializarAtributos() {
        assertNull(usuario.getId(), "O id do Usuario deveria ser nulo");
        assertEquals(NOME, usuario.getNome(), "O nome do Usuario deveria ser o esperado");
        assertEquals(EMAIL, usuario.getEmail(), "O e-mail do Usuario deveria ser o esperado");
        assertEquals(SENHA, usuario.getSenha(), "A senha do Usuario deveria ser a esperada");
    }

    /**
     * Testa o método {@link Usuario#getAuthorities()}.
     * 
     * <p>
     * Verifica se o usuário recebe a role "ROLE_USUARIO" por padrão.
     */
    @Test
    @DisplayName("getAuthorities deve retornar a role 'ROLE_USUARIO'")
    void testGetAuthorities_QuandoChamado_DeveRetornarRoleUsuario() {
        Collection<? extends GrantedAuthority> autoridades = usuario.getAuthorities();

        assertNotNull(autoridades, "A lista de autoridades não deve ser nula");
        assertEquals(1, autoridades.size(), "Deve haver exatamente uma autoridade");
        assertTrue(autoridades.contains(new SimpleGrantedAuthority("ROLE_USUARIO")),
                "A autoridade 'ROLE_USUARIO' deve estar presente");
    }

    /**
     * Testa o método {@link Usuario#getPassword()}.
     * 
     * <p>
     * Verifica se o método retorna a senha (hash) correta
     * que foi definida no construtor.
     */
    @Test
    @DisplayName("getPassword deve retornar a senha hasheada")
    void testGetPassword_QuandoChamado_DeveRetornarSenhaCorreta() {
        assertEquals(SENHA, usuario.getPassword(), "getPassword() deve retornar a senha definida");
    }

    /**
     * Testa o método {@link Usuario#getUsername()}.
     * 
     * <p>
     * Verifica se o método retorna o e-mail do usuário,
     * conforme a implementação da interface UserDetails.
     */
    @Test
    @DisplayName("getUsername deve retornar o e-mail")
    void testGetUsername_QuandoChamado_DeveRetornarEmailCorreto() {
        assertEquals(EMAIL, usuario.getUsername(), "getUsername() deve retornar o e-mail definido");
    }
}
