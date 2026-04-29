package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link PessoaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando um banco de dados em memória (H2) configurado pelo @DataJpaTest.
 *
 * @author Matheus F. N. Pereira
 */
class PessoaRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String NOME_USUARIO = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL_USUARIO = "matheusfnpereira@gmail.com";
    private static final String SENHA_USUARIO = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";

    private static final String NOME_PESSOA_1 = "Alexandre Orlando Gracio";
    private static final String NOME_PESSOA_2 = "Catherine Marie Cavalcanti Aussourd";

    private Usuario usuario;
    private Pessoa pessoa1;
    private Pessoa pessoa2;

    /**
     * Configura o cenário inicial antes de cada teste.
     * Instancia o usuário e as pessoas.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME_USUARIO, EMAIL_USUARIO, SENHA_USUARIO);
        pessoa1 = new Pessoa(NOME_PESSOA_1, usuario);
        pessoa2 = new Pessoa(NOME_PESSOA_2, usuario);
    }

    /**
     * Testa o método
     * {@link PessoaRepository#findByUsuarioIdAndId(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de sucesso, onde a pessoa é encontrada e pertence
     * ao usuário informado.
     */
    @Test
    @DisplayName("findByUsuarioIdAndId quando registro existir e pertencer ao usuário, deve retornar Optional com a pessoa")
    void testeFindByUsuarioIdAndId_QuandoRegistroExistir_DeveRetornarOptionalComPessoa() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);
        entityManager.flush();

        Optional<Pessoa> resultado = pessoaRepository.findByUsuarioIdAndId(usuario.getId(), pessoa1.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio");
        assertEquals(NOME_PESSOA_1, resultado.get().getNome(), "Deveria retornar a pessoa correta");
    }

    /**
     * Testa o isolamento de dados no método
     * {@link PessoaRepository#findByUsuarioIdAndId(UUID, UUID)}.
     *
     * <p>
     * Valida se a busca não retorna uma pessoa que existe no banco, mas que
     * pertence a um usuário diferente do informado na consulta (Defense in Depth).
     */
    @Test
    @DisplayName("findByUsuarioIdAndId quando pessoa pertencer a outro usuário, deve retornar Optional vazio")
    void testeFindByUsuarioIdAndId_QuandoPessoaDeOutroUsuario_DeveRetornarOptionalVazio() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);

        Usuario usuarioIntruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(usuarioIntruso);
        entityManager.flush();

        Optional<Pessoa> resultado = pessoaRepository.findByUsuarioIdAndId(usuarioIntruso.getId(), pessoa1.getId());

        assertFalse(resultado.isPresent(), "O Optional deveria estar vazio pois a pessoa pertence a outro usuário");
    }

    /**
     * Testa o método {@link PessoaRepository#findByUsuarioId}.
     *
     * <p>
     * Valida o cenário de sucesso, onde as pessoas vinculadas ao ID do usuário
     * são retornadas corretamente em uma lista.
     */
    @Test
    @DisplayName("findByUsuarioId quando existirem registros, deve retornar lista com as pessoas")
    void testeFindByUsuarioId_QuandoExistiremRegistros_DeveRetornarLista() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);
        entityManager.persist(pessoa2);
        entityManager.flush();

        List<Pessoa> resultado = pessoaRepository.findByUsuarioId(usuario.getId());

        assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia");
        assertEquals(2, resultado.size(), "Deveria retornar exatos 2 registros");

        assertTrue(resultado.stream().anyMatch(p -> p.getNome().equals(NOME_PESSOA_1)));
        assertTrue(resultado.stream().anyMatch(p -> p.getNome().equals(NOME_PESSOA_2)));
    }

    /**
     * Testa o isolamento de dados no método
     * {@link PessoaRepository#findByUsuarioId(UUID)}.
     *
     * <p>
     * Valida se a busca NÃO retorna registros que pertencem a outro usuário.
     */
    @Test
    @DisplayName("findByUsuarioId não deve retornar registros de outro usuário")
    void testeFindByUsuarioId_QuandoUsuarioForDiferente_NaoDeveRetornarRegistros() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);

        Usuario usuarioIntruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(usuarioIntruso);
        entityManager.flush();

        List<Pessoa> resultado = pessoaRepository.findByUsuarioId(usuarioIntruso.getId());

        assertTrue(resultado.isEmpty(), "A lista deveria estar vazia para o usuário sem registros");
    }
}
