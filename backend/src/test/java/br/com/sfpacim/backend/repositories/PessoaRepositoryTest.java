package br.com.sfpacim.backend.repositories;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;

/**
 * Teste de Integração para o {@link PessoaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando um banco de dados em memória (H2) configurado pelo @DataJpaTest.
 *
 * @author Matheus F. N. Pereira
 */
@DataJpaTest
class PessoaRepositoryTest {

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
     * Testa o método {@link PessoaRepository#findByUsuario(Usuario)}.
     *
     * <p>
     * Valida o cenário de sucesso, onde as pessoas vinculadas ao usuário
     * são retornadas corretamente.
     */
    @Test
    @DisplayName("findByUsuario quando existirem registros, deve retornar lista com as pessoas")
    void testeFindByUsuario_QuandoExistiremRegistros_DeveRetornarLista() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);
        entityManager.persist(pessoa2);
        entityManager.flush();

        List<Pessoa> resultado = pessoaRepository.findByUsuario(usuario);

        assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia");
        assertEquals(2, resultado.size(), "Deveria retornar exatos 2 registros");

        assertTrue(resultado.stream().anyMatch(p -> p.getNome().equals(NOME_PESSOA_1)));
        assertTrue(resultado.stream().anyMatch(p -> p.getNome().equals(NOME_PESSOA_2)));
    }

    /**
     * Testa o isolamento de dados.
     *
     * <p>
     * Valida se a busca NÃO retorna registros que pertencem a outro usuário.
     */
    @Test
    @DisplayName("findByUsuario não deve retornar registros de outro usuário")
    void testeFindByUsuario_QuandoUsuarioForDiferente_NaoDeveRetornarRegistros() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);

        Usuario usuarioIntruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(usuarioIntruso);
        entityManager.flush();

        List<Pessoa> resultado = pessoaRepository.findByUsuario(usuarioIntruso);

        assertTrue(resultado.isEmpty(), "A lista deveria estar vazia para o usuário sem registros");
    }
}
