package br.com.sfpacim.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import br.com.sfpacim.backend.dtos.pessoa.FiltroPessoaDTO;
import br.com.sfpacim.backend.dtos.pessoa.SelecaoPessoaDTO;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.repositories.specifications.PessoaSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link PessoaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando o banco de dados real configurado na classe base. Valida
 * o isolamento de dados e os recursos específicos do SGBD como o unaccent.
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
     * Instancia o usuário e as pessoas padrão.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME_USUARIO, EMAIL_USUARIO, SENHA_USUARIO);

        pessoa1 = new Pessoa(NOME_PESSOA_1, usuario);
        pessoa1.setTitular(true);

        pessoa2 = new Pessoa(NOME_PESSOA_2, usuario);
        pessoa2.setTitular(false);
    }

    /**
     * Testa o método {@link PessoaRepository#findByUsuarioIdAndId(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de sucesso, onde a pessoa é encontrada e pertence
     * ao usuário informado.
     */
    @Test
    @DisplayName("findByUsuarioIdAndId: Quando registro existir e pertencer ao usuário, deve retornar Optional com a pessoa")
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
     * pertence a um usuário diferente do informado na consulta.
     */
    @Test
    @DisplayName("findByUsuarioIdAndId: Quando pessoa pertencer a outro usuário, deve retornar Optional vazio")
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
     * Testa o método
     * {@link PessoaRepository#existeNomeDuplicado(UUID, String, UUID)}.
     *
     * <p>
     * Valida se a consulta identifica corretamente a duplicidade de nomes,
     * ignorando diferenças de acentuação e letras maiúsculas/minúsculas.
     */
    @Test
    @DisplayName("existeNomeDuplicado: Deve retornar true ignorando acentos e maiúsculas")
    void testeExisteDuplicada_IgnorandoAcentosECaixa_DeveRetornarTrue() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);
        entityManager.flush();

        boolean existe = pessoaRepository.existeNomeDuplicado(usuario.getId(), "aléXandrE orlando GRACIO", null);

        assertTrue(existe, "Deveria identificar a duplicidade ignorando acentos e caixa alta");
    }

    /**
     * Testa o método
     * {@link PessoaRepository#existeNomeDuplicado(UUID, String, UUID)}.
     *
     * <p>
     * Valida o cenário de atualização, garantindo que o próprio registro não é
     * apontado como duplicado de si mesmo.
     */
    @Test
    @DisplayName("existeNomeDuplicado: Ao atualizar a própria pessoa com mesmo nome, deve retornar false")
    void testeExisteDuplicada_QuandoForAtualizacaoDoMesmoRegistro_DeveRetornarFalse() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);
        entityManager.flush();

        boolean existe = pessoaRepository.existeNomeDuplicado(usuario.getId(), NOME_PESSOA_1, pessoa1.getId());

        assertFalse(existe, "Deveria retornar falso ao validar a atualização da própria pessoa");
    }

    /**
     * Testa o método
     * {@link PessoaRepository#buscarOpcoesParaSelecao(UUID, Boolean)}.
     *
     * <p>
     * Valida a projeção de dados para o DTO e a aplicação dinâmica do filtro
     * de titularidade booleana.
     */
    @Test
    @DisplayName("buscarOpcoesParaSelecao: Deve retornar DTOs respeitando o filtro dinâmico de titularidade")
    void testeBuscarOpcoesParaSelecao_DeveMapearParaDTOEFiltrarTitularidade() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);
        entityManager.persist(pessoa2);
        entityManager.flush();

        List<SelecaoPessoaDTO> apenasTitulares = pessoaRepository.buscarOpcoesParaSelecao(usuario.getId(), true);

        assertEquals(1, apenasTitulares.size(), "Deveria retornar apenas a pessoa marcada como titular");
        assertNotNull(apenasTitulares.get(0).id(), "O ID do DTO deve ser populado");
        assertEquals(NOME_PESSOA_1, apenasTitulares.get(0).nome(), "O nome mapeado deve corresponder ao titular");

        List<SelecaoPessoaDTO> todos = pessoaRepository.buscarOpcoesParaSelecao(usuario.getId(), null);

        assertEquals(2, todos.size(), "Deveria retornar todos os registros quando o filtro de titularidade for nulo");
    }

    /**
     * Testa a {@link PessoaSpec#comFiltros(UUID, FiltroPessoaDTO)}.
     *
     * <p>
     * Valida a aplicação conjunta dos filtros textuais e de titularidade na
     * construção da Specification.
     */
    @Test
    @DisplayName("PessoaSpec comFiltros: Filtros opcionais devem ser aplicados juntamente com o isolamento de usuário")
    void testePessoaSpec_QuandoFiltrosInformados_DeveFiltrarCorretamente() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);
        entityManager.persist(pessoa2);
        entityManager.flush();

        FiltroPessoaDTO filtroNome = new FiltroPessoaDTO("Catherine", null);
        List<Pessoa> resultadoNome = pessoaRepository.findAll(PessoaSpec.comFiltros(usuario.getId(), filtroNome));

        assertEquals(1, resultadoNome.size(),
                "Deveria retornar apenas a pessoa cujo nome corresponde ao filtro textual");
        assertEquals(NOME_PESSOA_2, resultadoNome.get(0).getNome());

        FiltroPessoaDTO filtroTitular = new FiltroPessoaDTO(null, true);
        List<Pessoa> resultadoTitular = pessoaRepository.findAll(PessoaSpec.comFiltros(usuario.getId(), filtroTitular));

        assertEquals(1, resultadoTitular.size(), "Deveria retornar apenas a pessoa com flag de titular verdadeira");
        assertEquals(NOME_PESSOA_1, resultadoTitular.get(0).getNome());
    }

    /**
     * Testa a {@link PessoaSpec#comFiltros(UUID, FiltroPessoaDTO)}.
     *
     * <p>
     * Valida o comportamento de retorno antecipado quando o filtro fornecido
     * for nulo, garantindo que o isolamento de dados do usuário logado continue
     * sendo aplicado corretamente em consultas sem parâmetros opcionais.
     */
    @Test
    @DisplayName("PessoaSpec comFiltros: Filtro nulo deve retornar todos os registros do usuário logado")
    void testePessoaSpec_QuandoFiltroNulo_DeveRetornarTodosDoUsuario() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa1);
        entityManager.persist(pessoa2);

        Usuario usuarioIntruso = new Usuario("Intruso", "intruso@email.com", "123");
        Pessoa pessoaIntruso = new Pessoa("Pessoa Intruso", usuarioIntruso);
        entityManager.persist(usuarioIntruso);
        entityManager.persist(pessoaIntruso);
        entityManager.flush();

        List<Pessoa> resultado = pessoaRepository.findAll(PessoaSpec.comFiltros(usuario.getId(), null));

        assertEquals(2, resultado.size(), "Deveria retornar todos os registros pertencentes ao usuário logado");
        assertTrue(resultado.stream().anyMatch(p -> p.getNome().equals(NOME_PESSOA_1)), "Deveria conter a pessoa 1");
        assertTrue(resultado.stream().anyMatch(p -> p.getNome().equals(NOME_PESSOA_2)), "Deveria conter a pessoa 2");
    }
}
