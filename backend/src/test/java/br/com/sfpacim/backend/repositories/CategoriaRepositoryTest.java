package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.TipoCategoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link CategoriaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas geradas,
 * garantindo o isolamento de dados entre usuários (Defense in Depth) e o
 * correto funcionamento da busca híbrida (sistema + usuário).
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
     * {@link CategoriaRepository#findByUsuarioIdAndId(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de sucesso garantindo que o usuário consegue
     * acessar a sua própria categoria pelo ID.
     */
    @Test
    @DisplayName("findByUsuarioIdOrSistemaAndId: Quando categoria pertencer ao usuário, deve retornar Optional com a categoria")
    void testeFindByUsuarioIdOrSistemaAndId_QuandoCategoriaDoUsuario_DeveRetornarCategoria() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        Optional<Categoria> resultado = categoriaRepository.findByUsuarioIdOrSistemaAndId(usuario.getId(),
                categoriaUsuario.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio");
        assertEquals(CAT_USUARIO_NOME, resultado.get().getNome(), "Deveria retornar a categoria correta do usuário");
    }

    /**
     * Testa o método
     * {@link CategoriaRepository#findByUsuarioIdOrSistemaAndId(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de sucesso garantindo que o usuário consegue
     * acessar uma categoria PADRÃO DO SISTEMA (usuario_id nulo) pelo ID.
     */
    @Test
    @DisplayName("findByUsuarioIdOrSistemaAndId: Quando categoria for do sistema, deve retornar Optional com a categoria")
    void testeFindByUsuarioIdOrSistemaAndId_QuandoCategoriaDoSistema_DeveRetornarCategoria() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaSistema);
        entityManager.flush();

        Optional<Categoria> resultado = categoriaRepository.findByUsuarioIdOrSistemaAndId(usuario.getId(),
                categoriaSistema.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio para categorias do sistema");
        assertEquals(CAT_SISTEMA_NOME, resultado.get().getNome(), "Deveria retornar a categoria padrão do sistema");
    }

    /**
     * Testa o isolamento no método
     * {@link CategoriaRepository#findByUsuarioIdOrSistemaAndId(UUID, UUID)}.
     *
     * <p>
     * Valida a proteção contra acesso indevido a categorias privadas de
     * outros usuários, garantindo que o banco retorne vazio.
     */
    @Test
    @DisplayName("findByUsuarioIdOrSistemaAndId: Quando categoria for de outro usuário, deve retornar Optional vazio")
    void testeFindByUsuarioIdOrSistemaAndId_QuandoOutroUsuario_DeveRetornarVazio() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaUsuario);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);
        entityManager.flush();

        Optional<Categoria> resultado = categoriaRepository.findByUsuarioIdOrSistemaAndId(intruso.getId(),
                categoriaUsuario.getId());

        assertFalse(resultado.isPresent(), "O Optional deveria estar vazio para garantir o isolamento entre tenants");
    }

    /**
     * Testa o método
     * {@link CategoriaRepository#findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(UUID)}.
     *
     * <p>
     * Valida se a listagem híbrida traz as categorias do usuário e as globais
     * do sistema.
     */
    @Test
    @DisplayName("findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc: Quando existirem registros, deve retornar lista híbrida")
    void testeFindByUsuarioIdOrUsuarioIsNullOrderByNomeAsc_QuandoExistiremRegistros_DeveRetornarLista() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaSistema);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        List<Categoria> resultado = categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId());

        assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia");
        assertEquals(2, resultado.size(), "Deveria retornar exatos 2 registros (1 do sistema, 1 do usuário)");

        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(CAT_SISTEMA_NOME)));
        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(CAT_USUARIO_NOME)));
    }

    /**
     * Testa o isolamento de dados no método de listagem.
     *
     * <p>
     * Valida se a listagem híbrida ignora completamente as categorias privadas
     * criadas por outros usuários.
     */
    @Test
    @DisplayName("findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc: Não deve retornar categorias privadas de outro usuário")
    void testeFindByUsuarioIdOrUsuarioIsNullOrderByNomeAsc_QuandoUsuarioDiferente_NaoDeveRetornarPrivadas() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaSistema);
        entityManager.persist(categoriaUsuario);

        Usuario outroUsuario = new Usuario("Outro", "outro@email.com", "senha");
        entityManager.persist(outroUsuario);

        Categoria categoriaOutro = new Categoria("Privada Outro", TipoCategoria.DESPESA, "icon", "#000", outroUsuario);
        entityManager.persist(categoriaOutro);
        entityManager.flush();

        List<Categoria> resultado = categoriaRepository.findByUsuarioIdOrUsuarioIsNullOrderByNomeAsc(usuario.getId());

        assertEquals(2, resultado.size(), "Deveria retornar apenas a do sistema e a do próprio usuário");
        assertTrue(resultado.stream().noneMatch(c -> c.getNome().equals("Privada Outro")),
                "Não deveria conter categorias personalizadas de outros usuários");
    }
}
