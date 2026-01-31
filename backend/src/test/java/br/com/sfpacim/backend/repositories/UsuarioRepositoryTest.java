package br.com.sfpacim.backend.repositories;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.sfpacim.backend.models.Usuario;

/**
 * Teste de Integração para o {@link UsuarioRepository}.
 * 
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando um banco de dados em memória (H2) configurado pelo @DataJpaTest.
 *
 * @author Matheus F. N. Pereira
 */
@DataJpaTest
class UsuarioRepositoryTest {

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
     * Testa o método {@link UsuarioRepository#findByEmail(String)}.
     * 
     * <p>
     * Valida o cenário de sucesso, onde o usuário é encontrado.
     */
    @Test
    @DisplayName("findByEmail quando e-mail existir, deve retornar Optional com Usuario")
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
    @DisplayName("findByEmail quando e-mail não existir, deve retornar Optional vazio")
    void testeFindByEmail_QuandoEmailNaoExistir_DeveRetornarVazio() {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail(EMAIL_INEXISTENTE);

        assertFalse(usuarioEncontrado.isPresent(), "O Optional deveria estar vazio");
    }

    /**
     * Testa a restrição (Constraint) de e-mail único (RF04)
     * definida com {@code @Column(unique=true)} na entidade {@link Usuario}.
     */
    @Test
    @DisplayName("persist quando e-mail for duplicado, deve lançar DataIntegrityViolationException")
    void testePersist_QuandoEmailDuplicado_DeveLancarExcecao() {
        entityManager.persistAndFlush(usuario);

        Usuario usuarioDuplicado = new Usuario("Matheus F. N. Pereira", EMAIL,
                "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgH");

        assertThrows(DataIntegrityViolationException.class, () -> {
            usuarioRepository.saveAndFlush(usuarioDuplicado);
        }, "Deveria lançar DataIntegrityViolationException ao salvar e-mail duplicado");
    }
}
