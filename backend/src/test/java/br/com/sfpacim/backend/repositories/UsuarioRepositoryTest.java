package br.com.sfpacim.backend.repositories;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.sfpacim.backend.models.Usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link UsuarioRepository}.
 * 
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando o banco de dados real configurado na classe base
 * {@link BaseRepositoryTest}.
 *
 * @author Matheus F. N. Pereira
 */
class UsuarioRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String NOME = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL = "matheusfnpereira@gmail.com";
    private static final String SENHA = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";
    private static final String EMAIL_INEXISTENTE = "naoexiste@email.com";

    private Usuario usuario;

    /**
     * Configura um objeto {@link Usuario} padrão antes de cada teste.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME, EMAIL, SENHA);
    }

    /**
     * Testa o método {@link UsuarioRepository#existsByEmail(String)}.
     * 
     * <p>
     * Valida o cenário de sucesso, onde o e-mail já está em uso.
     */
    @Test
    @DisplayName("existsByEmail: quando e-mail existir, deve retornar true")
    void testeExistsByEmail_QuandoEmailExistir_DeveRetornarTrue() {
        entityManager.persistAndFlush(usuario);

        boolean existe = usuarioRepository.existsByEmail(EMAIL);

        assertTrue(existe, "Deveria retornar true para um e-mail que já está cadastrado");
    }

    /**
     * Testa o método {@link UsuarioRepository#existsByEmail(String)}.
     * 
     * <p>
     * Valida o cenário onde o e-mail está disponível para uso.
     */
    @Test
    @DisplayName("existsByEmail: quando e-mail não existir, deve retornar false")
    void testeExistsByEmail_QuandoEmailNaoExistir_DeveRetornarFalse() {
        boolean existe = usuarioRepository.existsByEmail(EMAIL_INEXISTENTE);

        assertFalse(existe, "Deveria retornar false para um e-mail que não está cadastrado");
    }

    /**
     * Testa o método {@link UsuarioRepository#findByEmail(String)}.
     * 
     * <p>
     * Valida o cenário de sucesso, onde o usuário é encontrado.
     */
    @Test
    @DisplayName("findByEmail: quando e-mail existir, deve retornar Optional com Usuario")
    void testeFindByEmail_QuandoEmailExistir_DeveRetornarUsuario() {
        entityManager.persistAndFlush(usuario);

        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail(EMAIL);

        assertTrue(usuarioEncontrado.isPresent(), "O Optional não deveria estar vazio");
        assertEquals(NOME, usuarioEncontrado.get().getNome(), "O nome do usuário encontrado está incorreto");
    }

    /**
     * Testa o método {@link UsuarioRepository#findByEmail(String)}.
     *
     * <p>
     * Valida o cenário de falha, onde o e-mail não existe no banco.
     */
    @Test
    @DisplayName("findByEmail: quando e-mail não existir, deve retornar Optional vazio")
    void testeFindByEmail_QuandoEmailNaoExistir_DeveRetornarVazio() {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail(EMAIL_INEXISTENTE);

        assertFalse(usuarioEncontrado.isPresent(), "O Optional deveria estar vazio");
    }

    /**
     * Testa a restrição (Constraint) de e-mail único
     * definida com {@code @Column(unique=true)} na entidade {@link Usuario}.
     */
    @Test
    @DisplayName("persist: quando e-mail for duplicado, deve lançar DataIntegrityViolationException")
    void testePersist_QuandoEmailDuplicado_DeveLancarExcecao() {
        entityManager.persistAndFlush(usuario);

        Usuario usuarioDuplicado = new Usuario("Matheus F. N. Pereira", EMAIL,
                "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgH");

        assertThrows(DataIntegrityViolationException.class, () -> {
            usuarioRepository.saveAndFlush(usuarioDuplicado);
        }, "Deveria lançar DataIntegrityViolationException ao tentar salvar um usuário com e-mail já existente");
    }
}
