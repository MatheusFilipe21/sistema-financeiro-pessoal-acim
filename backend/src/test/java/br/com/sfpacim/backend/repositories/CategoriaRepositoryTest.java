package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import br.com.sfpacim.backend.dtos.categoria.FiltroCategoriaDTO;
import br.com.sfpacim.backend.dtos.categoria.SelecaoCategoriaDTO;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.repositories.specifications.CategoriaSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link CategoriaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas geradas,
 * garantindo o isolamento de dados entre usuários, o funcionamento da busca
 * híbrida (sistema + usuário) e a filtragem dinâmica.
 *
 * @author Matheus F. N. Pereira
 */
class CategoriaRepositoryTest extends BaseRepositoryTest {

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
    private Usuario usuarioInvasor;
    private Categoria categoriaSistema;
    private Categoria categoriaUsuario;

    /**
     * Configura o cenário inicial antes de cada teste.
     * Instancia os usuários e as categorias necessárias.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME_USUARIO, EMAIL_USUARIO, SENHA_USUARIO);
        usuarioInvasor = new Usuario("Usuario Invasor", "invasor@gmail.com", SENHA_USUARIO);

        categoriaSistema = new Categoria(CAT_SISTEMA_NOME, TipoCategoria.DESPESA, CAT_ICONE, CAT_COR, null);
        categoriaUsuario = new Categoria(CAT_USUARIO_NOME, TipoCategoria.DESPESA, "tv", "#0000FF", usuario);
    }

    /**
     * Testa o método
     * {@link CategoriaRepository#buscarPorIdEUsuarioOuSistema(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de sucesso garantindo que o usuário consegue
     * acessar a sua própria categoria pelo ID.
     */
    @Test
    @DisplayName("buscarPorIdEUsuarioOuSistema: Quando categoria pertencer ao usuário, deve retornar Optional com a categoria")
    void testeFindByUsuarioIdOrSistemaAndId_QuandoCategoriaDoUsuario_DeveRetornarCategoria() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        Optional<Categoria> resultado = categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(),
                categoriaUsuario.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio");
        assertEquals(CAT_USUARIO_NOME, resultado.get().getNome(), "Deveria retornar a categoria correta do usuário");
    }

    /**
     * Testa o método
     * {@link CategoriaRepository#buscarPorIdEUsuarioOuSistema(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de sucesso garantindo que o usuário consegue
     * acessar uma categoria padrão do sistema (usuario nulo) pelo ID.
     */
    @Test
    @DisplayName("buscarPorIdEUsuarioOuSistema: Quando categoria for do sistema, deve retornar Optional com a categoria")
    void testeFindByUsuarioIdOrSistemaAndId_QuandoCategoriaDoSistema_DeveRetornarCategoria() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaSistema);
        entityManager.flush();

        Optional<Categoria> resultado = categoriaRepository.buscarPorIdEUsuarioOuSistema(usuario.getId(),
                categoriaSistema.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio para categorias do sistema");
        assertEquals(CAT_SISTEMA_NOME, resultado.get().getNome(), "Deveria retornar a categoria padrão do sistema");
    }

    /**
     * Testa o método
     * {@link CategoriaRepository#buscarPorIdEUsuarioOuSistema(UUID, UUID)}.
     *
     * <p>
     * Valida a restrição de segurança (Defense in Depth) garantindo que um usuário
     * não consegue acessar a categoria pertencente a outro usuário.
     */
    @Test
    @DisplayName("buscarPorIdEUsuarioOuSistema: Quando categoria pertencer a outro usuário, deve retornar vazio")
    void testeBuscarPorId_QuandoCategoriaDeOutroUsuario_DeveRetornarVazio() {
        entityManager.persist(usuario);
        entityManager.persist(usuarioInvasor);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        Optional<Categoria> resultado = categoriaRepository.buscarPorIdEUsuarioOuSistema(usuarioInvasor.getId(),
                categoriaUsuario.getId());

        assertFalse(resultado.isPresent(), "Não deveria encontrar a categoria de outro usuário");
    }

    /**
     * Testa o método
     * {@link CategoriaRepository#existeCategoriaDuplicada(UUID, String, UUID)}.
     *
     * <p>
     * Valida se a consulta identifica corretamente a duplicidade de nomes,
     * ignorando diferenças de acentuação e letras maiúsculas/minúsculas,
     * fazendo uso da função unaccent do PostgreSQL.
     */
    @Test
    @DisplayName("existeCategoriaDuplicada: Deve retornar true ignorando acentos e maiúsculas")
    void testeExisteDuplicada_IgnorandoAcentosECaixa_DeveRetornarTrue() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        boolean existe = categoriaRepository.existeCategoriaDuplicada(usuario.getId(), "stréAMing", null);

        assertTrue(existe, "Deveria identificar a duplicidade ignorando acentos e caixa alta");
    }

    /**
     * Testa o método
     * {@link CategoriaRepository#existeCategoriaDuplicada(UUID, String, UUID)}.
     *
     * <p>
     * Valida o cenário de atualização, onde a busca por duplicidade deve ignorar
     * a própria categoria que está sendo atualizada pelo usuário.
     */
    @Test
    @DisplayName("existeCategoriaDuplicada: Ao atualizar a própria categoria com mesmo nome, deve retornar false")
    void testeExisteDuplicada_QuandoForAtualizacaoDoMesmoRegistro_DeveRetornarFalse() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        boolean existe = categoriaRepository.existeCategoriaDuplicada(usuario.getId(), CAT_USUARIO_NOME,
                categoriaUsuario.getId());

        assertFalse(existe, "Não deve ser considerado duplicado se for o mesmo ID atualizando a si mesmo");
    }

    /**
     * Testa o método {@link CategoriaRepository#buscarOpcoesParaSelecao(UUID)}.
     *
     * <p>
     * Valida a projeção de dados para o DTO e garante que a lista final contém
     * apenas as categorias do sistema e as categorias do próprio usuário.
     */
    @Test
    @DisplayName("buscarOpcoesParaSelecao: Deve retornar DTOs apenas do usuário logado e do sistema")
    void testeBuscarOpcoesParaSelecao_DeveMapearParaDTO() {
        entityManager.persist(usuario);
        entityManager.persist(usuarioInvasor);
        entityManager.persist(categoriaSistema);
        entityManager.persist(categoriaUsuario);
        entityManager.persist(new Categoria("Secreta", TipoCategoria.RECEITA, CAT_ICONE, CAT_COR, usuarioInvasor));
        entityManager.flush();

        List<SelecaoCategoriaDTO> opcoes = categoriaRepository.buscarOpcoesParaSelecao(usuario.getId());

        assertEquals(2, opcoes.size(), "Deveria retornar apenas categorias do sistema e do usuário");
        assertNotNull(opcoes.get(0).id(), "O mapeamento AS id deve instanciar o record corretamente");
        assertNotNull(opcoes.get(0).nome(), "O mapeamento AS nome deve instanciar o record corretamente");
    }

    /**
     * Testa a {@link CategoriaSpec#comFiltros(UUID, FiltroCategoriaDTO)}.
     *
     * <p>
     * Valida a aplicação do filtro por categorias padrão do sistema.
     */
    @Test
    @DisplayName("CategoriaSpec comFiltros: Filtro de sistema igual a true deve trazer apenas categorias globais")
    void testeCategoriaSpec_QuandoFiltroSistemaTrue_DeveTrazerApenasSistema() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaSistema);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        FiltroCategoriaDTO filtro = new FiltroCategoriaDTO(null, null, true);

        List<Categoria> resultado = categoriaRepository.findAll(CategoriaSpec.comFiltros(usuario.getId(), filtro));

        assertEquals(1, resultado.size(), "Deveria retornar apenas a categoria do sistema");
        assertEquals(CAT_SISTEMA_NOME, resultado.get(0).getNome(), "Deveria retornar a categoria de alimentação");
    }

    /**
     * Testa a {@link CategoriaSpec#comFiltros(UUID, FiltroCategoriaDTO)}.
     *
     * <p>
     * Valida a aplicação do filtro por tipo, garantindo que a busca por DESPESA
     * traga categorias do tipo DESPESA e também categorias do tipo AMBOS.
     */
    @Test
    @DisplayName("CategoriaSpec comFiltros: Filtro Tipo igual a DESPESA deve trazer categorias de DESPESA e AMBOS")
    void testeCategoriaSpec_QuandoFiltroTipo_DeveTrazerExatoOuAmbos() {
        entityManager.persist(usuario);
        entityManager.persist(new Categoria("Conta de Luz", TipoCategoria.DESPESA, CAT_ICONE, CAT_COR, usuario));
        entityManager.persist(new Categoria("Transferência", TipoCategoria.AMBOS, CAT_ICONE, CAT_COR, usuario));
        entityManager.persist(new Categoria("Salário", TipoCategoria.RECEITA, CAT_ICONE, CAT_COR, usuario));
        entityManager.flush();

        FiltroCategoriaDTO filtro = new FiltroCategoriaDTO(null, TipoCategoria.DESPESA, null);

        List<Categoria> resultado = categoriaRepository.findAll(CategoriaSpec.comFiltros(usuario.getId(), filtro));

        assertEquals(2, resultado.size(), "Deveria encontrar a DESPESA e a AMBOS, ignorando a RECEITA");
    }

    /**
     * Testa a {@link CategoriaSpec#comFiltros(UUID, FiltroCategoriaDTO)}.
     *
     * <p>
     * Valida o comportamento de retorno antecipado quando o filtro fornecido
     * for nulo, garantindo que a busca retorne tanto as categorias criadas
     * pelo usuário quanto as categorias padrão do sistema, bloqueando invasores.
     */
    @Test
    @DisplayName("CategoriaSpec comFiltros: Filtro nulo deve retornar categorias do usuário logado e do sistema")
    void testeCategoriaSpec_QuandoFiltroNulo_DeveRetornarDoUsuarioEDoSistema() {
        entityManager.persist(usuario);
        entityManager.persist(usuarioInvasor);
        entityManager.persist(categoriaSistema);
        entityManager.persist(categoriaUsuario);

        Categoria categoriaInvasor = new Categoria("Categoria Hacker", TipoCategoria.RECEITA, CAT_ICONE, CAT_COR,
                usuarioInvasor);
        entityManager.persist(categoriaInvasor);
        entityManager.flush();

        List<Categoria> resultado = categoriaRepository.findAll(CategoriaSpec.comFiltros(usuario.getId(), null));

        assertEquals(2, resultado.size(), "Deveria retornar estritamente a categoria do usuário e a do sistema");
        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(CAT_USUARIO_NOME)),
                "Deveria conter a categoria do usuário");
        assertTrue(resultado.stream().anyMatch(c -> c.getNome().equals(CAT_SISTEMA_NOME)),
                "Deveria conter a categoria do sistema");
    }

    /**
     * Testa a {@link CategoriaSpec#comFiltros(UUID, FiltroCategoriaDTO)}.
     *
     * <p>
     * Valida a aplicação do filtro de sistema igual a falso, garantindo que
     * a busca retorne estritamente as categorias criadas pelo próprio usuário,
     * ignorando as categorias padrão do sistema.
     */
    @Test
    @DisplayName("CategoriaSpec comFiltros: Filtro de sistema igual a false deve trazer apenas categorias do usuário")
    void testeCategoriaSpec_QuandoFiltroSistemaFalse_DeveTrazerApenasDoUsuario() {
        entityManager.persist(usuario);
        entityManager.persist(categoriaSistema);
        entityManager.persist(categoriaUsuario);
        entityManager.flush();

        FiltroCategoriaDTO filtro = new FiltroCategoriaDTO(null, null, false);

        List<Categoria> resultado = categoriaRepository.findAll(CategoriaSpec.comFiltros(usuario.getId(), filtro));

        assertEquals(1, resultado.size(), "Deveria retornar apenas a categoria criada pelo usuário logado");
        assertEquals(CAT_USUARIO_NOME, resultado.get(0).getNome(), "Deveria retornar a categoria personalizada");
    }
}
