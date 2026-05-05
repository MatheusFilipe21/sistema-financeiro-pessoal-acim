package br.com.sfpacim.backend.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import br.com.sfpacim.backend.dtos.conta.FiltroContaDTO;
import br.com.sfpacim.backend.dtos.conta.SelecaoContaDTO;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.repositories.specifications.ContaSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link ContaRepository}.
 *
 * <p>
 * Foca em testar a camada de persistência (JPA) e as consultas SQL geradas,
 * utilizando o banco de dados real configurado na classe base. Valida
 * estritamente a aplicação do conceito de Defense in Depth e a filtragem
 * dinâmica.
 *
 * @author Matheus F. N. Pereira
 */
class ContaRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String NOME_USUARIO = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL_USUARIO = "matheusfnpereira@gmail.com";
    private static final String SENHA_USUARIO = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";
    private static final String NOME_PESSOA = "Matheus Filipe do Nascimento Pereira";
    private static final String NOME_CONTA_1 = "Investimentos Mercado Pago";
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("100.00");

    private Usuario usuario;
    private Pessoa pessoa;
    private Conta conta1;

    /**
     * Configura o cenário inicial antes de cada teste.
     * Instancia a hierarquia de usuário, pessoa e contas.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME_USUARIO, EMAIL_USUARIO, SENHA_USUARIO);

        pessoa = new Pessoa(NOME_PESSOA, usuario);
        pessoa.setTitular(true);

        conta1 = new Conta(NOME_CONTA_1, InstituicaoFinanceira.NUBANK, SALDO_INICIAL, pessoa);
    }

    /**
     * Testa o método
     * {@link ContaRepository#findByPessoaUsuarioIdAndId(UUID, UUID)}.
     *
     * <p>
     * Valida o cenário de sucesso, onde a conta é encontrada e pertence
     * ao usuário informado.
     */
    @Test
    @DisplayName("findByPessoaUsuarioIdAndId: Quando conta existir e pertencer ao usuário, deve retornar Optional com a conta")
    void testeFindByPessoaUsuarioIdAndId_QuandoExistir_DeveRetornarOptionalComConta() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        Optional<Conta> resultado = contaRepository.findByPessoaUsuarioIdAndId(usuario.getId(), conta1.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio");
        assertEquals(NOME_CONTA_1, resultado.get().getNome(), "Deveria retornar a conta correta");
    }

    /**
     * Testa o isolamento no método
     * {@link ContaRepository#findByPessoaUsuarioIdAndId(UUID, UUID)}.
     *
     * <p>
     * Valida se a busca bloqueia o acesso a uma conta existente quando
     * consultada por um usuário diferente do titular.
     */
    @Test
    @DisplayName("findByPessoaUsuarioIdAndId: Quando conta for de outro usuário, deve retornar Optional vazio")
    void testeFindByPessoaUsuarioIdAndId_QuandoOutroUsuario_DeveRetornarOptionalVazio() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Usuario usuarioIntruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(usuarioIntruso);
        entityManager.flush();

        Optional<Conta> resultado = contaRepository.findByPessoaUsuarioIdAndId(usuarioIntruso.getId(), conta1.getId());

        assertFalse(resultado.isPresent(), "O Optional deveria estar vazio pois a conta pertence a outro usuário");
    }

    /**
     * Testa o método
     * {@link ContaRepository#existsByPessoaUsuarioIdAndPessoaId(UUID, UUID)}.
     *
     * <p>
     * Utilizado para validar dependências ativas em operações de exclusão.
     */
    @Test
    @DisplayName("existsByPessoaUsuarioIdAndPessoaId: Deve retornar verdadeiro se houver vínculo")
    void testeExistsByPessoaUsuarioIdAndPessoaId_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        boolean existe = contaRepository.existsByPessoaUsuarioIdAndPessoaId(usuario.getId(), pessoa.getId());

        assertTrue(existe, "Deveria retornar verdadeiro pois existe conta para a pessoa do usuário");
    }

    /**
     * Testa o isolamento no método
     * {@link ContaRepository#existsByPessoaUsuarioIdAndPessoaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByPessoaUsuarioIdAndPessoaId: Deve retornar falso para usuário incorreto")
    void testeExistsByPessoaUsuarioIdAndPessoaId_QuandoUsuarioIncorreto_DeveRetornarFalso() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Usuario outroUsuario = new Usuario("Outro", "outro@email.com", "123");
        entityManager.persist(outroUsuario);
        entityManager.flush();

        boolean existe = contaRepository.existsByPessoaUsuarioIdAndPessoaId(outroUsuario.getId(), pessoa.getId());

        assertFalse(existe, "Deveria retornar falso, simulando proteção contra acesso indevido");
    }

    /**
     * Testa o método
     * {@link ContaRepository#existeContaDuplicada(UUID, UUID, InstituicaoFinanceira, String, UUID)}.
     *
     * <p>
     * Valida a identificação de contas com o mesmo nome e instituição para a mesma
     * pessoa, ignorando acentuação e caixa, confirmando o uso do unaccent.
     */
    @Test
    @DisplayName("existeContaDuplicada: Deve retornar true ignorando acentos e maiúsculas")
    void testeExisteDuplicada_IgnorandoAcentosECaixa_DeveRetornarTrue() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        boolean existe = contaRepository.existeContaDuplicada(usuario.getId(), pessoa.getId(),
                InstituicaoFinanceira.NUBANK, "invéstiméntos mercado PAGO", null);

        assertTrue(existe, "Deveria identificar a duplicidade ignorando acentos e caixa alta");
    }

    /**
     * Testa o método
     * {@link ContaRepository#existeContaDuplicada(UUID, UUID, InstituicaoFinanceira, String, UUID)}.
     *
     * <p>
     * Valida que a validação de unicidade ignora o registro que está sendo
     * atualizado no momento.
     */
    @Test
    @DisplayName("existeContaDuplicada: Ao atualizar a própria conta com mesmo nome e instituição, deve retornar false")
    void testeExisteDuplicada_QuandoForAtualizacaoDoMesmoRegistro_DeveRetornarFalse() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        boolean existe = contaRepository.existeContaDuplicada(usuario.getId(), pessoa.getId(),
                InstituicaoFinanceira.NUBANK, NOME_CONTA_1, conta1.getId());

        assertFalse(existe, "Deveria retornar falso ao atualizar a própria conta mantendo os dados");
    }

    /**
     * Testa o método {@link ContaRepository#buscarOpcoesParaSelecao(UUID)}.
     *
     * <p>
     * Valida a projeção do DTO, verificando se o aninhamento de atributos
     * instancia as propriedades corretamente e respeita o isolamento de tenant.
     */
    @Test
    @DisplayName("buscarOpcoesParaSelecao: Deve retornar DTOs apenas das contas do usuário logado")
    void testeBuscarOpcoesParaSelecao_DeveMapearParaDTO() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Usuario usuarioIntruso = new Usuario("Intruso", "intruso@email.com", "123");
        Pessoa pessoaIntruso = new Pessoa("Pessoa Intruso", usuarioIntruso);
        Conta contaIntruso = new Conta("Conta Intruso", InstituicaoFinanceira.INTER, BigDecimal.ZERO, pessoaIntruso);

        entityManager.persist(usuarioIntruso);
        entityManager.persist(pessoaIntruso);
        entityManager.persist(contaIntruso);
        entityManager.flush();

        List<SelecaoContaDTO> opcoes = contaRepository.buscarOpcoesParaSelecao(usuario.getId());

        assertEquals(1, opcoes.size(), "Deveria retornar apenas a conta do usuário logado");
        assertNotNull(opcoes.get(0).id(), "O mapeamento AS id deve instanciar o record corretamente");
        assertEquals(NOME_CONTA_1, opcoes.get(0).nome(), "O mapeamento AS nome deve instanciar o record corretamente");
        assertEquals(NOME_PESSOA, opcoes.get(0).pessoaNome(),
                "O mapeamento aninhado c.pessoa.nome AS pessoaNome deve instanciar corretamente");
    }

    /**
     * Testa a {@link ContaSpec#comFiltros(UUID, FiltroContaDTO)}.
     *
     * <p>
     * Valida a aplicação conjunta dos filtros opcionais garantindo que a base
     * da consulta restringe pelo proprietário.
     */
    @Test
    @DisplayName("ContaSpec comFiltros: Filtro de instituição e pessoa deve aplicar os predicados corretamente")
    void testeContaSpec_QuandoFiltrosInformados_DeveFiltrarCorretamente() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Conta conta2 = new Conta("Conta Secundária", InstituicaoFinanceira.ITAU, BigDecimal.ZERO, pessoa);
        entityManager.persist(conta2);
        entityManager.flush();

        FiltroContaDTO filtro = new FiltroContaDTO(null, List.of(InstituicaoFinanceira.NUBANK),
                List.of(pessoa.getId()));

        List<Conta> resultado = contaRepository.findAll(ContaSpec.comFiltros(usuario.getId(), filtro));

        assertEquals(1, resultado.size(), "Deveria retornar apenas a conta do Nubank");
        assertEquals(NOME_CONTA_1, resultado.get(0).getNome());
    }

    /**
     * Testa a {@link ContaSpec#comFiltros(UUID, FiltroContaDTO)}.
     *
     * <p>
     * Valida o comportamento de retorno antecipado quando o filtro fornecido
     * for nulo, garantindo que o isolamento de dados do usuário logado continue
     * sendo aplicado corretamente.
     */
    @Test
    @DisplayName("ContaSpec comFiltros: Filtro nulo deve retornar todos os registros do usuário logado")
    void testeContaSpec_QuandoFiltroNulo_DeveRetornarTodosDoUsuario() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);

        Usuario usuarioIntruso = new Usuario("Intruso", "intruso@email.com", "123");
        Pessoa pessoaIntruso = new Pessoa("Pessoa Intruso", usuarioIntruso);
        Conta contaIntruso = new Conta("Conta Intruso", InstituicaoFinanceira.INTER, BigDecimal.ZERO, pessoaIntruso);
        entityManager.persist(usuarioIntruso);
        entityManager.persist(pessoaIntruso);
        entityManager.persist(contaIntruso);
        entityManager.flush();

        List<Conta> resultado = contaRepository.findAll(ContaSpec.comFiltros(usuario.getId(), null));

        assertEquals(1, resultado.size(), "Deveria retornar todos os registros pertencentes ao usuário logado");
        assertEquals(NOME_CONTA_1, resultado.get(0).getNome(), "Deveria retornar a conta correta");
    }

    /**
     * Testa a {@link ContaSpec#comFiltros(UUID, FiltroContaDTO)}.
     *
     * <p>
     * Valida se a condição do tipo de retorno protege a consulta de contagem
     * durante a paginação, evitando que o Join Fetch seja aplicado no count e
     * cause exceções.
     */
    @Test
    @DisplayName("ContaSpec comFiltros: Paginação deve executar sem erros de fetch no count")
    void testeContaSpec_QuandoPaginado_NaoDeveDarErroDeFetch() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        PageRequest pageable = PageRequest.of(0, 10);
        Page<Conta> resultado = contaRepository.findAll(ContaSpec.comFiltros(usuario.getId(), null), pageable);

        assertFalse(resultado.isEmpty(), "A página deveria conter elementos");
        assertEquals(1, resultado.getTotalElements(),
                "Deveria contar os elementos corretamente executando a query secundária");
        assertEquals(NOME_CONTA_1, resultado.getContent().get(0).getNome(),
                "Deveria retornar a conta correta na listagem paginada");
    }

    /**
     * Testa a {@link ContaSpec#comFiltros(UUID, FiltroContaDTO)}.
     *
     * <p>
     * Valida se as coleções vazias nos parâmetros de filtro são ignoradas
     * corretamente, sem gerar restrições incorretas na query final.
     */
    @Test
    @DisplayName("ContaSpec comFiltros: Filtro com listas vazias deve retornar todos os registros do usuário")
    void testeContaSpec_QuandoFiltroComListasVazias_DeveIgnorarFiltrosERetornarDoUsuario() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        FiltroContaDTO filtroVazio = new FiltroContaDTO(null, List.of(), List.of());
        List<Conta> resultado = contaRepository.findAll(ContaSpec.comFiltros(usuario.getId(), filtroVazio));

        assertEquals(1, resultado.size(), "Deveria ignorar as listas vazias e retornar os dados do usuário");
        assertEquals(NOME_CONTA_1, resultado.get(0).getNome(), "Deveria retornar a conta correta");
    }

    /**
     * Testa a {@link ContaSpec#comFiltros(UUID, FiltroContaDTO)}.
     *
     * <p>
     * Valida explicitamente o cenário em que a Specification é utilizada
     * para uma operação de contagem. Para o garantindo que o Join Fetch não
     * seja aplicado na contagem.
     */
    @Test
    @DisplayName("ContaSpec comFiltros: Operação de count deve ignorar fetch e retornar total corretamente")
    void testeContaSpec_QuandoCount_NaoDeveAplicarFetch() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta1);
        entityManager.flush();

        long totalContas = contaRepository.count(ContaSpec.comFiltros(usuario.getId(), null));

        assertEquals(1, totalContas, "Deveria contar corretamente os registros do usuário");
    }
}
