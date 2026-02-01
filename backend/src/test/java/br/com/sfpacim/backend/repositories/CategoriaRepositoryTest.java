package br.com.sfpacim.backend.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.TipoCategoria;

/**
 * Teste de Integração para o {@link CategoriaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando um banco de dados em memória (H2) configurado pelo @DataJpaTest.
 *
 * @author Matheus F. N. Pereira
 */
@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String NOME_USUARIO = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL_USUARIO = "matheusfnpereira@gmail.com";
    private static final String SENHA_USUARIO = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";

    private static final String CAT_SISTEMA_NOME = "Alimentação";
    private static final String CAT_USUARIO_NOME = "Streaming";
    private static final String CAT_ICONE = "restaurant";
    private static final String CAT_COR = "#FF0000";

    private Usuario usuario;
    private Categoria categoriaSistema;
    private Categoria categoriaUsuario;

    /**
     * Configura o cenário inicial antes de cada teste.
     * Instancia o usuário e as categorias.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME_USUARIO, EMAIL_USUARIO, SENHA_USUARIO);

        categoriaSistema = new Categoria(CAT_SISTEMA_NOME, TipoCategoria.DESPESA, CAT_ICONE, CAT_COR, null);

        categoriaUsuario = new Categoria(CAT_USUARIO_NOME, TipoCategoria.DESPESA, "tv", "#0000FF", usuario);
    }

    /**
     * Testa o método
     * {@link CategoriaRepository#findByUsuarioOrUsuarioIsNullOrderByNomeAsc(Usuario)}.
     *
     * <p>
     * Valida o cenário de sucesso, onde o método retorna tanto as categorias
     * do usuário quanto as categorias globais do sistema.
     */
    @Test
    @DisplayName("findByUsuarioOrUsuarioIsNullOrderByNomeAsc quando existirem registros, deve retornar lista com as categorias")
    void testeFindByUsuarioOrUsuarioIsNullOrderByNomeAsc_QuandoExistiremRegistros_DeveRetornarLista() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaSistema);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        List<Categoria> resultado = categoriaRepository.findByUsuarioOrUsuarioIsNullOrderByNomeAsc(usuario);

        assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia");
        assertEquals(2, resultado.size(), "Deveria retornar exatos 2 registros");

        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(CAT_SISTEMA_NOME)));
        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(CAT_USUARIO_NOME)));
    }

    /**
     * Testa o isolamento de dados.
     *
     * <p>
     * Valida se a busca não retorna categorias privadas de outros usuários.
     */
    @Test
    @DisplayName("findByUsuarioOrUsuarioIsNullOrderByNomeAsc não deve retornar registros de outro usuário")
    void testeFindByUsuarioOrUsuarioIsNullOrderByNomeAsc_QuandoUsuarioForDiferente_NaoDeveRetornarRegistros() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaSistema);
        entityManager.persist(categoriaUsuario);

        Usuario outroUsuario = new Usuario("Outro", "outro@email.com", "senha");
        entityManager.persist(outroUsuario);

        Categoria categoriaOutro = new Categoria("Privada Outro", TipoCategoria.DESPESA, "icon", "#000", outroUsuario);
        entityManager.persist(categoriaOutro);
        entityManager.flush();

        List<Categoria> resultado = categoriaRepository.findByUsuarioOrUsuarioIsNullOrderByNomeAsc(usuario);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().noneMatch(c -> c.getNome().equals("Privada Outro")));
    }

    /**
     * Testa a validação de conflito no cadastro
     * {@link CategoriaRepository#existsByNomeAndUsuarioConflitoCadastro}.
     *
     * <p>
     * Valida se identifica conflito ao tentar criar um nome igual a uma
     * categoria do sistema.
     */
    @Test
    @DisplayName("existsByNomeAndUsuarioConflitoCadastro quando conflitar com sistema, deve retornar verdadeiro")
    void testeExistsByNomeAndUsuarioConflitoCadastro_QuandoConflitarComSistema_DeveRetornarVerdadeiro() {
        entityManager.persist(categoriaSistema);
        entityManager.persist(usuario);
        entityManager.flush();

        boolean existe = categoriaRepository.existsByNomeAndUsuarioConflitoCadastro(CAT_SISTEMA_NOME, usuario);

        assertTrue(existe, "Deveria identificar conflito com categoria do sistema");
    }

    /**
     * Testa a validação de conflito ignorando case.
     *
     * <p>
     * Valida se o sistema identifica nomes iguais mesmo com diferença de
     * maiúsculas e minúsculas.
     */
    @Test
    @DisplayName("existsByNomeAndUsuarioConflitoCadastro quando diferir apenas case, deve retornar verdadeiro")
    void testeExistsByNomeAndUsuarioConflitoCadastro_QuandoDiferirApenasCase_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        boolean existe = categoriaRepository.existsByNomeAndUsuarioConflitoCadastro(CAT_USUARIO_NOME.toLowerCase(),
                usuario);

        assertTrue(existe, "Deveria identificar conflito ignorando o case");
    }

    /**
     * Testa a validação de conflito na atualização
     * {@link CategoriaRepository#existsByNomeAndUsuarioConflito}.
     *
     * <p>
     * Valida se não dá conflito ao atualizar o registro mantendo o próprio nome.
     */
    @Test
    @DisplayName("existsByNomeAndUsuarioConflito quando for próprio registro, deve retornar falso")
    void testeExistsByNomeAndUsuarioConflito_QuandoForProprioRegistro_DeveRetornarFalso() {
        entityManager.persist(usuario);
        Categoria salva = entityManager.persist(categoriaUsuario);
        entityManager.flush();

        boolean existe = categoriaRepository.existsByNomeAndUsuarioConflito(CAT_USUARIO_NOME, usuario, salva.getId());

        assertFalse(existe, "Não deveria dar conflito com o próprio registro sendo editado");
    }

    /**
     * Testa a validação de conflito na atualização.
     *
     * <p>
     * Valida se dá conflito ao tentar usar um nome que já existe em outra
     * categoria.
     */
    @Test
    @DisplayName("existsByNomeAndUsuarioConflito quando existir outra com mesmo nome, deve retornar verdadeiro")
    void testeExistsByNomeAndUsuarioConflito_QuandoExistirOutraComMesmoNome_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaUsuario);

        Categoria outraCategoria = new Categoria("Lazer", TipoCategoria.DESPESA, "icon", "#000", usuario);
        entityManager.persist(outraCategoria);
        entityManager.flush();

        boolean existe = categoriaRepository.existsByNomeAndUsuarioConflito(CAT_USUARIO_NOME, usuario,
                outraCategoria.getId());

        assertTrue(existe, "Deveria identificar conflito com a outra categoria existente");
    }
}