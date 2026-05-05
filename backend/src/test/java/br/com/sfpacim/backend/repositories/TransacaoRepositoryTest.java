package br.com.sfpacim.backend.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import br.com.sfpacim.backend.dtos.transacao.FiltroTransacaoDTO;
import br.com.sfpacim.backend.models.Categoria;
import br.com.sfpacim.backend.models.Conta;
import br.com.sfpacim.backend.models.Pessoa;
import br.com.sfpacim.backend.models.Transacao;
import br.com.sfpacim.backend.models.Usuario;
import br.com.sfpacim.backend.models.enums.InstituicaoFinanceira;
import br.com.sfpacim.backend.models.enums.StatusTransacao;
import br.com.sfpacim.backend.models.enums.TipoCategoria;
import br.com.sfpacim.backend.models.enums.TipoTransacao;
import br.com.sfpacim.backend.repositories.specifications.TransacaoSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de Integração para o {@link TransacaoRepository}.
 *
 * <p>
 * Valida a persistência de movimentações financeiras, verificação de
 * dependências
 * e o rigoroso isolamento de dados entre os usuários do sistema.
 * Adicionalmente,
 * testa a montagem de consultas dinâmicas através das Specifications.
 *
 * @author Matheus F. N. Pereira
 */
class TransacaoRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final String NOME_USUARIO = "Matheus Filipe do Nascimento Pereira";
    private static final String EMAIL_USUARIO = "matheusfnpereira@gmail.com";
    private static final String SENHA_USUARIO = "$2a$10$VUI0N7kPFDVnD6XZbLni6uyg3UF0RU/fQRNHnZb6oWhTGT3R9YqgK";

    private static final String DESC_TRANSACAO = "Compra no Mercado";
    private static final BigDecimal VALOR = new BigDecimal("250.00");
    private static final LocalDate DATA_HOJE = LocalDate.now();

    private Usuario usuario;
    private Pessoa pessoa;
    private Conta conta;
    private Categoria categoria;
    private Transacao transacao;

    /**
     * Configura o cenário de teste persistindo as entidades fundamentais e
     * a transação para evitar violações de chave estrangeira no banco em memória.
     */
    @BeforeEach
    void setUp() {
        usuario = new Usuario(NOME_USUARIO, EMAIL_USUARIO, SENHA_USUARIO);
        pessoa = new Pessoa("Titular", usuario);
        conta = new Conta("Conta Principal", InstituicaoFinanceira.NUBANK, BigDecimal.ZERO, pessoa);
        categoria = new Categoria("Alimentação", TipoCategoria.DESPESA, "icon", "#000", usuario);

        transacao = Transacao.builder()
                .descricao(DESC_TRANSACAO)
                .valor(VALOR)
                .dataCompetencia(DATA_HOJE)
                .dataVencimento(DATA_HOJE)
                .tipo(TipoTransacao.DESPESA)
                .status(StatusTransacao.PENDENTE)
                .usuario(usuario)
                .pessoa(pessoa)
                .conta(conta)
                .categoria(categoria)
                .build();
    }

    /**
     * Testa o método {@link TransacaoRepository#findByUsuarioIdAndId(UUID, UUID)}.
     */
    @Test
    @DisplayName("findByUsuarioIdAndId: Quando existir e pertencer ao usuário, deve retornar Optional com a transação e seus relacionamentos carregados")
    void testeFindByUsuarioIdAndId_QuandoExistir_DeveRetornarOptionalComTransacao() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        Optional<Transacao> resultado = transacaoRepository.findByUsuarioIdAndId(usuario.getId(), transacao.getId());

        assertTrue(resultado.isPresent(), "O Optional não deveria estar vazio");
        assertEquals(DESC_TRANSACAO, resultado.get().getDescricao(), "Deveria retornar a transação solicitada");
    }

    /**
     * Testa o isolamento no método
     * {@link TransacaoRepository#findByUsuarioIdAndId(UUID, UUID)}.
     */
    @Test
    @DisplayName("findByUsuarioIdAndId: Quando transação for de outro usuário, deve retornar Optional vazio")
    void testeFindByUsuarioIdAndId_QuandoOutroUsuario_DeveRetornarOptionalVazio() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);
        entityManager.flush();

        Optional<Transacao> resultado = transacaoRepository.findByUsuarioIdAndId(intruso.getId(), transacao.getId());

        assertFalse(resultado.isPresent(), "O Optional deveria estar vazio protegendo a transação de outro usuário");
    }

    /**
     * Testa o método
     * {@link TransacaoRepository#existsByUsuarioIdAndPessoaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndPessoaId: Deve retornar verdadeiro se houver vínculo")
    void testeExistsByUsuarioIdAndPessoaId_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndPessoaId(usuario.getId(), pessoa.getId());

        assertTrue(existe, "Deveria retornar verdadeiro devido ao vínculo da pessoa com a transação");
    }

    /**
     * Testa o isolamento no método
     * {@link TransacaoRepository#existsByUsuarioIdAndPessoaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndPessoaId: Com usuário incorreto, deve retornar falso")
    void testeExistsByUsuarioIdAndPessoaId_QuandoUsuarioIncorreto_DeveRetornarFalso() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndPessoaId(intruso.getId(), pessoa.getId());

        assertFalse(existe, "Deveria retornar falso garantindo o isolamento da consulta por pessoa");
    }

    /**
     * Testa o método
     * {@link TransacaoRepository#existsByUsuarioIdAndContaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndContaId: Deve retornar verdadeiro se houver vínculo")
    void testeExistsByUsuarioIdAndContaId_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndContaId(usuario.getId(), conta.getId());

        assertTrue(existe, "Deveria retornar verdadeiro devido ao vínculo da conta com a transação");
    }

    /**
     * Testa o isolamento no método
     * {@link TransacaoRepository#existsByUsuarioIdAndContaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndContaId: Com usuário incorreto, deve retornar falso")
    void testeExistsByUsuarioIdAndContaId_QuandoUsuarioIncorreto_DeveRetornarFalso() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndContaId(intruso.getId(), conta.getId());

        assertFalse(existe, "Deveria retornar falso garantindo o isolamento da consulta por conta");
    }

    /**
     * Testa o método
     * {@link TransacaoRepository#existsByUsuarioIdAndCategoriaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndCategoriaId: Deve retornar verdadeiro se houver vínculo")
    void testeExistsByUsuarioIdAndCategoriaId_DeveRetornarVerdadeiro() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndCategoriaId(usuario.getId(), categoria.getId());

        assertTrue(existe, "Deveria retornar verdadeiro devido ao vínculo da categoria com a transação");
    }

    /**
     * Testa o isolamento no método
     * {@link TransacaoRepository#existsByUsuarioIdAndCategoriaId(UUID, UUID)}.
     */
    @Test
    @DisplayName("existsByUsuarioIdAndCategoriaId: Com usuário incorreto, deve retornar falso")
    void testeExistsByUsuarioIdAndCategoriaId_QuandoUsuarioIncorreto_DeveRetornarFalso() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);
        entityManager.flush();

        boolean existe = transacaoRepository.existsByUsuarioIdAndCategoriaId(intruso.getId(), categoria.getId());

        assertFalse(existe, "Deveria retornar falso garantindo o isolamento da consulta por categoria");
    }

    /**
     * Testa a {@link TransacaoSpec#comFiltros(UUID, FiltroTransacaoDTO)}.
     *
     * <p>
     * Valida a construção correta da Specification garantindo que o filtro textual
     * e os parâmetros de limite de valor isolam perfeitamente a base de dados.
     */
    @Test
    @DisplayName("TransacaoSpec comFiltros: Filtros opcionais aplicados junto ao isolamento de usuário devem retornar a transação")
    void testeTransacaoSpec_QuandoFiltrosInformados_DeveFiltrarCorretamente() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        FiltroTransacaoDTO filtro = Mockito.mock(FiltroTransacaoDTO.class);
        Mockito.when(filtro.descricao()).thenReturn("Mercado");
        Mockito.when(filtro.valorMin()).thenReturn(new BigDecimal("200.00"));
        Mockito.when(filtro.valorMax()).thenReturn(new BigDecimal("300.00"));

        List<Transacao> resultado = transacaoRepository.findAll(TransacaoSpec.comFiltros(usuario.getId(), filtro));

        assertEquals(1, resultado.size(), "Deveria retornar a transação que atende aos critérios textuais e numéricos");
        assertEquals(DESC_TRANSACAO, resultado.get(0).getDescricao(),
                "A descrição da transação encontrada deve coincidir");
    }

    /**
     * Testa a {@link TransacaoSpec#comFiltros(UUID, FiltroTransacaoDTO)}.
     *
     * <p>
     * Valida o comportamento de retorno antecipado quando o filtro fornecido
     * for nulo, garantindo que o isolamento de dados do usuário logado continue
     * sendo aplicado corretamente em consultas sem parâmetros opcionais.
     */
    @Test
    @DisplayName("TransacaoSpec comFiltros: Filtro nulo deve retornar todos os registros do usuário logado")
    void testeTransacaoSpec_QuandoFiltroNulo_DeveRetornarTodosDoUsuario() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);

        Usuario intruso = new Usuario("Intruso", "intruso@email.com", "123");
        entityManager.persist(intruso);

        Pessoa pessoaIntruso = new Pessoa("Pessoa Intruso", intruso);
        entityManager.persist(pessoaIntruso);

        Conta contaIntruso = new Conta("Conta Intruso", InstituicaoFinanceira.NUBANK, BigDecimal.ZERO, pessoaIntruso);
        entityManager.persist(contaIntruso);

        Categoria categoriaIntruso = new Categoria("Categoria Intruso", TipoCategoria.DESPESA, "icon", "#000", intruso);
        entityManager.persist(categoriaIntruso);

        Transacao transacaoIntruso = Transacao.builder()
                .descricao("Transação do Intruso")
                .valor(BigDecimal.TEN)
                .dataCompetencia(DATA_HOJE)
                .dataVencimento(DATA_HOJE)
                .tipo(TipoTransacao.DESPESA)
                .status(StatusTransacao.PENDENTE)
                .usuario(intruso)
                .pessoa(pessoaIntruso)
                .conta(contaIntruso)
                .categoria(categoriaIntruso)
                .build();
        entityManager.persist(transacaoIntruso);
        entityManager.flush();

        List<Transacao> resultado = transacaoRepository.findAll(TransacaoSpec.comFiltros(usuario.getId(), null));

        assertEquals(1, resultado.size(), "Deveria retornar apenas a transação pertencente ao usuário logado");
        assertEquals(DESC_TRANSACAO, resultado.get(0).getDescricao(), "Deveria retornar a transação correta");
    }

    /**
     * Testa a {@link TransacaoSpec#comFiltros(UUID, FiltroTransacaoDTO)}.
     *
     * <p>
     * Valida explicitamente o cenário em que a Specification é utilizada
     * para uma operação de contagem. Isso assegura que o trecho de
     * getResultType() previna a aplicação do Join Fetch na contagem, evitando
     * exceções na execução da query.
     */
    @Test
    @DisplayName("TransacaoSpec comFiltros: Operação de count deve ignorar fetch e retornar total corretamente")
    void testeTransacaoSpec_QuandoCount_NaoDeveAplicarFetch() {
        entityManager.persist(usuario);
        entityManager.persist(pessoa);
        entityManager.persist(conta);
        entityManager.persist(categoria);
        entityManager.persist(transacao);
        entityManager.flush();

        long totalTransacoes = transacaoRepository.count(TransacaoSpec.comFiltros(usuario.getId(), null));

        assertEquals(1, totalTransacoes, "Deveria contar corretamente os registros do usuário");
    }
}
